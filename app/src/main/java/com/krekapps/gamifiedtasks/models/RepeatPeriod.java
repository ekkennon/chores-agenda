package com.krekapps.gamifiedtasks.models;

/**
 * Created by raefo on 17-Jun-17.
 */

public enum RepeatPeriod {
    NONE ("None"),
    HOUR ("Hour"),
    DAY ("Day"),
    WEEK ("Week"),
    MONTH ("Month"),
    YEAR ("Year");

    private final String period;

    RepeatPeriod(String p) {
        period = p;
    }

    public String getPeriod() {
        return period;
    }
}
