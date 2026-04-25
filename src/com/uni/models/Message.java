package com.uni.models;

import com.uni.enums.UrgencyLevel;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * A message sent from one user to another. The optional urgency field is
 * used when the message represents a complaint (Teacher → dean).
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private final User sender;
    private final User receiver;
    private final String text;
    private final Date date;
    private final UrgencyLevel urgency;

    public Message(User sender, User receiver, String text) {
        this(sender, receiver, text, null);
    }

    public Message(User sender, User receiver, String text, UrgencyLevel urgency) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.date = new Date();
        this.urgency = urgency;
    }

    public User getSender()           { return sender; }
    public User getReceiver()         { return receiver; }
    public String getText()           { return text; }
    public Date getDate()             { return date; }
    public UrgencyLevel getUrgency()  { return urgency; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message m = (Message) o;
        return Objects.equals(sender, m.sender)
                && Objects.equals(receiver, m.receiver)
                && Objects.equals(text, m.text)
                && Objects.equals(date, m.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, receiver, text, date);
    }

    @Override
    public String toString() {
        String from = sender == null ? "?" : sender.getFullName();
        String to   = receiver == null ? "?" : receiver.getFullName();
        return "Message{" + from + " -> " + to
                + (urgency != null ? " [" + urgency + "]" : "")
                + ": " + text + '}';
    }
}
