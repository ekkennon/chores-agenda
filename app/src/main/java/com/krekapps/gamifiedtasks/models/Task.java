package com.krekapps.gamifiedtasks.models;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by raefo on 12-Jun-17.
 */

public class Task {
    //TODO int position, String category, id = category + position;
    private String name;
    private Calendar due;
    private boolean isDue;

    public Task(String name) {
        isDue = false;
        this.name = name;
        due = Calendar.getInstance();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getDue() {
        return due;
    }

    public String getDueDateString() {
        return due.get(Calendar.DAY_OF_MONTH) + "/" + due.get(Calendar.MONTH) + "/" + due.get(Calendar.YEAR);
    }

    public void setDue(Calendar due) {
        this.due = due;
    }

    public void setDue(String date) {
        String[] d = date.split("/");
        due.set(Integer.parseInt(d[2]),Integer.parseInt(d[1]),Integer.parseInt(d[0]));
    }

    public boolean getIsDue() {
        return isDue;
    }

    public void setIsDue(boolean isDue) {
        this.isDue = isDue;
    }

    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder();
        toReturn.append(name);
        if (isDue) {
            toReturn.append(":");
            toReturn.append(getDueDateString());
        }
        return toReturn.toString();
    }
}
