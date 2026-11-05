package com.aiscreening.service;

/**
 * Custom checked exception demonstrating Java Exception Handling for invalid or corrupted resume files.
 */
public class ResumeParseException extends Exception {
    private static final long serialVersionUID = 1L;

    public ResumeParseException(String message) {
        super(message);
    }

    public ResumeParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
