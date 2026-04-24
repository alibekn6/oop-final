package com.uni.models;

import com.uni.enums.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Abstract employee of the university. Adds salary/hire date and an inbox
 * for inter-employee messages on top of {@link User}.
 */
public abstract class Employee extends User {
    private static final long serialVersionUID = 1L;

    protected double salary;
    protected Date hireDate;
    protected List<Message> inbox = new ArrayList<>();

    protected Employee(long id,
                       String login,
                       String password,
                       String firstName,
                       String lastName,
                       String email,
                       Language language,
                       double salary,
                       Date hireDate) {
        super(id, login, password, firstName, lastName, email, language);
        this.salary = salary;
        this.hireDate = hireDate;
    }

    /**
     * Send a message to another user. If the receiver is an Employee, the
     * message lands in their inbox; otherwise the message is just delivered
     * (and recorded on the message itself).
     */
    public void sendMessage(User receiver, Message message) {
        if (receiver instanceof Employee) {
            ((Employee) receiver).receiveMessage(message);
        }
    }

    public void receiveMessage(Message message) {
        inbox.add(message);
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(inbox);
    }

    public double getSalary()    { return salary; }
    public Date getHireDate()    { return hireDate; }

    public void setSalary(double salary) { this.salary = salary; }
}
