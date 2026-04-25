package com.uni.models;

import com.uni.enums.CourseType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A university course. Multiple lecture and practice instructors are
 * supported per spec. Comparable by course code (used for alphabetic
 * listings).
 */
public class Course implements Serializable, Comparable<Course> {
    private static final long serialVersionUID = 1L;

    private long id;
    private String courseCode;
    private String name;
    private int credits;
    private CourseType type;
    private int targetYear;

    private final List<Teacher> lectureInstructors  = new ArrayList<>();
    private final List<Teacher> practiceInstructors = new ArrayList<>();
    private final List<Student> students            = new ArrayList<>();
    private final List<Lesson>  lessons             = new ArrayList<>();

    public Course(long id,
                  String courseCode,
                  String name,
                  int credits,
                  CourseType type,
                  int targetYear) {
        this.id = id;
        this.courseCode = courseCode;
        this.name = name;
        this.credits = credits;
        this.type = type;
        this.targetYear = targetYear;
    }

    public long getId()                 { return id; }
    public String getCourseCode()       { return courseCode; }
    public String getName()             { return name; }
    public int getCredits()             { return credits; }
    public CourseType getType()         { return type; }
    public int getTargetYear()          { return targetYear; }

    public List<Teacher> getLectureInstructors()  { return Collections.unmodifiableList(lectureInstructors); }
    public List<Teacher> getPracticeInstructors() { return Collections.unmodifiableList(practiceInstructors); }
    public List<Student> getStudents()            { return Collections.unmodifiableList(students); }
    public List<Lesson>  getLessons()             { return Collections.unmodifiableList(lessons); }

    public void addLectureInstructor(Teacher t) {
        if (t != null && !lectureInstructors.contains(t)) lectureInstructors.add(t);
    }
    public void removeLectureInstructor(Teacher t) { lectureInstructors.remove(t); }

    public void addPracticeInstructor(Teacher t) {
        if (t != null && !practiceInstructors.contains(t)) practiceInstructors.add(t);
    }
    public void removePracticeInstructor(Teacher t) { practiceInstructors.remove(t); }

    public void addStudent(Student s) {
        if (s != null && !students.contains(s)) students.add(s);
    }
    public void removeStudent(Student s) { students.remove(s); }

    public void addLesson(Lesson lesson) {
        if (lesson != null) lessons.add(lesson);
    }

    /** Average final-total mark across all enrolled students. */
    public double getAverageMark() {
        if (students.isEmpty()) return 0.0;
        double sum = 0;
        int counted = 0;
        for (Student s : students) {
            Mark m = s.getTranscriptMap().get(this);
            if (m != null) {
                sum += m.getTotal();
                counted++;
            }
        }
        return counted == 0 ? 0.0 : sum / counted;
    }

    @Override
    public int compareTo(Course other) {
        if (other == null) return -1;
        return this.courseCode.compareTo(other.courseCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        Course c = (Course) o;
        return id == c.id || Objects.equals(courseCode, c.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode);
    }

    @Override
    public String toString() {
        return courseCode + " - " + name + " (" + credits + " cr, "
                + type + ", year " + targetYear + ")";
    }
}
