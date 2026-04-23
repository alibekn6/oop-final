package com.uni.exceptions;

/** Thrown when a non-researcher tries to join a ResearchProject. */
public class NotAResearcherException extends Exception {
    private static final long serialVersionUID = 1L;

    public NotAResearcherException(String message) {
        super(message);
    }
}
