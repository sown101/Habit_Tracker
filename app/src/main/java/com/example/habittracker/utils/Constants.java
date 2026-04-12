package com.example.habittracker.utils;

public final class Constants {

    private Constants() {
    }

    public static final String DATABASE_NAME = "habit_tracker_db";

    public static final String HABIT_TYPE_CHECKBOX = "CHECKBOX";
    public static final String HABIT_TYPE_COUNTER = "COUNTER";
    public static final String HABIT_TYPE_TIMER = "TIMER";

    public static final String FREQUENCY_DAILY = "Hàng ngày";
    public static final String FREQUENCY_WEEKLY = "Hàng tuần";
    public static final String FREQUENCY_MONTHLY = "Hàng tháng";

    public static final String COMPLETION_METHOD_MANUAL = "MANUAL";
    public static final String COMPLETION_METHOD_NOTIFICATION = "NOTIFICATION";
    public static final String COMPLETION_METHOD_TIMER = "TIMER";

    public static final String PREF_TIMER = "timer_habit_pref";

    public static final String EXTRA_HABIT_ID = "extra_habit_id";
    public static final String EXTRA_HABIT_TITLE = "extra_habit_title";
    public static final String EXTRA_NOTIF_ID = "extra_notif_id";
}