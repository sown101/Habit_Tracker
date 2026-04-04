package com.example.habittracker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.data.model.Reminder;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * Đặt lại toàn bộ alarm sau khi máy khởi động lại.
 *
 * Vấn đề: AlarmManager bị xóa sạch mỗi khi tắt máy.
 * Giải pháp: lắng nghe BOOT_COMPLETED → đặt lại tất cả reminder đang bật.
 *
 * Khai báo trong AndroidManifest.xml:
 *   <receiver android:name=".notifications.BootReceiver" android:exported="true">
 *       <intent-filter>
 *           <action android:name="android.intent.action.BOOT_COMPLETED"/>
 *       </intent-filter>
 *   </receiver>
 *
 * Permission cần thêm:
 *   <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
 *
 * Yêu cầu team thêm vào ReminderDao.java:
 *   @Query("SELECT * FROM reminders WHERE is_enabled = 1")
 *   List<Reminder> getAllEnabledReminders();
 *
 * Thanh — phần Receiver
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        rescheduleAllReminders(context);
    }

    /**
     * Query tất cả reminder đang bật rồi đặt lại alarm.
     * Dùng getter vì Habit dùng private fields.
     */
    private void rescheduleAllReminders(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);

        Executors.newSingleThreadExecutor().execute(() -> {
            // Cần team thêm hàm getAllEnabledReminders() vào ReminderDao
            List<Reminder> reminders = db.reminderDao().getAllEnabledReminders();

            for (Reminder reminder : reminders) {
                // Dùng getter vì Habit là private fields
                Habit habit = db.habitDao().getHabitById(reminder.getHabitId());
                if (habit == null) continue;

                // remindTime lưu dạng "07:30" — split để lấy giờ và phút
                String[] parts = reminder.getRemindTime().split(":");
                if (parts.length != 2) continue;

                int hour   = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);

                NotificationScheduler.scheduleReminder(
                        context,
                        habit.getId(),
                        habit.getTitle(),
                        hour,
                        minute,
                        reminder.getRequestCode()
                );
            }
        });
    }
}
