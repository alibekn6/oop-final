package com.uni.factory;

import com.uni.enums.Language;
import com.uni.enums.ManagerType;
import com.uni.enums.TeacherTitle;
import com.uni.enums.UserType;
import com.uni.models.Admin;
import com.uni.models.Manager;
import com.uni.models.ResearcherEmployee;
import com.uni.models.Student;
import com.uni.models.Teacher;
import com.uni.models.User;

import java.util.Date;

/**
 * Factory pattern. Creates {@link User} subclasses from a {@link UserType}
 * tag plus a small set of common parameters. Type-specific extras (title,
 * yearOfStudy, …) come in via the {@code extra} array.
 */
public final class UserFactory {

    private UserFactory() {}

    /**
     * @param type      which subclass to instantiate
     * @param id        unique id
     * @param login     login string
     * @param password  password string
     * @param firstName first name
     * @param lastName  last name
     * @param email     email
     * @param language  preferred language
     * @param extra     type-specific fields:
     *                  STUDENT             → [Integer year, String major]
     *                  TEACHER             → [TeacherTitle title, Double salary, Date hire]
     *                  MANAGER             → [ManagerType type, Double salary, Date hire]
     *                  ADMIN               → [Double salary, Date hire]
     *                  RESEARCHER_EMPLOYEE → [Double salary, Date hire]
     */
    public static User createUser(UserType type,
                                  long id,
                                  String login,
                                  String password,
                                  String firstName,
                                  String lastName,
                                  String email,
                                  Language language,
                                  Object... extra) {
        if (type == null) throw new IllegalArgumentException("type cannot be null");
        switch (type) {
            case STUDENT: {
                int year = extra.length > 0 ? (Integer) extra[0] : 1;
                String major = extra.length > 1 ? (String) extra[1] : "Undeclared";
                return new Student(id, login, password, firstName, lastName,
                        email, language, year, major);
            }
            case TEACHER: {
                TeacherTitle title = extra.length > 0
                        ? (TeacherTitle) extra[0] : TeacherTitle.TUTOR;
                double salary = extra.length > 1 ? (Double) extra[1] : 0.0;
                Date hire = extra.length > 2 ? (Date) extra[2] : new Date();
                return new Teacher(id, login, password, firstName, lastName,
                        email, language, salary, hire, title);
            }
            case MANAGER: {
                ManagerType mt = extra.length > 0
                        ? (ManagerType) extra[0] : ManagerType.DEPARTMENT;
                double salary = extra.length > 1 ? (Double) extra[1] : 0.0;
                Date hire = extra.length > 2 ? (Date) extra[2] : new Date();
                return new Manager(id, login, password, firstName, lastName,
                        email, language, salary, hire, mt);
            }
            case ADMIN: {
                double salary = extra.length > 0 ? (Double) extra[0] : 0.0;
                Date hire = extra.length > 1 ? (Date) extra[1] : new Date();
                return new Admin(id, login, password, firstName, lastName,
                        email, language, salary, hire);
            }
            case RESEARCHER_EMPLOYEE: {
                double salary = extra.length > 0 ? (Double) extra[0] : 0.0;
                Date hire = extra.length > 1 ? (Date) extra[1] : new Date();
                return new ResearcherEmployee(id, login, password, firstName,
                        lastName, email, language, salary, hire);
            }
            default:
                throw new IllegalArgumentException("Unknown user type: " + type);
        }
    }
}
