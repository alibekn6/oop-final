package com.uni.models;

import com.uni.enums.RequestStatus;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * A request submitted by an Employee that has to be signed by a Manager
 * (acting as dean / rector) before it can be approved or rejected.
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long id;
    private final Employee sender;
    private final String subject;
    private final String body;
    private final Date submittedAt;
    private RequestStatus status;
    private String resolutionNote;

    public Request(long id, Employee sender, String subject, String body) {
        this.id = id;
        this.sender = sender;
        this.subject = subject;
        this.body = body;
        this.submittedAt = new Date();
        this.status = RequestStatus.PENDING;
    }

    public long getId()                 { return id; }
    public Employee getSender()         { return sender; }
    public String getSubject()          { return subject; }
    public String getBody()             { return body; }
    public Date getSubmittedAt()        { return submittedAt; }
    public RequestStatus getStatus()    { return status; }
    public String getResolutionNote()   { return resolutionNote; }

    /** Marks the request as signed by the dean/rector. */
    public void sign() {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING requests can be signed (was " + status + ")");
        }
        this.status = RequestStatus.SIGNED;
    }

    public void approve(String note) {
        if (status != RequestStatus.SIGNED) {
            throw new IllegalStateException(
                    "Request must be SIGNED before approval (was " + status + ")");
        }
        this.status = RequestStatus.APPROVED;
        this.resolutionNote = note;
    }

    public void reject(String note) {
        if (status == RequestStatus.APPROVED || status == RequestStatus.REJECTED) {
            throw new IllegalStateException("Request already resolved");
        }
        this.status = RequestStatus.REJECTED;
        this.resolutionNote = note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request)) return false;
        return id == ((Request) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String from = sender == null ? "?" : sender.getFullName();
        return "Request#" + id + " [" + status + "] from " + from
                + ": " + subject;
    }
}
