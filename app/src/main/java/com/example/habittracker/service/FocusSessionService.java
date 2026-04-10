package com.example.habittracker.service;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.data.model.HabitLog;
import com.example.habittracker.utils.Constants;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.habittracker.R;
import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.FocusSession;
import com.example.habittracker.ui.main.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FocusSessionService extends Service {

    public static final String ACTION_START = "com.example.habittracker.action.START_FOCUS";
    public static final String ACTION_STOP = "com.example.habittracker.action.STOP_FOCUS";
    public static final String ACTION_COMPLETE = "com.example.habittracker.action.COMPLETE_FOCUS";

    public static final String EXTRA_HABIT_ID = "extra_habit_id";
    public static final String EXTRA_HABIT_TITLE = "extra_habit_title";
    public static final String EXTRA_DURATION_MINUTES = "extra_duration_minutes";

    private static final String CHANNEL_ID = "focus_session_channel";
    private static final int NOTIFICATION_ID = 3001;

    private CountDownTimer countDownTimer;

    private int currentHabitId = -1;
    private String currentHabitTitle = "";
    private int currentDurationMinutes = 25;
    private long sessionStartMillis = 0L;
    private int currentFocusSessionId = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        createFocusNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_NOT_STICKY;
        }

        String action = intent.getAction();

        if (ACTION_START.equals(action)) {
            currentHabitId = intent.getIntExtra(EXTRA_HABIT_ID, -1);
            currentHabitTitle = intent.getStringExtra(EXTRA_HABIT_TITLE);
            currentDurationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 25);

            if (currentHabitId == -1 || currentHabitTitle == null || currentHabitTitle.trim().isEmpty()) {
                stopSelf();
                return START_NOT_STICKY;
            }

            startFocusSession();

        } else if (ACTION_STOP.equals(action)) {
            stopFocusSession("STOPPED");

        } else if (ACTION_COMPLETE.equals(action)) {
            stopFocusSession("COMPLETED");
        }

        return START_STICKY;
    }

    private void startFocusSession() {
        sessionStartMillis = System.currentTimeMillis();

        startForeground(NOTIFICATION_ID, buildNotification(currentDurationMinutes * 60L * 1000L));

        saveSessionStart();

        long totalMillis = currentDurationMinutes * 60L * 1000L;

        countDownTimer = new CountDownTimer(totalMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Notification updatedNotification = buildNotification(millisUntilFinished);
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (manager != null) {
                    manager.notify(NOTIFICATION_ID, updatedNotification);
                }
            }

            @Override
            public void onFinish() {
                stopFocusSession("COMPLETED");
            }
        };

        countDownTimer.start();
    }

    private Notification buildNotification(long millisRemaining) {
        long totalSeconds = millisRemaining / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        String timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent openPendingIntent = PendingIntent.getActivity(
                this,
                1,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent stopIntent = new Intent(this, FocusSessionService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this,
                2,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent completeIntent = new Intent(this, FocusSessionService.class);
        completeIntent.setAction(ACTION_COMPLETE);
        PendingIntent completePendingIntent = PendingIntent.getService(
                this,
                3,
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Focus Session đang chạy")
                .setContentText(currentHabitTitle + " • Còn lại " + timeText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Habit: " + currentHabitTitle + "\nThời gian còn lại: " + timeText))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(openPendingIntent)
                .addAction(0, "Dừng", stopPendingIntent)
                .addAction(0, "Hoàn thành", completePendingIntent)
                .build();
    }

    private void createFocusNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Focus Session",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Thông báo cho phiên tập trung");

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private void saveSessionStart() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());

            FocusSession focusSession = new FocusSession(
                    currentHabitId,
                    getCurrentDateTime(),
                    null,
                    currentDurationMinutes,
                    0,
                    "RUNNING",
                    getCurrentDateTime()
            );

            long insertedId = db.focusSessionDao().insert(focusSession);
            currentFocusSessionId = (int) insertedId;
        }).start();
    }

    private void stopFocusSession(String status) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        long elapsedMillis = System.currentTimeMillis() - sessionStartMillis;
        int actualMinutes = (int) (elapsedMillis / (60 * 1000));

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());

            // 1. Update bảng focus_sessions
            if (currentFocusSessionId != -1) {
                FocusSession existing = db.focusSessionDao().getById(currentFocusSessionId);
                if (existing != null) {
                    existing.setEndTime(getCurrentDateTime());
                    existing.setActualDurationMinutes(actualMinutes);
                    existing.setStatus(status);
                    db.focusSessionDao().update(existing);
                }
            }

            // 2. Nếu người dùng bấm "Hoàn thành" thì cập nhật luôn HabitLog hôm nay
            if ("COMPLETED".equals(status) && currentHabitId != -1) {
                Habit habit = db.habitDao().getHabitById(currentHabitId);
                if (habit != null) {
                    String today = getTodayDate();

                    HabitLog existingLog = db.habitLogDao().getLogByHabitAndDate(currentHabitId, today);

                    if (existingLog == null) {
                        HabitLog newLog = new HabitLog(
                                currentHabitId,
                                today,
                                habit.getTargetValue(),
                                habit.getTargetValue(),
                                true,
                                getCurrentDateTime(),
                                "Hoàn thành bằng Focus Session",
                                Constants.COMPLETION_METHOD_SESSION
                        );
                        db.habitLogDao().insert(newLog);
                    } else {
                        existingLog.setCurrentValue(habit.getTargetValue());
                        existingLog.setTargetValue(habit.getTargetValue());
                        existingLog.setCompleted(true);
                        existingLog.setCompletedAt(getCurrentDateTime());
                        existingLog.setCompletionMethod(Constants.COMPLETION_METHOD_SESSION);
                        db.habitLogDao().update(existingLog);
                    }
                }
            }
        }).start();

        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}