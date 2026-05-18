package com.uni;

import com.uni.comparators.CitationsComparator;
import com.uni.comparators.DateComparator;
import com.uni.comparators.PaperLengthComparator;
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
import com.uni.factory.UserFactory;
import com.uni.models.Admin;
import com.uni.models.Course;
import com.uni.models.Manager;
import com.uni.models.Message;
import com.uni.models.News;
import com.uni.models.Request;
import com.uni.models.ResearchPaper;
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

    /* ANSI colour codes — gracefully degrade on terminals that ignore them. */
    private static final String RESET   = "[0m";
    private static final String BOLD    = "[1m";
    private static final String DIM     = "[2m";
    private static final String CYAN    = "[36m";
    private static final String GREEN   = "[32m";
    private static final String YELLOW  = "[33m";
    private static final String RED     = "[31m";
    private static final String MAGENTA = "[35m";

    /* ===================== Main ===================== */

    public static void main(String[] args) {
        DataStore ds = bootstrap();

        while (true) {
            User user = loginScreen(ds);
            if (user == null) break;          // user typed 'q'
            dispatch(ds, user);
        }

        clear();
        info("Saving state to data.ser ...");
        ds.save();
        ok("Bye!");
    }

    private static DataStore bootstrap() {
        DataStore ds;
        try {
            ds = DataStore.load();
            if (ds.getUsers().isEmpty()) {
                seed(ds);
                return ds;
            }
            return ds;
        } catch (Exception e) {
            DataStore.resetForTesting();
            ds = DataStore.getInstance();
            seed(ds);
            return ds;
        }
    }

    /* ===================== Login Screen ===================== */

    private static User loginScreen(DataStore ds) {
        while (true) {
            clear();
            banner();
            line();
            System.out.println("  " + BOLD + "Sign in" + RESET
                    + DIM + " (type 'q' to quit)" + RESET);
            line();
            String login = prompt("  Login    : ");
            if (login.equalsIgnoreCase("q")) return null;
            String pass  = readPassword("  Password : ");
            User u = ds.authenticate(login, pass);
            if (u == null) {
                err("  Invalid credentials.");
                pause();
                continue;
            }
            return u;
        }
    }

    /* ===================== Dispatch ===================== */

    private static void dispatch(DataStore ds, User user) {
        if (user instanceof Admin)                          adminMenu(ds, (Admin) user);
        else if (user instanceof Manager)                   managerMenu(ds, (Manager) user);
        else if (user instanceof Teacher)                   teacherMenu(ds, (Teacher) user);
        else if (user instanceof Student)                   studentMenu(ds, (Student) user);
        else if (user instanceof ResearcherEmployee)        researcherEmployeeMenu(ds, (ResearcherEmployee) user);
        else {
            err("No menu available for this user type.");
            pause();
        }
    }

    /* ===================== Admin ===================== */

    private static void adminMenu(DataStore ds, Admin admin) {
        while (true) {
            screen(admin, new String[] {
                    "1  List users",
                    "2  Add student",
                    "3  Remove user",
                    "4  View logs",
                    "0  Logout",
            });
            switch (prompt("  > ")) {
                case "1": showUsers(ds);                          pause(); break;
                case "2": addStudentFlow(ds, admin);              pause(); break;
                case "3": removeUserFlow(ds, admin);              pause(); break;
                case "4": showLogs(ds);                           pause(); break;
                case "0": return;
                default : err("Unknown option."); pause();
            }
        }
    }

    private static void showUsers(DataStore ds) {
        section("Users (" + ds.getUsers().size() + ")");
        for (User u : ds.getUsers()) {
            System.out.printf("  %-12s %-25s %s%n",
                    u.getLogin(), u.getFullName(),
                    DIM + u.getClass().getSimpleName() + RESET);
        }
    }

    private static void addStudentFlow(DataStore ds, Admin admin) {
        section("Add new student");
        try {
            long id = (long) (ds.getUsers().size() + 1);
            String login = prompt("  Login         : ");
            String pass  = prompt("  Password      : ");
            String first = prompt("  First name    : ");
            String last  = prompt("  Last name     : ");
            String email = prompt("  Email         : ");
            int year     = Integer.parseInt(prompt("  Year (1-4)    : "));
            String major = prompt("  Major         : ");
            Student s = (Student) UserFactory.createUser(UserType.STUDENT,
                    id, login, pass, first, last, email, Language.EN, year, major);
            admin.addUser(s);
            ok("Added: " + s.getFullName());
        } catch (Exception e) {
            err("Failed: " + e.getMessage());
        }
    }

    private static void removeUserFlow(DataStore ds, Admin admin) {
        section("Remove user");
        User target = pickUser(ds);
        if (target == null) { info("Cancelled."); return; }
        admin.removeUser(target);
        ok("Removed: " + target.getFullName());
    }

    private static void showLogs(DataStore ds) {
        section("Action logs (" + ds.getLogs().size() + ")");
        ds.getLogs().forEach(a -> System.out.println("  " + a));
    }

    /* ===================== Manager ===================== */

    private static void managerMenu(DataStore ds, Manager manager) {
        while (true) {
            screen(manager, new String[] {
                    "1  Assign course to teacher",
                    "2  Approve student registration",
                    "3  Statistical report",
                    "4  View students",
                    "5  Manage news",
                    "6  Requests (sign / approve / reject)",
                    "7  Make researcher (decorator)",
                    "8  Enable research role on student/teacher",
                    "0  Logout",
            });
            switch (prompt("  > ")) {
                case "1": assignCourseFlow(ds, manager);          pause(); break;
                case "2": approveRegFlow(ds, manager);            pause(); break;
                case "3": statReportFlow(ds, manager);            pause(); break;
                case "4": viewStudentsFlow(ds);                   pause(); break;
                case "5": manageNewsFlow(ds, manager);            pause(); break;
                case "6": requestsFlow(ds, manager);              pause(); break;
                case "7": makeResearcherFlow(ds, manager);        pause(); break;
                case "8": enableResearchFlow(ds);                 pause(); break;
                case "0": return;
                default : err("Unknown option."); pause();
            }
        }
    }

    private static void assignCourseFlow(DataStore ds, Manager manager) {
        section("Assign course to teacher");
        Course c = pickCourse(ds);     if (c == null) { info("Cancelled."); return; }
        Teacher t = pickTeacher(ds);   if (t == null) { info("Cancelled."); return; }
        manager.assignCourse(c, t);
        ok("Assigned " + t.getFullName() + " to " + c.getCourseCode());
    }

    private static void approveRegFlow(DataStore ds, Manager manager) {
        section("Approve student registration");
        Student s = pickStudent(ds);   if (s == null) { info("Cancelled."); return; }
        Course c = pickCourse(ds);     if (c == null) { info("Cancelled."); return; }
        manager.approveRegistration(s, c);
        ok("Approved.");
    }

    private static void statReportFlow(DataStore ds, Manager manager) {
        section("Statistical report");
        Course c = pickCourse(ds); if (c == null) { info("Cancelled."); return; }
        System.out.println();
        System.out.println(manager.createStatisticalReport(c));
    }

    private static void viewStudentsFlow(DataStore ds) {
        section("Students (" + ds.getStudents().size() + ")");
        ds.getStudents().forEach(s -> System.out.printf(
                "  %-25s  year %d  %s%n",
                s.getFullName(), s.getYearOfStudy(), s.getMajor()));
    }

    private static void manageNewsFlow(DataStore ds, Manager manager) {
        section("Manage news");
        System.out.println("  1  Publish");
        System.out.println("  2  List all");
        System.out.println("  3  Toggle pin");
        System.out.println("  0  Back");
        switch (prompt("  > ")) {
            case "1":
                String topic = prompt("  Topic     : ");
                String body  = prompt("  Content   : ");
                boolean pin  = prompt("  Pinned?(y/n): ").equalsIgnoreCase("y");
                News n = manager.manageNews(topic, body, pin);
                ok("Published #" + n.getId());
                break;
            case "2":
                if (ds.getNews().isEmpty()) info("No news yet.");
                else ds.getNews().forEach(x -> System.out.println("  " + x));
                break;
            case "3":
                List<News> all = ds.getNews();
                if (all.isEmpty()) { info("No news to pin."); break; }
                for (int i = 0; i < all.size(); i++) {
                    System.out.println("  " + (i + 1) + ") " + all.get(i));
                }
                int idx = parseIdx(prompt("  Index     : "), all.size());
                if (idx < 0) { info("Cancelled."); break; }
                News x = all.get(idx);
                manager.pinNews(x, !x.isPinned());
                ok("Pinned = " + x.isPinned());
                break;
            default:
                break;
        }
    }

    private static void requestsFlow(DataStore ds, Manager manager) {
        section("Employee requests");
        System.out.println("  1  Show all");
        System.out.println("  2  Show pending only");
        System.out.println("  3  Sign a request");
        System.out.println("  4  Approve a request");
        System.out.println("  5  Reject a request");
        System.out.println("  0  Back");
        String op = prompt("  > ");
        switch (op) {
            case "1":
            case "2":
                RequestStatus filter = op.equals("2") ? RequestStatus.PENDING : null;
                List<Request> shown = manager.viewRequests(filter);
                System.out.println();
                if (shown.isEmpty()) info("No requests.");
                else shown.forEach(r -> System.out.println("  " + r));
                break;
            case "3":
            case "4":
            case "5":
                List<Request> all = ds.getRequests();
                if (all.isEmpty()) { info("No requests."); break; }
                for (int i = 0; i < all.size(); i++) {
                    System.out.println("  " + (i + 1) + ") " + all.get(i));
                }
                int idx = parseIdx(prompt("  Index     : "), all.size());
                if (idx < 0) { info("Cancelled."); break; }
                Request r = all.get(idx);
                try {
                    if (op.equals("3"))      manager.signRequest(r);
                    else if (op.equals("4")) manager.approveRequest(r, prompt("  Note      : "));
                    else                     manager.rejectRequest(r, prompt("  Note      : "));
                    ok(r.toString());
                } catch (Exception e) {
                    err(e.getMessage());
                }
                break;
            default:
                break;
        }
    }

    private static void makeResearcherFlow(DataStore ds, Manager manager) {
        section("Make any user a researcher (Decorator pattern)");
        User u = pickUser(ds); if (u == null) { info("Cancelled."); return; }
        Researcher r = manager.makeResearcher(u);
        ok("Wrapped as researcher: " + r.getResearcherName());
    }

    private static void enableResearchFlow(DataStore ds) {
        section("Enable research role on student/teacher");
        User u = pickUser(ds); if (u == null) { info("Cancelled."); return; }
        if (u instanceof Student) {
            ((Student) u).setResearcher(true);
            ok("Student " + u.getFullName() + " is now a researcher.");
        } else if (u instanceof Teacher) {
            ((Teacher) u).setResearcher(true);
            ok("Teacher " + u.getFullName() + " is now a researcher.");
        } else {
            err("Only Student or Teacher can be opted into research via this option.");
        }
    }

    /* ===================== Teacher ===================== */

    private static void teacherMenu(DataStore ds, Teacher teacher) {
        while (true) {
            screen(teacher, new String[] {
                    "1  View my courses",
                    "2  Put mark",
                    "3  Send complaint",
                    "4  Send message",
                    "5  View inbox",
                    "6  Submit request to dean",
                    "7  Research",
                    "0  Logout",
            });
            switch (prompt("  > ")) {
                case "1": viewMyCourses(teacher);                 pause(); break;
                case "2": putMarkFlow(ds, teacher);               pause(); break;
                case "3": complaintFlow(ds, teacher);             pause(); break;
                case "4": sendMessageFlow(ds, teacher);           pause(); break;
                case "5": viewInbox(teacher);                     pause(); break;
                case "6": submitRequestFlow(teacher);             pause(); break;
                case "7": researcherSubMenu(ds, teacher);         break;
                case "0": return;
                default : err("Unknown option."); pause();
            }
        }
    }

    private static void viewMyCourses(Teacher teacher) {
        section("My courses (" + teacher.viewMyCourses().size() + ")");
        teacher.viewMyCourses().forEach(c -> System.out.printf("  %-8s  %s%n",
                c.getCourseCode(), c.getName()));
    }

    private static void putMarkFlow(DataStore ds, Teacher teacher) {
        section("Put mark");
        try {
            Student s = pickStudent(ds); if (s == null) { info("Cancelled."); return; }
            Course c  = pickCourse(ds);  if (c == null) { info("Cancelled."); return; }
            int att   = Integer.parseInt(prompt("  Attempt (1/2/3) : "));
            double sc = Double.parseDouble(prompt("  Score           : "));
            teacher.putMark(s, c, att, sc);
            ok("Recorded.");
        } catch (Exception e) {
            err(e.getMessage());
        }
    }

    private static void complaintFlow(DataStore ds, Teacher teacher) {
        section("Send complaint about student");
        Student s = pickStudent(ds); if (s == null) { info("Cancelled."); return; }
        try {
            UrgencyLevel level = UrgencyLevel.valueOf(
                    prompt("  Level (LOW/MEDIUM/HIGH) : ").toUpperCase());
            teacher.setComplaint(s, level, prompt("  Reason : "));
            ok("Complaint filed.");
        } catch (Exception e) {
            err("Invalid level.");
        }
    }

    private static void sendMessageFlow(DataStore ds, Teacher teacher) {
        section("Send message");
        User to = pickUser(ds); if (to == null) { info("Cancelled."); return; }
        Message m = new Message(teacher, to, prompt("  Text : "));
        teacher.sendMessage(to, m);
        ds.addMessage(m);
        ok("Sent.");
    }

    private static void viewInbox(Teacher teacher) {
        section("Inbox (" + teacher.getMessages().size() + ")");
        if (teacher.getMessages().isEmpty()) {
            info("No messages.");
            return;
        }
        teacher.getMessages().forEach(m -> System.out.println("  " + m));
    }

    private static void submitRequestFlow(Teacher teacher) {
        section("Submit request to dean / rector");
        Request rq = teacher.submitRequest(
                prompt("  Subject : "),
                prompt("  Body    : "));
        ok("Submitted: " + rq);
    }

    /* ===================== Student ===================== */

    private static void studentMenu(DataStore ds, Student student) {
        while (true) {
            screen(student, new String[] {
                    "1  View courses",
                    "2  Register for a course",
                    "3  View marks",
                    "4  Full transcript",
                    "5  Rate teacher",
                    "6  Set supervisor (4th year only)",
                    "7  Research",
                    "0  Logout",
            });
            switch (prompt("  > ")) {
                case "1": viewCourses(ds);                        pause(); break;
                case "2": registerFlow(ds, student);              pause(); break;
                case "3": viewMarks(student);                     pause(); break;
                case "4": transcript(student);                    pause(); break;
                case "5": rateTeacherFlow(ds, student);           pause(); break;
                case "6": setSupervisorFlow(ds, student);         pause(); break;
                case "7": researcherSubMenu(ds, student);         break;
                case "0": return;
                default : err("Unknown option."); pause();
            }
        }
    }

    private static void viewCourses(DataStore ds) {
        section("Courses (" + ds.getCourses().size() + ")");
        ds.getCourses().forEach(c -> System.out.printf(
                "  %-8s  %-30s  year %d, %d credits%n",
                c.getCourseCode(), c.getName(),
                c.getTargetYear(), c.getCredits()));
    }

    private static void registerFlow(DataStore ds, Student student) {
        section("Register for a course");
        Course c = pickCourse(ds); if (c == null) { info("Cancelled."); return; }
        try {
            student.registerForCourse(c);
            ok("Registration request submitted — awaiting Manager approval.");
        } catch (CreditLimitException | MaxFailedReachedException ex) {
            err(ex.getMessage());
        } catch (Exception ex) {
            err(ex.getMessage());
        }
    }

    private static void viewMarks(Student student) {
        section("My marks");
        if (student.viewMarks().isEmpty()) {
            info("No marks yet.");
            return;
        }
        student.viewMarks().forEach((k, v) -> System.out.printf(
                "  %-8s  %s  (%.1f)%n",
                k.getCourseCode(), v.getLetter(), v.getTotal()));
    }

    private static void transcript(Student student) {
        section("Transcript");
        System.out.println(student.getTranscript());
    }

    private static void rateTeacherFlow(DataStore ds, Student student) {
        section("Rate teacher");
        Teacher t = pickTeacher(ds); if (t == null) { info("Cancelled."); return; }
        try {
            double rating = Double.parseDouble(prompt("  Rating (0-5) : "));
            student.rateTeacher(t, rating);
            ok("Rated.");
        } catch (Exception e) {
            err("Invalid rating.");
        }
    }

    private static void setSupervisorFlow(DataStore ds, Student student) {
        section("Set research supervisor");
        info("Supervisor must be a Researcher with h-index >= 3.");
        User sup = pickUser(ds); if (sup == null) { info("Cancelled."); return; }
        if (!(sup instanceof Researcher)) {
            err("That user is not a researcher.");
            return;
        }
        try {
            student.setSupervisor((Researcher) sup);
            ok("Supervisor set: " + sup.getFullName());
        } catch (LowHIndexException ex) {
            err(ex.getMessage());
        } catch (Exception ex) {
            err(ex.getMessage());
        }
    }

    /* ===================== Researcher Employee ===================== */

    private static void researcherEmployeeMenu(DataStore ds, ResearcherEmployee re) {
        researcherSubMenu(ds, re);
    }

    private static void researcherSubMenu(DataStore ds, Researcher r) {
        while (true) {
            screen("Research · " + r.getResearcherName(), new String[] {
                    "1  Publish paper",
                    "2  Print my papers (sortable)",
                    "3  Show my h-index",
                    "4  All university papers (sortable)",
                    "5  Top cited researcher (university-wide)",
                    "0  Back",
            });
            switch (prompt("  > ")) {
                case "1": publishPaperFlow(r);                    pause(); break;
                case "2":
                    section("My papers");
                    r.printPapers(pickComparator());
                    pause();
                    break;
                case "3":
                    section("H-index");
                    System.out.println("  " + BOLD + r.calculateHIndex() + RESET);
                    pause();
                    break;
                case "4":
                    section("All university papers");
                    ds.printAllUniversityPapers(pickComparator());
                    pause();
                    break;
                case "5":
                    section("Top cited researcher");
                    ds.printTopCitedResearcher();
                    pause();
                    break;
                case "0": return;
                default : err("Unknown option."); pause();
            }
        }
    }

    private static void publishPaperFlow(Researcher r) {
        section("Publish a research paper");
        try {
            String title  = prompt("  Title      : ");
            String journ  = prompt("  Journal    : ");
            String doi    = prompt("  DOI        : ");
            int pages     = Integer.parseInt(prompt("  Pages      : "));
            int citations = Integer.parseInt(prompt("  Citations  : "));
            ResearchPaper p = new ResearchPaper(title, Arrays.asList(r), journ,
                    pages, citations, new Date(), doi, "");
            r.publishPaper(p);
            ok("Published: " + p);
        } catch (Exception e) {
            err(e.getMessage());
        }
    }

    private static Comparator<ResearchPaper> pickComparator() {
        System.out.println();
        System.out.println("  Sort by:");
        System.out.println("    1  Date (newest first)");
        System.out.println("    2  Citations (most first)");
        System.out.println("    3  Pages (longest first)");
        switch (prompt("  > ")) {
            case "2": return new CitationsComparator();
            case "3": return new PaperLengthComparator();
            default:  return new DateComparator();
        }
    }

    /* ===================== Pickers ===================== */

    private static User pickUser(DataStore ds) {
        System.out.println();
        List<User> list = ds.getUsers();
        for (int i = 0; i < list.size(); i++) {
            System.out.printf("    %2d) %-12s %-25s %s%n",
                    i + 1, list.get(i).getLogin(),
                    list.get(i).getFullName(),
                    DIM + list.get(i).getClass().getSimpleName() + RESET);
        }
        return pickFrom(list);
    }

    private static Student pickStudent(DataStore ds) {
        System.out.println();
        List<Student> list = ds.getStudents();
        for (int i = 0; i < list.size(); i++) {
            System.out.printf("    %2d) %s%n", i + 1, list.get(i).getFullName());
        }
        return pickFrom(list);
    }

    private static Teacher pickTeacher(DataStore ds) {
        System.out.println();
        List<Teacher> list = ds.getTeachers();
        for (int i = 0; i < list.size(); i++) {
            System.out.printf("    %2d) %-25s %s%n",
                    i + 1, list.get(i).getFullName(),
                    DIM + list.get(i).getTitle() + RESET);
        }
        return pickFrom(list);
    }

    private static Course pickCourse(DataStore ds) {
        System.out.println();
        List<Course> list = ds.getCourses();
        for (int i = 0; i < list.size(); i++) {
            System.out.printf("    %2d) %-8s %s%n",
                    i + 1, list.get(i).getCourseCode(),
                    list.get(i).getName());
        }
        return pickFrom(list);
    }

    private static <T> T pickFrom(List<T> list) {
        if (list.isEmpty()) { info("List is empty."); return null; }
        int idx = parseIdx(prompt("  Index (0 = cancel) : "), list.size());
        return idx < 0 ? null : list.get(idx);
    }

    private static int parseIdx(String s, int size) {
        try {
            int i = Integer.parseInt(s.trim()) - 1;
            return (i >= 0 && i < size) ? i : -1;
        } catch (Exception e) { return -1; }
    }

    /* ===================== UI helpers ===================== */

    /** Clears the screen on ANSI-capable terminals. */
    private static void clear() {
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }

    private static void banner() {
        clear();
        System.out.println();
        System.out.println(CYAN + "  ╔══════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "  ║" + BOLD + "       University Information System — CLI            " + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "  ╚══════════════════════════════════════════════════════╝" + RESET);
        System.out.println();
        System.out.println(DIM + "  Demo credentials (login / password):" + RESET);
        System.out.println(DIM + "    admin       / admin   (Admin)" + RESET);
        System.out.println(DIM + "    manager     / m1      (Manager)" + RESET);
        System.out.println(DIM + "    prof.smith  / p1      (Teacher, professor)" + RESET);
        System.out.println(DIM + "    tutor.kim   / t1      (Teacher)" + RESET);
        System.out.println(DIM + "    rex         / r1      (Researcher employee)" + RESET);
        System.out.println(DIM + "    alibek      / s1      (Student, year 4)" + RESET);
        System.out.println();
    }

    private static void screen(User user, String[] options) {
        screen(user.getClass().getSimpleName() + " · " + user.getFullName(), options);
    }

    private static void screen(String title, String[] options) {
        clear();
        System.out.println();
        System.out.println(CYAN + "  ──────────────────────────────────────────────────────" + RESET);
        System.out.println("  " + BOLD + title + RESET);
        System.out.println(CYAN + "  ──────────────────────────────────────────────────────" + RESET);
        System.out.println();
        for (String opt : options) {
            System.out.println("    " + opt);
        }
        System.out.println();
    }

    private static void section(String title) {
        System.out.println();
        System.out.println(MAGENTA + "  ── " + title + " ──" + RESET);
        System.out.println();
    }

    private static void line() {
        System.out.println(CYAN + "  ──────────────────────────────────────────────────────" + RESET);
    }

    private static void ok(String msg)   { System.out.println(GREEN  + "  ✓ " + msg + RESET); }
    private static void err(String msg)  { System.out.println(RED    + "  ✗ " + msg + RESET); }
    private static void info(String msg) { System.out.println(YELLOW + "  • " + msg + RESET); }

    private static void pause() {
        System.out.println();
        System.out.print(DIM + "  Press Enter to continue..." + RESET);
        try { IN.nextLine(); } catch (Exception ignored) {}
    }

    /* ===================== Input helpers ===================== */

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
