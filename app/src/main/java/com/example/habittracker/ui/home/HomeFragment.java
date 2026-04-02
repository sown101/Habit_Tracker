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
import com.example.habittracker.data.db.AppDatabase; // Import AppDatabase của bạn

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvHabits;
    private HabitAdapter adapter;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvHabits = view.findViewById(R.id.rvHabits);
        rvHabits.setLayoutManager(new LinearLayoutManager(getContext()));

        // 1. Khởi tạo adapter với danh sách rỗng trước để tránh lỗi hiển thị lúc đang load
        adapter = new HabitAdapter(new ArrayList<>());
        rvHabits.setAdapter(adapter);

        // 2. Gọi hàm load dữ liệu thật từ Database
        loadHabitsFromDatabase();
        // Lắng nghe tín hiệu từ màn hình Thêm Thói Quen
        getParentFragmentManager().setFragmentResultListener("refresh_habits", getViewLifecycleOwner(), (requestKey, result) -> {
            // Hễ nhận được tín hiệu là tự động gọi lại hàm load Database
            loadHabitsFromDatabase();
        });

        return view;
    }

    // --- HÀM TẢI DỮ LIỆU TỪ ROOM DATABASE ---
    private void loadHabitsFromDatabase() {
        // Lấy instance của Database
        AppDatabase db = AppDatabase.getInstance(requireContext());

        // Chạy luồng ngầm để đọc dữ liệu (Không được đọc trên Main Thread)
        new Thread(() -> {
            // Lấy danh sách thói quen của userId = 1
            List<Habit> realHabits = db.habitDao().getAllActiveHabitsByUser(1);

            // Quay lại luồng chính để cập nhật giao diện
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Tạo adapter mới với dữ liệu thật và gắn lại vào RecyclerView
                    adapter = new HabitAdapter(realHabits);
                    rvHabits.setAdapter(adapter);
                });
            }
        }).start();
    }
}