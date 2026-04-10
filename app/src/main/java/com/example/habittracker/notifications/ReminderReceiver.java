package com.example.habittracker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

import com.example.habittracker.utils.NotificationUtils;

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

        // 1. Hiển thị thông báo
        NotificationUtils.showReminderNotification(context, habitId, habitTitle, notifId);

        // 2. Tự động hẹn lại báo thức này cho ngày hôm sau
        Calendar nextDay = Calendar.getInstance();
        nextDay.add(Calendar.DAY_OF_YEAR, 1);
        NotificationScheduler.scheduleReminder(
                context, habitId, habitTitle,
                nextDay.get(Calendar.HOUR_OF_DAY),
                nextDay.get(Calendar.MINUTE),
                notifId
        );
    }
}