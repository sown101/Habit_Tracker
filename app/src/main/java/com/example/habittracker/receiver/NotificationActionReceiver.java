package com.example.habittracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.data.model.HabitLog;
import com.example.habittracker.notifications.NotificationScheduler;
import com.example.habittracker.utils.Constants;
import com.example.habittracker.utils.NotificationUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        int habitId = intent.getIntExtra(NotificationUtils.EXTRA_HABIT_ID, -1);
        int notifId = intent.getIntExtra(NotificationUtils.EXTRA_NOTIF_ID, -1);
        String habitTitle = intent.getStringExtra(NotificationUtils.EXTRA_HABIT_TITLE);

        if (habitId == -1) {
            return;
        }

        if (NotificationUtils.ACTION_COMPLETE.equals(action)) {
            markHabitComplete(context, habitId);
            if (notifId != -1) {
                NotificationUtils.cancelNotification(context, notifId);
            }
        } else if (NotificationUtils.ACTION_SNOOZE.equals(action)) {
            if (habitTitle != null && notifId != -1) {
                NotificationScheduler.scheduleOneTimeReminder(
                        context,
                        habitId,
                        habitTitle,
                        10,
                        notifId + 5000
                );
                NotificationUtils.cancelNotification(context, notifId);
            }
        }
    }

    private void markHabitComplete(Context context, int habitId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);

            Habit habit = db.habitDao().getHabitById(habitId);
            if (habit == null) {
                return;
            }

            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date());

            HabitLog existingLog = db.habitLogDao().getLogByHabitAndDate(habitId, today);

            if (existingLog != null) {
                existingLog.setCurrentValue(habit.getTargetValue());
                existingLog.setTargetValue(habit.getTargetValue());
                existingLog.setCompleted(true);
                existingLog.setCompletedAt(today);
                existingLog.setCompletionMethod(Constants.COMPLETION_METHOD_NOTIFICATION);
                db.habitLogDao().update(existingLog);
            } else {
                HabitLog newLog = new HabitLog(
                        habitId,
                        today,
                        habit.getTargetValue(),
                        habit.getTargetValue(),
                        true,
                        today,
                        null,
                        Constants.COMPLETION_METHOD_NOTIFICATION
                );
                db.habitLogDao().insert(newLog);
            }
        });
    }
}