package com.aiscreening.service;

import com.aiscreening.model.CandidateResume;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Resume Parser Service demonstrating Java File Handling (PDF, DOCX via ZipInputStream XML extraction, TXT),
 * Regular Expressions, NLP feature extraction, and Exception Handling.
 */
public class ResumeParserService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(?:\\+?\\d{1,3}[\\s\\-]?)?(?:\\(?\\d{3,4}\\)?[\\s\\-]?)?\\d{3}[\\s\\-]?\\d{4}");
    private static final Pattern EXP_YEARS_PATTERN =
            Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*\\+?\\s*(?:years?|yrs?)(?:\\s+of)?\\s+(?:experience|exp)?",
                    Pattern.CASE_INSENSITIVE);

    private final SkillAnalyzerService skillAnalyzer;

    public ResumeParserService(SkillAnalyzerService skillAnalyzer) {
        this.skillAnalyzer = skillAnalyzer;
    }

    /**
     * Parses an uploaded resume file (.pdf, .docx, or .txt) into a structured CandidateResume object.
     */
    public CandidateResume parseUploadedResume(String fileName, byte[] fileBytes, String fallbackText)
            throws ResumeParseException {
        long startTime = System.currentTimeMillis();

        if ((fileBytes == null || fileBytes.length == 0) && (fallbackText == null || fallbackText.isBlank())) {
            throw new ResumeParseException("Uploaded resume is empty. Please provide a valid PDF, DOCX, or TXT file, or paste resume text.");
        }

        String safeFileName = (fileName == null || fileName.isBlank()) ? "pasted_resume.txt" : fileName.trim();
        String extractedText;

        if (fallbackText != null && !fallbackText.isBlank() && (fileBytes == null || fileBytes.length == 0)) {
            extractedText = fallbackText;
        } else {
            String lowerName = safeFileName.toLowerCase(Locale.ROOT);
            if (lowerName.endsWith(".docx")) {
                extractedText = extractTextFromDocx(fileBytes);
            } else if (lowerName.endsWith(".pdf")) {
                extractedText = extractTextFromPdf(fileBytes);
            } else if (lowerName.endsWith(".txt") || lowerName.endsWith(".md") || lowerName.endsWith(".csv")) {
                extractedText = new String(fileBytes, StandardCharsets.UTF_8);
            } else {
                // Attempt text decoding if extension is unknown
                extractedText = new String(fileBytes, StandardCharsets.UTF_8);
            }
        }

        if (extractedText == null || extractedText.trim().length() < 10) {
            throw new ResumeParseException("Could not extract sufficient readable text from file: " + safeFileName);
        }

        return buildCandidateFromText(UUID.randomUUID().toString().substring(0, 8), safeFileName, extractedText, startTime);
    }

    /**
     * Parses raw text directly with a custom ID and filename.
     */
    public CandidateResume parseFromText(String id, String fileName, String rawText) {
        return buildCandidateFromText(id, fileName, rawText, System.currentTimeMillis());
    }

    private CandidateResume buildCandidateFromText(String id, String fileName, String rawText, long startTimeMs) {
        String candidateName = extractCandidateName(rawText, fileName);
        String email = extractEmail(rawText);
        String phone = extractPhone(rawText);
        EducationInfo edu = extractEducation(rawText);
        double experienceYears = extractExperienceYears(rawText);
        Map<String, String> skillsWithEvidence = skillAnalyzer.extractSkillsWithEvidence(rawText);
        List<String> certifications = extractCertifications(rawText);
        String workSummary = extractWorkSummary(rawText);
        long elapsed = Math.max(1L, System.currentTimeMillis() - startTimeMs);

        return new CandidateResume(
                id,
                candidateName,
                email,
                phone,
                fileName,
                rawText.trim(),
                edu.label,
                edu.rank,
                experienceYears,
                skillsWithEvidence,
                certifications,
                workSummary,
                elapsed
        );
    }

    /**
     * Native Java DOCX parser using ZipInputStream to read word/document.xml and strip XML tags.
     */
    private String extractTextFromDocx(byte[] fileBytes) throws ResumeParseException {
        StringBuilder sb = new StringBuilder();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(fileBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("word/document.xml".equalsIgnoreCase(entry.getName())) {
                    byte[] xmlBytes = zis.readAllBytes();
                    String xml = new String(xmlBytes, StandardCharsets.UTF_8);
                    // Replace paragraph and break tags with newlines before stripping XML tags
                    String withNewlines = xml
                            .replaceAll("</w:p>", "\n")
                            .replaceAll("<w:br[^>]*/?>", "\n")
                            .replaceAll("<w:tab[^>]*/?>", " ");
                    String plain = withNewlines.replaceAll("<[^>]+>", " ")
                            .replaceAll("&amp;", "&")
                            .replaceAll("&lt;", "<")
                            .replaceAll("&gt;", ">")
                            .replaceAll("[ \\t]+", " ");
                    sb.append(plain);
                    break;
                }
            }
        } catch (IOException e) {
            throw new ResumeParseException("Failed to read DOCX archive structure: " + e.getMessage(), e);
        }

        if (sb.length() == 0) {
            // Fallback if a plain text file was renamed .docx
            String fallback = new String(fileBytes, StandardCharsets.UTF_8);
            if (fallback.trim().length() > 10 && !fallback.startsWith("PK")) {
                return fallback;
            }
            throw new ResumeParseException("Invalid DOCX file: word/document.xml not found.");
        }
        return sb.toString();
    }

    /**
     * Native Java PDF text stream extractor: extracts text literals inside PDF BT/ET blocks and parentheses,
     * with readable ASCII fallback.
     */
    private String extractTextFromPdf(byte[] fileBytes) throws ResumeParseException {
        String raw = new String(fileBytes, StandardCharsets.ISO_8859_1);
        if (!raw.startsWith("%PDF")) {
            // If user uploaded a plain-text file with .pdf extension, read as UTF-8
            return new String(fileBytes, StandardCharsets.UTF_8);
        }

        StringBuilder extracted = new StringBuilder();
        // Extract literal strings inside PDF text objects (...)
        Pattern pdfStringPattern = Pattern.compile("\\(([^()\\\\]{2,200})\\)");
        Matcher matcher = pdfStringPattern.matcher(raw);
        while (matcher.find()) {
            String token = matcher.group(1);
            // Filter out PDF metadata/font descriptors
            if (!token.startsWith("PDF-") && !token.contains("Adobe") && !token.contains("Font")) {
                extracted.append(token).append(" ");
            }
        }

        String result = extracted.toString().replaceAll("[^\\x20-\\x7E\\r\\n]", " ").replaceAll("\\s+", " ").trim();
        if (result.length() >= 25) {
            return result;
        }

        // Fallback: scan readable ASCII lines from the PDF buffer
        StringBuilder asciiFallback = new StringBuilder();
        Matcher asciiMatcher = Pattern.compile("[A-Za-z0-9@._,+\\-:/() ]{6,}").matcher(raw);
        while (asciiMatcher.find()) {
            String segment = asciiMatcher.group();
            if (!segment.contains("obj") && !segment.contains("endobj") && !segment.contains("stream")) {
                asciiFallback.append(segment).append("\n");
            }
        }
        String fallbackResult = asciiFallback.toString().trim();
        if (fallbackResult.length() < 15) {
            throw new ResumeParseException("PDF appears to be scanned/image-only or compressed without uncompressed text streams.");
        }
        return fallbackResult;
    }

    private String extractCandidateName(String text, String fileName) {
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.toLowerCase(Locale.ROOT).startsWith("name:")) {
                return trimmed.substring(5).trim();
            }
            if (trimmed.length() >= 3 && trimmed.length() <= 45
                    && !trimmed.contains("@")
                    && !trimmed.toLowerCase(Locale.ROOT).contains("resume")
                    && !trimmed.toLowerCase(Locale.ROOT).contains("curriculum")) {
                return trimmed;
            }
        }
        return fileName.replaceAll("\\.[a-zA-Z0-9]+$", "").replace('_', ' ');
    }

    private String extractEmail(String text) {
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : "not-provided@candidate.org";
    }

    private String extractPhone(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        while (matcher.find()) {
            String candidate = matcher.group().trim();
            if (candidate.replaceAll("\\D", "").length() >= 10) {
                return candidate;
            }
        }
        return "+91-98000-00000";
    }

    private double extractExperienceYears(String text) {
        Matcher matcher = EXP_YEARS_PATTERN.matcher(text);
        double maxFound = 0.0;
        while (matcher.find()) {
            try {
                double val = Double.parseDouble(matcher.group(1));
                if (val <= 40.0 && val > maxFound) {
                    maxFound = val;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (maxFound > 0.0) {
            return maxFound;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("fresher") || lower.contains("entry level") || lower.contains("intern")) {
            return 0.5;
        }
        return 1.0;
    }

    private EducationInfo extractEducation(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("ph.d") || lower.contains("phd") || lower.contains("doctorate")) {
            return new EducationInfo("Ph.D. / Doctorate", 5);
        }
        if (lower.contains("m.tech") || lower.contains("mtech") || lower.contains("m.s.") || lower.contains("master")
                || lower.contains("mca") || lower.contains("m.sc") || lower.contains("mba")) {
            return new EducationInfo("Master's Degree (M.Tech / MCA / MS)", 4);
        }
        if (lower.contains("b.tech") || lower.contains("btech") || lower.contains("b.e.") || lower.contains("bachelor")
                || lower.contains("bca") || lower.contains("b.sc")) {
            return new EducationInfo("Bachelor's Degree (B.Tech / B.E. / B.Sc)", 3);
        }
        if (lower.contains("diploma") || lower.contains("polytechnic")) {
            return new EducationInfo("Technical Diploma", 2);
        }
        return new EducationInfo("Bachelor's Degree (Inferred)", 3);
    }

    private List<String> extractCertifications(String text) {
        List<String> certs = new ArrayList<>();
        String[] knownCerts = {
                "Oracle Certified Professional (OCP Java)",
                "AWS Certified Solutions Architect",
                "AWS Certified Developer",
                "Spring Professional Certification",
                "Certified Kubernetes Application Developer (CKAD)",
                "MySQL Database Administrator Certified",
                "Scrum Master Certified (CSM)"
        };
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("oracle certified") || lower.contains("ocp") || lower.contains("ocjp")) {
            certs.add(knownCerts[0]);
        }
        if (lower.contains("aws certified solutions")) {
            certs.add(knownCerts[1]);
        } else if (lower.contains("aws certified")) {
            certs.add(knownCerts[2]);
        }
        if (lower.contains("spring professional") || lower.contains("pivotal certified") || lower.contains("vmware spring")) {
            certs.add(knownCerts[3]);
        }
        if (lower.contains("ckad") || lower.contains("cka") || lower.contains("kubernetes certified")) {
            certs.add(knownCerts[4]);
        }
        if (lower.contains("mysql certified") || lower.contains("oracle mysql")) {
            certs.add(knownCerts[5]);
        }
        if (lower.contains("scrum master") || lower.contains("csm")) {
            certs.add(knownCerts[6]);
        }
        return certs;
    }

    private String extractWorkSummary(String text) {
        String[] lines = text.split("\\r?\\n");
        StringBuilder summary = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.toLowerCase(Locale.ROOT).contains("experience")
                    || trimmed.toLowerCase(Locale.ROOT).contains("engineer")
                    || trimmed.toLowerCase(Locale.ROOT).contains("developer")
                    || trimmed.toLowerCase(Locale.ROOT).contains("architect")
                    || trimmed.toLowerCase(Locale.ROOT).contains("worked")) {
                if (trimmed.length() > 15) {
                    if (summary.length() > 0) summary.append(" | ");
                    summary.append(trimmed);
                    if (summary.length() > 220) break;
                }
            }
        }
        if (summary.length() == 0) {
            return "Software engineering background analysed via NLP text extraction.";
        }
        return summary.toString();
    }

    private static class EducationInfo {
        final String label;
        final int rank;

        EducationInfo(String label, int rank) {
            this.label = label;
            this.rank = rank;
        }
    }
}
