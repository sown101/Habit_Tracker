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

    private DailyCompletionUtils() {}

    // ===== STREAK TỔNG (tính theo ngày hoàn hảo - hoàn thành tất cả habit) =====

    // Kiểm tra ngày đó có hoàn thành tất cả habit không
    public static boolean isPerfectDay(AppDatabase db, int userId, String date) {
        List<Habit> habits = getHabitsActiveOnDate(db, userId, date);
        if (habits.isEmpty()) return false;

        for (Habit habit : habits) {
            HabitLog log = db.habitLogDao().getLogByHabitAndDate(habit.getId(), date);
            if (log == null || !log.isCompleted()) return false;
        }
        return true;
    }

    // Đếm số habit đã hoàn thành trong ngày
    public static int getCompletedCountForDay(AppDatabase db, int userId, String date) {
        List<Habit> habits = getHabitsActiveOnDate(db, userId, date);
        int completed = 0;
        for (Habit habit : habits) {
            HabitLog log = db.habitLogDao().getLogByHabitAndDate(habit.getId(), date);
            if (log != null && log.isCompleted()) completed++;
        }
        return completed;
    }

    // Tổng số habit trong ngày
    public static int getTotalHabitsForDay(AppDatabase db, int userId, String date) {
        return getHabitsActiveOnDate(db, userId, date).size();
    }

    // Tính streak hiện tại (tính lùi từ hôm nay, đếm số ngày hoàn hảo liên tiếp)
    public static int calculateCurrentDayStreak(AppDatabase db, int userId) {
        int streak = 0;
        Calendar calendar = Calendar.getInstance();

        while (true) {
            String date = formatDate(calendar);
            if (isPerfectDay(db, userId, date)) {
                streak++;
                calendar.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }
        return streak;
    }

    // Tính streak dài nhất trong maxDaysBack ngày gần nhất
    public static int calculateLongestDayStreak(AppDatabase db, int userId, int maxDaysBack) {
        List<Boolean> perfectDays = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -(maxDaysBack - 1));

        for (int i = 0; i < maxDaysBack; i++) {
            perfectDays.add(isPerfectDay(db, userId, formatDate(calendar)));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        int longest = 0, current = 0;
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

    // ===== STREAK RIÊNG TỪNG HABIT =====

    /**
     * Tính streak hiện tại của 1 habit cụ thể.
     * Tính lùi từ hôm nay: nếu ngày đó habit đã hoàn thành thì +1, không thì dừng.
     */
    public static int calculateHabitCurrentStreak(AppDatabase db, int habitId) {
        int streak = 0;
        Calendar calendar = Calendar.getInstance();

        // Giới hạn tối đa 365 ngày để tránh vòng lặp vô hạn
        for (int i = 0; i < 365; i++) {
            String date = formatDate(calendar);
            HabitLog log = db.habitLogDao().getLogByHabitAndDate(habitId, date);

            if (log != null && log.isCompleted()) {
                streak++;
                calendar.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }
        return streak;
    }

    /**
     * Tính streak dài nhất của 1 habit trong maxDaysBack ngày gần nhất.
     */
    public static int calculateHabitLongestStreak(AppDatabase db, int habitId, int maxDaysBack) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -(maxDaysBack - 1));

        int longest = 0, current = 0;

        for (int i = 0; i < maxDaysBack; i++) {
            String date = formatDate(calendar);
            HabitLog log = db.habitLogDao().getLogByHabitAndDate(habitId, date);

            if (log != null && log.isCompleted()) {
                current++;
                longest = Math.max(longest, current);
            } else {
                current = 0;
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return longest;
    }

    /**
     * Đếm tổng số ngày habit đã hoàn thành.
     */
    public static int calculateHabitTotalCompleted(AppDatabase db, int habitId) {
        List<HabitLog> logs = db.habitLogDao().getLogsByHabit(habitId);
        int total = 0;
        for (HabitLog log : logs) {
            if (log.isCompleted()) total++;
        }
        return total;
    }

    // ===== HELPER =====

    // Lấy danh sách habit đang hoạt động vào ngày đó (được tạo trước hoặc đúng ngày đó)
    public static List<Habit> getHabitsActiveOnDate(AppDatabase db, int userId, String date) {
        List<Habit> allHabits = db.habitDao().getAllActiveHabitsByUser(userId);
        List<Habit> result = new ArrayList<>();

        if (allHabits == null) return result;

        for (Habit habit : allHabits) {
            if (wasHabitCreatedOnOrBeforeDate(habit, date)) {
                result.add(habit);
            }
        }
        return result;
    }

    private static boolean wasHabitCreatedOnOrBeforeDate(Habit habit, String date) {
        if (habit == null) return false;
        String createdAt = habit.getCreatedAt();
        if (createdAt == null || createdAt.trim().isEmpty()) return true;
        String createdDate = createdAt.length() >= 10 ? createdAt.substring(0, 10) : createdAt;
        return createdDate.compareTo(date) <= 0;
    }

    private static String formatDate(Calendar calendar) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
    }
}