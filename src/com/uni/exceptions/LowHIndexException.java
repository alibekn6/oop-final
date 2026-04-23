package com.uni.exceptions;

/** Thrown when a researcher with h-index &lt; 3 is assigned as a supervisor. */
public class LowHIndexException extends Exception {
    private static final long serialVersionUID = 1L;

    public LowHIndexException(String message) {
        super(message);
    }
}
