package com.uni;

import com.uni.comparators.CitationsComparator;
import com.uni.comparators.DateComparator;
import com.uni.comparators.PaperLengthComparator;
import com.uni.enums.CitationFormat;
import com.uni.enums.CourseType;
import com.uni.enums.Language;
import com.uni.enums.ManagerType;
import com.uni.enums.RequestStatus;
import com.uni.enums.TeacherTitle;
import com.uni.enums.UrgencyLevel;
import com.uni.enums.UserType;
import com.uni.exceptions.CreditLimitException;
import com.uni.exceptions.LowHIndexException;
import com.uni.exceptions.MaxFailedReachedException;
import com.uni.exceptions.NotAResearcherException;
import com.uni.factory.UserFactory;
import com.uni.models.Admin;
import com.uni.models.Comment;
import com.uni.models.Course;
import com.uni.models.Manager;
import com.uni.models.Mark;
import com.uni.models.Message;
import com.uni.models.News;
import com.uni.models.Request;
import com.uni.models.ResearchPaper;
import com.uni.models.ResearchProject;
import com.uni.models.Researcher;
import com.uni.models.ResearcherEmployee;
import com.uni.models.Student;
import com.uni.models.Teacher;
import com.uni.models.User;
import com.uni.storage.DataStore;

import java.io.Console;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Interactive console UI for the university system. Loads the
 * serialized {@link DataStore} if present, otherwise seeds a small
 * demo dataset. Authentication is required; the menu shown after
 * login depends on the user's concrete subtype.
 *
 * Run: {@code java -cp out com.uni.UniversityCLI}
 */
public class UniversityCLI {

    private static final Scanner IN = new Scanner(System.in);

    public static void main(String[] args) {
        DataStore ds;
        try {
            ds = DataStore.load();
            if (ds.getUsers().isEmpty()) {
                System.out.println("(empty store — seeding demo data)");
                seed(ds);
            } else {
                System.out.println("Loaded " + ds.getUsers().size() + " users from disk.");
            }
        } catch (Exception e) {
            DataStore.resetForTesting();
            ds = DataStore.getInstance();
            seed(ds);
            System.out.println("(no save file — seeded demo data)");
        }

        banner();
        while (true) {
            System.out.println();
            System.out.println("--- Login (type 'q' to quit) ---");
            String login = prompt("Login: ");
            if (login.equalsIgnoreCase("q")) break;
            String pass = readPassword("Password: ");
            User user = ds.authenticate(login, pass);
            if (user == null) {
                System.out.println("Invalid credentials.");
                continue;
            }
            System.out.println("Welcome, " + user.getFullName() + " (" + user.getClass().getSimpleName() + ")");
            dispatch(ds, user);
        }

        System.out.println("Saving... bye.");
        ds.save();
    }

    /* ===================== Dispatch ===================== */

    private static void dispatch(DataStore ds, User user) {
        if (user instanceof Admin)              adminMenu(ds, (Admin) user);
        else if (user instanceof Manager)       managerMenu(ds, (Manager) user);
        else if (user instanceof Teacher)       teacherMenu(ds, (Teacher) user);
        else if (user instanceof Student)       studentMenu(ds, (Student) user);
        else if (user instanceof ResearcherEmployee)
                                                researcherMenu(ds, (ResearcherEmployee) user);
        else                                    System.out.println("(no menu for this user type)");
    }

    /* ===================== Admin ===================== */

    private static void adminMenu(DataStore ds, Admin admin) {
        while (true) {
            System.out.println("\n[Admin] 1) List users  2) Add student  3) Remove user  4) View logs  0) Logout");
            switch (prompt("> ")) {
                case "1":
                    for (User u : ds.getUsers()) {
                        System.out.println("  " + u.getLogin() + "  "
                                + u.getFullName() + "  ("
                                + u.getClass().getSimpleName() + ")");
                    }
                    break;
                case "2":
                    addStudentFlow(ds, admin);
                    break;
                case "3":
                    String l = prompt("Login to remove: ");
                    User target = findUser(ds, l);
                    if (target == null) System.out.println("Not found.");
                    else { admin.removeUser(target); System.out.println("Removed."); }
                    break;
                case "4":
                    ds.getLogs().forEach(a -> System.out.println("  " + a));
                    break;
                case "0":
                    return;
                default:
                    System.out.println("?");
            }
        }
    }

    private static void addStudentFlow(DataStore ds, Admin admin) {
        try {
            long id = (long) (ds.getUsers().size() + 1);
            String login = prompt("Login: ");
            String pass  = prompt("Password: ");
            String first = prompt("First name: ");
            String last  = prompt("Last name: ");
            String email = prompt("Email: ");
            int year     = Integer.parseInt(prompt("Year of study (1-4): "));
            String major = prompt("Major: ");
            Student s = (Student) UserFactory.createUser(UserType.STUDENT,
                    id, login, pass, first, last, email, Language.EN, year, major);
            admin.addUser(s);
            System.out.println("Added: " + s.getFullName());
        } catch (Exception e) {
            System.out.println("Failed: " + e.getMessage());
        }
    }

    /* ===================== Manager ===================== */

    private static void managerMenu(DataStore ds, Manager manager) {
        while (true) {
            System.out.println("\n[Manager] 1) Assign course  2) Approve registration  3) Statistical report"
                    + "  4) View students  5) Manage news  6) Requests  7) Make researcher  0) Logout");
            switch (prompt("> ")) {
                case "1":
                    Course c1 = pickCourse(ds);
                    Teacher t1 = pickTeacher(ds);
                    if (c1 != null && t1 != null) {
                        manager.assignCourse(c1, t1);
                        System.out.println("Assigned " + t1.getFullName() + " to " + c1.getCourseCode());
                    }
                    break;
                case "2":
                    Course c2 = pickCourse(ds);
                    Student s2 = pickStudent(ds);
                    if (c2 != null && s2 != null) {
                        manager.approveRegistration(s2, c2);
                        System.out.println("Approved.");
                    }
                    break;
                case "3":
                    Course c3 = pickCourse(ds);
                    if (c3 != null) System.out.println(manager.createStatisticalReport(c3));
                    break;
                case "4":
                    ds.getStudents().forEach(s -> System.out.println("  " + s.getFullName()
                            + " (year " + s.getYearOfStudy() + ", " + s.getMajor() + ")"));
                    break;
                case "5":
                    manageNewsFlow(ds, manager);
                    break;
                case "6":
                    requestsFlow(ds, manager);
                    break;
                case "7":
                    User u = pickUser(ds);
                    if (u != null) {
                        Researcher r = manager.makeResearcher(u);
                        System.out.println("Wrapped as researcher: " + r.getResearcherName());
                    }
                    break;
                case "0":
                    return;
                default:
                    System.out.println("?");
            }
        }
    }

    private static void manageNewsFlow(DataStore ds, Manager manager) {
        System.out.println("\n  News: 1) Publish  2) List  3) Pin/unpin  0) Back");
        switch (prompt("  > ")) {
            case "1":
                String topic = prompt("Topic: ");
                String body  = prompt("Content: ");
                boolean pin  = prompt("Pinned? (y/n): ").equalsIgnoreCase("y");
                News n = manager.manageNews(topic, body, pin);
                System.out.println("Published #" + n.getId());
                break;
            case "2":
                for (News news : ds.getNews()) System.out.println("  " + news);
                break;
            case "3":
                List<News> all = ds.getNews();
                for (int i = 0; i < all.size(); i++) {
                    System.out.println("  " + (i + 1) + ") " + all.get(i));
                }
                int idx = Integer.parseInt(prompt("Index: ")) - 1;
                if (idx >= 0 && idx < all.size()) {
                    News x = all.get(idx);
                    manager.pinNews(x, !x.isPinned());
                    System.out.println("Pinned=" + x.isPinned());
                }
                break;
        }
    }

    private static void requestsFlow(DataStore ds, Manager manager) {
        System.out.println("\n  Requests: 1) All  2) Pending  3) Sign  4) Approve  5) Reject  0) Back");
        switch (prompt("  > ")) {
            case "1": case "2":
                RequestStatus filter = prompt("  > ").equals("2") ? RequestStatus.PENDING : null;
                manager.viewRequests(filter).forEach(r -> System.out.println("  " + r));
                break;
            case "3": case "4": case "5":
                List<Request> all = ds.getRequests();
                for (int i = 0; i < all.size(); i++) {
                    System.out.println("  " + (i + 1) + ") " + all.get(i));
                }
                int idx = Integer.parseInt(prompt("Index: ")) - 1;
                if (idx < 0 || idx >= all.size()) { System.out.println("?"); return; }
                Request r = all.get(idx);
                try {
                    String op = prompt("  > ");
                    if (op.equals("3")) manager.signRequest(r);
                    else if (op.equals("4")) manager.approveRequest(r, prompt("Note: "));
                    else manager.rejectRequest(r, prompt("Note: "));
                    System.out.println(r);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
                break;
        }
    }

    /* ===================== Teacher ===================== */

    private static void teacherMenu(DataStore ds, Teacher teacher) {
        while (true) {
            System.out.println("\n[Teacher] 1) View courses  2) Put mark  3) Send complaint"
                    + "  4) Send message  5) Submit request  6) Research  0) Logout");
            switch (prompt("> ")) {
                case "1":
                    teacher.viewMyCourses().forEach(c -> System.out.println("  " + c.getCourseCode()
                            + "  " + c.getName()));
                    break;
                case "2":
                    putMarkFlow(ds, teacher);
                    break;
                case "3":
                    Student s = pickStudent(ds);
                    if (s != null) {
                        UrgencyLevel level = UrgencyLevel.valueOf(prompt("Level (LOW/MEDIUM/HIGH): "));
                        teacher.setComplaint(s, level, prompt("Reason: "));
                        System.out.println("Complaint filed.");
                    }
                    break;
                case "4":
                    User to = pickUser(ds);
                    if (to != null) {
                        Message m = new Message(teacher, to, prompt("Text: "));
                        teacher.sendMessage(to, m);
                        ds.addMessage(m);
                        System.out.println("Sent.");
                    }
                    break;
                case "5":
                    Request rq = teacher.submitRequest(prompt("Subject: "), prompt("Body: "));
                    System.out.println("Submitted: " + rq);
                    break;
                case "6":
                    researcherSubMenu(ds, teacher);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("?");
            }
        }
    }

    private static void putMarkFlow(DataStore ds, Teacher teacher) {
        try {
            Student s = pickStudent(ds);
            if (s == null) return;
            Course c = pickCourse(ds);
            if (c == null) return;
            int att = Integer.parseInt(prompt("Attempt (1/2/3): "));
            double sc = Double.parseDouble(prompt("Score: "));
            teacher.putMark(s, c, att, sc);
            System.out.println("Recorded.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /* ===================== Student ===================== */

    private static void studentMenu(DataStore ds, Student student) {
        while (true) {
            System.out.println("\n[Student] 1) View courses  2) Register  3) View marks"
                    + "  4) Transcript  5) Rate teacher  6) Set supervisor  7) Research  0) Logout");
            switch (prompt("> ")) {
                case "1":
                    ds.getCourses().forEach(c -> System.out.println("  " + c.getCourseCode()
                            + "  " + c.getName() + "  (year " + c.getTargetYear()
                            + ", " + c.getCredits() + "cr)"));
                    break;
                case "2":
                    Course c = pickCourse(ds);
                    if (c != null) {
                        try {
                            student.registerForCourse(c);
                            System.out.println("Submitted — awaiting Manager approval.");
                        } catch (CreditLimitException | MaxFailedReachedException ex) {
                            System.out.println("Blocked: " + ex.getMessage());
                        } catch (Exception ex) {
                            System.out.println("Blocked: " + ex.getMessage());
                        }
                    }
                    break;
                case "3":
                    student.viewMarks().forEach((k, v) ->
                            System.out.println("  " + k.getCourseCode() + " : " + v.getLetter()
                                    + " (" + v.getTotal() + ")"));
                    break;
                case "4":
                    System.out.println(student.getTranscript());
                    break;
                case "5":
                    Teacher t = pickTeacher(ds);
                    if (t != null) {
                        student.rateTeacher(t, Double.parseDouble(prompt("Rating (0-5): ")));
                        System.out.println("Rated.");
                    }
                    break;
                case "6":
                    User sup = pickUser(ds);
                    if (sup instanceof Researcher) {
                        try {
                            student.setSupervisor((Researcher) sup);
                            System.out.println("Supervisor set.");
                        } catch (LowHIndexException ex) {
                            System.out.println("Blocked: " + ex.getMessage());
                        }
                    } else {
                        System.out.println("Not a researcher.");
                    }
                    break;
                case "7":
                    researcherSubMenu(ds, student);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("?");
            }
        }
    }

    /* ===================== Researcher Employee ===================== */

    private static void researcherMenu(DataStore ds, ResearcherEmployee re) {
        researcherSubMenu(ds, re);
    }

    private static void researcherSubMenu(DataStore ds, Researcher r) {
        while (true) {
            System.out.println("\n  [Researcher] 1) Publish paper  2) Print my papers"
                    + "  3) H-index  4) All university papers  5) Top cited  0) Back");
            switch (prompt("  > ")) {
                case "1":
                    publishPaperFlow(r);
                    break;
                case "2":
                    Comparator<ResearchPaper> cmp = pickComparator();
                    r.printPapers(cmp);
                    break;
                case "3":
                    System.out.println("  h-index = " + r.calculateHIndex());
                    break;
                case "4":
                    ds.printAllUniversityPapers(pickComparator());
                    break;
                case "5":
                    ds.printTopCitedResearcher();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("?");
            }
        }
    }

    private static void publishPaperFlow(Researcher r) {
        try {
            String title  = prompt("Title: ");
            String journal = prompt("Journal: ");
            String doi    = prompt("DOI: ");
            int pages     = Integer.parseInt(prompt("Pages: "));
            int citations = Integer.parseInt(prompt("Citations: "));
            ResearchPaper p = new ResearchPaper(title, Arrays.asList(r), journal,
                    pages, citations, new Date(), doi, "");
            r.publishPaper(p);
            System.out.println("Published: " + p);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static Comparator<ResearchPaper> pickComparator() {
        System.out.println("    Sort by: 1) Date  2) Citations  3) Pages");
        switch (prompt("    > ")) {
            case "2": return new CitationsComparator();
            case "3": return new PaperLengthComparator();
            default:  return new DateComparator();
        }
    }

    /* ===================== Pickers ===================== */

    private static User pickUser(DataStore ds) {
        List<User> users = ds.getUsers();
        for (int i = 0; i < users.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + users.get(i).getLogin()
                    + "  " + users.get(i).getFullName());
        }
        int idx = parseIdx(prompt("Index (0=cancel): "), users.size());
        return idx < 0 ? null : users.get(idx);
    }

    private static Student pickStudent(DataStore ds) {
        List<Student> list = ds.getStudents();
        for (int i = 0; i < list.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + list.get(i).getFullName());
        }
        int idx = parseIdx(prompt("Index (0=cancel): "), list.size());
        return idx < 0 ? null : list.get(idx);
    }

    private static Teacher pickTeacher(DataStore ds) {
        List<Teacher> list = ds.getTeachers();
        for (int i = 0; i < list.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + list.get(i).getFullName()
                    + " (" + list.get(i).getTitle() + ")");
        }
        int idx = parseIdx(prompt("Index (0=cancel): "), list.size());
        return idx < 0 ? null : list.get(idx);
    }

    private static Course pickCourse(DataStore ds) {
        List<Course> list = ds.getCourses();
        for (int i = 0; i < list.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + list.get(i).getCourseCode()
                    + "  " + list.get(i).getName());
        }
        int idx = parseIdx(prompt("Index (0=cancel): "), list.size());
        return idx < 0 ? null : list.get(idx);
    }

    private static int parseIdx(String s, int size) {
        try {
            int i = Integer.parseInt(s.trim()) - 1;
            return (i >= 0 && i < size) ? i : -1;
        } catch (Exception e) { return -1; }
    }

    /* ===================== Helpers ===================== */

    private static String prompt(String msg) {
        System.out.print(msg);
        return IN.nextLine();
    }

    private static String readPassword(String msg) {
        Console c = System.console();
        if (c != null) {
            char[] p = c.readPassword(msg);
            return p == null ? "" : new String(p);
        }
        return prompt(msg);
    }

    private static User findUser(DataStore ds, String login) {
        for (User u : ds.getUsers()) if (u.getLogin().equals(login)) return u;
        return null;
    }

    private static void banner() {
        System.out.println();
        System.out.println("==========================================");
        System.out.println("  University Information System — CLI");
        System.out.println("==========================================");
        System.out.println("Demo credentials (login / password):");
        System.out.println("  admin     / admin    (Admin)");
        System.out.println("  manager   / m1       (Manager)");
        System.out.println("  prof.smith/ p1       (Teacher, researcher)");
        System.out.println("  alibek    / s1       (Student, year 4)");
        System.out.println("  rex       / r1       (Researcher employee)");
    }

    /* ===================== Seed ===================== */

    private static void seed(DataStore ds) {
        long uid = 1;
        Date hire = new Date();
        Admin admin = (Admin) UserFactory.createUser(UserType.ADMIN, uid++,
                "admin", "admin", "Aibar", "Sysadmin", "admin@uni.kz",
                Language.EN, 5000.0, hire);
        Manager manager = (Manager) UserFactory.createUser(UserType.MANAGER, uid++,
                "manager", "m1", "Mira", "Boss", "mira@uni.kz", Language.EN,
                ManagerType.DEPARTMENT, 4000.0, hire);
        Teacher prof = (Teacher) UserFactory.createUser(UserType.TEACHER, uid++,
                "prof.smith", "p1", "Sam", "Smith", "sam@uni.kz", Language.EN,
                TeacherTitle.PROFESSOR, 6000.0, hire);
        Teacher tutor = (Teacher) UserFactory.createUser(UserType.TEACHER, uid++,
                "tutor.kim", "t1", "Karina", "Kim", "karina@uni.kz", Language.EN,
                TeacherTitle.TUTOR, 2500.0, hire);
        ResearcherEmployee re = (ResearcherEmployee) UserFactory.createUser(
                UserType.RESEARCHER_EMPLOYEE, uid++, "rex", "r1", "Renee", "Xu",
                "renee@uni.kz", Language.EN, 4500.0, hire);
        Student s1 = (Student) UserFactory.createUser(UserType.STUDENT, uid++,
                "alibek", "s1", "Alibek", "Andanuarbek", "alibek@uni.kz",
                Language.EN, 4, "Computer Science");
        for (User u : Arrays.asList(admin, manager, prof, tutor, re, s1)) ds.addUser(u);

        ds.addCourse(new Course(1, "CS201", "Object-Oriented Programming",
                6, CourseType.MAJOR, 4));
        ds.addCourse(new Course(2, "MA101", "Calculus II",
                4, CourseType.MAJOR, 2));
    }
}
