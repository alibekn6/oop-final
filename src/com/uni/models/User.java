package com.uni.models;

import com.uni.enums.Language;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract base of every person in the system. Holds credentials, name,
 * preferred language, and exposes the {@link #login(String, String)} check
 * used by the authentication flow.
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    protected long id;
    protected String login;
    protected String password;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected Language language;

    protected User(long id,
                   String login,
                   String password,
                   String firstName,
                   String lastName,
                   String email,
                   Language language) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.language = language;
    }

    /** Authenticate by comparing stored credentials. */
    public boolean login(String login, String password) {
        return Objects.equals(this.login, login)
                && Objects.equals(this.password, password);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public long getId()           { return id; }
    public String getLogin()      { return login; }
    public String getFirstName()  { return firstName; }
    public String getLastName()   { return lastName; }
    public String getEmail()      { return email; }
    public Language getLanguage() { return language; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName)   { this.lastName = lastName; }
    public void setEmail(String email)         { this.email = email; }
    public void setLanguage(Language language) { this.language = language; }
    public void setPassword(String password)   { this.password = password; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(login, user.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, login);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{id=" + id
                + ", login='" + login + '\''
                + ", name='" + getFullName() + '\''
                + '}';
    }
}
