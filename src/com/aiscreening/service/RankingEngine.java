package com.aiscreening.service;

import com.aiscreening.model.CandidateResume;
import com.aiscreening.model.JobOpening;
import com.aiscreening.model.ScreeningResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Ranking Engine demonstrating:
 * - Multithreading (ExecutorService thread pool processing multiple resumes concurrently)
 * - Java Stream API (filtering, sorting, ranking, and statistical aggregation)
 */
public class RankingEngine {

    private final ScoringStrategy scoringStrategy;
    private final ExecutorService executorService;
    private final AtomicInteger totalMultithreadedTasksProcessed = new AtomicInteger(0);

    public RankingEngine(ScoringStrategy scoringStrategy) {
        this.scoringStrategy = scoringStrategy;
        // Fixed thread pool for concurrent resume screening
        this.executorService = Executors.newFixedThreadPool(
                Math.max(4, Runtime.getRuntime().availableProcessors())
        );
    }

    /**
     * Evaluates and ranks all candidates against a specific JobOpening concurrently using Java Multithreading,
     * then sorts and assigns ranks using the Java Stream API.
     */
    public List<ScreeningResult> rankCandidatesForJob(
            Collection<CandidateResume> candidates,
            JobOpening job,
            String skillFilter,
            double minExperienceFilter,
            double minScoreFilter,
            String searchKeyword) {

        if (job == null || candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        // Submit parallel evaluation tasks to the thread pool
        List<Callable<ScreeningResult>> tasks = candidates.stream()
                .map(candidate -> (Callable<ScreeningResult>) () -> {
                    totalMultithreadedTasksProcessed.incrementAndGet();
                    return scoringStrategy.evaluate(candidate, job);
                })
                .collect(Collectors.toList());

        List<ScreeningResult> rawResults = new ArrayList<>();
        try {
            List<Future<ScreeningResult>> futures = executorService.invokeAll(tasks);
            for (Future<ScreeningResult> future : futures) {
                rawResults.add(future.get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new RuntimeException("Concurrent screening failed", e);
        }

        // Use Java Stream API to filter and sort candidates from highest to lowest match
        List<ScreeningResult> sortedAndFiltered = rawResults.stream()
                .filter(r -> r.getExactSkillPercentage() >= minScoreFilter)
                .filter(r -> r.getCandidate().getExperienceYears() >= minExperienceFilter)
                .filter(r -> {
                    if (skillFilter == null || skillFilter.isBlank() || "ALL".equalsIgnoreCase(skillFilter)) {
                        return true;
                    }
                    String target = skillFilter.trim().toLowerCase(Locale.ROOT);
                    return r.getCandidate().getExtractedSkills().stream()
                            .anyMatch(s -> s.toLowerCase(Locale.ROOT).contains(target));
                })
                .filter(r -> {
                    if (searchKeyword == null || searchKeyword.isBlank()) {
                        return true;
                    }
                    String kw = searchKeyword.trim().toLowerCase(Locale.ROOT);
                    return r.getCandidate().getCandidateName().toLowerCase(Locale.ROOT).contains(kw)
                            || r.getCandidate().getEmail().toLowerCase(Locale.ROOT).contains(kw)
                            || r.getCandidate().getHighestEducation().toLowerCase(Locale.ROOT).contains(kw)
                            || r.getCandidate().getExtractedSkills().stream()
                                    .anyMatch(s -> s.toLowerCase(Locale.ROOT).contains(kw));
                })
                .sorted(Comparator
                        .comparingDouble(ScreeningResult::getExactSkillPercentage).reversed()
                        .thenComparing(Comparator.comparingDouble(ScreeningResult::getOverallCompositeScore).reversed())
                        .thenComparing(r -> r.getCandidate().getExperienceYears(), Comparator.reverseOrder()))
                .collect(Collectors.toList());

        // Assign 1-based rank positions
        for (int i = 0; i < sortedAndFiltered.size(); i++) {
            sortedAndFiltered.get(i).setRank(i + 1);
        }

        return sortedAndFiltered;
    }

    public int getTotalMultithreadedTasksProcessed() {
        return totalMultithreadedTasksProcessed.get();
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
