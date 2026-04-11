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
        List<Habit> habits = getHabitsActiveOnDate(db, userId, date);
        if (habits.isEmpty()) {
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
        List<Habit> habits = getHabitsActiveOnDate(db, userId, date);
        int completed = 0;

        for (Habit habit : habits) {
            HabitLog log = db.habitLogDao().getLogByHabitAndDate(habit.getId(), date);
            if (log != null && log.isCompleted()) {
                completed++;
            }
        }

        return completed;
    }

    public static int getTotalHabitsForDay(AppDatabase db, int userId, String date) {
        return getHabitsActiveOnDate(db, userId, date).size();
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

    public static List<Habit> getHabitsActiveOnDate(AppDatabase db, int userId, String date) {
        List<Habit> allHabits = db.habitDao().getAllActiveHabitsByUser(userId);
        List<Habit> result = new ArrayList<>();

        if (allHabits == null) {
            return result;
        }

        for (Habit habit : allHabits) {
            if (wasHabitCreatedOnOrBeforeDate(habit, date)) {
                result.add(habit);
            }
        }

        return result;
    }

    private static boolean wasHabitCreatedOnOrBeforeDate(Habit habit, String date) {
        if (habit == null) {
            return false;
        }

        String createdAt = habit.getCreatedAt();
        if (createdAt == null || createdAt.trim().isEmpty()) {
            return true;
        }

        String createdDate = createdAt.length() >= 10 ? createdAt.substring(0, 10) : createdAt;
        return createdDate.compareTo(date) <= 0;
    }
}