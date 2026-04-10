package com.example.habittracker.utils;

public class Constants {
    private Constants() {
    }

    // Habit types
    public static final String HABIT_TYPE_CHECKBOX = "CHECKBOX";
    public static final String HABIT_TYPE_COUNTER = "COUNTER";

    // Frequency types
    public static final String FREQUENCY_DAILY = "DAILY";
    public static final String FREQUENCY_WEEKLY = "WEEKLY";
    public static final String FREQUENCY_CUSTOM = "CUSTOM";

    // Completion methods
    public static final String COMPLETION_METHOD_MANUAL = "MANUAL";
    public static final String COMPLETION_METHOD_NOTIFICATION = "NOTIFICATION";
    public static final String COMPLETION_METHOD_SHAKE = "SHAKE";
    public static final String COMPLETION_METHOD_SESSION = "SESSION";
    public static final String COMPLETION_METHOD_SENSOR = "SENSOR";
    public static final String COMPLETION_METHOD_GOOGLE_SYNC = "GOOGLE_SYNC";

    // Focus session status
    public static final String SESSION_STATUS_RUNNING = "RUNNING";
    public static final String SESSION_STATUS_PAUSED = "PAUSED";
    public static final String SESSION_STATUS_COMPLETED = "COMPLETED";
    public static final String SESSION_STATUS_STOPPED = "STOPPED";

    //thêm lần đầu khi sửa
    public static final String CHANNEL_ID_FOCUS = "focus_session_channel";
    public static final String CHANNEL_NAME_FOCUS = "Focus Session";

    public static final String ACTION_FOCUS_STOP = "com.example.habittracker.ACTION_FOCUS_STOP";
    public static final String ACTION_FOCUS_COMPLETE = "com.example.habittracker.ACTION_FOCUS_COMPLETE";

    public static final String EXTRA_HABIT_ID = "extra_habit_id";
    public static final String EXTRA_HABIT_TITLE = "extra_habit_title";
    public static final String EXTRA_DURATION_MINUTES = "extra_duration_minutes";
    public static final String EXTRA_SESSION_ID = "extra_session_id";

    // Database
    public static final String DATABASE_NAME = "habit_tracker_db";
}
