package com.krekapps.gamifiedtasks.models;

import java.util.Date;

/**
 * Created by raefo on 12-Jun-17.
 */

public class Task {
    private String name;
    private Date due;

    public Task(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDue() {
        return due;
    }

    public boolean isDueNull() {
        return due == null;
    }

    public void setDue(Date due) {
        this.due = due;
    }
}
