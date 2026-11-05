package com.aiscreening.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a job opening with weighted required skills, minimum experience, and education requirements.
 */
public class JobOpening extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private final String title;
    private final String department;
    private final String description;
    // Map of Canonical Skill Name -> Weight (1 to 5)
    private final Map<String, Integer> requiredSkillsWithWeights;
    private final int minExperienceYears;
    private final String requiredEducation;

    public JobOpening(String id, String title, String department, String description,
                      Map<String, Integer> requiredSkillsWithWeights, int minExperienceYears, String requiredEducation) {
        super(id);
        this.title = title;
        this.department = department;
        this.description = description;
        this.requiredSkillsWithWeights = new LinkedHashMap<>(requiredSkillsWithWeights);
        this.minExperienceYears = minExperienceYears;
        this.requiredEducation = requiredEducation;
    }

    public String getTitle() {
        return title;
    }

    public String getDepartment() {
        return department;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Integer> getRequiredSkillsWithWeights() {
        return requiredSkillsWithWeights;
    }

    public Set<String> getRequiredSkills() {
        return requiredSkillsWithWeights.keySet();
    }

    public int getMinExperienceYears() {
        return minExperienceYears;
    }

    public String getRequiredEducation() {
        return requiredEducation;
    }

    @Override
    public String getEntityType() {
        return "JOB_OPENING";
    }
}
