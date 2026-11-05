package com.aiscreening;

import com.aiscreening.model.AdminUser;
import com.aiscreening.model.JobOpening;
import com.aiscreening.model.ScreeningResult;
import com.aiscreening.service.DatabaseRepository;
import com.aiscreening.service.MatchingEngine;
import com.aiscreening.service.RankingEngine;
import com.aiscreening.service.ResumeParserService;
import com.aiscreening.service.SkillAnalyzerService;
import com.aiscreening.web.HtmlViews;
import com.aiscreening.web.WebServer;

import java.util.List;

/**
 * Main entry point for the AI-Based Resume Screening System (100% Pure Java Full-Stack Web App).
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "--verify".equalsIgnoreCase(args[0])) {
            runSelfVerification();
            return;
        }

        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }

        WebServer server = new WebServer(port);
        server.start();

        System.out.println("=========================================================================");
        System.out.println("  AI-BASED RESUME SCREENING SYSTEM (100% Pure Java, Zero JavaScript)");
        System.out.println("  Server running at: http://localhost:" + port);
        System.out.println("  REST API endpoint: http://localhost:" + port + "/api/rankings?jobId=JOB-101");
        System.out.println("  Default HR Login : admin / admin123");
        System.out.println("=========================================================================");
    }

    private static void runSelfVerification() {
        SkillAnalyzerService skillAnalyzer = new SkillAnalyzerService();
        ResumeParserService parser = new ResumeParserService(skillAnalyzer);
        MatchingEngine matchingEngine = new MatchingEngine();
        RankingEngine rankingEngine = new RankingEngine(matchingEngine);
        DatabaseRepository repo = new DatabaseRepository(parser);

        AdminUser admin = repo.authenticate("admin", "admin123");
        JobOpening job = repo.getJobById("JOB-101");
        List<ScreeningResult> ranked = rankingEngine.rankCandidatesForJob(
                repo.getAllCandidates(), job, "ALL", 0.0, 0.0, null
        );

        String dashboardHtml = HtmlViews.renderDashboard(
                admin, repo.getAllJobs(), repo.getAllCandidates(), job, ranked,
                rankingEngine.getTotalMultithreadedTasksProcessed(), null, false
        );

        System.out.println("=== SELF-VERIFICATION REPORT ===");
        System.out.println("Target Job: " + job.getId() + " - " + job.getTitle());
        System.out.println("Required Skills: " + job.getRequiredSkills());
        System.out.println("Dashboard HTML Length: " + dashboardHtml.length() + " chars | Contains <script>: " + dashboardHtml.toLowerCase().contains("<script"));
        System.out.println("\nRanked Candidates (Highest to Lowest Match):");
        for (ScreeningResult r : ranked) {
            System.out.printf("  Rank #%d | %-28s | Skills: %d/%d | Exact Match: %5.1f%% | AI Score: %5.1f%% | Tier: %s%n",
                    r.getRank(),
                    r.getCandidate().getCandidateName(),
                    r.getMatchedSkillCount(),
                    r.getTotalRequiredSkills(),
                    r.getExactSkillPercentage(),
                    r.getOverallCompositeScore(),
                    r.getRecommendationTier());
        }
        rankingEngine.shutdown();
    }
}
