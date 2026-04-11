package com.example.habittracker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.utils.Constants;
import com.example.habittracker.utils.NotificationUtils;

import java.util.Calendar;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        android.util.Log.d("REMINDER_RECEIVER", "received");
        if (intent == null) return;

        int habitId = intent.getIntExtra(NotificationUtils.EXTRA_HABIT_ID, -1);
        String habitTitle = intent.getStringExtra(NotificationUtils.EXTRA_HABIT_TITLE);
        int notifId = intent.getIntExtra(NotificationUtils.EXTRA_NOTIF_ID, habitId);

        if (habitId == -1 || habitTitle == null || habitTitle.trim().isEmpty()) {
            return;
        }

        AppDatabase db = AppDatabase.getInstance(context);
        Habit habit = db.habitDao().getHabitById(habitId);
        boolean isCounterHabit = habit != null
                && Constants.HABIT_TYPE_COUNTER.equalsIgnoreCase(habit.getHabitType());

        NotificationUtils.showReminderNotification(
                context,
                habitId,
                habitTitle,
                notifId,
                isCounterHabit
        );

        Calendar nextDay = Calendar.getInstance();
        nextDay.add(Calendar.DAY_OF_YEAR, 1);
        NotificationScheduler.scheduleReminder(
                context,
                habitId,
                habitTitle,
                nextDay.get(Calendar.HOUR_OF_DAY),
                nextDay.get(Calendar.MINUTE),
                notifId
        );
    }
}