package com.uni;

import com.uni.comparators.CitationsComparator;
import com.uni.comparators.DateComparator;
import com.uni.comparators.PaperLengthComparator;
import com.uni.enums.CitationFormat;
import com.uni.enums.CourseType;
import com.uni.enums.Language;
import com.uni.enums.LessonType;
import com.uni.enums.ManagerType;
import com.uni.enums.TeacherTitle;
import com.uni.enums.UrgencyLevel;
import com.uni.enums.UserType;
import com.uni.exceptions.CreditLimitException;
import com.uni.exceptions.LowHIndexException;
import com.uni.exceptions.MaxFailedReachedException;
import com.uni.exceptions.NotAResearcherException;
import com.uni.factory.UserFactory;
import com.uni.models.Admin;
import com.uni.models.Course;
import com.uni.models.Lesson;
import com.uni.models.Manager;
import com.uni.models.Mark;
import com.uni.models.Message;
import com.uni.models.ResearchPaper;
import com.uni.models.ResearchProject;
import com.uni.models.Researcher;
import com.uni.models.ResearcherDecorator;
import com.uni.models.ResearcherEmployee;
import com.uni.models.Student;
import com.uni.models.Teacher;
import com.uni.models.User;
import com.uni.models.UserAction;
import com.uni.storage.DataStore;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * End-to-end demo class. Walks through every scenario from the spec:
 * authentication, course registration (success + failure paths),
 * marking, research workflows, supervisor assignment, project joins,
 * citation formatting, and persistence.
 */
public class UniversityDemo {

    public static void main(String[] args) throws Exception {
        DataStore.resetForTesting();
        DataStore ds = DataStore.getInstance();

        section("1. SEED DATA");
        seedData(ds);
        System.out.println("Users: " + ds.getUsers().size()
                + ", Courses: " + ds.getCourses().size());

        section("2. AUTHENTICATION");
        demoAuthentication(ds);

        section("3. COURSE REGISTRATION — SUCCESS");
        demoRegistrationSuccess(ds);

        section("4. COURSE REGISTRATION — EXCEPTIONS");
        demoRegistrationFailures(ds);

        section("5. PUTTING MARKS");
        demoPuttingMarks(ds);

        section("6. RESEARCH — PUBLISH, SORT, H-INDEX, TOP CITED");
        demoResearch(ds);

        section("7. SUPERVISOR ASSIGNMENT");
        demoSupervisor(ds);

        section("8. RESEARCH PROJECT — JOIN / NOT-A-RESEARCHER");
        demoResearchProject(ds);

        section("9. CITATIONS — PLAIN_TEXT & BIBTEX");
        demoCitations(ds);

        section("10. PERSISTENCE — SAVE / LOAD");
        demoPersistence(ds);

        section("DONE");
    }

    /* ============== Sections ============== */

    private static void seedData(DataStore ds) {
        long uid = 1;
        Date hire = new Date();

        Admin admin = (Admin) UserFactory.createUser(UserType.ADMIN,
                uid++, "admin", "admin", "Aibar", "Sysadmin",
                "admin@uni.kz", Language.EN, 5000.0, hire);

        Manager manager = (Manager) UserFactory.createUser(UserType.MANAGER,
                uid++, "manager", "m1", "Mira", "Boss",
                "mira@uni.kz", Language.EN, ManagerType.DEPARTMENT, 4000.0, hire);

        Teacher prof = (Teacher) UserFactory.createUser(UserType.TEACHER,
                uid++, "prof.smith", "p1", "Sam", "Smith",
                "sam@uni.kz", Language.EN, TeacherTitle.PROFESSOR, 6000.0, hire);

        Teacher tutor = (Teacher) UserFactory.createUser(UserType.TEACHER,
                uid++, "tutor.kim", "t1", "Karina", "Kim",
                "karina@uni.kz", Language.EN, TeacherTitle.TUTOR, 2500.0, hire);

        Teacher slec = (Teacher) UserFactory.createUser(UserType.TEACHER,
                uid++, "slec.lee", "s1", "Liam", "Lee",
                "liam@uni.kz", Language.EN, TeacherTitle.SENIOR_LECTURER, 3500.0, hire);
        slec.setResearcher(true);                 // opted in but h-index will be < 3

        ResearcherEmployee re = (ResearcherEmployee) UserFactory.createUser(
                UserType.RESEARCHER_EMPLOYEE,
                uid++, "rex", "r1", "Renee", "Xu",
                "renee@uni.kz", Language.EN, 4500.0, hire);

        Student s1 = (Student) UserFactory.createUser(UserType.STUDENT,
                uid++, "alibek", "s1", "Alibek", "Andanuarbek",
                "alibek@uni.kz", Language.EN, 4, "Computer Science");
        Student s2 = (Student) UserFactory.createUser(UserType.STUDENT,
                uid++, "bota", "s2", "Bota", "Sat",
                "bota@uni.kz", Language.EN, 2, "Math");
        Student s3 = (Student) UserFactory.createUser(UserType.STUDENT,
                uid++, "doomed", "s3", "Dauren", "Failed",
                "dauren@uni.kz", Language.EN, 3, "Physics");

        for (User u : Arrays.asList(admin, manager, prof, tutor, slec, re, s1, s2, s3)) {
            ds.addUser(u);
        }

        // Courses
        Course oop = new Course(1, "CS201", "Object-Oriented Programming",
                6, CourseType.MAJOR, 4);
        Course math = new Course(2, "MA101", "Calculus II",
                4, CourseType.MAJOR, 2);
        Course phil = new Course(3, "HUM110", "Philosophy of Science",
                3, CourseType.FREE_ELECTIVE, 4);
        Course bigOne = new Course(4, "AI400", "Advanced AI",
                18, CourseType.MAJOR, 4);

        for (Course c : Arrays.asList(oop, math, phil, bigOne)) ds.addCourse(c);

        // Manager assigns instructors
        manager.assignCourse(oop, prof);
        manager.assignCourse(math, tutor);
        manager.assignCourse(phil, slec);
        manager.assignPracticeCourse(oop, tutor);

        // Lessons
        oop.addLesson(new Lesson(LessonType.LECTURE, "A101", new Date(), oop));
        oop.addLesson(new Lesson(LessonType.PRACTICE, "B202", new Date(), oop));

        ds.log(admin, "seeded users and courses");
    }

    private static void demoAuthentication(DataStore ds) {
        User aliceWhoDoesntExist = ds.authenticate("ghost", "x");
        System.out.println("ghost / x  -> " + aliceWhoDoesntExist);

        User admin = ds.authenticate("admin", "admin");
        System.out.println("admin / admin -> " + admin);

        User student = ds.authenticate("alibek", "s1");
        System.out.println("alibek / s1 -> " + student);

        if (admin instanceof Admin) {
            List<UserAction> logs = ((Admin) admin).viewAllLogs();
            System.out.println("Admin sees " + logs.size() + " log entries:");
            for (UserAction a : logs) System.out.println("  " + a);
        }
    }

    private static void demoRegistrationSuccess(DataStore ds) {
        Student s = (Student) findUser(ds, "bota");
        Manager mgr = (Manager) findUser(ds, "manager");
        Course math = findCourse(ds, "MA101");

        try {
            s.registerForCourse(math);
            mgr.approveRegistration(s, math);
            System.out.println(s.getFullName() + " registered for "
                    + math.getCourseCode());
            System.out.println(s.getTranscript());
        } catch (Exception e) {
            System.out.println("Unexpected: " + e);
        }
    }

    private static void demoRegistrationFailures(DataStore ds) {
        Student alibek = (Student) findUser(ds, "alibek");
        Manager mgr    = (Manager) findUser(ds, "manager");
        Course oop     = findCourse(ds, "CS201");
        Course phil    = findCourse(ds, "HUM110");
        Course bigOne  = findCourse(ds, "AI400");

        try {
            alibek.registerForCourse(oop);
            mgr.approveRegistration(alibek, oop);
            System.out.println("registered " + oop.getCourseCode()
                    + " (credits=" + alibek.getCurrentCredits() + ")");

            alibek.registerForCourse(phil);
            mgr.approveRegistration(alibek, phil);
            System.out.println("registered " + phil.getCourseCode()
                    + " (credits=" + alibek.getCurrentCredits() + ")");

            alibek.registerForCourse(bigOne);   // should bust the cap
            System.out.println("oops, the 18-credit course slipped through!");
        } catch (CreditLimitException e) {
            System.out.println("CreditLimitException as expected: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected: " + e);
        }

        // Now the MaxFailedReachedException path
        Student doomed = (Student) findUser(ds, "doomed");
        Course oop2 = findCourse(ds, "CS201");
        try {
            // force 3 fails on this student
            for (int i = 0; i < 3; i++) {
                try { doomed.recordFail(); } catch (MaxFailedReachedException ignored) {}
            }
            doomed.registerForCourse(oop2);
            System.out.println("oops, doomed slipped through despite 3 fails!");
        } catch (MaxFailedReachedException e) {
            System.out.println("MaxFailedReachedException as expected: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected: " + e);
        }
    }

    private static void demoPuttingMarks(DataStore ds) throws MaxFailedReachedException {
        Student bota   = (Student) findUser(ds, "bota");
        Teacher tutor  = (Teacher) findUser(ds, "tutor.kim");
        Course math    = findCourse(ds, "MA101");

        tutor.putMark(bota, math, 1, 25);
        tutor.putMark(bota, math, 2, 22);
        tutor.putMark(bota, math, 3, 30);

        Mark m = bota.getTranscriptMap().get(math);
        System.out.println("Bota " + math.getCourseCode() + ": "
                + m.getTotal() + " (" + m.getLetter() + ")"
                + " — passed=" + m.isPassed());

        // failing path: another student fails the final
        Student alibek = (Student) findUser(ds, "alibek");
        Teacher prof   = (Teacher) findUser(ds, "prof.smith");
        Course oop     = findCourse(ds, "CS201");
        prof.putMark(alibek, oop, 1, 10);
        prof.putMark(alibek, oop, 2, 8);
        try {
            prof.putMark(alibek, oop, 3, 5);
        } catch (MaxFailedReachedException e) {
            System.out.println("alibek hit the fails cap: " + e.getMessage());
        }
        Mark m2 = alibek.getTranscriptMap().get(oop);
        System.out.println("Alibek " + oop.getCourseCode() + ": "
                + m2.getTotal() + " (" + m2.getLetter() + ")"
                + " — failCount=" + alibek.getFailCount());

        // Manager's report
        Manager mgr = (Manager) findUser(ds, "manager");
        System.out.println(mgr.createStatisticalReport(math));

        // Rate the teacher
        bota.rateTeacher(tutor, 4.5);
        bota.rateTeacher(tutor, 5.0);
        System.out.println("Tutor Kim avg rating: " + tutor.calculateRating());
    }

    private static void demoResearch(DataStore ds) {
        Teacher prof   = (Teacher) findUser(ds, "prof.smith");
        Teacher slec   = (Teacher) findUser(ds, "slec.lee");
        ResearcherEmployee rex = (ResearcherEmployee) findUser(ds, "rex");

        ResearchPaper p1 = new ResearchPaper("Edge AI on commodity hardware",
                Arrays.asList((Researcher) prof), "JACM", 14, 50,
                date(2024, 3, 1), "10.1/x.1", "Faster inference at the edge.");
        ResearchPaper p2 = new ResearchPaper("Garbage collection in 2026",
                Arrays.asList((Researcher) prof, (Researcher) rex), "OOPSLA",
                22, 30, date(2026, 1, 12), "10.1/x.2", "Modern GC tradeoffs.");
        ResearchPaper p3 = new ResearchPaper("On the design of teaching tools",
                Arrays.asList((Researcher) prof), "TOCE", 8, 12,
                date(2023, 5, 22), "10.1/x.3", "Pedagogy for OOP courses.");
        ResearchPaper p4 = new ResearchPaper("Tiny LLMs for low-resource languages",
                Arrays.asList((Researcher) rex), "ACL", 10, 75,
                date(2025, 7, 1), "10.1/x.4", "Compressing LLMs.");
        ResearchPaper p5 = new ResearchPaper("On compiler optimisations",
                Arrays.asList((Researcher) slec), "PLDI", 9, 2,
                date(2025, 2, 14), "10.1/x.5", "A small compiler trick.");

        prof.publishPaper(p1);
        prof.publishPaper(p2);
        prof.publishPaper(p3);
        rex.publishPaper(p2);     // co-authored
        rex.publishPaper(p4);
        slec.publishPaper(p5);

        System.out.println("prof.smith h-index = " + prof.calculateHIndex());
        System.out.println("rex h-index        = " + rex.calculateHIndex());
        System.out.println("slec.lee h-index   = " + slec.calculateHIndex());

        prof.printPapers(new CitationsComparator());
        rex.printPapers(new DateComparator());

        ds.printAllUniversityPapers(new PaperLengthComparator());
        ds.printTopCitedResearcher();
    }

    private static void demoSupervisor(DataStore ds) {
        Student alibek = (Student) findUser(ds, "alibek");        // year 4
        Teacher prof   = (Teacher) findUser(ds, "prof.smith");    // h-index ≥ 3 expected
        Teacher slec   = (Teacher) findUser(ds, "slec.lee");      // h-index 1

        try {
            alibek.setSupervisor(prof);
            System.out.println("supervisor set: " + alibek.getSupervisor().getResearcherName());
        } catch (LowHIndexException e) {
            System.out.println("Unexpected: " + e);
        }

        try {
            alibek.setSupervisor(slec);
            System.out.println("oops, low h-index slipped through");
        } catch (LowHIndexException e) {
            System.out.println("LowHIndexException as expected: " + e.getMessage());
        }
    }

    private static void demoResearchProject(DataStore ds) {
        ResearcherEmployee rex = (ResearcherEmployee) findUser(ds, "rex");
        Manager mgr            = (Manager) findUser(ds, "manager");
        Student bota           = (Student) findUser(ds, "bota");

        ResearchProject project = new ResearchProject("Compilers & GC");
        ds.addResearchProject(project);

        try {
            project.addParticipant(rex);
            System.out.println("rex joined project '" + project.getTopic() + "'");
        } catch (NotAResearcherException e) {
            System.out.println("Unexpected: " + e);
        }

        try {
            project.addParticipant(bota);   // bota is not a researcher
            System.out.println("oops, non-researcher joined");
        } catch (NotAResearcherException e) {
            System.out.println("NotAResearcherException as expected: " + e.getMessage());
        }

        // Decorator path: Manager turns bota into a researcher
        Researcher botaR = mgr.makeResearcher(bota);
        System.out.println("decorated: " + botaR);
        try {
            project.addParticipant(botaR);
            System.out.println("decorated bota joined: " + project.getParticipants().size()
                    + " participants total");
        } catch (NotAResearcherException e) {
            System.out.println("Unexpected: " + e);
        }
    }

    private static void demoCitations(DataStore ds) {
        Teacher prof = (Teacher) findUser(ds, "prof.smith");
        ResearchPaper p = prof.getPapers().get(0);
        System.out.println("PLAIN_TEXT:\n  " + p.getCitation(CitationFormat.PLAIN_TEXT));
        System.out.println("BIBTEX:\n" + p.getCitation(CitationFormat.BIBTEX));
    }

    private static void demoPersistence(DataStore ds) {
        ds.save("data.ser");
        int beforeUsers = ds.getUsers().size();
        int beforePapers = ds.getAllPapers().size();
        System.out.println("saved (users=" + beforeUsers
                + ", papers=" + beforePapers + ")");

        // simulate a "restart"
        DataStore.resetForTesting();
        DataStore reloaded = DataStore.load("data.ser");
        System.out.println("loaded (users=" + reloaded.getUsers().size()
                + ", papers=" + reloaded.getAllPapers().size() + ")");

        // confirm a complex object survived
        User alibek = findUser(reloaded, "alibek");
        if (alibek instanceof Student) {
            Student s = (Student) alibek;
            System.out.println("Restored student transcript:");
            System.out.println(s.getTranscript());
        }
    }

    /* ============== Helpers ============== */

    private static void section(String title) {
        System.out.println();
        System.out.println("==================== " + title + " ====================");
    }

    private static User findUser(DataStore ds, String login) {
        for (User u : ds.getUsers()) if (u.getLogin().equals(login)) return u;
        throw new IllegalStateException("user not found: " + login);
    }

    private static Course findCourse(DataStore ds, String code) {
        for (Course c : ds.getCourses()) if (c.getCourseCode().equals(code)) return c;
        throw new IllegalStateException("course not found: " + code);
    }

    private static Date date(int year, int month1Based, int day) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month1Based - 1, day);
        return cal.getTime();
    }
}
