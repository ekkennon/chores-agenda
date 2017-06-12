package com.krekapps.gamifiedtasks.models;

import android.content.Context;
import android.preference.PreferenceGroup;
import android.util.AttributeSet;

/**
 * Created by raefo on 10-Jun-17.
 */

public class CategoryGroup extends PreferenceGroup {

    public CategoryGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CategoryGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CategoryGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
