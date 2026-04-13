package com.example.habittracker.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.habittracker.utils.Constants;

import java.util.Calendar;

public class NotificationScheduler {

    private NotificationScheduler() {
    }

    public static void scheduleReminder(Context context,
                                        int habitId,
                                        String habitTitle,
                                        int hour,
                                        int minute,
                                        int requestCode) {

        Log.d("NOTI_SCHEDULER", "scheduleReminder called");

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(Constants.EXTRA_HABIT_ID, habitId);
        intent.putExtra(Constants.EXTRA_HABIT_TITLE, habitTitle);
        intent.putExtra(Constants.EXTRA_NOTIF_ID, requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        long triggerAtMillis = calendar.getTimeInMillis();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                );
            }

            Log.d("NOTI_SCHEDULER", "Reminder scheduled at: " + calendar.getTime());
        } catch (SecurityException e) {
            Log.e("NOTI_SCHEDULER", "Missing exact alarm permission", e);
        }
    }

    public static void cancelReminder(Context context, int requestCode) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        Log.d("NOTI_SCHEDULER", "Reminder canceled for requestCode=" + requestCode);
    }
}