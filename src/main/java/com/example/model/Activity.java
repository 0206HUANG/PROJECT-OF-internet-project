package com.example.model;

import java.io.Serializable;
import java.util.Date;

public class Activity implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String message;
    private final Date time;

    public Activity(String message, Date time) {
        this.message = message;
        this.time = new Date(time.getTime());
    }

    public String getMessage() {
        return message;
    }

    public Date getTime() {
        return new Date(time.getTime());
    }
}
