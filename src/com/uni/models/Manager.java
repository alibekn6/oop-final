package com.uni.models;

import com.uni.enums.Language;
import com.uni.enums.ManagerType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Academic manager. Approves registrations, assigns teachers to courses,
 * generates statistical reports, and can promote any user to a Researcher
 * via {@link #makeResearcher(User)} (Decorator pattern).
 */
public class Manager extends Employee {
    private static final long serialVersionUID = 1L;

    private ManagerType type;

    public Manager(long id,
                   String login,
                   String password,
                   String firstName,
                   String lastName,
                   String email,
                   Language language,
                   double salary,
                   Date hireDate,
                   ManagerType type) {
        super(id, login, password, firstName, lastName, email, language,
                salary, hireDate);
        this.type = type;
    }

    public ManagerType getType()             { return type; }
    public void setType(ManagerType type)    { this.type = type; }

    /** Assigns a teacher as a lecture instructor of a course. */
    public void assignCourse(Course course, Teacher teacher) {
        if (course == null || teacher == null) return;
        course.addLectureInstructor(teacher);
        teacher.addCourse(course);
    }

    public void assignPracticeCourse(Course course, Teacher teacher) {
        if (course == null || teacher == null) return;
        course.addPracticeInstructor(teacher);
        teacher.addCourse(course);
    }

    /** Finalises a student's pending registration. */
    public void approveRegistration(Student student, Course course) {
        if (student == null || course == null) return;
        course.addStudent(student);
        student.onRegistrationApproved(course);
    }

    public void addCourse(Course course) {
        com.uni.storage.DataStore.getInstance().addCourse(course);
    }

    /** Returns a multi-line statistical report about a course. */
    public String createStatisticalReport(Course course) {
        if (course == null) return "(no course)";
        List<Student> students = course.getStudents();
        if (students.isEmpty()) {
            return "Report for " + course.getCourseCode() + ": no enrolled students.";
        }
        double sum = 0;
        int passed = 0, failed = 0;
        for (Student s : students) {
            Mark m = s.getTranscriptMap().get(course);
            if (m == null) continue;
            sum += m.getTotal();
            if (m.isPassed()) passed++;
            else failed++;
        }
        double avg = sum / students.size();
        return "Report for " + course.getCourseCode() + ":\n"
                + "  enrolled : " + students.size() + "\n"
                + "  average  : " + String.format("%.2f", avg) + "\n"
                + "  passed   : " + passed + "\n"
                + "  failed   : " + failed
                + " (" + String.format("%.1f", 100.0 * failed / students.size())
                + "%)";
    }

    public List<Student> viewStudents(Comparator<Student> comparator) {
        List<Student> all = com.uni.storage.DataStore.getInstance().getStudents();
        List<Student> copy = new ArrayList<>(all);
        if (comparator != null) copy.sort(comparator);
        return copy;
    }

    public List<Teacher> viewTeachers(Comparator<Teacher> comparator) {
        List<Teacher> all = com.uni.storage.DataStore.getInstance().getTeachers();
        List<Teacher> copy = new ArrayList<>(all);
        if (comparator != null) copy.sort(comparator);
        return copy;
    }

    /** Wraps any User in a {@link ResearcherDecorator} and registers them. */
    public Researcher makeResearcher(User user) {
        if (user == null) throw new IllegalArgumentException("user cannot be null");
        ResearcherDecorator decorator = new ResearcherDecorator(user);
        com.uni.storage.DataStore.getInstance().addResearcher(decorator);
        return decorator;
    }
}
