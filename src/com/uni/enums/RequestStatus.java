package com.uni.enums;

/**
 * Status of an employee {@link com.uni.models.Request}. A request starts as
 * {@code PENDING}, becomes {@code SIGNED} after the manager (dean/rector)
 * signs it, then is finalised as {@code APPROVED} or {@code REJECTED}.
 */
public enum RequestStatus {
    PENDING,
    SIGNED,
    APPROVED,
    REJECTED
}
