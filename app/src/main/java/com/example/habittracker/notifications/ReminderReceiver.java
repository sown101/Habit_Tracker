package com.example.habittracker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.habittracker.utils.Constants;
import com.example.habittracker.utils.NotificationUtils;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("REMINDER_RECEIVER", "received");

        if (intent == null) {
            return;
        }

        int habitId = intent.getIntExtra(Constants.EXTRA_HABIT_ID, -1);
        String habitTitle = intent.getStringExtra(Constants.EXTRA_HABIT_TITLE);
        int notifId = intent.getIntExtra(Constants.EXTRA_NOTIF_ID, habitId);

        if (habitId == -1 || habitTitle == null || habitTitle.trim().isEmpty()) {
            return;
        }

        NotificationUtils.showReminderNotification(context, habitId, habitTitle, notifId);
    }
}