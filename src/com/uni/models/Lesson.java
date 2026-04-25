package com.uni.models;

import com.uni.enums.LessonType;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/** A single lecture or practice slot of a course. */
public class Lesson implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LessonType lessonType;
    private final String room;
    private final Date schedule;
    private final Course course;

    public Lesson(LessonType lessonType, String room, Date schedule, Course course) {
        this.lessonType = lessonType;
        this.room = room;
        this.schedule = schedule;
        this.course = course;
    }

    public LessonType getLessonType() { return lessonType; }
    public String getRoom()           { return room; }
    public Date getSchedule()         { return schedule; }
    public Course getCourse()         { return course; }

    public String getInfo() {
        return lessonType + " of "
                + (course == null ? "?" : course.getCourseCode())
                + " in room " + room
                + " at " + schedule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lesson)) return false;
        Lesson l = (Lesson) o;
        return lessonType == l.lessonType
                && Objects.equals(room, l.room)
                && Objects.equals(schedule, l.schedule)
                && Objects.equals(course, l.course);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lessonType, room, schedule, course);
    }

    @Override
    public String toString() { return getInfo(); }
}
