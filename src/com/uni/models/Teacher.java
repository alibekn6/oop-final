package com.uni.models;

import com.uni.enums.Language;
import com.uni.enums.TeacherTitle;
import com.uni.enums.UrgencyLevel;
import com.uni.exceptions.MaxFailedReachedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Teaching staff. Always implements {@link Researcher}; PROFESSOR is
 * automatically a researcher, other titles can opt in.
 */
public class Teacher extends Employee implements Researcher {
    private static final long serialVersionUID = 1L;

    private TeacherTitle title;
    private final List<Course> activeCourses = new ArrayList<>();
    private final List<Double> ratings       = new ArrayList<>();

    private final List<ResearchPaper> papers     = new ArrayList<>();
    private final List<ResearchProject> projects = new ArrayList<>();

    /** Used for non-professor teachers who additionally opt in to research. */
    private boolean explicitResearcher;

    public Teacher(long id,
                   String login,
                   String password,
                   String firstName,
                   String lastName,
                   String email,
                   Language language,
                   double salary,
                   Date hireDate,
                   TeacherTitle title) {
        super(id, login, password, firstName, lastName, email, language,
                salary, hireDate);
        this.title = title;
    }

    public TeacherTitle getTitle()                  { return title; }
    public List<Course> getActiveCourses()          { return Collections.unmodifiableList(activeCourses); }
    public List<Double> getRatings()                { return Collections.unmodifiableList(ratings); }
    public List<ResearchProject> getResearchProjects() { return Collections.unmodifiableList(projects); }

    public void setTitle(TeacherTitle title)        { this.title = title; }

    public void addCourse(Course c)    { if (c != null && !activeCourses.contains(c)) activeCourses.add(c); }
    public void removeCourse(Course c) { activeCourses.remove(c); }

    public List<Course> viewMyCourses() { return getActiveCourses(); }

    public List<Student> viewStudents(Course course) {
        return course == null ? Collections.emptyList() : course.getStudents();
    }

    /**
     * Set/update a mark on a student for one of three attestations.
     * @param attestationType 1 = first att, 2 = second att, 3 = final.
     */
    public void putMark(Student student, Course course,
                        int attestationType, double score)
            throws MaxFailedReachedException {
        if (!activeCourses.contains(course)) {
            throw new IllegalStateException(
                    getFullName() + " does not teach " + course.getCourseCode());
        }
        Mark mark = student.getTranscriptMap().get(course);
        if (mark == null) {
            throw new IllegalStateException(
                    student.getFullName() + " is not registered for "
                            + course.getCourseCode());
        }
        switch (attestationType) {
            case 1: mark.setFirstAttestation(score);  break;
            case 2: mark.setSecondAttestation(score); break;
            case 3: mark.setFinalExam(score);         break;
            default:
                throw new IllegalArgumentException(
                        "attestationType must be 1, 2 or 3 (got " + attestationType + ")");
        }
        if (attestationType == 3 && !mark.isPassed()) {
            student.recordFail();
        }
    }

    public void setComplaint(Student student, UrgencyLevel level, String text) {
        System.out.println("[complaint by " + getFullName()
                + " | " + level + "] re: "
                + (student == null ? "?" : student.getFullName())
                + " — " + text);
    }

    public void recordRating(double rating) {
        if (rating < 0)  rating = 0;
        if (rating > 5)  rating = 5;
        ratings.add(rating);
    }

    public double calculateRating() {
        if (ratings.isEmpty()) return 0.0;
        double sum = 0;
        for (Double r : ratings) sum += r;
        return sum / ratings.size();
    }

    /* ===================== Researcher role ===================== */

    @Override
    public boolean isResearcher() {
        return title == TeacherTitle.PROFESSOR || explicitResearcher;
    }

    public void setResearcher(boolean researcher) { this.explicitResearcher = researcher; }

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
        if (!isResearcher()) {
            throw new IllegalStateException(
                    getFullName() + " is not a researcher; cannot publish.");
        }
        if (paper != null) papers.add(paper);
    }

    @Override
    public String getResearcherName() {
        return getFullName() + (title == TeacherTitle.PROFESSOR
                ? " (Prof.)" : "");
    }

    public void addResearchProject(ResearchProject p) {
        if (p != null && !projects.contains(p)) projects.add(p);
    }
}
