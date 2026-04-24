package com.uni.models;

import com.uni.enums.Language;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * System administrator: manages user accounts and reads audit logs.
 */
public class Admin extends Employee {
    private static final long serialVersionUID = 1L;

    public Admin(long id,
                 String login,
                 String password,
                 String firstName,
                 String lastName,
                 String email,
                 Language language,
                 double salary,
                 Date hireDate) {
        super(id, login, password, firstName, lastName, email, language,
                salary, hireDate);
    }

    public void addUser(User user) {
        com.uni.storage.DataStore.getInstance().addUser(user);
    }

    public void removeUser(User user) {
        com.uni.storage.DataStore.getInstance().removeUser(user);
    }

    public void updateUser(User user) {
        // identity is preserved by id/login; just re-stamp the entry
        com.uni.storage.DataStore ds = com.uni.storage.DataStore.getInstance();
        ds.removeUser(user);
        ds.addUser(user);
    }

    public List<UserAction> viewAllLogs() {
        return new ArrayList<>(com.uni.storage.DataStore.getInstance().getLogs());
    }

    public List<UserAction> viewUserLogs(User user) {
        List<UserAction> result = new ArrayList<>();
        for (UserAction a : com.uni.storage.DataStore.getInstance().getLogs()) {
            if (a.getActor() != null && a.getActor().equals(user)) {
                result.add(a);
            }
        }
        return result;
    }
}
