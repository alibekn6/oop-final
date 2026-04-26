package com.uni.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Decorator that gives any {@link User} the {@link Researcher} role
 * without modifying their class. Used by Manager.makeResearcher.
 */
public class ResearcherDecorator implements Researcher, Serializable {
    private static final long serialVersionUID = 1L;

    private final User originalUser;
    private final List<ResearchPaper> papers     = new ArrayList<>();
    private final List<ResearchProject> projects = new ArrayList<>();

    public ResearcherDecorator(User user) {
        if (user == null) throw new IllegalArgumentException("user cannot be null");
        this.originalUser = user;
    }

    public User getOriginalUser() { return originalUser; }

    @Override
    public double calculateHIndex() {
        return ResearcherSupport.hIndex(papers);
    }

    @Override
    public void printPapers(Comparator<ResearchPaper> comparator) {
        ResearcherSupport.printPapers(this, papers, comparator);
    }

    @Override
    public List<ResearchPaper> getPapers() {
        return Collections.unmodifiableList(papers);
    }

    @Override
    public void publishPaper(ResearchPaper paper) {
        if (paper != null) papers.add(paper);
    }

    @Override
    public String getResearcherName() {
        return originalUser.getFullName() + " (decorated researcher)";
    }

    public List<ResearchProject> getResearchProjects() {
        return Collections.unmodifiableList(projects);
    }

    public void addResearchProject(ResearchProject project) {
        if (project != null && !projects.contains(project)) projects.add(project);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResearcherDecorator)) return false;
        ResearcherDecorator that = (ResearcherDecorator) o;
        return Objects.equals(originalUser, that.originalUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalUser);
    }

    @Override
    public String toString() {
        return "ResearcherDecorator(" + originalUser.getFullName()
                + ", " + papers.size() + " papers)";
    }
}
