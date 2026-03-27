package com.example.habittracker.ui.main;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.habittracker.R;
import com.example.habittracker.ui.home.HomeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Nạp HomeFragment vào fragment_container ngay khi mở app
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // 2. Bắt sự kiện cho nút Dấu Cộng (Add Habit)
        FloatingActionButton fabAdd = findViewById(R.id.nav_add);
        fabAdd.setOnClickListener(v -> {
            // Sau này chúng ta sẽ viết code mở màn hình "Thêm kế hoạch" ở đây
            // Tạm thời để trống nhé!
        });

        // Bạn có thể tự thêm bắt sự kiện cho nav_home, nav_calendar... tương tự nhé
    }
}
