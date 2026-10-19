package com.example.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.model.Activity;

@Service
public class ActivityService {

    private static final int MAX_ACTIVITIES = 10;

    private final Deque<Activity> activities = new ArrayDeque<>();

    public synchronized void record(String message) {
        activities.addFirst(new Activity(message, new Date()));
        while (activities.size() > MAX_ACTIVITIES) {
            activities.removeLast();
        }
    }

    // Most recent first
    public synchronized List<Activity> recent() {
        return new ArrayList<>(activities);
    }
}
