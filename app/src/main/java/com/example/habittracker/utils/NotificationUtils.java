package com.example.habittracker.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.habittracker.R;
import com.example.habittracker.ui.main.MainActivity;

public final class NotificationUtils {

    public static final String CHANNEL_ID_REMINDER = "habit_reminder_channel";
    public static final String CHANNEL_ID_SUMMARY = "habit_summary_channel";

    private NotificationUtils() {
    }

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) {
            return;
        }

        NotificationChannel reminderChannel = new NotificationChannel(
                CHANNEL_ID_REMINDER,
                "Habit reminders",
                NotificationManager.IMPORTANCE_HIGH
        );
        reminderChannel.setDescription("Simple reminder notifications");

        NotificationChannel summaryChannel = new NotificationChannel(
                CHANNEL_ID_SUMMARY,
                "Daily summary",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        summaryChannel.setDescription("Daily habit summary");

        manager.createNotificationChannel(reminderChannel);
        manager.createNotificationChannel(summaryChannel);
    }

    public static void showReminderNotification(Context context,
                                                int habitId,
                                                String habitTitle,
                                                int notifId) {

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra(Constants.EXTRA_HABIT_ID, habitId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent openPendingIntent = PendingIntent.getActivity(
                context,
                notifId + 1000,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(habitTitle)
                        .setContentText("It's time to complete your habit.")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("It's time to complete your habit."))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(openPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationManagerCompat.from(context).notify(notifId, builder.build());
    }

    public static void cancelNotification(Context context, int notifId) {
        NotificationManagerCompat.from(context).cancel(notifId);
    }
}