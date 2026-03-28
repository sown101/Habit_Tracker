package com.example.habittracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.ui.adapter.HabitAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView rvHabits = view.findViewById(R.id.rvHabits);
        rvHabits.setLayoutManager(new LinearLayoutManager(getContext()));

        // --- TẠO DỮ LIỆU GIẢ VỚI CONSTRUCTOR  ---
        List<Habit> dummyHabits = new ArrayList<>();

        // Thói quen 1: Học tiếng Anh
        Habit h1 = new Habit(1, "Học tiếng anh", "Học IELTS", "Học tập", "Regular", 1, "tiếng", "Daily", false, "", false, false, 0, true, "2026-03-27", "2026-03-27");
        h1.setCompletedToday(true); // Đánh dấu đã xong
        dummyHabits.add(h1);

        // Thói quen 2: Tập thể dục
        Habit h2 = new Habit(1, "Tập thể dục", "Chạy bộ", "Thể thao", "Regular", 20, "phút", "Daily", true, "06:00", false, false, 0, true, "2026-03-27", "2026-03-27");
        h2.setCompletedToday(false);
        dummyHabits.add(h2);

        // Thói quen 3: Đọc sách
        Habit h3 = new Habit(1, "Đọc sách", "Sách kinh tế", "Khác", "Regular", 15, "phút", "Daily", false, "", false, true, 15, true, "2026-03-27", "2026-03-27");
        h3.setCompletedToday(false);
        dummyHabits.add(h3);

        // --- GẮN ADAPTER ---
        HabitAdapter adapter = new HabitAdapter(dummyHabits);
        rvHabits.setAdapter(adapter);

        return view;
    }
}