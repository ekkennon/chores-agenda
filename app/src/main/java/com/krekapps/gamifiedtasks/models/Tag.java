package com.krekapps.gamifiedtasks.models;

/**
 * Created by raefo on 21-Jun-17.
 */

public class Tag {
    private String name;

    public Tag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
