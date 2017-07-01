package com.krekapps.gamifiedtasks.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
    private Set<Tag> tags;
    private List<Tag> alTags;
    private int id;

    public Task(String name) {
        this.name = name;
        hasDueDate = false;
        dueDate = Calendar.getInstance();
        isRepeating = false;
        repeatFrequency = 0;
        repeatPeriod = RepeatPeriod.NONE;
        tags = new HashSet<>();
        alTags = new ArrayList<>();
        id = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
            int month;
            switch (d[1]) {
                case "January":
                    month = 0;
                    break;
                case "February":
                    month = 1;
                    break;
                case "March":
                    month = 2;
                    break;
                case "April":
                    month = 3;
                    break;
                case "May":
                    month = 4;
                    break;
                case "June":
                    month = 5;
                    break;
                case "July":
                    month = 6;
                    break;
                case "August":
                    month = 7;
                    break;
                case "September":
                    month = 8;
                    break;
                case "October":
                    month = 9;
                    break;
                case "November":
                    month = 10;
                    break;
                case "December":
                    month = 11;
                    break;
                default:
                    month = 12;//TODO this should produce an error
                    break;
            }
            /*
            TODO this was getting a ParseException trying to parse date of "5"
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(new SimpleDateFormat("MMM", Locale.getDefault()).parse(d[1]));
            } catch (ParseException e) {
                e.printStackTrace();
            }*/

            dueDate.set(Integer.parseInt(d[2]), month, Integer.parseInt(d[0]));
        } else {
            hasDueDate = false;
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

    public boolean isDueToday() {
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

    public Set<Tag> getTags() {
        return tags;
    }

    public List<Tag> getAlTags() {
        return alTags;
    }

    public String getTagsString() {
        StringBuilder tagslist = new StringBuilder();
        for (Tag t : tags) {
            tagslist.append(t.toString());
            tagslist.append(",");
        }
        tagslist.deleteCharAt(tagslist.length() - 1);
        return tagslist.toString();
    }

    public void addTag(Tag t) {
        tags.add(t);
    }

    public void removeTag(Tag t) {
        tags.remove(t);
    }

    public static Task fromString(String s) {
        String[] list = s.split(":");
        Task t;
        if (list.length % 2 == 1) {
            t = new Task(/*"name:" + */s);
        } else {
            Map<String, String> map = new HashMap<>();
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
            if (map.containsKey("tags")) {
                String[] tagslist = map.get("tags").split(",");
                if (tagslist.length == 1) {
                    t.addTag(new Tag(tagslist[0]));
                } else {
                    for (String tagstring : tagslist) {
                        t.addTag(new Tag(tagstring));
                    }
                }
            }
        }
        return t;
    }

    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder();
        if (id > 0) {
            toReturn.append("id:");
            toReturn.append(id);
            toReturn.append(":");
        }
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
        if (tags != null && !tags.isEmpty() && tags.size() > 0) {
            toReturn.append(":tags:");
            toReturn.append(getTagsString());
        }
        return toReturn.toString();
    }
}
