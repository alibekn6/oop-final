package com.uni.models;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * A comment left by a user under a news entry.
 */
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;

    private final User author;
    private final String text;
    private final Date date;

    public Comment(User author, String text) {
        this.author = author;
        this.text = text;
        this.date = new Date();
    }

    public User getAuthor() { return author; }
    public String getText() { return text; }
    public Date getDate()   { return date; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        Comment c = (Comment) o;
        return Objects.equals(author, c.author)
                && Objects.equals(text, c.text)
                && Objects.equals(date, c.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, text, date);
    }

    @Override
    public String toString() {
        String who = author == null ? "?" : author.getFullName();
        return who + ": " + text;
    }
}
