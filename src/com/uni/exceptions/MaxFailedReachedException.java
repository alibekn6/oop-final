package com.uni.exceptions;

/** Thrown when a student fails a course for the 3rd time. */
public class MaxFailedReachedException extends Exception {
    private static final long serialVersionUID = 1L;

    public MaxFailedReachedException(String message) {
        super(message);
    }
}
