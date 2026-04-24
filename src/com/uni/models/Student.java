package com.uni.models;

import com.uni.enums.Language;
import com.uni.exceptions.CreditLimitException;
import com.uni.exceptions.LowHIndexException;
import com.uni.exceptions.MaxFailedReachedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bachelor student (1..4 years). Implements {@link Researcher} so that any
 * student opted into research can publish papers; isResearcher() guards
 * activation.
 */
public class Student extends User implements Researcher {
    private static final long serialVersionUID = 1L;

    public static final int MAX_CREDITS  = 21;
    public static final int MAX_FAILS    = 3;

    private int yearOfStudy;
    private String major;
    private int currentCredits;
    private int failCount;

    private final Map<Course, Mark> transcript = new LinkedHashMap<>();
    private Researcher supervisor;        // only meaningful for 4th-year students
    private boolean researcherFlag;

    private final List<ResearchPaper> papers = new ArrayList<>();
    private final List<ResearchProject> projects = new ArrayList<>();

    public Student(long id,
                   String login,
                   String password,
                   String firstName,
                   String lastName,
                   String email,
                   Language language,
                   int yearOfStudy,
                   String major) {
        super(id, login, password, firstName, lastName, email, language);
        this.yearOfStudy = yearOfStudy;
        this.major = major;
    }

    public int getYearOfStudy()         { return yearOfStudy; }
    public String getMajor()            { return major; }
    public int getCurrentCredits()      { return currentCredits; }
    public int getFailCount()           { return failCount; }
    public Map<Course, Mark> getTranscriptMap() { return Collections.unmodifiableMap(transcript); }
    public Researcher getSupervisor()   { return supervisor; }

    public void setYearOfStudy(int year) { this.yearOfStudy = year; }
    public void setMajor(String major)   { this.major = major; }

    /**
     * Begin a registration request. The Manager finalises it via
     * {@link Manager#approveRegistration(Student, Course)}. Throws if the
     * student would exceed credit caps or has 3 fails already.
     */
    public void registerForCourse(Course course)
            throws CreditLimitException, MaxFailedReachedException {
        if (failCount >= MAX_FAILS) {
            throw new MaxFailedReachedException(
                    getFullName() + " has " + failCount
                            + " failed courses and cannot register for new ones.");
        }
        if (currentCredits + course.getCredits() > MAX_CREDITS) {
            throw new CreditLimitException(
                    getFullName() + " would exceed " + MAX_CREDITS
                            + " credits (" + currentCredits + " + "
                            + course.getCredits() + ")");
        }
        if (course.getTargetYear() != 0 && course.getTargetYear() != yearOfStudy) {
            System.out.println("[warn] " + course.getCourseCode()
                    + " is for year " + course.getTargetYear()
                    + ", but " + getFullName() + " is year " + yearOfStudy);
        }
    }

    /** Quick check used by UI/listings — same logic as registerForCourse. */
    public boolean canRegisterFor(Course course) {
        return failCount < MAX_FAILS
                && currentCredits + course.getCredits() <= MAX_CREDITS;
    }

    /** Called by Manager when registration is approved. */
    void onRegistrationApproved(Course course) {
        currentCredits += course.getCredits();
        transcript.putIfAbsent(course, new Mark(0, 0, 0, course, this));
    }

    public Map<Course, Mark> viewMarks() {
        return new HashMap<>(transcript);
    }

    public List<Course> viewCourses() {
        return new ArrayList<>(transcript.keySet());
    }

    public String getTranscript() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Transcript: ").append(getFullName())
                .append(" (year ").append(yearOfStudy).append(", ").append(major).append(") ===\n");
        if (transcript.isEmpty()) {
            sb.append("(no courses)\n");
        } else {
            for (Map.Entry<Course, Mark> e : transcript.entrySet()) {
                sb.append("  ").append(e.getKey().getCourseCode())
                        .append("  ").append(e.getValue().getLetter())
                        .append("  (").append(e.getValue().getTotal()).append(")\n");
            }
        }
        sb.append("Credits: ").append(currentCredits)
                .append(", Fails: ").append(failCount);
        return sb.toString();
    }

    public void rateTeacher(Teacher teacher, double rating) {
        if (teacher != null) teacher.recordRating(rating);
    }

    public void setSupervisor(Researcher supervisor) throws LowHIndexException {
        if (yearOfStudy != 4) {
            throw new IllegalStateException(
                    "Only 4th-year students can have a supervisor.");
        }
        if (supervisor == null) {
            throw new IllegalArgumentException("supervisor cannot be null");
        }
        if (supervisor.calculateHIndex() < 3) {
            throw new LowHIndexException(
                    "Supervisor " + supervisor.getResearcherName()
                            + " has h-index < 3 and cannot supervise.");
        }
        this.supervisor = supervisor;
    }

    public void recordFail() throws MaxFailedReachedException {
        failCount++;
        if (failCount >= MAX_FAILS) {
            throw new MaxFailedReachedException(
                    getFullName() + " has reached the max-fails limit ("
                            + MAX_FAILS + ").");
        }
    }

    /* ===================== Researcher role ===================== */

    @Override
    public boolean isResearcher() { return researcherFlag; }

    public void setResearcher(boolean researcher) { this.researcherFlag = researcher; }

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
    public String getResearcherName() { return getFullName(); }

    public List<ResearchProject> getResearchProjects() {
        return Collections.unmodifiableList(projects);
    }

    public void addResearchProject(ResearchProject project) {
        if (project != null && !projects.contains(project)) projects.add(project);
    }
}
