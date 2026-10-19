package com.example.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// A class taught by a teacher, with its own list of learning content (a playlist)
public class Classroom implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final String name;
    private final String description;
    private final String schoolName;
    private final String teacherUsername;
    private final String teacherName;
    private final Date createdAt;
    private final CopyOnWriteArrayList<Long> contentIds = new CopyOnWriteArrayList<>();

    public Classroom(long id, String name, String description, String schoolName, User teacher, Date createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.schoolName = schoolName;
        this.teacherUsername = teacher.getUsername();
        this.teacherName = teacher.getFullName();
        this.createdAt = new Date(createdAt.getTime());
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public String getTeacherUsername() {
        return teacherUsername;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public Date getCreatedAt() {
        return new Date(createdAt.getTime());
    }

    public List<Long> getContentIds() {
        return new ArrayList<>(contentIds);
    }

    public int getContentCount() {
        return contentIds.size();
    }

    // Returns false if the content was already in this class
    public boolean addContent(long contentId) {
        return contentIds.addIfAbsent(contentId);
    }

    public boolean removeContent(long contentId) {
        return contentIds.remove(Long.valueOf(contentId));
    }
}
