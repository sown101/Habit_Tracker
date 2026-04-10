package com.example.habittracker.utils;

import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.data.model.HabitLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DailyCompletionUtils {

    private DailyCompletionUtils() {
    }

    public static boolean isPerfectDay(AppDatabase db, int userId, String date) {
        List<Habit> habits = db.habitDao().getAllActiveHabitsByUser(userId);
        if (habits == null || habits.isEmpty()) {
            return false;
        }

        for (Habit habit : habits) {
            HabitLog log = db.habitLogDao().getLogByHabitAndDate(habit.getId(), date);
            if (log == null || !log.isCompleted()) {
                return false;
            }
        }

        return true;
    }

    public static int getCompletedCountForDay(AppDatabase db, int userId, String date) {
        List<Habit> habits = db.habitDao().getAllActiveHabitsByUser(userId);
        int completed = 0;

        for (Habit habit : habits) {
            HabitLog log = db.habitLogDao().getLogByHabitAndDate(habit.getId(), date);
            if (log != null && log.isCompleted()) {
                completed++;
            }
        }

        return completed;
    }

    public static int getTotalHabitsForDay(AppDatabase db, int userId) {
        List<Habit> habits = db.habitDao().getAllActiveHabitsByUser(userId);
        return habits == null ? 0 : habits.size();
    }

    public static int calculateCurrentDayStreak(AppDatabase db, int userId) {
        int streak = 0;
        Calendar calendar = Calendar.getInstance();

        while (true) {
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(calendar.getTime());

            if (isPerfectDay(db, userId, date)) {
                streak++;
                calendar.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }

        return streak;
    }

    public static int calculateLongestDayStreak(AppDatabase db, int userId, int maxDaysBack) {
        List<Boolean> perfectDays = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -(maxDaysBack - 1));

        for (int i = 0; i < maxDaysBack; i++) {
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(calendar.getTime());
            perfectDays.add(isPerfectDay(db, userId, date));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        int longest = 0;
        int current = 0;

        for (Boolean perfect : perfectDays) {
            if (perfect) {
                current++;
                longest = Math.max(longest, current);
            } else {
                current = 0;
            }
        }

        return longest;
    }
}