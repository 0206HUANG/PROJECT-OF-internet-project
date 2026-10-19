package com.example.model;

import java.io.Serializable;

public class School implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final String name;
    private final String location;
    private final String status;

    public School(long id, String name, String location, String status) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }

    public boolean isActive() {
        return "Active".equals(status);
    }
}
