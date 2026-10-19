package com.example.service;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.model.Role;
import com.example.model.User;

@Service
public class UserService {

    // Keyed by lower-case username; registered users use their email address as username
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public UserService() {
        // Demo accounts so the application can be used right after deployment
        addUser(new User("admin", "System Administrator", "admin@edustream.local", "admin", Role.ADMIN));
        addUser(new User("teacher", "Demo Teacher", "teacher@edustream.local", "teacher", Role.TEACHER));
    }

    // Accepts either the username or the email address; returns null when the credentials are wrong
    public User authenticate(String login, String password) {
        User user = findByLogin(login);
        return user != null && user.passwordMatches(password) ? user : null;
    }

    // Returns false when an account with this email address already exists
    public synchronized boolean register(String fullName, String email, String password, Role role) {
        String key = normalize(email);
        if (findByLogin(key) != null) {
            return false;
        }
        addUser(new User(key, fullName, key, password, role));
        return true;
    }

    public int count() {
        return users.size();
    }

    public int countByRole(Role role) {
        int count = 0;
        for (User user : users.values()) {
            if (user.getRole() == role) {
                count++;
            }
        }
        return count;
    }

    private User findByLogin(String login) {
        String key = normalize(login);
        for (User user : users.values()) {
            if (user.getUsername().equals(key) || user.getEmail().equals(key)) {
                return user;
            }
        }
        return null;
    }

    private void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
