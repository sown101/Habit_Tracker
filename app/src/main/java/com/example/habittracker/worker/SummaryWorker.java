package com.example.habittracker.worker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import com.example.habittracker.ui.main.MainActivity;
import com.example.habittracker.utils.Constants;
import com.example.habittracker.utils.DailyCompletionUtils;
import com.example.habittracker.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SummaryWorker extends Worker {

    private static final String CHANNEL_ID = "daily_summary_channel";
    private static final String CHANNEL_NAME = "Tổng kết hằng ngày";
    private static final String CHANNEL_DESCRIPTION = "Thông báo tổng kết tiến độ thói quen mỗi ngày";
    private static final int NOTIFICATION_ID = 5001;

    public SummaryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        if (!hasNotificationPermission(context)) {
            return Result.success();
        }

        int userId = SessionManager.getUserId(context);
        if (userId == -1) {
            return Result.success();
        }

        AppDatabase db = AppDatabase.getInstance(context);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        int totalHabits = DailyCompletionUtils.getTotalHabitsForDay(db, userId, today);
        int completedHabits = DailyCompletionUtils.getCompletedCountForDay(db, userId, today);
        boolean perfectDay = DailyCompletionUtils.isPerfectDay(db, userId, today);
        int currentDayStreak = DailyCompletionUtils.calculateCurrentDayStreak(db, userId);

        createNotificationChannel(context);

        String title = buildTitle(totalHabits, completedHabits, perfectDay);
        String content = buildContent(totalHabits, completedHabits, perfectDay, currentDayStreak);

        showNotification(context, title, content);

        return Result.success();
    }

    private String buildTitle(int totalHabits, int completedHabits, boolean perfectDay) {
        if (totalHabits <= 0) {
            return "Tổng kết hôm nay";
        }

        if (perfectDay) {
            return "Tuyệt vời, bạn đã hoàn thành hết 🎉";
        }

        if (completedHabits > 0) {
            return "Tổng kết hôm nay";
        }

        return "Một ngày mới để cố gắng hơn";
    }

    private String buildContent(int totalHabits, int completedHabits, boolean perfectDay, int currentDayStreak) {
        if (totalHabits <= 0) {
            return "Hôm nay bạn chưa có habit nào. Hãy tạo một vài thói quen nhỏ để bắt đầu nhé.";
        }

        if (perfectDay) {
            return "Bạn đã hoàn thành " + completedHabits + "/" + totalHabits
                    + " habit hôm nay. Chuỗi ngày hoàn hảo hiện tại: "
                    + currentDayStreak + " ngày.";
        }

        return "Bạn đã hoàn thành " + completedHabits + "/" + totalHabits
                + " habit hôm nay. Chuỗi ngày hoàn hảo hiện tại: "
                + currentDayStreak + " ngày.";
    }

    private void showNotification(Context context, String title, String content) {
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra(Constants.EXTRA_OPEN_DAILY_SUMMARY_POPUP, true);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                9001,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(CHANNEL_DESCRIPTION);
        notificationManager.createNotificationChannel(channel);
    }

    private boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }

        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED;
    }
}