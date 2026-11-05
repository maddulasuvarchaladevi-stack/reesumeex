package com.aiscreening.web;

import com.aiscreening.model.AdminUser;
import com.aiscreening.model.CandidateResume;
import com.aiscreening.model.JobOpening;
import com.aiscreening.model.ScreeningResult;
import com.aiscreening.service.DatabaseRepository;
import com.aiscreening.service.MatchingEngine;
import com.aiscreening.service.RankingEngine;
import com.aiscreening.service.ResumeParseException;
import com.aiscreening.service.ResumeParserService;
import com.aiscreening.service.SkillAnalyzerService;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Pure Java HTTP Web Server & Controller Layer.
 * Handles session authentication, multipart file uploads (PDF/DOCX/TXT), form submissions, and REST API endpoints.
 */
public class WebServer {

    private final int port;
    private final SkillAnalyzerService skillAnalyzer;
    private final ResumeParserService resumeParser;
    private final MatchingEngine matchingEngine;
    private final RankingEngine rankingEngine;
    private final DatabaseRepository repository;
    private final Map<String, AdminUser> activeSessions = new ConcurrentHashMap<>();
    private HttpServer server;

    public WebServer(int port) {
        this.port = port;
        this.skillAnalyzer = new SkillAnalyzerService();
        this.resumeParser = new ResumeParserService(skillAnalyzer);
        this.matchingEngine = new MatchingEngine();
        this.rankingEngine = new RankingEngine(matchingEngine);
        this.repository = new DatabaseRepository(resumeParser);
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        server.createContext("/", this::handleRoot);
        server.createContext("/login", this::handleLogin);
        server.createContext("/register", this::handleRegister);
        server.createContext("/logout", this::handleLogout);
        server.createContext("/dashboard", this::handleDashboard);
        server.createContext("/jobs", this::handleJobs);
        server.createContext("/upload", this::handleUploadFile);
        server.createContext("/upload-text", this::handleUploadText);
        server.createContext("/delete-candidate", this::handleDeleteCandidate);
        server.createContext("/rankings", this::handleRankings);
        server.createContext("/candidate", this::handleCandidateProfile);
        server.createContext("/analytics", this::handleAnalytics);
        server.createContext("/api/rankings", this::handleRestApiRankings);

        server.start();
    }

    private void handleRoot(HttpExchange ex) throws IOException {
        AdminUser user = getAuthenticatedUser(ex);
        if (user == null) {
            redirect(ex, "/login");
        } else {
            redirect(ex, "/dashboard");
        }
    }

    private void handleLogin(HttpExchange ex) throws IOException {
        if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> form = parseFormUrlEncoded(body);
            AdminUser user = repository.authenticate(form.get("username"), form.get("password"));
            if (user != null) {
                String token = UUID.randomUUID().toString();
                activeSessions.put(token, user);
                ex.getResponseHeaders().add("Set-Cookie", "AISESSION=" + token + "; Path=/; HttpOnly");
                redirect(ex, "/dashboard");
                return;
            }
            sendHtml(ex, 401, HtmlViews.renderLoginPage("Invalid username or password. Try admin / admin123.", true));
            return;
        }
        Map<String, String> query = parseQueryParams(ex.getRequestURI().getRawQuery());
        String msg = query.get("msg");
        sendHtml(ex, 200, HtmlViews.renderLoginPage(msg, false));
    }

    private void handleRegister(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            redirect(ex, "/login");
            return;
        }
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = parseFormUrlEncoded(body);
        String username = form.getOrDefault("username", "").trim();
        String password = form.getOrDefault("password", "").trim();
        String fullName = form.getOrDefault("fullName", "").trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            sendHtml(ex, 400, HtmlViews.renderLoginPage("All registration fields are required.", true));
            return;
        }
        AdminUser created = repository.registerAdmin(username, password, fullName);
        if (created == null) {
            sendHtml(ex, 409, HtmlViews.renderLoginPage("Username '" + username + "' is already taken.", true));
            return;
        }
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, created);
        ex.getResponseHeaders().add("Set-Cookie", "AISESSION=" + token + "; Path=/; HttpOnly");
        redirect(ex, "/dashboard");
    }

    private void handleLogout(HttpExchange ex) throws IOException {
        String token = getSessionCookie(ex);
        if (token != null) {
            activeSessions.remove(token);
        }
        ex.getResponseHeaders().add("Set-Cookie", "AISESSION=; Path=/; Max-Age=0");
        redirect(ex, "/login?msg=Successfully+logged+out");
    }

    private void handleDashboard(HttpExchange ex) throws IOException {
        AdminUser user = getOrDefaultDemoUser(ex);
        Map<String, String> q = parseQueryParams(ex.getRequestURI().getRawQuery());
        JobOpening selectedJob = repository.getJobById(q.get("jobId"));
        List<ScreeningResult> ranked = rankingEngine.rankCandidatesForJob(
                repository.getAllCandidates(), selectedJob, "ALL", 0.0, 0.0, null
        );
        String html = HtmlViews.renderDashboard(
                user,
                repository.getAllJobs(),
                repository.getAllCandidates(),
                selectedJob,
                ranked,
                rankingEngine.getTotalMultithreadedTasksProcessed(),
                q.get("flash"),
                false
        );
        sendHtml(ex, 200, html);
    }

    private void handleJobs(HttpExchange ex) throws IOException {
        AdminUser user = getOrDefaultDemoUser(ex);
        String flash = null;
        boolean isError = false;

        if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> form = parseFormUrlEncoded(body);
            String title = form.getOrDefault("title", "").trim();
            String department = form.getOrDefault("department", "Engineering").trim();
            String description = form.getOrDefault("description", "").trim();
            String requiredSkillsCsv = form.getOrDefault("requiredSkills", "").trim();
            String requiredEdu = form.getOrDefault("requiredEducation", "Bachelor's Degree");
            int minExp = parseIntSafe(form.get("minExperience"), 2);

            Map<String, Integer> skillsMap = skillAnalyzer.extractJobSkillsWithWeights(description, requiredSkillsCsv);
            if (title.isEmpty() || skillsMap.isEmpty()) {
                flash = "Please provide a Job Title and at least one identifiable required skill.";
                isError = true;
            } else {
                String jobId = "JOB-" + (101 + repository.getAllJobs().size());
                JobOpening newJob = new JobOpening(jobId, title, department, description, skillsMap, minExp, requiredEdu);
                repository.saveJob(newJob);
                flash = "Created Job Opening " + jobId + " (" + title + ") with " + skillsMap.size() + " weighted skills.";
            }
        }

        String html = HtmlViews.renderJobsPage(user, repository.getAllJobs(), flash, isError);
        sendHtml(ex, 200, html);
    }

    private void handleUploadFile(HttpExchange ex) throws IOException {
        AdminUser user = getOrDefaultDemoUser(ex);
        String flash = null;
        boolean isError = false;

        if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
            try {
                String contentType = ex.getRequestHeaders().getFirst("Content-Type");
                byte[] bodyBytes = ex.getRequestBody().readAllBytes();
                MultipartData mp = parseMultipart(bodyBytes, contentType);

                String nextId = String.format(Locale.US, "CAND-%02d", repository.getAllCandidates().size() + 1);
                CandidateResume parsed = resumeParser.parseUploadedResume(mp.fileName, mp.fileBytes, null);
                CandidateResume withCleanId = new CandidateResume(
                        nextId,
                        parsed.getCandidateName(),
                        parsed.getEmail(),
                        parsed.getPhone(),
                        parsed.getFileName(),
                        parsed.getRawText(),
                        parsed.getHighestEducation(),
                        parsed.getEducationRank(),
                        parsed.getExperienceYears(),
                        parsed.getExtractedSkillsWithEvidence(),
                        parsed.getCertifications(),
                        parsed.getWorkHistorySummary(),
                        parsed.getParseTimeMs()
                );
                repository.saveCandidate(withCleanId);
                flash = "Successfully parsed '" + withCleanId.getFileName() + "' for candidate "
                        + withCleanId.getCandidateName() + " — extracted "
                        + withCleanId.getExtractedSkills().size() + " technical skills!";
            } catch (ResumeParseException e) {
                flash = "Resume Parse Error: " + e.getMessage();
                isError = true;
            } catch (Exception e) {
                flash = "Unexpected upload error: " + e.getMessage();
                isError = true;
            }
        }

        String html = HtmlViews.renderUploadPage(user, repository.getAllJobs(), repository.getAllCandidates(), flash, isError);
        sendHtml(ex, 200, html);
    }

    private void handleUploadText(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            redirect(ex, "/upload");
            return;
        }
        AdminUser user = getOrDefaultDemoUser(ex);
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = parseFormUrlEncoded(body);
        String fileName = form.getOrDefault("fileName", "Pasted_Resume.txt");
        String resumeText = form.getOrDefault("resumeText", "");

        String flash;
        boolean isError = false;
        try {
            String nextId = String.format(Locale.US, "CAND-%02d", repository.getAllCandidates().size() + 1);
            CandidateResume parsed = resumeParser.parseUploadedResume(fileName, null, resumeText);
            CandidateResume withCleanId = new CandidateResume(
                    nextId,
                    parsed.getCandidateName(),
                    parsed.getEmail(),
                    parsed.getPhone(),
                    parsed.getFileName(),
                    parsed.getRawText(),
                    parsed.getHighestEducation(),
                    parsed.getEducationRank(),
                    parsed.getExperienceYears(),
                    parsed.getExtractedSkillsWithEvidence(),
                    parsed.getCertifications(),
                    parsed.getWorkHistorySummary(),
                    parsed.getParseTimeMs()
            );
            repository.saveCandidate(withCleanId);
            flash = "Parsed candidate '" + withCleanId.getCandidateName() + "' (" + nextId + ") — extracted skills: "
                    + String.join(", ", withCleanId.getExtractedSkills());
        } catch (ResumeParseException e) {
            flash = e.getMessage();
            isError = true;
        }

        String html = HtmlViews.renderUploadPage(user, repository.getAllJobs(), repository.getAllCandidates(), flash, isError);
        sendHtml(ex, 200, html);
    }

    private void handleDeleteCandidate(HttpExchange ex) throws IOException {
        if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> form = parseFormUrlEncoded(body);
            String id = form.get("id");
            if (id != null) {
                repository.deleteCandidate(id);
            }
        }
        redirect(ex, "/upload");
    }

    private void handleRankings(HttpExchange ex) throws IOException {
        AdminUser user = getOrDefaultDemoUser(ex);
        Map<String, String> q = parseQueryParams(ex.getRequestURI().getRawQuery());
        JobOpening selectedJob = repository.getJobById(q.get("jobId"));
        String skillFilter = q.getOrDefault("skill", "ALL");
        double minExp = parseDoubleSafe(q.get("minExp"), 0.0);
        double minScore = parseDoubleSafe(q.get("minScore"), 0.0);
        String keyword = q.getOrDefault("q", "");

        List<ScreeningResult> results = rankingEngine.rankCandidatesForJob(
                repository.getAllCandidates(), selectedJob, skillFilter, minExp, minScore, keyword
        );

        String html = HtmlViews.renderRankingsPage(
                user,
                repository.getAllJobs(),
                selectedJob,
                results,
                skillFilter,
                minExp,
                minScore,
                keyword,
                skillAnalyzer.getSkillSynonymTaxonomy().keySet()
        );
        sendHtml(ex, 200, html);
    }

    private void handleCandidateProfile(HttpExchange ex) throws IOException {
        AdminUser user = getOrDefaultDemoUser(ex);
        Map<String, String> q = parseQueryParams(ex.getRequestURI().getRawQuery());
        CandidateResume candidate = repository.getCandidateById(q.get("id"));
        if (candidate == null) {
            redirect(ex, "/rankings");
            return;
        }
        JobOpening selectedJob = repository.getJobById(q.get("jobId"));
        ScreeningResult evaluation = matchingEngine.evaluate(candidate, selectedJob);

        String html = HtmlViews.renderCandidateProfilePage(
                user, candidate, selectedJob, evaluation, repository.getAllJobs()
        );
        sendHtml(ex, 200, html);
    }

    private void handleAnalytics(HttpExchange ex) throws IOException {
        AdminUser user = getOrDefaultDemoUser(ex);
        String html = HtmlViews.renderAnalyticsPage(
                user,
                repository.getAllJobs(),
                repository.getAllCandidates(),
                skillAnalyzer.getSkillSynonymTaxonomy(),
                rankingEngine.getTotalMultithreadedTasksProcessed()
        );
        sendHtml(ex, 200, html);
    }

    /**
     * JSON REST API endpoint (/api/rankings?jobId=JOB-101) demonstrating backend REST API services.
     */
    private void handleRestApiRankings(HttpExchange ex) throws IOException {
        Map<String, String> q = parseQueryParams(ex.getRequestURI().getRawQuery());
        JobOpening job = repository.getJobById(q.get("jobId"));
        List<ScreeningResult> results = rankingEngine.rankCandidatesForJob(
                repository.getAllCandidates(), job, "ALL", 0.0, 0.0, null
        );

        StringBuilder json = new StringBuilder();
        json.append("{\n")
            .append("  \"jobId\": \"").append(job.getId()).append("\",\n")
            .append("  \"jobTitle\": \"").append(job.getTitle()).append("\",\n")
            .append("  \"totalCandidatesEvaluated\": ").append(results.size()).append(",\n")
            .append("  \"rankings\": [\n");
        for (int i = 0; i < results.size(); i++) {
            ScreeningResult r = results.get(i);
            json.append("    {\n")
                .append("      \"rank\": ").append(r.getRank()).append(",\n")
                .append("      \"candidateId\": \"").append(r.getCandidate().getId()).append("\",\n")
                .append("      \"candidateName\": \"").append(r.getCandidate().getCandidateName()).append("\",\n")
                .append("      \"skillsMatched\": \"").append(r.getMatchedSkillCount()).append("/").append(r.getTotalRequiredSkills()).append("\",\n")
                .append("      \"matchPercentage\": ").append(r.getExactSkillPercentage()).append(",\n")
                .append("      \"compositeAiScore\": ").append(r.getOverallCompositeScore()).append("\n")
                .append("    }").append(i < results.size() - 1 ? "," : "").append("\n");
        }
        json.append("  ]\n}");

        byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private AdminUser getAuthenticatedUser(HttpExchange ex) {
        String token = getSessionCookie(ex);
        return token != null ? activeSessions.get(token) : null;
    }

    private AdminUser getOrDefaultDemoUser(HttpExchange ex) {
        AdminUser user = getAuthenticatedUser(ex);
        if (user != null) {
            return user;
        }
        // Automatically provide the default Lead HR Admin session so direct navigation works seamlessly
        return repository.authenticate("admin", "admin123");
    }

    private String getSessionCookie(HttpExchange ex) {
        List<String> cookies = ex.getRequestHeaders().get("Cookie");
        if (cookies == null) return null;
        for (String header : cookies) {
            String[] parts = header.split(";");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.startsWith("AISESSION=")) {
                    return trimmed.substring("AISESSION=".length());
                }
            }
        }
        return null;
    }

    private void sendHtml(HttpExchange ex, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        Headers headers = ex.getResponseHeaders();
        headers.set("Content-Type", "text/html; charset=UTF-8");
        headers.set("Cache-Control", "no-store");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().set("Location", location);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    private Map<String, String> parseQueryParams(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) return map;
        for (String pair : rawQuery.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(k, v);
            }
        }
        return map;
    }

    private Map<String, String> parseFormUrlEncoded(String body) {
        return parseQueryParams(body);
    }

    private int parseIntSafe(String val, int fallback) {
        if (val == null) return fallback;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private double parseDoubleSafe(String val, double fallback) {
        if (val == null) return fallback;
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static class MultipartData {
        String fileName = "uploaded_resume.txt";
        byte[] fileBytes = new byte[0];
    }

    private MultipartData parseMultipart(byte[] body, String contentTypeHeader) throws ResumeParseException {
        if (contentTypeHeader == null || !contentTypeHeader.contains("boundary=")) {
            throw new ResumeParseException("Invalid multipart/form-data request.");
        }
        String boundary = "--" + contentTypeHeader.substring(contentTypeHeader.indexOf("boundary=") + 9).trim();
        if (boundary.contains(";")) {
            boundary = boundary.substring(0, boundary.indexOf(';')).trim();
        }

        String raw = new String(body, StandardCharsets.ISO_8859_1);
        String[] parts = raw.split(java.util.regex.Pattern.quote(boundary));
        MultipartData data = new MultipartData();

        for (String part : parts) {
            if (part.contains("filename=\"")) {
                int fnStart = part.indexOf("filename=\"") + 10;
                int fnEnd = part.indexOf("\"", fnStart);
                if (fnEnd > fnStart) {
                    data.fileName = part.substring(fnStart, fnEnd);
                }
                int headerEnd = part.indexOf("\r\n\r\n");
                if (headerEnd > 0) {
                    int contentStart = headerEnd + 4;
                    int contentEnd = part.length();
                    if (part.endsWith("\r\n")) {
                        contentEnd -= 2;
                    }
                    String fileSlice = part.substring(contentStart, Math.max(contentStart, contentEnd));
                    data.fileBytes = fileSlice.getBytes(StandardCharsets.ISO_8859_1);
                }
            }
        }
        return data;
    }
}
