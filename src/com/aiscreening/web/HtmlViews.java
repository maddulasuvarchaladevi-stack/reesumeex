package com.aiscreening.web;

import com.aiscreening.model.AdminUser;
import com.aiscreening.model.CandidateResume;
import com.aiscreening.model.JobOpening;
import com.aiscreening.model.ScreeningResult;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Pure Java Server-Side Frontend View Renderer.
 * Generates responsive HTML5 + CSS3 interfaces with ZERO JavaScript.
 */
public class HtmlViews {

    private static String baseStyles() {
        return """
            :root {
                --bg: #0f172a;
                --bg-card: #1e293b;
                --bg-card-alt: #26354d;
                --text: #f8fafc;
                --text-muted: #94a3b8;
                --primary: #3b82f6;
                --primary-hover: #2563eb;
                --accent: #8b5cf6;
                --success: #10b981;
                --warning: #f59e0b;
                --danger: #ef4444;
                --border: #334155;
            }
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
                font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
                background-color: var(--bg);
                color: var(--text);
                line-height: 1.6;
                min-height: 100vh;
                display: flex;
                flex-direction: column;
            }
            a { color: #60a5fa; text-decoration: none; }
            a:hover { text-decoration: underline; }
            .topbar {
                background: linear-gradient(90deg, #0f172a 0%, #1e1b4b 50%, #0f172a 100%);
                border-bottom: 1px solid var(--border);
                padding: 0.9rem 2rem;
                display: flex;
                justify-content: space-between;
                align-items: center;
                flex-wrap: wrap;
                gap: 1rem;
            }
            .brand {
                display: flex;
                align-items: center;
                gap: 0.75rem;
                font-size: 1.25rem;
                font-weight: 700;
                color: #fff;
            }
            .brand-badge {
                background: linear-gradient(135deg, var(--primary), var(--accent));
                color: #fff;
                padding: 0.25rem 0.65rem;
                border-radius: 6px;
                font-size: 0.8rem;
                font-weight: 700;
                letter-spacing: 0.5px;
            }
            .nav-links {
                display: flex;
                gap: 0.5rem;
                flex-wrap: wrap;
                align-items: center;
            }
            .nav-link {
                color: var(--text-muted);
                padding: 0.5rem 0.9rem;
                border-radius: 6px;
                font-weight: 500;
                font-size: 0.92rem;
                transition: background 0.15s, color 0.15s;
            }
            .nav-link:hover, .nav-link.active {
                background: var(--bg-card);
                color: #fff;
                text-decoration: none;
            }
            .nav-link.active {
                border-bottom: 2px solid var(--primary);
            }
            .user-pill {
                display: flex;
                align-items: center;
                gap: 0.75rem;
                font-size: 0.88rem;
                color: var(--text-muted);
            }
            .container {
                max-width: 1320px;
                width: 100%;
                margin: 0 auto;
                padding: 1.75rem 2rem;
                flex: 1;
            }
            .page-header {
                display: flex;
                justify-content: space-between;
                align-items: flex-start;
                flex-wrap: wrap;
                gap: 1rem;
                margin-bottom: 1.5rem;
                padding-bottom: 1rem;
                border-bottom: 1px solid var(--border);
            }
            .page-title { font-size: 1.65rem; font-weight: 700; color: #fff; }
            .page-subtitle { color: var(--text-muted); font-size: 0.95rem; margin-top: 0.25rem; }
            .grid-4 {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
                gap: 1.25rem;
                margin-bottom: 1.75rem;
            }
            .grid-2 {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(420px, 1fr));
                gap: 1.5rem;
                margin-bottom: 1.75rem;
            }
            .card {
                background: var(--bg-card);
                border: 1px solid var(--border);
                border-radius: 10px;
                padding: 1.4rem;
                box-shadow: 0 4px 12px rgba(0,0,0,0.2);
                margin-bottom: 1.5rem;
            }
            .card-title {
                font-size: 1.1rem;
                font-weight: 600;
                margin-bottom: 1rem;
                color: #fff;
                display: flex;
                justify-content: space-between;
                align-items: center;
            }
            .stat-card {
                background: linear-gradient(145deg, #1e293b, #172033);
                border: 1px solid var(--border);
                border-radius: 10px;
                padding: 1.25rem;
            }
            .stat-label { color: var(--text-muted); font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.5px; }
            .stat-value { font-size: 2rem; font-weight: 800; color: #fff; margin: 0.35rem 0; }
            .stat-meta { font-size: 0.82rem; color: #60a5fa; }
            .btn {
                display: inline-block;
                background: var(--primary);
                color: #fff;
                border: none;
                padding: 0.6rem 1.2rem;
                border-radius: 6px;
                font-weight: 600;
                font-size: 0.9rem;
                cursor: pointer;
                text-decoration: none;
            }
            .btn:hover { background: var(--primary-hover); text-decoration: none; }
            .btn-secondary { background: #334155; color: #f8fafc; }
            .btn-secondary:hover { background: #475569; }
            .btn-success { background: var(--success); color: #fff; }
            .btn-danger { background: var(--danger); color: #fff; padding: 0.35rem 0.75rem; font-size: 0.8rem; }
            .btn-sm { padding: 0.35rem 0.75rem; font-size: 0.82rem; }
            .badge {
                display: inline-block;
                padding: 0.2rem 0.6rem;
                border-radius: 999px;
                font-size: 0.78rem;
                font-weight: 600;
                margin: 0.15rem;
            }
            .badge-match { background: rgba(16, 185, 129, 0.18); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.4); }
            .badge-synonym { background: rgba(139, 92, 246, 0.2); color: #c4b5fd; border: 1px solid rgba(139, 92, 246, 0.45); }
            .badge-miss { background: rgba(239, 68, 68, 0.16); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.35); }
            .badge-info { background: rgba(59, 130, 246, 0.18); color: #93c5fd; border: 1px solid rgba(59, 130, 246, 0.4); }
            .badge-warn { background: rgba(245, 158, 11, 0.18); color: #fcd34d; border: 1px solid rgba(245, 158, 11, 0.4); }
            table {
                width: 100%;
                border-collapse: collapse;
                font-size: 0.92rem;
            }
            th, td {
                padding: 0.85rem 0.9rem;
                text-align: left;
                border-bottom: 1px solid var(--border);
                vertical-align: middle;
            }
            th {
                background: rgba(15, 23, 42, 0.6);
                color: var(--text-muted);
                font-weight: 600;
                text-transform: uppercase;
                font-size: 0.78rem;
                letter-spacing: 0.5px;
            }
            tr:hover td { background: rgba(255, 255, 255, 0.02); }
            .progress-track {
                width: 100%;
                background: #0f172a;
                border-radius: 999px;
                height: 10px;
                overflow: hidden;
                border: 1px solid var(--border);
                margin-top: 0.3rem;
            }
            .progress-fill {
                height: 100%;
                border-radius: 999px;
            }
            .form-group { margin-bottom: 1rem; }
            label {
                display: block;
                font-size: 0.88rem;
                font-weight: 600;
                margin-bottom: 0.35rem;
                color: #cbd5e1;
            }
            input[type="text"], input[type="password"], input[type="number"], input[type="email"], input[type="file"], select, textarea {
                width: 100%;
                padding: 0.65rem 0.85rem;
                background: #0f172a;
                border: 1px solid var(--border);
                border-radius: 6px;
                color: #fff;
                font-size: 0.92rem;
                font-family: inherit;
            }
            input:focus, select:focus, textarea:focus {
                outline: none;
                border-color: var(--primary);
            }
            .alert {
                padding: 0.9rem 1.2rem;
                border-radius: 8px;
                margin-bottom: 1.25rem;
                font-size: 0.92rem;
            }
            .alert-success { background: rgba(16, 185, 129, 0.15); border: 1px solid var(--success); color: #6ee7b7; }
            .alert-error { background: rgba(239, 68, 68, 0.15); border: 1px solid var(--danger); color: #fca5a5; }
            .rank-circle {
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 32px;
                height: 32px;
                border-radius: 50%;
                font-weight: 700;
                font-size: 0.9rem;
            }
            .rank-1 { background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; }
            .rank-2 { background: linear-gradient(135deg, #94a3b8, #64748b); color: #fff; }
            .rank-3 { background: linear-gradient(135deg, #b45309, #92400e); color: #fff; }
            .rank-other { background: #0f172a; border: 1px solid var(--border); color: var(--text-muted); }
            .footer {
                border-top: 1px solid var(--border);
                padding: 1.2rem 2rem;
                text-align: center;
                color: var(--text-muted);
                font-size: 0.84rem;
                background: #0b1120;
            }
            """;
    }

    private static String layout(String title, String activeNav, AdminUser user, String flashMessage, boolean isError, String bodyContent) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
            .append("<meta charset=\"UTF-8\">\n")
            .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            .append("<title>").append(escape(title)).append(" | AI-Based Resume Screening System</title>\n")
            .append("<style>").append(baseStyles()).append("</style>\n")
            .append("</head>\n<body>\n");

        // Top Navigation Bar
        html.append("<header class=\"topbar\">\n")
            .append("  <div class=\"brand\">\n")
            .append("    <span class=\"brand-badge\">JAVA 21 + NLP</span>\n")
            .append("    <span>AI-Based Resume Screening System</span>\n")
            .append("  </div>\n");

        if (user != null) {
            html.append("  <nav class=\"nav-links\">\n")
                .append(navLink("/dashboard", "Dashboard", "dashboard".equals(activeNav)))
                .append(navLink("/jobs", "Job Openings", "jobs".equals(activeNav)))
                .append(navLink("/upload", "Upload Resumes", "upload".equals(activeNav)))
                .append(navLink("/rankings", "Candidate Rankings", "rankings".equals(activeNav)))
                .append(navLink("/analytics", "AI Analytics & Architecture", "analytics".equals(activeNav)))
                .append("  </nav>\n")
                .append("  <div class=\"user-pill\">\n")
                .append("    <span>Signed in as <strong>").append(escape(user.getFullName())).append("</strong></span>\n")
                .append("    <a href=\"/logout\" class=\"btn btn-secondary btn-sm\">Logout</a>\n")
                .append("  </div>\n");
        }
        html.append("</header>\n");

        html.append("<main class=\"container\">\n");
        if (flashMessage != null && !flashMessage.isBlank()) {
            html.append("<div class=\"alert ").append(isError ? "alert-error" : "alert-success").append("\">")
                .append(escape(flashMessage))
                .append("</div>\n");
        }
        html.append(bodyContent);
        html.append("</main>\n");

        html.append("<footer class=\"footer\">\n")
            .append("  100% Pure Java Full-Stack Web Application (Zero JavaScript) &bull; Powered by Java OOP, Collections, Multithreading, Stream API, File Parsers &amp; NLP Synonym Matching\n")
            .append("</footer>\n</body>\n</html>");
        return html.toString();
    }

    private static String navLink(String href, String label, boolean active) {
        return "<a href=\"" + href + "\" class=\"nav-link" + (active ? " active" : "") + "\">" + escape(label) + "</a>";
    }

    /**
     * 1. Admin Login & Registration Page
     */
    public static String renderLoginPage(String message, boolean isError) {
        String body = """
            <div style="max-width: 920px; margin: 2rem auto;">
                <div style="text-align: center; margin-bottom: 2rem;">
                    <span class="badge badge-info">100% Pure Java Web Application &bull; Zero JavaScript</span>
                    <h1 style="font-size: 2.1rem; margin-top: 0.5rem; color: #fff;">Automated AI &amp; NLP Resume Screening Portal</h1>
                    <p style="color: var(--text-muted); max-width: 680px; margin: 0.5rem auto 0;">
                        Parse PDF/DOCX resumes, extract technical skills &amp; synonyms using Natural Language Processing,
                        and automatically rank candidates against job descriptions with fair, bias-free scoring.
                    </p>
                </div>
                <div class="grid-2">
                    <div class="card">
                        <div class="card-title">HR Administrator Sign In</div>
                        <form method="POST" action="/login">
                            <div class="form-group">
                                <label>Username</label>
                                <input type="text" name="username" value="admin" required>
                            </div>
                            <div class="form-group">
                                <label>Password</label>
                                <input type="password" name="password" value="admin123" required>
                            </div>
                            <button type="submit" class="btn" style="width: 100%; margin-top: 0.5rem;">Sign In to HR Portal</button>
                        </form>
                        <div style="margin-top: 1.25rem; padding-top: 1rem; border-top: 1px solid var(--border); font-size: 0.85rem; color: var(--text-muted);">
                            <strong>Demo Credentials Pre-filled:</strong><br>
                            Admin: <code>admin</code> / <code>admin123</code> &nbsp;|&nbsp; Recruiter: <code>hr</code> / <code>hr123</code>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-title">Register New HR Administrator</div>
                        <form method="POST" action="/register">
                            <div class="form-group">
                                <label>Full Name &amp; Title</label>
                                <input type="text" name="fullName" placeholder="e.g., Neha Gupta (Senior Talent Partner)" required>
                            </div>
                            <div class="form-group">
                                <label>Choose Username</label>
                                <input type="text" name="username" placeholder="e.g., neha_hr" required>
                            </div>
                            <div class="form-group">
                                <label>Password</label>
                                <input type="password" name="password" placeholder="Create password" required>
                            </div>
                            <button type="submit" class="btn btn-secondary" style="width: 100%; margin-top: 0.5rem;">Create Admin Account</button>
                        </form>
                    </div>
                </div>
            </div>
            """;
        return layout("Admin Login", "login", null, message, isError, body);
    }

    /**
     * 2. Recruitment Dashboard
     */
    public static String renderDashboard(AdminUser user,
                                         Collection<JobOpening> jobs,
                                         Collection<CandidateResume> candidates,
                                         JobOpening selectedJob,
                                         List<ScreeningResult> rankedResults,
                                         int multithreadedTasksCount,
                                         String flash,
                                         boolean isError) {
        long strongMatches = rankedResults.stream().filter(r -> r.getExactSkillPercentage() >= 80.0).count();
        double avgScore = rankedResults.stream().mapToDouble(ScreeningResult::getExactSkillPercentage).average().orElse(0.0);

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"page-header\">\n")
          .append("  <div>\n")
          .append("    <h1 class=\"page-title\">Recruitment Screening Dashboard</h1>\n")
          .append("    <p class=\"page-subtitle\">Real-time AI skill extraction, weighted matching, and candidate ranking overview.</p>\n")
          .append("  </div>\n")
          .append("  <div style=\"display:flex; gap:0.75rem;\">\n")
          .append("    <a href=\"/upload\" class=\"btn\">+ Upload Resumes (PDF / DOCX)</a>\n")
          .append("    <a href=\"/jobs\" class=\"btn btn-secondary\">+ Create Job Opening</a>\n")
          .append("  </div>\n")
          .append("</div>\n");

        // 4 KPI Cards
        sb.append("<div class=\"grid-4\">\n")
          .append("  <div class=\"stat-card\">\n")
          .append("    <div class=\"stat-label\">Total Uploaded Resumes</div>\n")
          .append("    <div class=\"stat-value\">").append(candidates.size()).append("</div>\n")
          .append("    <div class=\"stat-meta\">Parsed via Java PDF/DOCX Engine</div>\n")
          .append("  </div>\n")
          .append("  <div class=\"stat-card\">\n")
          .append("    <div class=\"stat-label\">Active Job Openings</div>\n")
          .append("    <div class=\"stat-value\">").append(jobs.size()).append("</div>\n")
          .append("    <div class=\"stat-meta\">Active benchmark: ").append(escape(selectedJob.getId())).append("</div>\n")
          .append("  </div>\n")
          .append("  <div class=\"stat-card\">\n")
          .append("    <div class=\"stat-label\">Shortlisted (&ge; 80% Match)</div>\n")
          .append("    <div class=\"stat-value\" style=\"color: var(--success);\">").append(strongMatches).append("</div>\n")
          .append("    <div class=\"stat-meta\">For ").append(escape(selectedJob.getTitle())).append("</div>\n")
          .append("  </div>\n")
          .append("  <div class=\"stat-card\">\n")
          .append("    <div class=\"stat-label\">Average Skill Match</div>\n")
          .append("    <div class=\"stat-value\">").append(String.format(Locale.US, "%.1f%%", avgScore)).append("</div>\n")
          .append("    <div class=\"stat-meta\">").append(multithreadedTasksCount).append(" concurrent thread evaluations run</div>\n")
          .append("  </div>\n")
          .append("</div>\n");

        // Job Selector Bar
        sb.append("<div class=\"card\" style=\"padding: 1rem 1.4rem;\">\n")
          .append("  <form method=\"GET\" action=\"/dashboard\" style=\"display:flex; align-items:center; gap:1rem; flex-wrap:wrap;\">\n")
          .append("    <label style=\"margin:0; white-space:nowrap;\">Active Job Opening for Live Ranking:</label>\n")
          .append("    <select name=\"jobId\" style=\"max-width:440px;\">\n");
        for (JobOpening j : jobs) {
            boolean sel = j.getId().equals(selectedJob.getId());
            sb.append("      <option value=\"").append(escape(j.getId())).append("\"")
              .append(sel ? " selected" : "").append(">")
              .append(escape(j.getId())).append(" — ").append(escape(j.getTitle()))
              .append(" (Required: ").append(escape(String.join(", ", j.getRequiredSkills()))).append(")</option>\n");
        }
        sb.append("    </select>\n")
          .append("    <button type=\"submit\" class=\"btn btn-sm\">Switch Active Job</button>\n")
          .append("    <a href=\"/rankings?jobId=").append(escape(selectedJob.getId())).append("\" class=\"btn btn-secondary btn-sm\">Open Advanced Search &amp; Filters</a>\n")
          .append("  </form>\n")
          .append("</div>\n");

        // Active Job Required Skills Pill Box
        sb.append("<div class=\"card\">\n")
          .append("  <div class=\"card-title\">\n")
          .append("    <span>Target Job Requirements: ").append(escape(selectedJob.getTitle())).append(" (").append(escape(selectedJob.getDepartment())).append(")</span>\n")
          .append("    <span class=\"badge badge-info\">Min Experience: ").append(selectedJob.getMinExperienceYears()).append(" yrs &bull; ").append(escape(selectedJob.getRequiredEducation())).append("</span>\n")
          .append("  </div>\n")
          .append("  <p style=\"color: var(--text-muted); margin-bottom: 0.75rem;\">").append(escape(selectedJob.getDescription())).append("</p>\n")
          .append("  <div><strong>Required Skills &amp; Weights: </strong>");
        for (Map.Entry<String, Integer> entry : selectedJob.getRequiredSkillsWithWeights().entrySet()) {
            sb.append("<span class=\"badge badge-info\">").append(escape(entry.getKey()))
              .append(" (Weight: ").append(entry.getValue()).append("/5)</span> ");
        }
        sb.append("  </div>\n</div>\n");

        // Top Ranked Candidates Table (Matching Step 5 & Step 6 of specification)
        sb.append("<div class=\"card\">\n")
          .append("  <div class=\"card-title\">\n")
          .append("    <span>Automated Candidate Ranking Table (Sorted Highest to Lowest Match)</span>\n")
          .append("    <span class=\"badge badge-match\">Fair &amp; Bias-Free AI Evaluation</span>\n")
          .append("  </div>\n")
          .append(renderRankingTableHtml(rankedResults, selectedJob))
          .append("</div>\n");

        return layout("Recruitment Dashboard", "dashboard", user, flash, isError, sb.toString());
    }

    /**
     * 3. Job Description Creation & Management Page
     */
    public static String renderJobsPage(AdminUser user, Collection<JobOpening> jobs, String flash, boolean isError) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"page-header\">\n")
          .append("  <div>\n")
          .append("    <h1 class=\"page-title\">Job Description Management &amp; Skill Extraction</h1>\n")
          .append("    <p class=\"page-subtitle\">Create new job postings. The AI engine automatically extracts or weights required technical skills.</p>\n")
          .append("  </div>\n")
          .append("</div>\n");

        sb.append("<div class=\"grid-2\">\n")
          .append("  <div class=\"card\">\n")
          .append("    <div class=\"card-title\">Create New Job Opening</div>\n")
          .append("    <form method=\"POST\" action=\"/jobs\">\n")
          .append("      <div class=\"form-group\">\n")
          .append("        <label>Job Title</label>\n")
          .append("        <input type=\"text\" name=\"title\" placeholder=\"e.g., Senior Full-Stack Java Engineer\" required>\n")
          .append("      </div>\n")
          .append("      <div class=\"form-group\">\n")
          .append("        <label>Department</label>\n")
          .append("        <input type=\"text\" name=\"department\" placeholder=\"e.g., Enterprise FinTech Platform\" required>\n")
          .append("      </div>\n")
          .append("      <div class=\"form-group\">\n")
          .append("        <label>Required Skills (comma-separated, optional weight 1-5 e.g. Java:5, Spring Boot:5, MySQL:4, Hibernate:4, REST API:4)</label>\n")
          .append("        <input type=\"text\" name=\"requiredSkills\" value=\"Java:5, Spring Boot:5, MySQL:4, Hibernate:4, REST API:4\">\n")
          .append("      </div>\n")
          .append("      <div style=\"display:grid; grid-template-columns: 1fr 1fr; gap: 1rem;\">\n")
          .append("        <div class=\"form-group\">\n")
          .append("          <label>Minimum Experience (Years)</label>\n")
          .append("          <input type=\"number\" name=\"minExperience\" value=\"3\" min=\"0\" max=\"25\" required>\n")
          .append("        </div>\n")
          .append("        <div class=\"form-group\">\n")
          .append("          <label>Required Education Level</label>\n")
          .append("          <select name=\"requiredEducation\">\n")
          .append("            <option value=\"Bachelor's Degree\">Bachelor's Degree (B.Tech / B.E. / B.Sc)</option>\n")
          .append("            <option value=\"Master's Degree\">Master's Degree (M.Tech / MCA / MS)</option>\n")
          .append("            <option value=\"Ph.D.\">Ph.D. / Doctorate</option>\n")
          .append("            <option value=\"Technical Diploma\">Technical Diploma</option>\n")
          .append("          </select>\n")
          .append("        </div>\n")
          .append("      </div>\n")
          .append("      <div class=\"form-group\">\n")
          .append("        <label>Job Description (NLP Analyzer also scans this text for required skills)</label>\n")
          .append("        <textarea name=\"description\" rows=\"4\" placeholder=\"Describe responsibilities, architecture, and technical qualifications...\" required>We are looking for a Java Developer proficient in Spring Boot, MySQL, Hibernate ORM, and REST APIs to build scalable backend services.</textarea>\n")
          .append("      </div>\n")
          .append("      <button type=\"submit\" class=\"btn\">Create Job &amp; Run AI Screening</button>\n")
          .append("    </form>\n")
          .append("  </div>\n");

        sb.append("  <div>\n");
        for (JobOpening job : jobs) {
            sb.append("    <div class=\"card\">\n")
              .append("      <div class=\"card-title\">\n")
              .append("        <span>").append(escape(job.getId())).append(": ").append(escape(job.getTitle())).append("</span>\n")
              .append("        <a href=\"/rankings?jobId=").append(escape(job.getId())).append("\" class=\"btn btn-sm\">View Ranked Candidates</a>\n")
              .append("      </div>\n")
              .append("      <div style=\"font-size:0.86rem; color:var(--text-muted); margin-bottom:0.6rem;\">")
              .append("Department: <strong>").append(escape(job.getDepartment())).append("</strong> &bull; ")
              .append("Min Experience: <strong>").append(job.getMinExperienceYears()).append(" yrs</strong> &bull; ")
              .append("Education: <strong>").append(escape(job.getRequiredEducation())).append("</strong>")
              .append("      </div>\n")
              .append("      <p style=\"font-size:0.9rem; margin-bottom:0.8rem;\">").append(escape(job.getDescription())).append("</p>\n")
              .append("      <div>");
            for (Map.Entry<String, Integer> sk : job.getRequiredSkillsWithWeights().entrySet()) {
                sb.append("<span class=\"badge badge-info\">").append(escape(sk.getKey()))
                  .append(" (W:").append(sk.getValue()).append(")</span> ");
            }
            sb.append("      </div>\n")
              .append("    </div>\n");
        }
        sb.append("  </div>\n</div>\n");

        return layout("Job Openings", "jobs", user, flash, isError, sb.toString());
    }

    /**
     * 4. Resume Upload & Parsing Page
     */
    public static String renderUploadPage(AdminUser user, Collection<JobOpening> jobs, Collection<CandidateResume> candidates, String flash, boolean isError) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"page-header\">\n")
          .append("  <div>\n")
          .append("    <h1 class=\"page-title\">Resume Upload &amp; Automated NLP Parsing</h1>\n")
          .append("    <p class=\"page-subtitle\">Upload candidate resumes in PDF, DOCX, or TXT format, or paste raw resume text for instant skill extraction.</p>\n")
          .append("  </div>\n")
          .append("</div>\n");

        sb.append("<div class=\"grid-2\">\n")
          .append("  <div class=\"card\">\n")
          .append("    <div class=\"card-title\">Option 1: Upload Resume File (.pdf, .docx, .txt)</div>\n")
          .append("    <form method=\"POST\" action=\"/upload\" enctype=\"multipart/form-data\">\n")
          .append("      <div class=\"form-group\">\n")
          .append("        <label>Select PDF, DOCX, or TXT Resume File</label>\n")
          .append("        <input type=\"file\" name=\"resumeFile\" accept=\".pdf,.docx,.txt\" required>\n")
          .append("      </div>\n")
          .append("      <div class=\"form-group\">\n")
          .append("        <label>Target Job Opening for Immediate Evaluation</label>\n")
          .append("        <select name=\"jobId\">\n");
        for (JobOpening j : jobs) {
            sb.append("          <option value=\"").append(escape(j.getId())).append("\">")
              .append(escape(j.getId())).append(" — ").append(escape(j.getTitle())).append("</option>\n");
        }
        sb.append("        </select>\n")
          .append("      </div>\n")
          .append("      <button type=\"submit\" class=\"btn\">Upload &amp; Parse Resume File</button>\n")
          .append("    </form>\n")
          .append("    <div style=\"margin-top: 1.25rem; padding-top: 1rem; border-top: 1px solid var(--border); font-size: 0.85rem; color: var(--text-muted);\">\n")
          .append("      <strong>Java File Handling Architecture:</strong><br>\n")
          .append("      &bull; <code>.docx</code> files are extracted using Java <code>ZipInputStream</code> parsing <code>word/document.xml</code>.<br>\n")
          .append("      &bull; <code>.pdf</code> files are parsed via stream token extraction with exception handling.<br>\n")
          .append("      &bull; Extracted skills are mapped against our NLP Synonym Dictionary.\n")
          .append("    </div>\n")
          .append("  </div>\n");

        sb.append("  <div class=\"card\">\n")
          .append("    <div class=\"card-title\">Option 2: Direct Resume Text Paste (Quick Test)</div>\n")
          .append("    <form method=\"POST\" action=\"/upload-text\">\n")
          .append("      <div class=\"form-group\">\n")
          .append("        <label>File Label / Candidate Document Name</label>\n")
          .append("        <input type=\"text\" name=\"fileName\" value=\"Candidate_H_Rahul_Verma.pdf\" required>\n")
          .append("      </div>\n")
          .append("      <div class=\"form-group\">\n")
          .append("        <label>Paste Full Resume Content (Name, Email, Education, Experience, Skills)</label>\n")
          .append("        <textarea name=\"resumeText\" rows=\"7\" required>Rahul Verma\nEmail: rahul.verma@javadev.in | Phone: +91-98112-88990\nEducation: B.Tech in Computer Science\nExperience: 4.5 years of experience in Backend Software Engineering.\nSkills: Java Programming, Spring MVC, Spring Boot, Hibernate ORM, MySQL, RESTful Web Services, Docker, Git, JUnit.\nCertifications: Oracle Certified Professional (OCP Java).\nWorked on high-volume banking APIs and relational database optimization.</textarea>\n")
          .append("      </div>\n")
          .append("      <button type=\"submit\" class=\"btn btn-success\">Parse Text &amp; Add Candidate</button>\n")
          .append("    </form>\n")
          .append("  </div>\n")
          .append("</div>\n");

        // List of all uploaded resumes
        sb.append("<div class=\"card\">\n")
          .append("  <div class=\"card-title\">Uploaded &amp; Parsed Candidate Repository (").append(candidates.size()).append(" Resumes)</div>\n")
          .append("  <table>\n")
          .append("    <thead>\n")
          .append("      <tr>\n")
          .append("        <th>ID</th>\n")
          .append("        <th>Candidate Name &amp; Contact</th>\n")
          .append("        <th>File</th>\n")
          .append("        <th>Education &amp; Exp</th>\n")
          .append("        <th>Extracted Technical Skills (NLP)</th>\n")
          .append("        <th>Actions</th>\n")
          .append("      </tr>\n")
          .append("    </thead>\n")
          .append("    <tbody>\n");
        for (CandidateResume c : candidates) {
            sb.append("      <tr>\n")
              .append("        <td><code>").append(escape(c.getId())).append("</code></td>\n")
              .append("        <td><strong>").append(escape(c.getCandidateName())).append("</strong><br><span style=\"font-size:0.8rem; color:var(--text-muted);\">").append(escape(c.getEmail())).append("</span></td>\n")
              .append("        <td>").append(escape(c.getFileName())).append("</td>\n")
              .append("        <td>").append(escape(c.getHighestEducation())).append("<br><span class=\"badge badge-info\">").append(c.getExperienceYears()).append(" yrs exp</span></td>\n")
              .append("        <td>");
            for (Map.Entry<String, String> sk : c.getExtractedSkillsWithEvidence().entrySet()) {
                boolean isSyn = sk.getValue().startsWith("NLP Synonym");
                sb.append("<span class=\"badge ").append(isSyn ? "badge-synonym" : "badge-match").append("\" title=\"").append(escape(sk.getValue())).append("\">")
                  .append(escape(sk.getKey())).append(isSyn ? " *" : "").append("</span>");
            }
            sb.append("</td>\n")
              .append("        <td style=\"white-space:nowrap;\">\n")
              .append("          <a href=\"/candidate?id=").append(escape(c.getId())).append("\" class=\"btn btn-secondary btn-sm\">Report</a>\n")
              .append("          <form method=\"POST\" action=\"/delete-candidate\" style=\"display:inline;\">\n")
              .append("            <input type=\"hidden\" name=\"id\" value=\"").append(escape(c.getId())).append("\">\n")
              .append("            <button type=\"submit\" class=\"btn btn-danger\">Delete</button>\n")
              .append("          </form>\n")
              .append("        </td>\n")
              .append("      </tr>\n");
        }
        sb.append("    </tbody>\n  </table>\n</div>\n");

        return layout("Upload Resumes", "upload", user, flash, isError, sb.toString());
    }

    /**
     * 5. Candidate Ranking & Search/Filter Page
     */
    public static String renderRankingsPage(AdminUser user,
                                            Collection<JobOpening> jobs,
                                            JobOpening selectedJob,
                                            List<ScreeningResult> results,
                                            String skillFilter,
                                            double minExpFilter,
                                            double minScoreFilter,
                                            String keyword,
                                            Collection<String> allKnownSkills) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"page-header\">\n")
          .append("  <div>\n")
          .append("    <h1 class=\"page-title\">Candidate Ranking &amp; Stream API Filtering</h1>\n")
          .append("    <p class=\"page-subtitle\">Filter and rank candidates by job requirements, specific skills, minimum experience, and match percentage.</p>\n")
          .append("  </div>\n")
          .append("</div>\n");

        // Filter Form
        sb.append("<div class=\"card\">\n")
          .append("  <div class=\"card-title\">Search &amp; Filter Candidates (Powered by Java Stream API)</div>\n")
          .append("  <form method=\"GET\" action=\"/rankings\">\n")
          .append("    <div style=\"display:grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; align-items: end;\">\n")
          .append("      <div class=\"form-group\" style=\"margin:0;\">\n")
          .append("        <label>Target Job Opening</label>\n")
          .append("        <select name=\"jobId\">\n");
        for (JobOpening j : jobs) {
            boolean sel = j.getId().equals(selectedJob.getId());
            sb.append("          <option value=\"").append(escape(j.getId())).append("\"").append(sel ? " selected" : "").append(">")
              .append(escape(j.getId())).append(" — ").append(escape(j.getTitle())).append("</option>\n");
        }
        sb.append("        </select>\n")
          .append("      </div>\n")
          .append("      <div class=\"form-group\" style=\"margin:0;\">\n")
          .append("        <label>Filter by Technical Skill</label>\n")
          .append("        <select name=\"skill\">\n")
          .append("          <option value=\"ALL\">All Skills</option>\n");
        for (String sk : allKnownSkills) {
            boolean sel = sk.equalsIgnoreCase(skillFilter);
            sb.append("          <option value=\"").append(escape(sk)).append("\"").append(sel ? " selected" : "").append(">")
              .append(escape(sk)).append("</option>\n");
        }
        sb.append("        </select>\n")
          .append("      </div>\n")
          .append("      <div class=\"form-group\" style=\"margin:0;\">\n")
          .append("        <label>Min Experience (Years)</label>\n")
          .append("        <input type=\"number\" step=\"0.5\" min=\"0\" max=\"20\" name=\"minExp\" value=\"").append(minExpFilter).append("\">\n")
          .append("      </div>\n")
          .append("      <div class=\"form-group\" style=\"margin:0;\">\n")
          .append("        <label>Min Skill Match Score (%)</label>\n")
          .append("        <input type=\"number\" step=\"10\" min=\"0\" max=\"100\" name=\"minScore\" value=\"").append((int) minScoreFilter).append("\">\n")
          .append("      </div>\n")
          .append("      <div class=\"form-group\" style=\"margin:0;\">\n")
          .append("        <label>Keyword (Name / Education)</label>\n")
          .append("        <input type=\"text\" name=\"q\" value=\"").append(escape(keyword == null ? "" : keyword)).append("\" placeholder=\"Search name or degree...\">\n")
          .append("      </div>\n")
          .append("      <div style=\"display:flex; gap:0.5rem;\">\n")
          .append("        <button type=\"submit\" class=\"btn\">Apply Filters</button>\n")
          .append("        <a href=\"/rankings?jobId=").append(escape(selectedJob.getId())).append("\" class=\"btn btn-secondary\">Reset</a>\n")
          .append("      </div>\n")
          .append("    </div>\n")
          .append("  </form>\n")
          .append("</div>\n");

        sb.append("<div class=\"card\">\n")
          .append("  <div class=\"card-title\">\n")
          .append("    <span>Screening Results for ").append(escape(selectedJob.getTitle())).append(" (").append(results.size()).append(" Candidates Matched)</span>\n")
          .append("    <span class=\"badge badge-info\">Required: ").append(escape(String.join(", ", selectedJob.getRequiredSkills()))).append("</span>\n")
          .append("  </div>\n")
          .append(renderRankingTableHtml(results, selectedJob))
          .append("</div>\n");

        return layout("Candidate Rankings", "rankings", user, null, false, sb.toString());
    }

    private static String renderRankingTableHtml(List<ScreeningResult> results, JobOpening selectedJob) {
        if (results.isEmpty()) {
            return "<p style=\"color: var(--text-muted); padding: 1rem 0;\">No candidates matched the current filter criteria.</p>";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<table>\n")
          .append("  <thead>\n")
          .append("    <tr>\n")
          .append("      <th>Rank</th>\n")
          .append("      <th>Candidate</th>\n")
          .append("      <th>Skills Matched</th>\n")
          .append("      <th>Exact Skill Match</th>\n")
          .append("      <th>Composite AI Score</th>\n")
          .append("      <th>Matched &amp; Missing Skills Breakdown</th>\n")
          .append("      <th>Experience &amp; Education</th>\n")
          .append("      <th>Report</th>\n")
          .append("    </tr>\n")
          .append("  </thead>\n")
          .append("  <tbody>\n");

        for (ScreeningResult r : results) {
            CandidateResume c = r.getCandidate();
            String rankClass = r.getRank() == 1 ? "rank-1" : (r.getRank() == 2 ? "rank-2" : (r.getRank() == 3 ? "rank-3" : "rank-other"));
            String barColor = r.getExactSkillPercentage() >= 80 ? "var(--success)"
                    : (r.getExactSkillPercentage() >= 60 ? "var(--warning)" : "var(--danger)");

            sb.append("    <tr>\n")
              .append("      <td><span class=\"rank-circle ").append(rankClass).append("\">#").append(r.getRank()).append("</span></td>\n")
              .append("      <td>\n")
              .append("        <strong>").append(escape(c.getCandidateName())).append("</strong><br>\n")
              .append("        <span style=\"font-size:0.8rem; color:var(--text-muted);\">").append(escape(c.getEmail())).append("</span>\n")
              .append("      </td>\n")
              .append("      <td><strong>").append(r.getMatchedSkillCount()).append(" / ").append(r.getTotalRequiredSkills()).append("</strong></td>\n")
              .append("      <td style=\"min-width:140px;\">\n")
              .append("        <strong style=\"font-size:1.05rem;\">").append(String.format(Locale.US, "%.0f%%", r.getExactSkillPercentage())).append("</strong>\n")
              .append("        <div class=\"progress-track\"><div class=\"progress-fill\" style=\"width:").append((int) r.getExactSkillPercentage()).append("%; background:").append(barColor).append(";\"></div></div>\n")
              .append("      </td>\n")
              .append("      <td>\n")
              .append("        <span class=\"badge ").append(r.getExactSkillPercentage() >= 80 ? "badge-match" : (r.getExactSkillPercentage() >= 60 ? "badge-warn" : "badge-miss")).append("\">")
              .append(String.format(Locale.US, "%.1f%% AI Score", r.getOverallCompositeScore())).append("</span><br>\n")
              .append("        <span style=\"font-size:0.76rem; color:var(--text-muted);\">").append(escape(r.getRecommendationTier())).append("</span>\n")
              .append("      </td>\n")
              .append("      <td>\n");

            for (Map.Entry<String, String> m : r.getMatchedSkillsEvidence().entrySet()) {
                boolean isSynonym = m.getValue().startsWith("NLP Synonym");
                sb.append("<span class=\"badge ").append(isSynonym ? "badge-synonym" : "badge-match")
                  .append("\" title=\"").append(escape(m.getValue())).append("\">&#10003; ")
                  .append(escape(m.getKey())).append(isSynonym ? " (Synonym)" : "").append("</span>");
            }
            for (String miss : r.getMissingSkills()) {
                sb.append("<span class=\"badge badge-miss\">&#10007; ").append(escape(miss)).append("</span>");
            }

            sb.append("      </td>\n")
              .append("      <td>\n")
              .append("        <strong>").append(c.getExperienceYears()).append(" yrs</strong><br>\n")
              .append("        <span style=\"font-size:0.8rem; color:var(--text-muted);\">").append(escape(c.getHighestEducation())).append("</span>\n")
              .append("      </td>\n")
              .append("      <td>\n")
              .append("        <a href=\"/candidate?id=").append(escape(c.getId())).append("&jobId=").append(escape(selectedJob.getId())).append("\" class=\"btn btn-sm\">Full Profile</a>\n")
              .append("      </td>\n")
              .append("    </tr>\n");
        }

        sb.append("  </tbody>\n</table>\n");
        return sb.toString();
    }

    /**
     * 6. Individual Candidate Profile & Detailed Screening Evaluation Report
     */
    public static String renderCandidateProfilePage(AdminUser user,
                                                    CandidateResume candidate,
                                                    JobOpening selectedJob,
                                                    ScreeningResult evaluation,
                                                    Collection<JobOpening> allJobs) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"page-header\">\n")
          .append("  <div>\n")
          .append("    <h1 class=\"page-title\">Candidate Evaluation Report: ").append(escape(candidate.getCandidateName())).append("</h1>\n")
          .append("    <p class=\"page-subtitle\">Detailed NLP skill extraction, synonym evidence, and explainable AI scoring breakdown.</p>\n")
          .append("  </div>\n")
          .append("  <div style=\"display:flex; gap:0.75rem;\">\n")
          .append("    <a href=\"/rankings?jobId=").append(escape(selectedJob.getId())).append("\" class=\"btn btn-secondary\">&larr; Back to Rankings</a>\n")
          .append("  </div>\n")
          .append("</div>\n");

        // Job switcher for this candidate
        sb.append("<div class=\"card\" style=\"padding: 1rem 1.4rem;\">\n")
          .append("  <form method=\"GET\" action=\"/candidate\" style=\"display:flex; align-items:center; gap:1rem; flex-wrap:wrap;\">\n")
          .append("    <input type=\"hidden\" name=\"id\" value=\"").append(escape(candidate.getId())).append("\">\n")
          .append("    <label style=\"margin:0;\">Compare Candidate Against Job Opening:</label>\n")
          .append("    <select name=\"jobId\" style=\"max-width:420px;\">\n");
        for (JobOpening j : allJobs) {
            boolean sel = j.getId().equals(selectedJob.getId());
            sb.append("      <option value=\"").append(escape(j.getId())).append("\"").append(sel ? " selected" : "").append(">")
              .append(escape(j.getId())).append(" — ").append(escape(j.getTitle())).append("</option>\n");
        }
        sb.append("    </select>\n")
          .append("    <button type=\"submit\" class=\"btn btn-sm\">Recalculate Match</button>\n")
          .append("  </form>\n")
          .append("</div>\n");

        // Score Breakdown Cards
        sb.append("<div class=\"grid-4\">\n")
          .append("  <div class=\"stat-card\">\n")
          .append("    <div class=\"stat-label\">Exact Skill Match Ratio</div>\n")
          .append("    <div class=\"stat-value\" style=\"color: var(--success);\">")
          .append(evaluation.getMatchedSkillCount()).append("/").append(evaluation.getTotalRequiredSkills())
          .append(" (").append(String.format(Locale.US, "%.0f%%", evaluation.getExactSkillPercentage())).append(")</div>\n")
          .append("    <div class=\"stat-meta\">").append(escape(evaluation.getRecommendationTier())).append("</div>\n")
          .append("  </div>\n")
          .append("  <div class=\"stat-card\">\n")
          .append("    <div class=\"stat-label\">Weighted Technical Score</div>\n")
          .append("    <div class=\"stat-value\">").append(String.format(Locale.US, "%.1f%%", evaluation.getWeightedSkillScore())).append("</div>\n")
          .append("    <div class=\"stat-meta\">Accounts for 1-5 skill importance weights</div>\n")
          .append("  </div>\n")
          .append("  <div class=\"stat-card\">\n")
          .append("    <div class=\"stat-label\">Experience &amp; Education</div>\n")
          .append("    <div class=\"stat-value\">").append(String.format(Locale.US, "%.0f%% / %.0f%%", evaluation.getExperienceScore(), evaluation.getEducationScore())).append("</div>\n")
          .append("    <div class=\"stat-meta\">").append(candidate.getExperienceYears()).append(" yrs exp &bull; ").append(escape(candidate.getHighestEducation())).append("</div>\n")
          .append("  </div>\n")
          .append("  <div class=\"stat-card\">\n")
          .append("    <div class=\"stat-label\">Composite AI Score</div>\n")
          .append("    <div class=\"stat-value\" style=\"color:#60a5fa;\">").append(String.format(Locale.US, "%.1f%%", evaluation.getOverallCompositeScore())).append("</div>\n")
          .append("    <div class=\"stat-meta\">75% Skill + 15% Exp + 10% Education</div>\n")
          .append("  </div>\n")
          .append("</div>\n");

        sb.append("<div class=\"grid-2\">\n")
          .append("  <div class=\"card\">\n")
          .append("    <div class=\"card-title\">Explainable AI Screening Rationale</div>\n")
          .append("    <ul style=\"padding-left: 1.25rem; color: #e2e8f0;\">\n");
        for (String point : evaluation.getAiExplanationPoints()) {
            sb.append("      <li style=\"margin-bottom: 0.65rem;\">").append(escape(point)).append("</li>\n");
        }
        sb.append("    </ul>\n")
          .append("    <div style=\"margin-top: 1.2rem; padding: 0.9rem; background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.35); border-radius: 8px; font-size: 0.86rem;\">\n")
          .append("      <strong>Ethical AI &amp; Unbiased Ranking Guarantee:</strong> Candidate ranking is computed strictly from job-related technical competencies, verified experience, and educational qualifications—never from gender, age, or ethnicity.\n")
          .append("    </div>\n")
          .append("  </div>\n");

        sb.append("  <div class=\"card\">\n")
          .append("    <div class=\"card-title\">Skill-by-Skill Evidence &amp; Synonym Detection</div>\n")
          .append("    <table>\n")
          .append("      <thead><tr><th>Required Skill</th><th>Weight</th><th>Status &amp; NLP Evidence</th></tr></thead>\n")
          .append("      <tbody>\n");
        for (Map.Entry<String, Integer> req : selectedJob.getRequiredSkillsWithWeights().entrySet()) {
            String skill = req.getKey();
            String evidence = evaluation.getMatchedSkillsEvidence().get(skill);
            sb.append("        <tr>\n")
              .append("          <td><strong>").append(escape(skill)).append("</strong></td>\n")
              .append("          <td>").append(req.getValue()).append(" / 5</td>\n")
              .append("          <td>");
            if (evidence != null) {
                boolean isSyn = evidence.startsWith("NLP Synonym");
                sb.append("<span class=\"badge ").append(isSyn ? "badge-synonym" : "badge-match").append("\">&#10003; ")
                  .append(escape(evidence)).append("</span>");
            } else {
                sb.append("<span class=\"badge badge-miss\">&#10007; Not detected in resume</span>");
            }
            sb.append("</td>\n        </tr>\n");
        }
        sb.append("      </tbody>\n    </table>\n");

        if (!evaluation.getBonusSkills().isEmpty()) {
            sb.append("    <div style=\"margin-top: 1rem;\">\n")
              .append("      <strong>Additional / Bonus Skills Identified in Resume:</strong><br>\n");
            for (String bonus : evaluation.getBonusSkills()) {
                sb.append("<span class=\"badge badge-info\">+ ").append(escape(bonus)).append("</span> ");
            }
            sb.append("    </div>\n");
        }
        sb.append("  </div>\n</div>\n");

        // Candidate Metadata & Raw Extracted Resume Text
        sb.append("<div class=\"grid-2\">\n")
          .append("  <div class=\"card\">\n")
          .append("    <div class=\"card-title\">Extracted Candidate Metadata</div>\n")
          .append("    <table>\n")
          .append("      <tr><th>Candidate ID</th><td><code>").append(escape(candidate.getId())).append("</code></td></tr>\n")
          .append("      <tr><th>Full Name</th><td><strong>").append(escape(candidate.getCandidateName())).append("</strong></td></tr>\n")
          .append("      <tr><th>Email Address</th><td>").append(escape(candidate.getEmail())).append("</td></tr>\n")
          .append("      <tr><th>Phone Number</th><td>").append(escape(candidate.getPhone())).append("</td></tr>\n")
          .append("      <tr><th>Source Document</th><td>").append(escape(candidate.getFileName())).append(" (Parsed in ").append(candidate.getParseTimeMs()).append(" ms)</td></tr>\n")
          .append("      <tr><th>Education</th><td>").append(escape(candidate.getHighestEducation())).append("</td></tr>\n")
          .append("      <tr><th>Work Experience</th><td>").append(candidate.getExperienceYears()).append(" years</td></tr>\n")
          .append("      <tr><th>Certifications</th><td>").append(candidate.getCertifications().isEmpty() ? "None listed" : escape(String.join(", ", candidate.getCertifications()))).append("</td></tr>\n")
          .append("    </table>\n")
          .append("  </div>\n")
          .append("  <div class=\"card\">\n")
          .append("    <div class=\"card-title\">Extracted Raw Resume Text</div>\n")
          .append("    <pre style=\"background:#0f172a; padding:1rem; border-radius:8px; border:1px solid var(--border); white-space:pre-wrap; font-size:0.86rem; color:#cbd5e1; max-height:290px; overflow-y:auto;\">")
          .append(escape(candidate.getRawText()))
          .append("</pre>\n")
          .append("  </div>\n")
          .append("</div>\n");

        return layout("Candidate Profile - " + candidate.getCandidateName(), "rankings", user, null, false, sb.toString());
    }

    /**
     * 7. Recruitment Analytics & Advanced Java Architecture Dashboard
     */
    public static String renderAnalyticsPage(AdminUser user,
                                             Collection<JobOpening> jobs,
                                             Collection<CandidateResume> candidates,
                                             Map<String, List<String>> synonymTaxonomy,
                                             int multithreadedCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"page-header\">\n")
          .append("  <div>\n")
          .append("    <h1 class=\"page-title\">Recruitment Analytics &amp; Java Architecture Overview</h1>\n")
          .append("    <p class=\"page-subtitle\">Skill distribution charts, NLP synonym dictionary, and Advanced Java implementation metrics.</p>\n")
          .append("  </div>\n")
          .append("</div>\n");

        // Compute skill frequency across all resumes using Stream API
        Map<String, Long> skillCounts = candidates.stream()
                .flatMap(c -> c.getExtractedSkills().stream())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        sb.append("<div class=\"grid-2\">\n")
          .append("  <div class=\"card\">\n")
          .append("    <div class=\"card-title\">Candidate Talent Pool Skill Distribution (Pure CSS Chart)</div>\n");

        int totalCands = Math.max(1, candidates.size());
        List<Map.Entry<String, Long>> sortedSkills = skillCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        for (Map.Entry<String, Long> entry : sortedSkills) {
            int pct = (int) Math.round((entry.getValue() * 100.0) / totalCands);
            sb.append("    <div style=\"margin-bottom: 0.75rem;\">\n")
              .append("      <div style=\"display:flex; justify-content:space-between; font-size:0.86rem;\">\n")
              .append("        <span><strong>").append(escape(entry.getKey())).append("</strong></span>\n")
              .append("        <span>").append(entry.getValue()).append(" / ").append(candidates.size()).append(" candidates (").append(pct).append("%)</span>\n")
              .append("      </div>\n")
              .append("      <div class=\"progress-track\"><div class=\"progress-fill\" style=\"width:").append(pct).append("%; background: linear-gradient(90deg, var(--primary), var(--accent));\"></div></div>\n")
              .append("    </div>\n");
        }
        sb.append("  </div>\n");

        // Advanced Java Concepts Used Card
        sb.append("  <div class=\"card\">\n")
          .append("    <div class=\"card-title\">Advanced Java Concepts Demonstrated</div>\n")
          .append("    <table>\n")
          .append("      <thead><tr><th>Java Concept</th><th>Implementation in This Project</th></tr></thead>\n")
          .append("      <tbody>\n")
          .append("        <tr><td><strong>OOP &amp; Polymorphism</strong></td><td>Abstract <code>BaseEntity</code> inheritance; <code>ScoringStrategy</code> interface with polymorphic <code>MatchingEngine</code>.</td></tr>\n")
          .append("        <tr><td><strong>Collections Framework</strong></td><td><code>ConcurrentHashMap</code>, <code>LinkedHashMap</code>, <code>HashSet</code>, and <code>ArrayList</code> for skill taxonomies and candidate stores.</td></tr>\n")
          .append("        <tr><td><strong>Multithreading</strong></td><td><code>ExecutorService</code> thread pool &amp; <code>Callable/Future</code> concurrently evaluating resumes (").append(multithreadedCount).append(" tasks executed).</td></tr>\n")
          .append("        <tr><td><strong>Stream API</strong></td><td><code>stream().filter().sorted().collect()</code> pipelines in <code>RankingEngine</code> and analytics aggregation.</td></tr>\n")
          .append("        <tr><td><strong>File Handling</strong></td><td>Native <code>ZipInputStream</code> XML parser for <code>.docx</code> and stream literal extractor for <code>.pdf</code> files.</td></tr>\n")
          .append("        <tr><td><strong>Exception Handling</strong></td><td>Custom checked <code>ResumeParseException</code> with graceful user error alerts.</td></tr>\n")
          .append("        <tr><td><strong>JDBC / Persistence</strong></td><td><code>PreparedStatement</code> SQL persistence in <code>DatabaseRepository</code> + automatic in-memory fallback.</td></tr>\n")
          .append("      </tbody>\n")
          .append("    </table>\n")
          .append("  </div>\n")
          .append("</div>\n");

        // NLP Synonym Taxonomy Table
        sb.append("<div class=\"card\">\n")
          .append("  <div class=\"card-title\">NLP Skill Synonym &amp; Related Technology Knowledge Graph</div>\n")
          .append("  <p style=\"color:var(--text-muted); font-size:0.9rem; margin-bottom:1rem;\">Instead of naive keyword matching, the <code>SkillAnalyzerService</code> maps synonymous terms and related frameworks to canonical skills:</p>\n")
          .append("  <table>\n")
          .append("    <thead><tr><th>Canonical Skill</th><th>Recognized NLP Synonyms, Aliases &amp; Sub-Technologies</th></tr></thead>\n")
          .append("    <tbody>\n");
        for (Map.Entry<String, List<String>> entry : synonymTaxonomy.entrySet()) {
            sb.append("      <tr>\n")
              .append("        <td><span class=\"badge badge-info\">").append(escape(entry.getKey())).append("</span></td>\n")
              .append("        <td>").append(escape(String.join(", ", entry.getValue()))).append("</td>\n")
              .append("      </tr>\n");
        }
        sb.append("    </tbody>\n  </table>\n</div>\n");

        return layout("Analytics & Architecture", "analytics", user, null, false, sb.toString());
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
