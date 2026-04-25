package com.uni.models;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Audit-log entry: who did what, when. Stored in DataStore.logs and
 * surfaced via Admin.viewAllLogs / viewUserLogs.
 */
public class UserAction implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long id;
    private final Date timestamp;
    private final User actor;
    private final String actionDetails;

    public UserAction(long id, User actor, String actionDetails) {
        this.id = id;
        this.timestamp = new Date();
        this.actor = actor;
        this.actionDetails = actionDetails;
    }

    public long getId()              { return id; }
    public Date getTimestamp()       { return timestamp; }
    public User getActor()           { return actor; }
    public String getActionDetails() { return actionDetails; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAction)) return false;
        UserAction that = (UserAction) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String who = actor == null ? "system" : actor.getLogin();
        return "[" + timestamp + "] " + who + ": " + actionDetails;
    }
}
