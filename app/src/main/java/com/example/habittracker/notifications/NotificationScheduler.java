package com.example.habittracker.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

/**
 * Dùng AlarmManager để đặt/hủy lịch nhắc habit.
 * Giống cái đồng hồ báo thức — đến giờ thì Android tự gọi ReminderReceiver.
 *
 * Cách dùng từ chỗ khác (ví dụ sau khi lưu habit):
 *   NotificationScheduler.scheduleReminder(context, habit.getId(), habit.getTitle(), 7, 30, habit.getId());
 *
 * Thanh — phần Notifications
 */
public class NotificationScheduler {

    /**
     * Đặt lịch nhắc habit, lặp lại mỗi ngày vào đúng giờ.
     *
     * @param context     context
     * @param habitId     ID habit
     * @param habitTitle  tên habit (gửi qua Intent để Receiver biết hiện gì)
     * @param hour        giờ nhắc (0–23)
     * @param minute      phút nhắc (0–59)
     * @param requestCode mã duy nhất — dùng habitId cho tiện
     */
    public static void scheduleReminder(Context context,
                                        int habitId,
                                        String habitTitle,
                                        int hour,
                                        int minute,
                                        int requestCode) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(NotificationUtils.EXTRA_HABIT_ID, habitId);
        intent.putExtra(NotificationUtils.EXTRA_HABIT_TITLE, habitTitle);
        intent.putExtra(NotificationUtils.EXTRA_NOTIF_ID, requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Tính thời điểm alarm hôm nay lúc hour:minute
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Nếu giờ đó đã qua hôm nay rồi → đặt cho ngày mai
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Android 12+ cần kiểm tra quyền trước khi đặt exact alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        cal.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                );
            }
        } else {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    /**
     * Hủy alarm đã đặt.
     * requestCode phải giống hệt lúc scheduleReminder(), nếu không hủy không được.
     */
    public static void cancelReminder(Context context,
                                      int requestCode,
                                      int habitId,
                                      String habitTitle) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(NotificationUtils.EXTRA_HABIT_ID, habitId);
        intent.putExtra(NotificationUtils.EXTRA_HABIT_TITLE, habitTitle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }
}
