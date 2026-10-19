package com.example.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.Classroom;
import com.example.model.User;

@Service
public class ClassService {

    private final Map<Long, Classroom> classes = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Autowired
    public ClassService(UserService userService) {
        // Sample classes for the demo teacher account
        User demoTeacher = userService.findByUsername("teacher");
        if (demoTeacher != null) {
            create("Math Class", "Algebra and geometry basics.", "School A", demoTeacher);
            create("English Class", "Reading, writing and speaking practice.", "School B", demoTeacher);
        }
    }

    public Classroom create(String name, String description, String schoolName, User teacher) {
        Classroom classroom = new Classroom(nextId.getAndIncrement(), name, description, schoolName, teacher,
                new Date());
        classes.put(classroom.getId(), classroom);
        return classroom;
    }

    public Classroom findById(long id) {
        return classes.get(id);
    }

    public List<Classroom> findAll() {
        List<Classroom> result = new ArrayList<>(classes.values());
        Collections.sort(result, Comparator.comparingLong(Classroom::getId));
        return result;
    }

    public List<Classroom> findByTeacher(String username) {
        List<Classroom> result = new ArrayList<>();
        for (Classroom classroom : findAll()) {
            if (classroom.getTeacherUsername().equals(username)) {
                result.add(classroom);
            }
        }
        return result;
    }

    // Administrators manage every class, teachers only their own
    public List<Classroom> findManageableBy(User user) {
        return user.isAdmin() ? findAll() : findByTeacher(user.getUsername());
    }

    public boolean canManage(User user, Classroom classroom) {
        return user.isAdmin() || classroom.getTeacherUsername().equals(user.getUsername());
    }

    public Classroom delete(long id) {
        return classes.remove(id);
    }

    // Classes that include the given library item
    public List<Classroom> findContaining(long contentId) {
        List<Classroom> result = new ArrayList<>();
        for (Classroom classroom : findAll()) {
            if (classroom.getContentIds().contains(contentId)) {
                result.add(classroom);
            }
        }
        return result;
    }

    // Called when a library item is deleted
    public void removeContentEverywhere(long contentId) {
        for (Classroom classroom : classes.values()) {
            classroom.removeContent(contentId);
        }
    }

    public int count() {
        return classes.size();
    }
}
