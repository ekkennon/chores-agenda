package com.krekapps.gamifiedtasks.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ekk on 08-Jul-17.
 */

public class TaskList {
    List<Task> tasks;

    public TaskList() {
        tasks = new ArrayList<>();
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
