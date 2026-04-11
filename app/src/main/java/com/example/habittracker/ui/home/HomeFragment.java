package com.example.habittracker.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private RecyclerView rvHabits;
    private HabitAdapter adapter;
    private AppDatabase db;
    private TextView txtProgressRatio;

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvHabits = view.findViewById(R.id.rvHabits);
        txtProgressRatio = view.findViewById(R.id.txtProgressRatio);

        rvHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        db = AppDatabase.getInstance(requireContext());

        adapter = new HabitAdapter(
                null,
                this::handleHabitCheckedChanged,
                new HabitAdapter.OnCounterActionListener() {
                    @Override
                    public void onCounterPlus(Habit habit, int position) {
                        updateCounterHabit(habit, position, +1);
                    }

                    @Override
                    public void onCounterMinus(Habit habit, int position) {
                        updateCounterHabit(habit, position, -1);
                    }
                }
        );

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

            int completedCount = 0;

            for (Habit habit : habits) {
                HabitLog todayLog = db.habitLogDao().getLogByHabitAndDate(habit.getId(), today);

                int currentValue = todayLog != null ? todayLog.getCurrentValue() : 0;
                boolean completedToday;

                if (habit.isCounterHabit()) {
                    completedToday = currentValue >= habit.getSafeTargetValue();
                } else {
                    completedToday = todayLog != null && todayLog.isCompleted();
                }

                habit.setCurrentValueToday(currentValue);
                habit.setCompletedToday(completedToday);

                if (completedToday) {
                    completedCount++;
                }
            }

            int finalCompletedCount = completedCount;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.updateData(habits);
                    updateProgressText(finalCompletedCount, habits.size());
                });
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

            int targetValue = habit.getSafeTargetValue();
            int newCurrentValue = isChecked ? targetValue : 0;
            boolean newCompletedState = isChecked;

            if (existingLog == null) {
                HabitLog newLog = new HabitLog(
                        habit.getId(),
                        today,
                        newCurrentValue,
                        targetValue,
                        newCompletedState,
                        isChecked ? now : null,
                        null,
                        Constants.COMPLETION_METHOD_MANUAL
                );
                db.habitLogDao().insert(newLog);
            } else {
                existingLog.setCurrentValue(newCurrentValue);
                existingLog.setTargetValue(targetValue);
                existingLog.setCompleted(newCompletedState);
                existingLog.setCompletedAt(isChecked ? now : null);

                if (TextUtils.isEmpty(existingLog.getCompletionMethod()) || isChecked) {
                    existingLog.setCompletionMethod(Constants.COMPLETION_METHOD_MANUAL);
                }

                db.habitLogDao().update(existingLog);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    loadHabitsFromDatabase();
                    Toast.makeText(
                            requireContext(),
                            isChecked ? "Đã đánh dấu hoàn thành" : "Đã bỏ đánh dấu hoàn thành",
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }
        }).start();
    }

    private void updateCounterHabit(Habit habit, int position, int delta) {
        if (habit == null || position == RecyclerView.NO_POSITION) {
            return;
        }

        new Thread(() -> {
            String today = getTodayDate();
            String now = getCurrentDateTime();

            HabitLog existingLog = db.habitLogDao().getLogByHabitAndDate(habit.getId(), today);

            int targetValue = habit.getSafeTargetValue();
            int currentValue = existingLog != null ? existingLog.getCurrentValue() : 0;
            int newValue = Math.max(0, currentValue + delta);
            boolean isCompleted = newValue >= targetValue;

            if (existingLog == null) {
                HabitLog newLog = new HabitLog(
                        habit.getId(),
                        today,
                        newValue,
                        targetValue,
                        isCompleted,
                        isCompleted ? now : null,
                        null,
                        Constants.COMPLETION_METHOD_MANUAL
                );
                db.habitLogDao().insert(newLog);
            } else {
                existingLog.setCurrentValue(newValue);
                existingLog.setTargetValue(targetValue);
                existingLog.setCompleted(isCompleted);
                existingLog.setCompletedAt(isCompleted ? now : null);

                if (TextUtils.isEmpty(existingLog.getCompletionMethod()) || delta > 0) {
                    existingLog.setCompletionMethod(Constants.COMPLETION_METHOD_MANUAL);
                }

                db.habitLogDao().update(existingLog);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    loadHabitsFromDatabase();

                    String unit = habit.getDisplayUnit();
                    String actionText = delta > 0 ? "+1 " + unit : "-1 " + unit;

                    Toast.makeText(requireContext(), actionText, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateProgressText(int completedCount, int totalCount) {
        if (txtProgressRatio == null) {
            return;
        }

        if (totalCount <= 0) {
            txtProgressRatio.setText("Hôm nay chưa có thói quen nào");
            return;
        }

        txtProgressRatio.setText(completedCount + " trên " + totalCount + " đã hoàn thành");
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