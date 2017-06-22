package com.krekapps.gamifiedtasks.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by raefo on 12-Jun-17.
 */

public class Task {
    //TODO int position, String category, id = category + position;
    private String name;
    private Calendar dueDate;
    private boolean hasDueDate;
    private boolean isRepeating;
    private int repeatFrequency;
    private RepeatPeriod repeatPeriod;
    private ArrayList<Tag> tags;

    public Task(String name) {
        this.name = name;
        hasDueDate = false;
        dueDate = Calendar.getInstance();
        isRepeating = false;
        repeatFrequency = 0;
        repeatPeriod = RepeatPeriod.NONE;
        tags = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getDue() {
        return dueDate;
    }

    public String getDueDateString() {
        //SimpleDateFormat monthParse = ;
        //SimpleDateFormat monthDisplay =
        //new SimpleDateFormat("MMMM").format(new SimpleDateFormat("MM").parse(dueDate.get));
        //return monthDisplay.format();

        return dueDate.get(Calendar.DAY_OF_MONTH) + "/" + dueDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + "/" + dueDate.get(Calendar.YEAR);
    }

    public void setDueDate(Calendar due) {
        this.dueDate = due;
    }

    public void setDueDate(String date) {
        String[] d = date.split("/");
        if (d.length == 3) {
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(new SimpleDateFormat("MMM", Locale.getDefault()).parse(d[1]));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            dueDate.set(Integer.parseInt(d[2]), cal.get(Calendar.MONTH), Integer.parseInt(d[0]));
        }
    }

    public void updateDueDate() {
        //if (repeatPeriod == RepeatPeriod.DAY) {
            dueDate.roll(Calendar.DAY_OF_MONTH,repeatFrequency);
        //}
    }

    public boolean hasDueDate() {
        return hasDueDate;
    }

    public void setHasDueDate(boolean isDue) {
        this.hasDueDate = isDue;
    }

    public boolean isOverdue() {
        if (hasDueDate) {
            Calendar today = Calendar.getInstance();
            return dueDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) && dueDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) && dueDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
        }
        return false;
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
    }

    public int getRepeatFrequency() {
        return repeatFrequency;
    }

    public void setRepeatFrequency(int repeatFrequency) {
        this.repeatFrequency = repeatFrequency;
    }

    public RepeatPeriod getRepeatPeriod() {
        return repeatPeriod;
    }

    public String getRepeatPeriodString() {
        return repeatPeriod.getPeriod();
    }

    public void setRepeatPeriod(RepeatPeriod repeatPeriod) {
        this.repeatPeriod = repeatPeriod;
    }

    public void setRepeatPeriod(String repeatPeriod) {
        this.repeatPeriod = RepeatPeriod.valueOf(repeatPeriod);
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag t) {
        tags.add(t);
    }

    public static Task fromString(String s) {
        String[] list = s.split(":");
        Task t;
        if (list.length % 2 == 1) {
            t = new Task(s);
        } else {
            HashMap<String, String> map = new HashMap<>();
            for (int i = 0; i < list.length; i += 2) {
                map.put(list[i], list[i + 1]);
            }

            t = new Task(map.get("name"));
            if (map.containsKey("due")) {
                t.setHasDueDate(true);
                t.setDueDate(map.get("due"));
            }
            if (map.containsKey("repeating")) {
                t.setRepeating(true);
                t.setRepeatFrequency(Integer.parseInt(map.get("repeating")));
                t.setRepeatPeriod(RepeatPeriod.DAY);//map.get("per"));//TODO this should not default to daily
            }
        }
        return t;
    }

    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder();
        toReturn.append("name:");
        toReturn.append(name);
        if (hasDueDate) {
            toReturn.append(":due:");
            toReturn.append(getDueDateString());
        }
        if (isRepeating) {
            toReturn.append(":repeating:");
            toReturn.append(repeatFrequency);
            toReturn.append(":per:");
            toReturn.append(getRepeatPeriodString());
        }
        return toReturn.toString();
    }
}
