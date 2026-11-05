package com.aiscreening.service;

import com.aiscreening.model.CandidateResume;
import com.aiscreening.model.JobOpening;
import com.aiscreening.model.ScreeningResult;

/**
 * Interface demonstrating OOP Abstraction & Polymorphism for different AI Resume Scoring algorithms.
 */
public interface ScoringStrategy {
    ScreeningResult evaluate(CandidateResume candidate, JobOpening job);
}
