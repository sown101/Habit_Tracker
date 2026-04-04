package com.example.habittracker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.HabitLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * Xử lý khi user bấm nút trực tiếp trên notification (ví dụ "✅ Hoàn thành").
 *
 * Flow:
 *   User bấm "✅ Hoàn thành" trên notification
 *   → broadcast gửi đến đây với action = ACTION_COMPLETE
 *   → cập nhật HabitLog trong DB → đóng notification
 *
 * Khai báo trong AndroidManifest.xml:
 *   <receiver android:name=".notifications.NotificationActionReceiver" android:exported="false"/>
 *
 * Thanh — phần Receiver
 */
public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int habitId   = intent.getIntExtra(NotificationUtils.EXTRA_HABIT_ID, -1);
        int notifId   = intent.getIntExtra(NotificationUtils.EXTRA_NOTIF_ID, -1);

        if (action == null || habitId == -1) return;

        if (NotificationUtils.ACTION_COMPLETE.equals(action)) {
            markHabitComplete(context, habitId);

            if (notifId != -1) {
                NotificationUtils.cancelNotification(context, notifId);
            }
        }
    }

    /**
     * Đánh dấu habit hoàn thành hôm nay.
     *
     * Lưu ý quan trọng:
     *   - HabitLog của team dùng getter/setter, không truy cập field trực tiếp
     *   - isCompleted() trả về boolean, không phải int
     *   - HabitLog không có no-arg constructor — phải dùng constructor đầy đủ tham số
     */
    private void markHabitComplete(Context context, int habitId) {
        String today   = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        AppDatabase db = AppDatabase.getInstance(context);

        Executors.newSingleThreadExecutor().execute(() -> {
            HabitLog existingLog = db.habitLogDao().getLogByHabitAndDate(habitId, today);

            if (existingLog != null) {
                // Log đã có rồi, chỉ cần update trạng thái
                // Dùng setter vì HabitLog dùng private field
                existingLog.setCompleted(true);
                existingLog.setCompletedAt(today);
                existingLog.setCompletionMethod("notification");
                db.habitLogDao().update(existingLog);
            } else {
                // Chưa có log hôm nay — tạo mới bằng constructor đầy đủ tham số
                // (HabitLog không có no-arg constructor nên không thể new HabitLog())
                HabitLog newLog = new HabitLog(
                        habitId,        // int habitId
                        today,          // String logDate
                        1,              // int currentValue
                        1,              // int targetValue
                        true,           // boolean isCompleted
                        today,          // String completedAt
                        null,           // String note
                        "notification"  // String completionMethod
                );
                db.habitLogDao().insert(newLog);
            }
        });
    }
}
