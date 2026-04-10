package com.example.habittracker.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.habittracker.R;
import com.example.habittracker.ui.home.AddHabit;
import com.example.habittracker.ui.home.HomeFragment;
import com.example.habittracker.ui.settings.SettingsFragment;
import com.example.habittracker.ui.stats.StatsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.habittracker.ui.calendar.CalendarFragment;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 101;

    private ImageView navHome;
    private ImageView navStats;
    private ImageView navSettings;
    private FloatingActionButton navAdd;
    private ImageView navCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askNotificationPermission();
        com.example.habittracker.utils.NotificationUtils.createNotificationChannels(this);

        navHome = findViewById(R.id.nav_home);
        navStats = findViewById(R.id.nav_stats);
        navSettings = findViewById(R.id.nav_settings);
        navAdd = findViewById(R.id.nav_add);
        navCalendar = findViewById(R.id.nav_calendar);

        if (savedInstanceState == null) {
            openHomeFragment();
        }

        navHome.setOnClickListener(v -> openHomeFragment());

        navStats.setOnClickListener(v -> openStatsFragment());

        navCalendar.setOnClickListener(v -> openCalendarFragment());

        navSettings.setOnClickListener(v -> openSettingsFragment());

        navAdd.setOnClickListener(v -> {
            AddHabit bottomSheet = new AddHabit();
            bottomSheet.show(getSupportFragmentManager(), "AddHabitBottomSheet");
        });
    }

    private void openHomeFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    private void openStatsFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new StatsFragment())
                .commit();
    }

    private void openCalendarFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CalendarFragment())
                .commit();
    }

    private void openSettingsFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .commit();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền thông báo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. Cannot show notifications.", Toast.LENGTH_LONG).show();
            }
        }
    }
}