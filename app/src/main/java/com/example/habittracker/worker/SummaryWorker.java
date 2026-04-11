package com.example.habittracker.worker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.habittracker.R;
import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.utils.DailyCompletionUtils;
import com.example.habittracker.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SummaryWorker extends Worker {

    private static final String CHANNEL_ID = "daily_summary_channel";
    private static final int NOTIFICATION_ID = 5001;

    public SummaryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        int userId = SessionManager.getUserId(context);

        if (userId == -1) {
            return Result.success();
        }

        AppDatabase db = AppDatabase.getInstance(context);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        int totalHabits = DailyCompletionUtils.getTotalHabitsForDay(db, userId);
        int completedHabits = DailyCompletionUtils.getCompletedCountForDay(db, userId, today);
        boolean perfectDay = DailyCompletionUtils.isPerfectDay(db, userId, today);
        int currentStreak = DailyCompletionUtils.calculateCurrentDayStreak(db, userId);

        createChannel(context);

        String title = "Tổng kết hôm nay";
        String content;

        if (totalHabits == 0) {
            content = "Hôm nay bạn chưa có thói quen nào.";
        } else if (perfectDay) {
            content = "Bạn đã hoàn thành " + completedHabits + "/" + totalHabits
                    + " thói quen. Perfect day. Chuỗi hiện tại: " + currentStreak + " ngày.";
        } else {
            content = "Bạn đã hoàn thành " + completedHabits + "/" + totalHabits
                    + " thói quen. Chuỗi hiện tại: " + currentStreak + " ngày.";
        }

        showNotification(context, title, content);

        return Result.success();
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Tổng kết hằng ngày",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Thông báo tổng kết tiến độ thói quen mỗi ngày");
        manager.createNotificationChannel(channel);
    }

    private void showNotification(Context context, String title, String content) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
    }
}