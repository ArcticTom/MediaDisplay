package com.oneupsquad.mediadisplay.db;

import android.provider.BaseColumns;

/**
 * Created by User on 27.8.2017.
 */

public class Contract {

    public static final String DB_NAME = "com.oneupsquad.mediadisplay.db";
    public static final int DB_VERSION = 1;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE = "tasks";

        public static final String COL_TASK_TITLE = "title";
    }
}
