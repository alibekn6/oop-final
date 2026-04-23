package com.uni.exceptions;

/** Thrown when a student tries to register for courses exceeding 21 credits. */
public class CreditLimitException extends Exception {
    private static final long serialVersionUID = 1L;

    public CreditLimitException(String message) {
        super(message);
    }
}
