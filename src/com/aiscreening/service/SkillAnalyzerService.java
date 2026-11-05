package com.aiscreening.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NLP-based Skill Analyzer demonstrating Java Collections Framework (HashMap, HashSet, LinkedHashMap)
 * and intelligent Synonym / Related-Technology Detection.
 */
public class SkillAnalyzerService {

    // Canonical Skill -> List of synonyms, aliases, and closely related sub-technologies
    private final Map<String, List<String>> skillSynonymTaxonomy = new LinkedHashMap<>();

    // Demographic / personal terms intentionally ignored by the AI engine for fair, bias-free screening
    private final Set<String> biasScrubTerms = new HashSet<>(Arrays.asList(
            "male", "female", "gender", "married", "single", "marital status",
            "age", "date of birth", "dob", "religion", "nationality", "ethnicity"
    ));

    public SkillAnalyzerService() {
        initializeTaxonomy();
    }

    private void initializeTaxonomy() {
        skillSynonymTaxonomy.put("Java", Arrays.asList(
                "java", "core java", "java programming", "java development", "jdk", "j2ee", "java se", "java ee", "openjdk", "java 17", "java 21", "multithreading", "collections framework"
        ));
        skillSynonymTaxonomy.put("Spring Boot", Arrays.asList(
                "spring boot", "springboot", "spring framework", "spring mvc", "spring cloud", "spring security", "spring data", "spring ioc"
        ));
        skillSynonymTaxonomy.put("MySQL", Arrays.asList(
                "mysql", "my sql", "mariadb", "relational database", "rdbms", "sql queries", "innodb"
        ));
        skillSynonymTaxonomy.put("Hibernate", Arrays.asList(
                "hibernate", "jpa", "java persistence api", "orm", "object relational mapping", "hibernate orm", "spring data jpa"
        ));
        skillSynonymTaxonomy.put("REST API", Arrays.asList(
                "rest api", "rest apis", "restful api", "restful web services", "restful services", "json api", "jax-rs", "http endpoints", "openapi", "swagger"
        ));
        skillSynonymTaxonomy.put("Microservices", Arrays.asList(
                "microservices", "microservice architecture", "distributed systems", "service mesh", "api gateway", "eureka"
        ));
        skillSynonymTaxonomy.put("Docker", Arrays.asList(
                "docker", "containerization", "dockerfile", "docker compose", "containers"
        ));
        skillSynonymTaxonomy.put("Kubernetes", Arrays.asList(
                "kubernetes", "k8s", "helm", "eks", "gke", "container orchestration"
        ));
        skillSynonymTaxonomy.put("AWS", Arrays.asList(
                "aws", "amazon web services", "ec2", "s3", "rds", "lambda", "cloudformation"
        ));
        skillSynonymTaxonomy.put("PostgreSQL", Arrays.asList(
                "postgresql", "postgres", "pl/pgsql"
        ));
        skillSynonymTaxonomy.put("JDBC", Arrays.asList(
                "jdbc", "java database connectivity", "connection pooling", "hikaricp"
        ));
        skillSynonymTaxonomy.put("Maven", Arrays.asList(
                "maven", "apache maven", "pom.xml", "gradle", "build automation"
        ));
        skillSynonymTaxonomy.put("Git", Arrays.asList(
                "git", "github", "gitlab", "bitbucket", "version control"
        ));
        skillSynonymTaxonomy.put("JUnit", Arrays.asList(
                "junit", "junit 5", "mockito", "unit testing", "tdd", "test driven development"
        ));
        skillSynonymTaxonomy.put("Kafka", Arrays.asList(
                "kafka", "apache kafka", "event streaming", "message broker", "rabbitmq"
        ));
        skillSynonymTaxonomy.put("Python", Arrays.asList(
                "python", "python 3", "py", "fastapi", "flask", "django"
        ));
        skillSynonymTaxonomy.put("Machine Learning", Arrays.asList(
                "machine learning", "ml", "deep learning", "nlp", "natural language processing", "scikit-learn", "pytorch", "tensorflow"
        ));
        skillSynonymTaxonomy.put("SQL", Arrays.asList(
                "sql", "structured query language", "pl/sql", "t-sql", "database design"
        ));
        skillSynonymTaxonomy.put("HTML/CSS", Arrays.asList(
                "html", "html5", "css", "css3", "responsive design", "web ui"
        ));
        skillSynonymTaxonomy.put("Agile/Scrum", Arrays.asList(
                "agile", "scrum", "kanban", "jira", "sprint planning"
        ));
    }

    /**
     * Extracts canonical skills from text along with explainable NLP evidence (exact match vs synonym match).
     */
    public Map<String, String> extractSkillsWithEvidence(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return Collections.emptyMap();
        }
        String normalized = " " + rawText.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9+#./\\-_\\s]", " ")
                .replaceAll("\\s+", " ") + " ";

        Map<String, String> detected = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : skillSynonymTaxonomy.entrySet()) {
            String canonicalSkill = entry.getKey();
            List<String> variants = entry.getValue();

            for (String variant : variants) {
                String regex = "(?<![a-z0-9])" + Pattern.quote(variant.toLowerCase(Locale.ROOT)) + "(?![a-z0-9])";
                Matcher matcher = Pattern.compile(regex).matcher(normalized);
                if (matcher.find()) {
                    if (variant.equalsIgnoreCase(canonicalSkill)) {
                        detected.put(canonicalSkill, "Direct Match ('" + canonicalSkill + "')");
                    } else {
                        detected.put(canonicalSkill, "NLP Synonym Match ('" + variant + "' -> " + canonicalSkill + ")");
                    }
                    break; // Prefer direct match if listed first in taxonomy
                }
            }
        }
        return detected;
    }

    /**
     * Extracts required skills with default or inferred weights from a Job Description text and custom comma-separated skill list.
     */
    public Map<String, Integer> extractJobSkillsWithWeights(String jobDescription, String explicitSkillsCsv) {
        Map<String, Integer> weightedSkills = new LinkedHashMap<>();

        if (explicitSkillsCsv != null && !explicitSkillsCsv.isBlank()) {
            String[] parts = explicitSkillsCsv.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.isEmpty()) continue;

                int weight = 5;
                String skillToken = trimmed;
                // Allow optional syntax like "Java:5" or "Hibernate:4"
                if (trimmed.contains(":")) {
                    String[] kv = trimmed.split(":", 2);
                    skillToken = kv[0].trim();
                    try {
                        weight = Math.max(1, Math.min(5, Integer.parseInt(kv[1].trim())));
                    } catch (NumberFormatException ignored) {
                        weight = 5;
                    }
                }
                String resolved = resolveCanonicalSkillName(skillToken);
                weightedSkills.put(resolved, weight);
            }
        }

        // Also scan job description via NLP if no explicit skills were provided
        if (weightedSkills.isEmpty() && jobDescription != null) {
            Map<String, String> fromJd = extractSkillsWithEvidence(jobDescription);
            for (String skill : fromJd.keySet()) {
                weightedSkills.put(skill, 4);
            }
        }

        return weightedSkills;
    }

    /**
     * Resolves a user-entered skill string to its canonical taxonomy name if available.
     */
    public String resolveCanonicalSkillName(String rawSkill) {
        if (rawSkill == null || rawSkill.isBlank()) return "";
        String cleaned = rawSkill.trim();
        for (Map.Entry<String, List<String>> entry : skillSynonymTaxonomy.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(cleaned)) {
                return entry.getKey();
            }
            for (String syn : entry.getValue()) {
                if (syn.equalsIgnoreCase(cleaned)) {
                    return entry.getKey();
                }
            }
        }
        return cleaned;
    }

    public Map<String, List<String>> getSkillSynonymTaxonomy() {
        return Collections.unmodifiableMap(skillSynonymTaxonomy);
    }

    public Set<String> getBiasScrubTerms() {
        return Collections.unmodifiableSet(biasScrubTerms);
    }
}
