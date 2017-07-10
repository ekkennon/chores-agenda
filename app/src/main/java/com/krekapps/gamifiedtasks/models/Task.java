package com.krekapps.gamifiedtasks.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by ekk on 12-Jun-17.
 */

public class Task {
    private String name;
    private Calendar dueDate;
    private boolean hasDueDate;
    private boolean isRepeating;
    private int repeatFrequency;
    private RepeatPeriod repeatPeriod;
    private Set<Tag> tags;
    private List<Tag> alTags;
    private int id;//TODO int position, String category, id = category + position;

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

    private String getDueDateString() {
        return dueDate.get(Calendar.DAY_OF_MONTH) + "/" + dueDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + "/" + dueDate.get(Calendar.YEAR);
    }

    public void setDueDate(Calendar due) {
        this.dueDate = due;
    }

    private void setDueDate(String date) {
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
                    Calendar cal = Calendar.getInstance();
                    month = cal.get(Calendar.MONTH);//TODO this could be a problem until calculating overdue dates is possible
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

    public void setRepeatFrequency(int repeatFrequency) {
        this.repeatFrequency = repeatFrequency;
    }

    private String getRepeatPeriodString() {
        return repeatPeriod.getPeriod();
    }

    public void setRepeatPeriod(RepeatPeriod repeatPeriod) {
        this.repeatPeriod = repeatPeriod;
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
            tagslist.append(t.toString().trim());
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
        if (list.length % 2 == 0) {
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
        } else {
            t = new Task(s);
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
