package com.uni.storage;

import com.uni.models.Course;
import com.uni.models.Message;
import com.uni.models.ResearchPaper;
import com.uni.models.ResearchProject;
import com.uni.models.Researcher;
import com.uni.models.ResearcherDecorator;
import com.uni.models.Student;
import com.uni.models.Teacher;
import com.uni.models.User;
import com.uni.models.UserAction;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Singleton holding all in-memory state and persistence. The single
 * instance can be saved/loaded via Java object serialization.
 */
public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_FILE = "data.ser";

    private static DataStore instance;

    private final List<User>            users     = new ArrayList<>();
    private final List<Course>          courses   = new ArrayList<>();
    private final List<ResearchProject> projects  = new ArrayList<>();
    private final List<UserAction>      logs      = new ArrayList<>();
    private final List<Message>         messages  = new ArrayList<>();
    /** Decorator-style researchers that are not Users themselves. */
    private final List<Researcher>      decorators = new ArrayList<>();

    private long nextLogId = 1;

    private DataStore() {}

    public static synchronized DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    /* ===================== Users ===================== */

    public void addUser(User user) {
        if (user == null || users.contains(user)) return;
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
        decorators.removeIf(d -> d instanceof ResearcherDecorator
                && ((ResearcherDecorator) d).getOriginalUser().equals(user));
    }

    public List<User> getUsers() { return Collections.unmodifiableList(users); }

    public List<Student> getStudents() {
        List<Student> out = new ArrayList<>();
        for (User u : users) if (u instanceof Student) out.add((Student) u);
        return out;
    }

    public List<Teacher> getTeachers() {
        List<Teacher> out = new ArrayList<>();
        for (User u : users) if (u instanceof Teacher) out.add((Teacher) u);
        return out;
    }

    /* ===================== Courses / Projects ===================== */

    public void addCourse(Course c) {
        if (c != null && !courses.contains(c)) courses.add(c);
    }
    public List<Course> getCourses() { return Collections.unmodifiableList(courses); }

    public void addResearchProject(ResearchProject p) {
        if (p != null && !projects.contains(p)) projects.add(p);
    }
    public List<ResearchProject> getResearchProjects() {
        return Collections.unmodifiableList(projects);
    }

    /* ===================== Researchers ===================== */

    public void addResearcher(Researcher r) {
        if (r != null && !decorators.contains(r)) decorators.add(r);
    }

    /** All active researchers in the university (deduplicated by identity). */
    public List<Researcher> getAllResearchers() {
        Set<Researcher> seen = new LinkedHashSet<>();
        Set<User> wrapped = new HashSet<>();
        for (Researcher r : decorators) {
            if (r instanceof ResearcherDecorator) {
                wrapped.add(((ResearcherDecorator) r).getOriginalUser());
            }
            seen.add(r);
        }
        for (User u : users) {
            if (u instanceof Researcher && ((Researcher) u).isResearcher()
                    && !wrapped.contains(u)) {
                seen.add((Researcher) u);
            }
        }
        return new ArrayList<>(seen);
    }

    public List<ResearchPaper> getAllPapers() {
        // dedupe by reference equality
        Set<ResearchPaper> seen = new LinkedHashSet<>();
        for (Researcher r : getAllResearchers()) seen.addAll(r.getPapers());
        return new ArrayList<>(seen);
    }

    public void printAllUniversityPapers(Comparator<ResearchPaper> comparator) {
        List<ResearchPaper> all = getAllPapers();
        if (comparator != null) all.sort(comparator);
        System.out.println("=== All University Papers (" + all.size() + ") ===");
        int i = 1;
        for (ResearchPaper p : all) {
            System.out.println("  " + i++ + ". " + p);
        }
    }

    /** Researcher with the highest total citation count. */
    public Researcher printTopCitedResearcher() {
        Researcher top = null;
        int topCitations = -1;
        for (Researcher r : getAllResearchers()) {
            int total = 0;
            for (ResearchPaper p : r.getPapers()) total += p.getCitations();
            if (total > topCitations) {
                topCitations = total;
                top = r;
            }
        }
        if (top != null) {
            System.out.println("Top cited researcher: " + top.getResearcherName()
                    + " (" + topCitations + " total citations)");
        } else {
            System.out.println("Top cited researcher: (none)");
        }
        return top;
    }

    /* ===================== Logs / Messages ===================== */

    public void addLog(UserAction action) {
        if (action != null) logs.add(action);
    }

    /** Convenience: build and store a log entry in one call. */
    public void log(User actor, String details) {
        logs.add(new UserAction(nextLogId++, actor, details));
    }

    public List<UserAction> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    public void addMessage(Message message) {
        if (message != null) messages.add(message);
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    /* ===================== Authentication ===================== */

    /**
     * Returns the user whose credentials match, or null. Logs the attempt.
     */
    public User authenticate(String login, String password) {
        for (User u : users) {
            if (u.login(login, password)) {
                log(u, "logged in");
                return u;
            }
        }
        log(null, "failed login attempt for '" + login + "'");
        return null;
    }

    /* ===================== Persistence ===================== */

    public void save() { save(DEFAULT_FILE); }

    public void save(String path) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(this);
            log(null, "saved data to " + path);
        } catch (IOException e) {
            System.err.println("Failed to save: " + e.getMessage());
        }
    }

    public static DataStore load() { return load(DEFAULT_FILE); }

    public static DataStore load(String path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            DataStore loaded = (DataStore) ois.readObject();
            instance = loaded;
            instance.log(null, "loaded data from " + path);
            return instance;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load: " + e.getMessage());
            return getInstance();
        }
    }

    /** Mainly for the demo's "restart" simulation — wipes singleton. */
    public static void resetForTesting() { instance = null; }
}
