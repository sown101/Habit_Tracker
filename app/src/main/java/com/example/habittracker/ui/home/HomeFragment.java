package com.example.habittracker.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;
import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.data.model.HabitLog;
import com.example.habittracker.ui.adapter.HabitAdapter;
import com.example.habittracker.utils.Constants;
import com.example.habittracker.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private RecyclerView rvHabits;
    private HabitAdapter adapter;
    private AppDatabase db;

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvHabits = view.findViewById(R.id.rvHabits);
        rvHabits.setLayoutManager(new LinearLayoutManager(requireContext()));

        db = AppDatabase.getInstance(requireContext());

        adapter = new HabitAdapter(new ArrayList<>(), this::handleHabitCheckedChanged);
        rvHabits.setAdapter(adapter);

        getParentFragmentManager().setFragmentResultListener(
                "refresh_habits",
                getViewLifecycleOwner(),
                (requestKey, result) -> loadHabitsFromDatabase()
        );

        loadHabitsFromDatabase();

        return view;
    }

    private void loadHabitsFromDatabase() {
        int userId = SessionManager.getUserId(requireContext());

        if (userId == -1) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "Không tìm thấy phiên đăng nhập", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        new Thread(() -> {
            List<Habit> habits = db.habitDao().getAllActiveHabitsByUser(userId);
            String today = getTodayDate();

            for (Habit habit : habits) {
                HabitLog todayLog = db.habitLogDao().getLogByHabitAndDate(habit.getId(), today);
                boolean completedToday = todayLog != null && todayLog.isCompleted();
                habit.setCompletedToday(completedToday);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.updateData(habits));
            }
        }).start();
    }

    private void handleHabitCheckedChanged(Habit habit, boolean isChecked, int position) {
        if (habit == null || position == RecyclerView.NO_POSITION) {
            return;
        }

        new Thread(() -> {
            String today = getTodayDate();
            String now = getCurrentDateTime();
            HabitLog existingLog = db.habitLogDao().getLogByHabitAndDate(habit.getId(), today);

            if (existingLog == null) {
                HabitLog newLog = new HabitLog(
                        habit.getId(),
                        today,
                        isChecked ? habit.getTargetValue() : 0,
                        habit.getTargetValue(),
                        isChecked,
                        isChecked ? now : null,
                        null,
                        Constants.COMPLETION_METHOD_MANUAL
                );
                db.habitLogDao().insert(newLog);
            } else {
                existingLog.setCurrentValue(isChecked ? habit.getTargetValue() : 0);
                existingLog.setTargetValue(habit.getTargetValue());
                existingLog.setCompleted(isChecked);
                existingLog.setCompletedAt(isChecked ? now : null);

                if (TextUtils.isEmpty(existingLog.getCompletionMethod()) || isChecked) {
                    existingLog.setCompletionMethod(Constants.COMPLETION_METHOD_MANUAL);
                }

                db.habitLogDao().update(existingLog);
            }

            habit.setCompletedToday(isChecked);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.updateHabitCheckedState(position, isChecked);

                    String message = isChecked
                            ? "Đã đánh dấu hoàn thành"
                            : "Đã bỏ đánh dấu hoàn thành";
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHabitsFromDatabase();
    }
}