package com.uni.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * A news entry published by a manager. Supports pinning and comments.
 */
public class News implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long id;
    private String topic;
    private String content;
    private boolean pinned;
    private final User author;
    private final Date publishedAt;
    private final List<Comment> comments = new ArrayList<>();

    public News(long id, String topic, String content, User author) {
        this.id = id;
        this.topic = topic;
        this.content = content;
        this.author = author;
        this.publishedAt = new Date();
    }

    public long getId()              { return id; }
    public String getTopic()         { return topic; }
    public String getContent()       { return content; }
    public boolean isPinned()        { return pinned; }
    public User getAuthor()          { return author; }
    public Date getPublishedAt()     { return publishedAt; }
    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public void setTopic(String topic)     { this.topic = topic; }
    public void setContent(String content) { this.content = content; }
    public void setPinned(boolean pinned)  { this.pinned = pinned; }

    public void addComment(Comment comment) {
        if (comment != null) comments.add(comment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof News)) return false;
        return id == ((News) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return (pinned ? "[PINNED] " : "")
                + topic + " (#" + id + ", " + comments.size() + " comments)";
    }
}
