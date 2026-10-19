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

    public School findById(long id) {
        for (School school : schools) {
            if (school.getId() == id) {
                return school;
            }
        }
        return null;
    }

    // Returns null if the school does not exist
    public synchronized School update(long id, String name, String location, String status) {
        for (int i = 0; i < schools.size(); i++) {
            if (schools.get(i).getId() == id) {
                School updated = new School(id, name, location, status);
                schools.set(i, updated);
                return updated;
            }
        }
        return null;
    }

    public synchronized School delete(long id) {
        School school = findById(id);
        if (school != null) {
            schools.remove(school);
        }
        return school;
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
