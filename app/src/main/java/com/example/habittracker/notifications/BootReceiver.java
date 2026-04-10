package com.example.habittracker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.data.model.Reminder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            List<Reminder> reminders = db.reminderDao().getAllEnabledReminders();

            for (Reminder reminder : reminders) {
                Habit habit = db.habitDao().getHabitById(reminder.getHabitId());
                if (habit == null || !habit.isActive()) {
                    continue;
                }

                int[] hourMinute = parseReminderTime(reminder.getRemindTime());
                if (hourMinute == null) {
                    continue;
                }

                NotificationScheduler.scheduleReminder(
                        context,
                        habit.getId(),
                        habit.getTitle(),
                        hourMinute[0],
                        hourMinute[1],
                        reminder.getRequestCode()
                );
            }
        });
    }

    private int[] parseReminderTime(String timeText) {
        if (timeText == null || timeText.trim().isEmpty()) {
            return null;
        }

        String[] patterns = {"HH:mm", "hh:mm a"};
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(sdf.parse(timeText.trim()));
                return new int[]{
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE)
                };
            } catch (ParseException ignored) {
            }
        }

        return null;
    }
}