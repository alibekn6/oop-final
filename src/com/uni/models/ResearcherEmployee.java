package com.uni.models;

import com.uni.enums.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * An employee whose only role is research (not Teacher, not Manager,
 * not Admin). Always a {@link Researcher}.
 */
public class ResearcherEmployee extends Employee implements Researcher {
    private static final long serialVersionUID = 1L;

    private final List<ResearchPaper> papers     = new ArrayList<>();
    private final List<ResearchProject> projects = new ArrayList<>();

    public ResearcherEmployee(long id,
                              String login,
                              String password,
                              String firstName,
                              String lastName,
                              String email,
                              Language language,
                              double salary,
                              Date hireDate) {
        super(id, login, password, firstName, lastName, email, language,
                salary, hireDate);
    }

    @Override
    public boolean isResearcher() { return true; }

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
        return getFullName() + " (researcher)";
    }

    public List<ResearchProject> getResearchProjects() {
        return Collections.unmodifiableList(projects);
    }

    public void addResearchProject(ResearchProject project) {
        if (project != null && !projects.contains(project)) projects.add(project);
    }
}
