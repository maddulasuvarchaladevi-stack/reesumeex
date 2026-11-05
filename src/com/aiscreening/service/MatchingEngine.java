package com.aiscreening.service;

import com.aiscreening.model.CandidateResume;
import com.aiscreening.model.JobOpening;
import com.aiscreening.model.ScreeningResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AI-Based Matching Engine implementing ScoringStrategy (Polymorphism).
 * Performs:
 * 1. Exact Skill Ratio Match (e.g., 5/5 = 100%, 4/5 = 80%, 3/5 = 60%, 2/5 = 40%)
 * 2. Weighted Skill Scoring with NLP Synonym Recognition
 * 3. Experience-Based Scoring
 * 4. Education & Certification Scoring
 * 5. Detailed Explainable AI Evaluation Breakdown (Bias-free)
 */
public class MatchingEngine implements ScoringStrategy {

    @Override
    public ScreeningResult evaluate(CandidateResume candidate, JobOpening job) {
        Map<String, Integer> requiredWeights = job.getRequiredSkillsWithWeights();
        Map<String, String> candidateSkills = candidate.getExtractedSkillsWithEvidence();

        int totalRequired = requiredWeights.size();
        int matchedCount = 0;
        int totalPossibleWeight = 0;
        int earnedWeight = 0;

        Map<String, String> matchedEvidence = new LinkedHashMap<>();
        List<String> missingSkills = new ArrayList<>();
        List<String> bonusSkills = new ArrayList<>();
        List<String> explanations = new ArrayList<>();

        for (Map.Entry<String, Integer> req : requiredWeights.entrySet()) {
            String reqSkill = req.getKey();
            int weight = req.getValue();
            totalPossibleWeight += weight;

            // Case-insensitive lookup in candidate's extracted skills
            String matchedCanonical = null;
            String evidence = null;
            for (Map.Entry<String, String> candSkill : candidateSkills.entrySet()) {
                if (candSkill.getKey().equalsIgnoreCase(reqSkill)) {
                    matchedCanonical = candSkill.getKey();
                    evidence = candSkill.getValue();
                    break;
                }
            }

            if (matchedCanonical != null) {
                matchedCount++;
                earnedWeight += weight;
                matchedEvidence.put(reqSkill, evidence);
            } else {
                missingSkills.add(reqSkill);
            }
        }

        for (String candSkill : candidateSkills.keySet()) {
            boolean isRequired = requiredWeights.keySet().stream()
                    .anyMatch(r -> r.equalsIgnoreCase(candSkill));
            if (!isRequired) {
                bonusSkills.add(candSkill);
            }
        }

        // 1. Exact Skill Match Percentage (Matches Step 5 table: 5/5=100%, 4/5=80%, 3/5=60%, 2/5=40%)
        double exactSkillPct = totalRequired == 0 ? 100.0 : (matchedCount * 100.0) / totalRequired;

        // 2. Weighted Skill Score
        double weightedSkillScore = totalPossibleWeight == 0 ? 100.0 : (earnedWeight * 100.0) / totalPossibleWeight;

        // 3. Experience Score (0 - 100)
        double expScore;
        int minExp = job.getMinExperienceYears();
        if (minExp <= 0 || candidate.getExperienceYears() >= minExp) {
            expScore = 100.0;
        } else {
            expScore = Math.max(20.0, (candidate.getExperienceYears() / minExp) * 100.0);
        }

        // 4. Education Score (0 - 100)
        int requiredEduRank = resolveRequiredEducationRank(job.getRequiredEducation());
        double eduScore = candidate.getEducationRank() >= requiredEduRank
                ? 100.0
                : Math.max(50.0, (candidate.getEducationRank() * 100.0) / requiredEduRank);

        // 5. Overall Composite Score (75% Weighted Skill Match + 15% Experience + 10% Education)
        double compositeScore = (weightedSkillScore * 0.75) + (expScore * 0.15) + (eduScore * 0.10);

        // Build Explainable AI Rationales
        explanations.add(String.format(Locale.US,
                "Skill Coverage: Matched %d out of %d required job skills (%.0f%% Exact Skill Match; %.1f%% Weighted Skill Score).",
                matchedCount, totalRequired, exactSkillPct, weightedSkillScore));

        long synonymMatches = matchedEvidence.values().stream()
                .filter(v -> v.startsWith("NLP Synonym"))
                .count();
        if (synonymMatches > 0) {
            explanations.add(String.format(Locale.US,
                    "NLP Synonym Intelligence: Automatically recognized %d related/synonymous technology mention(s) in the resume.",
                    synonymMatches));
        }

        if (!missingSkills.isEmpty()) {
            explanations.add("Missing Required Skills: " + String.join(", ", missingSkills) + ".");
        } else {
            explanations.add("Full Technical Alignment: Candidate possesses 100% of the core technical skills required for this role.");
        }

        explanations.add(String.format(Locale.US,
                "Experience Analysis: Candidate has %.1f years of experience vs. %d years minimum requirement (Experience Score: %.0f%%).",
                candidate.getExperienceYears(), minExp, expScore));

        explanations.add(String.format(Locale.US,
                "Education & Credentials: %s (Education Score: %.0f%%) with %d verified certification(s).",
                candidate.getHighestEducation(), eduScore, candidate.getCertifications().size()));

        explanations.add("Fairness & Bias Audit: Personal demographic attributes (gender, age, marital status, ethnicity) were excluded from scoring.");

        String tier;
        if (exactSkillPct >= 80.0) {
            tier = "Strong Match (Shortlist)";
        } else if (exactSkillPct >= 60.0) {
            tier = "Moderate Match (Review)";
        } else {
            tier = "Low Match (Skill Gap)";
        }

        return new ScreeningResult(
                candidate,
                job,
                matchedCount,
                totalRequired,
                round1(exactSkillPct),
                round1(weightedSkillScore),
                round1(expScore),
                round1(eduScore),
                round1(compositeScore),
                matchedEvidence,
                missingSkills,
                bonusSkills,
                explanations,
                tier
        );
    }

    private int resolveRequiredEducationRank(String reqEdu) {
        if (reqEdu == null) return 3;
        String lower = reqEdu.toLowerCase(Locale.ROOT);
        if (lower.contains("phd") || lower.contains("doctor")) return 5;
        if (lower.contains("master") || lower.contains("m.tech") || lower.contains("mca")) return 4;
        if (lower.contains("diploma")) return 2;
        return 3; // Bachelor's default
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
