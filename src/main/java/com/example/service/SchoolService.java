package com.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.model.School;

@Service
public class SchoolService {

    private final List<School> schools = new CopyOnWriteArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public SchoolService() {
        // Sample data shown before any school has been added
        add("School A", "Downtown", "Active");
        add("School B", "North District", "Active");
        add("School C", "East District", "Inactive");
    }

    public School add(String name, String location, String status) {
        School school = new School(nextId.getAndIncrement(), name, location, status);
        schools.add(school);
        return school;
    }

    public List<School> findAll() {
        return new ArrayList<>(schools);
    }

    public int count() {
        return schools.size();
    }

    public int countActive() {
        int count = 0;
        for (School school : schools) {
            if (school.isActive()) {
                count++;
            }
        }
        return count;
    }
}
