package com.example.habittracker.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.utils.Constants;
import com.example.habittracker.utils.NotificationUtils;

public class ReminderWorker extends Worker {

    public static final String KEY_HABIT_ID = "habit_id";
    public static final String KEY_HABIT_TITLE = "habit_title";
    public static final String KEY_NOTIFICATION_ID = "notification_id";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        int habitId = getInputData().getInt(KEY_HABIT_ID, -1);
        String habitTitle = getInputData().getString(KEY_HABIT_TITLE);
        int notifId = getInputData().getInt(KEY_NOTIFICATION_ID, habitId);

        if (habitId == -1) {
            return Result.failure();
        }

        AppDatabase db = AppDatabase.getInstance(context);
        Habit habit = db.habitDao().getHabitById(habitId);

        if (habit == null) {
            return Result.failure();
        }

        if (habitTitle == null || habitTitle.trim().isEmpty()) {
            habitTitle = habit.getTitle();
        }

        boolean isCounterHabit =
                Constants.HABIT_TYPE_COUNTER.equalsIgnoreCase(habit.getHabitType());

        NotificationUtils.createNotificationChannels(context);
        NotificationUtils.showReminderNotification(
                context,
                habitId,
                habitTitle,
                notifId,
                isCounterHabit
        );

        return Result.success();
    }
}