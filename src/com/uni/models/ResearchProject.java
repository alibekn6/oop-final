package com.uni.models;

import com.uni.exceptions.NotAResearcherException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * A long-running research effort with a topic, participants, and the papers
 * that came out of it. Non-researchers cannot join.
 */
public class ResearchProject implements Serializable {
    private static final long serialVersionUID = 1L;

    private String topic;
    private final List<Researcher> participants = new ArrayList<>();
    private final List<ResearchPaper> publishedPapers = new ArrayList<>();
    private final Date startDate;

    public ResearchProject(String topic) {
        this.topic = topic;
        this.startDate = new Date();
    }

    public String getTopic()                       { return topic; }
    public Date getStartDate()                     { return startDate; }
    public List<Researcher> getParticipants()      { return Collections.unmodifiableList(participants); }
    public List<ResearchPaper> getPapers()         { return Collections.unmodifiableList(publishedPapers); }

    public void setTopic(String topic) { this.topic = topic; }

    /**
     * Add a participant to the project. Accepts any Object so we can validate
     * — anything not implementing {@link Researcher} (or with isResearcher()
     * false) triggers {@link NotAResearcherException}.
     */
    public void addParticipant(Object candidate) throws NotAResearcherException {
        if (!(candidate instanceof Researcher)
                || !((Researcher) candidate).isResearcher()) {
            String name = candidate == null
                    ? "null"
                    : (candidate instanceof User
                        ? ((User) candidate).getFullName()
                        : candidate.toString());
            throw new NotAResearcherException(
                    name + " is not a researcher and cannot join project '" + topic + "'");
        }
        participants.add((Researcher) candidate);
    }

    public void addPaper(ResearchPaper paper) {
        if (paper != null) publishedPapers.add(paper);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResearchProject)) return false;
        ResearchProject that = (ResearchProject) o;
        return Objects.equals(topic, that.topic)
                && Objects.equals(startDate, that.startDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, startDate);
    }

    @Override
    public String toString() {
        return "ResearchProject{topic='" + topic + "', participants="
                + participants.size() + ", papers=" + publishedPapers.size() + "}";
    }
}
