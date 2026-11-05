package com.aiscreening.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents an uploaded and parsed candidate resume.
 */
public class CandidateResume extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private final String candidateName;
    private final String email;
    private final String phone;
    private final String fileName;
    private final String rawText;
    private final String highestEducation;
    private final int educationRank; // 1=High School, 2=Diploma, 3=Bachelor's, 4=Master's, 5=PhD
    private final double experienceYears;
    // Canonical skill -> how it was detected in the resume (e.g., "Spring Boot" -> "Direct mention" or "Synonym: Spring MVC")
    private final Map<String, String> extractedSkillsWithEvidence;
    private final List<String> certifications;
    private final String workHistorySummary;
    private final long parseTimeMs;

    public CandidateResume(String id, String candidateName, String email, String phone, String fileName,
                           String rawText, String highestEducation, int educationRank, double experienceYears,
                           Map<String, String> extractedSkillsWithEvidence, List<String> certifications,
                           String workHistorySummary, long parseTimeMs) {
        super(id);
        this.candidateName = candidateName;
        this.email = email;
        this.phone = phone;
        this.fileName = fileName;
        this.rawText = rawText;
        this.highestEducation = highestEducation;
        this.educationRank = educationRank;
        this.experienceYears = experienceYears;
        this.extractedSkillsWithEvidence = new LinkedHashMap<>(extractedSkillsWithEvidence);
        this.certifications = new ArrayList<>(certifications);
        this.workHistorySummary = workHistorySummary;
        this.parseTimeMs = parseTimeMs;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRawText() {
        return rawText;
    }

    public String getHighestEducation() {
        return highestEducation;
    }

    public int getEducationRank() {
        return educationRank;
    }

    public double getExperienceYears() {
        return experienceYears;
    }

    public Map<String, String> getExtractedSkillsWithEvidence() {
        return extractedSkillsWithEvidence;
    }

    public Set<String> getExtractedSkills() {
        return new LinkedHashSet<>(extractedSkillsWithEvidence.keySet());
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public String getWorkHistorySummary() {
        return workHistorySummary;
    }

    public long getParseTimeMs() {
        return parseTimeMs;
    }

    @Override
    public String getEntityType() {
        return "CANDIDATE_RESUME";
    }
}
