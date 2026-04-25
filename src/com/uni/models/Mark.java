package com.uni.models;

import java.io.Serializable;
import java.util.Objects;

/**
 * Course mark made of three components — 1st attestation (0..30),
 * 2nd attestation (0..30), final exam (0..40). Total 0..100.
 */
public class Mark implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final double MAX_FIRST  = 30.0;
    public static final double MAX_SECOND = 30.0;
    public static final double MAX_FINAL  = 40.0;
    public static final double PASS_THRESHOLD = 50.0;

    private double firstAttestation;
    private double secondAttestation;
    private double finalExam;
    private final Course course;
    private final Student student;

    public Mark(double first, double second, double finalExam,
                Course course, Student student) {
        this.firstAttestation  = clamp(first,  MAX_FIRST);
        this.secondAttestation = clamp(second, MAX_SECOND);
        this.finalExam         = clamp(finalExam, MAX_FINAL);
        this.course = course;
        this.student = student;
    }

    private static double clamp(double v, double max) {
        if (v < 0)   return 0;
        if (v > max) return max;
        return v;
    }

    public double getFirstAttestation()  { return firstAttestation; }
    public double getSecondAttestation() { return secondAttestation; }
    public double getFinalExam()         { return finalExam; }
    public Course getCourse()            { return course; }
    public Student getStudent()          { return student; }

    public void setFirstAttestation(double score)  { this.firstAttestation  = clamp(score, MAX_FIRST); }
    public void setSecondAttestation(double score) { this.secondAttestation = clamp(score, MAX_SECOND); }
    public void setFinalExam(double score)         { this.finalExam         = clamp(score, MAX_FINAL); }

    public double getTotal() {
        return firstAttestation + secondAttestation + finalExam;
    }

    public String getLetter() {
        double total = getTotal();
        if (total >= 90) return "A";
        if (total >= 75) return "B";
        if (total >= 60) return "C";
        if (total >= 50) return "D";
        return "F";
    }

    public boolean isPassed() {
        return getTotal() >= PASS_THRESHOLD;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mark)) return false;
        Mark m = (Mark) o;
        return Objects.equals(course, m.course)
                && Objects.equals(student, m.student);
    }

    @Override
    public int hashCode() {
        return Objects.hash(course, student);
    }

    @Override
    public String toString() {
        return "Mark{" + (course == null ? "?" : course.getCourseCode())
                + ": " + firstAttestation + " + " + secondAttestation
                + " + " + finalExam + " = " + getTotal()
                + " (" + getLetter() + ")}";
    }
}
