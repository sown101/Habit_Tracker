package com.example.habittracker.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.habittracker.receiver.NotificationActionReceiver;
import com.example.habittracker.ui.main.MainActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

public class NotificationUtils {

    public static final String CHANNEL_ID_REMINDER = "habit_reminder_channel";
    public static final String CHANNEL_ID_SUMMARY = "habit_summary_channel";

    public static final String ACTION_COMPLETE = "com.example.habittracker.ACTION_COMPLETE_HABIT";
    public static final String ACTION_SNOOZE = "com.example.habittracker.ACTION_SNOOZE_HABIT";

    public static final String EXTRA_HABIT_ID = "extra_habit_id";
    public static final String EXTRA_HABIT_TITLE = "extra_habit_title";
    public static final String EXTRA_NOTIF_ID = "extra_notif_id";

    public static final String ACTION_COUNTER_PLUS = "com.example.habittracker.ACTION_COUNTER_PLUS";
    public static final String ACTION_COUNTER_MINUS = "com.example.habittracker.ACTION_COUNTER_MINUS";
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
                "Nhắc nhở thói quen",
                NotificationManager.IMPORTANCE_HIGH
        );
        reminderChannel.setDescription("Thông báo nhắc bạn thực hiện thói quen");

        NotificationChannel summaryChannel = new NotificationChannel(
                CHANNEL_ID_SUMMARY,
                "Tổng kết thói quen",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        summaryChannel.setDescription("Thông báo tổng kết hằng ngày");

        manager.createNotificationChannel(reminderChannel);
        manager.createNotificationChannel(summaryChannel);
    }

    public static void showReminderNotification(Context context,
                                                int habitId,
                                                String habitTitle,
                                                int notifId,
                                                boolean isCounterHabit) {

        Intent completeIntent = new Intent(context, NotificationActionReceiver.class);
        completeIntent.setAction(ACTION_COMPLETE);
        completeIntent.putExtra(EXTRA_HABIT_ID, habitId);
        completeIntent.putExtra(EXTRA_HABIT_TITLE, habitTitle);
        completeIntent.putExtra(EXTRA_NOTIF_ID, notifId);

        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                context,
                notifId + 1000,
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent snoozeIntent = new Intent(context, NotificationActionReceiver.class);
        snoozeIntent.setAction(ACTION_SNOOZE);
        snoozeIntent.putExtra(EXTRA_HABIT_ID, habitId);
        snoozeIntent.putExtra(EXTRA_HABIT_TITLE, habitTitle);
        snoozeIntent.putExtra(EXTRA_NOTIF_ID, notifId);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                notifId + 2000,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent plusIntent = new Intent(context, NotificationActionReceiver.class);
        plusIntent.setAction(ACTION_COUNTER_PLUS);
        plusIntent.putExtra(EXTRA_HABIT_ID, habitId);
        plusIntent.putExtra(EXTRA_HABIT_TITLE, habitTitle);
        plusIntent.putExtra(EXTRA_NOTIF_ID, notifId);

        PendingIntent plusPendingIntent = PendingIntent.getBroadcast(
                context,
                notifId + 3000,
                plusIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra(EXTRA_HABIT_ID, habitId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent openPendingIntent = PendingIntent.getActivity(
                context,
                notifId + 4000,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Nhắc nhở thói quen")
                        .setContentText("Đã đến giờ: " + habitTitle)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Đã đến giờ thực hiện thói quen: " + habitTitle))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(openPendingIntent);

        if (isCounterHabit) {
            builder.addAction(0, "+1", plusPendingIntent)
                    .addAction(0, "Nhắc lại 10 phút", snoozePendingIntent);
        } else {
            builder.addAction(0, "Hoàn thành", completePendingIntent)
                    .addAction(0, "Nhắc lại 10 phút", snoozePendingIntent);
        }

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