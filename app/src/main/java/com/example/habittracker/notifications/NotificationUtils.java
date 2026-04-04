package com.example.habittracker.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.habittracker.R;
import com.example.habittracker.ui.main.MainActivity;

/**
 * Lớp tiện ích tạo và quản lý notification.
 * Các lớp khác chỉ cần gọi hàm ở đây, không cần biết chi tiết.
 *
 * Lưu ý: gọi createNotificationChannels() trong MainActivity.onCreate() trước khi dùng.
 *
 * Thanh — phần Notifications
 */
public class NotificationUtils {

    // ID channel — Android 8.0+ bắt buộc phải có channel
    public static final String CHANNEL_ID_REMINDER = "habit_reminder_channel";
    public static final String CHANNEL_ID_SUMMARY  = "habit_summary_channel";

    // Action — phân biệt user bấm nút nào trên notification
    public static final String ACTION_COMPLETE = "ACTION_COMPLETE_HABIT";
    public static final String ACTION_SNOOZE   = "ACTION_SNOOZE_HABIT";

    // Key để đính kèm data vào Intent
    public static final String EXTRA_HABIT_ID    = "extra_habit_id";
    public static final String EXTRA_HABIT_TITLE = "extra_habit_title";
    public static final String EXTRA_NOTIF_ID    = "extra_notif_id";

    /**
     * Tạo notification channel — gọi 1 lần trong MainActivity.onCreate().
     * Android tự bỏ qua nếu channel đã tồn tại, không sao cả.
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Channel nhắc habit — IMPORTANCE_HIGH để có âm thanh + popup
            NotificationChannel reminderCh = new NotificationChannel(
                    CHANNEL_ID_REMINDER,
                    "Nhắc nhở habit",
                    NotificationManager.IMPORTANCE_HIGH
            );
            reminderCh.setDescription("Nhắc bạn thực hiện thói quen hàng ngày");
            manager.createNotificationChannel(reminderCh);

            // Channel tổng kết cuối ngày — không cần popup
            NotificationChannel summaryCh = new NotificationChannel(
                    CHANNEL_ID_SUMMARY,
                    "Tổng kết hàng ngày",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            summaryCh.setDescription("Điểm lại kết quả cuối ngày");
            manager.createNotificationChannel(summaryCh);
        }
    }

    /**
     * Hiển thị notification nhắc habit, kèm nút "✅ Hoàn thành".
     *
     * @param context     context
     * @param habitId     ID habit trong DB
     * @param habitTitle  tên habit hiện trên notification
     * @param notifId     ID notification — phải unique để không ghi đè nhau
     */
    public static void showReminderNotification(Context context,
                                                int habitId,
                                                String habitTitle,
                                                int notifId) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Intent cho nút "Hoàn thành" — NotificationActionReceiver xử lý
        Intent completeIntent = new Intent(context, NotificationActionReceiver.class);
        completeIntent.setAction(ACTION_COMPLETE);
        completeIntent.putExtra(EXTRA_HABIT_ID, habitId);
        completeIntent.putExtra(EXTRA_NOTIF_ID, notifId);

        PendingIntent completePi = PendingIntent.getBroadcast(
                context,
                habitId * 10, // requestCode unique
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent mở app khi bấm vào thân notification
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra(EXTRA_HABIT_ID, habitId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent openPi = PendingIntent.getActivity(
                context,
                habitId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
                        .setSmallIcon(R.drawable.ic_notification) // nhớ tạo file drawable này
                        .setContentTitle("⏰ Đến giờ rồi!")
                        .setContentText("Đừng quên: " + habitTitle)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Bạn chưa làm: " + habitTitle
                                        + "\nBấm hoàn thành nếu đã làm xong!"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(openPi)
                        .addAction(0, "✅ Hoàn thành", completePi);

        manager.notify(notifId, builder.build());
    }

    /** Đóng notification theo ID */
    public static void cancelNotification(Context context, int notifId) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notifId);
    }
}
