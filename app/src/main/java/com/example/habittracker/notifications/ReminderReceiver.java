package com.example.habittracker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Android gọi lớp này khi AlarmManager báo đến giờ nhắc habit.
 *
 * Khai báo trong AndroidManifest.xml:
 *   <receiver android:name=".notifications.ReminderReceiver" android:exported="true"/>
 *
 * Thanh — phần Receiver
 */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int habitId       = intent.getIntExtra(NotificationUtils.EXTRA_HABIT_ID, -1);
        String habitTitle = intent.getStringExtra(NotificationUtils.EXTRA_HABIT_TITLE);
        int notifId       = intent.getIntExtra(NotificationUtils.EXTRA_NOTIF_ID, habitId);

        // Kiểm tra data hợp lệ trước khi làm gì
        if (habitId == -1 || habitTitle == null) return;

        NotificationUtils.showReminderNotification(context, habitId, habitTitle, notifId);
    }
}
