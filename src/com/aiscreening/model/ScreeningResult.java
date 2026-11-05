package com.aiscreening.model;

import java.util.List;
import java.util.Map;

/**
 * Holds the AI-based evaluation result of comparing a CandidateResume against a JobOpening.
 */
public class ScreeningResult {
    private final CandidateResume candidate;
    private final JobOpening job;
    private int rank;
    private final int matchedSkillCount;
    private final int totalRequiredSkills;
    private final double exactSkillPercentage;     // e.g. 4/5 = 80.0%
    private final double weightedSkillScore;       // Weighted skill match (0-100)
    private final double experienceScore;          // Experience evaluation (0-100)
    private final double educationScore;           // Education evaluation (0-100)
    private final double overallCompositeScore;    // Combined AI score (0-100)
    private final Map<String, String> matchedSkillsEvidence; // Skill -> evidence/synonym
    private final List<String> missingSkills;
    private final List<String> bonusSkills;
    private final List<String> aiExplanationPoints;
    private final String recommendationTier;

    public ScreeningResult(CandidateResume candidate, JobOpening job, int matchedSkillCount, int totalRequiredSkills,
                           double exactSkillPercentage, double weightedSkillScore, double experienceScore,
                           double educationScore, double overallCompositeScore,
                           Map<String, String> matchedSkillsEvidence, List<String> missingSkills,
                           List<String> bonusSkills, List<String> aiExplanationPoints, String recommendationTier) {
        this.candidate = candidate;
        this.job = job;
        this.matchedSkillCount = matchedSkillCount;
        this.totalRequiredSkills = totalRequiredSkills;
        this.exactSkillPercentage = exactSkillPercentage;
        this.weightedSkillScore = weightedSkillScore;
        this.experienceScore = experienceScore;
        this.educationScore = educationScore;
        this.overallCompositeScore = overallCompositeScore;
        this.matchedSkillsEvidence = matchedSkillsEvidence;
        this.missingSkills = missingSkills;
        this.bonusSkills = bonusSkills;
        this.aiExplanationPoints = aiExplanationPoints;
        this.recommendationTier = recommendationTier;
    }

    public CandidateResume getCandidate() {
        return candidate;
    }

    public JobOpening getJob() {
        return job;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getMatchedSkillCount() {
        return matchedSkillCount;
    }

    public int getTotalRequiredSkills() {
        return totalRequiredSkills;
    }

    public double getExactSkillPercentage() {
        return exactSkillPercentage;
    }

    public double getWeightedSkillScore() {
        return weightedSkillScore;
    }

    public double getExperienceScore() {
        return experienceScore;
    }

    public double getEducationScore() {
        return educationScore;
    }

    public double getOverallCompositeScore() {
        return overallCompositeScore;
    }

    public Map<String, String> getMatchedSkillsEvidence() {
        return matchedSkillsEvidence;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public List<String> getBonusSkills() {
        return bonusSkills;
    }

    public List<String> getAiExplanationPoints() {
        return aiExplanationPoints;
    }

    public String getRecommendationTier() {
        return recommendationTier;
    }
}
