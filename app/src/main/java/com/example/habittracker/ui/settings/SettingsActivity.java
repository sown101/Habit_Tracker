package com.example.habittracker.ui.settings;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import com.example.habittracker.R;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchNotifications;
    private TextView tvReminderTime;
    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "HabitTrackerPrefs";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_REMINDER_TIME = "reminder_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //các thành phần giao diện
        switchNotifications = findViewById(R.id.switchNotifications);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        //tải dữ liệu đã lưu
        loadSettings();

        //xử lý công tắc thông báo
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
        });

        //xử lý chọn giờ nhắc nhở
        findViewById(R.id.btnSetTime).setOnClickListener(v -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);

            TimePickerDialog mTimePicker = new TimePickerDialog(SettingsActivity.this, (view, hourOfDay, min) -> {
                String time = String.format("%02d:%02d", hourOfDay, min);
                tvReminderTime.setText(time);
                sharedPreferences.edit().putString(KEY_REMINDER_TIME, time).apply();
            }, hour, minute, true);
            mTimePicker.setTitle("Chọn giờ nhắc nhở");
            mTimePicker.show();
        });

        //xử lý xóa dữ liệu
        findViewById(R.id.btnResetData).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Tất cả thói quen và lịch sử sẽ bị xóa vĩnh viễn. Bạn có chắc không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // Logic xóa dữ liệu thực tế sẽ viết ở đây
                        Toast.makeText(this, "Đã xóa toàn bộ dữ liệu", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void loadSettings() {
        boolean isEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS, false);
        switchNotifications.setChecked(isEnabled);

        String savedTime = sharedPreferences.getString(KEY_REMINDER_TIME, "20:00");
        tvReminderTime.setText(savedTime);
    }
}