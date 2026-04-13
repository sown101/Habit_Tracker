package com.example.habittracker.ui.home;

import android.os.Bundle;
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
import com.example.habittracker.ui.timer.TimerHabitDialogFragment;
import com.example.habittracker.utils.Constants;
import com.example.habittracker.utils.DailyCompletionUtils;
import com.example.habittracker.utils.SessionManager;
import com.example.habittracker.utils.SettingsManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private RecyclerView rvHabits;
    private HabitAdapter adapter;
    private AppDatabase db;

    private TextView txtProgressRatio;
    private TextView tvStreakCount;
    private TextView txtGreeting;

    private boolean shouldShowDailySummaryPopup = false;
    private boolean popupAlreadyShown = false;

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
        tvStreakCount = view.findViewById(R.id.tvStreakCount);
        txtGreeting = view.findViewById(R.id.txtGreeting);

        if (getArguments() != null) {
            shouldShowDailySummaryPopup =
                    getArguments().getBoolean(Constants.ARG_SHOW_DAILY_SUMMARY_POPUP, false);
        }

        updateGreeting();

        db = AppDatabase.getInstance(requireContext());

        rvHabits.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new HabitAdapter(
                null,
                this::handleHabitCheckedChanged,
                new HabitAdapter.OnCounterActionListener() {
                    @Override
                    public void onCounterPlus(Habit habit, int position) {
                        updateCounterHabit(habit, position, 1, false);
                    }

                    @Override
                    public void onCounterMinus(Habit habit, int position) {
                        updateCounterHabit(habit, position, 0, true);
                    }
                },
                (habit, position) -> openTimerHabit(habit)
        );

        rvHabits.setAdapter(adapter);

        getParentFragmentManager().setFragmentResultListener(
                "refresh_habits",
                getViewLifecycleOwner(),
                (requestKey, result) -> loadHabitsFromDatabase()
        );

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateGreeting();
        loadHabitsFromDatabase();
    }

    private void updateGreeting() {
        String displayName = SettingsManager.getDisplayName(requireContext());

        if (displayName != null && !displayName.trim().isEmpty()) {
            txtGreeting.setText("Hello,\n" + displayName.trim() + "!");
        } else {
            txtGreeting.setText("Hello,\nFriend!");
        }
    }

    private void openTimerHabit(Habit habit) {
        if (habit == null || !habit.isTimerHabit()) {
            return;
        }

        TimerHabitDialogFragment dialog =
                TimerHabitDialogFragment.newInstance(habit.getId());
        dialog.show(getParentFragmentManager(), "timer_habit_dialog");
    }

    private void loadHabitsFromDatabase() {
        if (!isAdded()) {
            return;
        }

        int userId = SessionManager.getUserId(requireContext());
        if (userId == -1) {
            Toast.makeText(requireContext(), "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
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

                int streak = DailyCompletionUtils.calculateHabitCurrentStreak(db, habit.getId());
                habit.setCurrentStreak(streak);

                if (completedToday) {
                    completedCount++;
                }
            }

            int dayStreak = DailyCompletionUtils.calculateCurrentDayStreak(db, userId);
            int finalCompletedCount = completedCount;
            int finalDayStreak = dayStreak;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.updateData(habits);
                    updateProgressText(finalCompletedCount, habits.size());
                    tvStreakCount.setText(String.valueOf(finalDayStreak));

                    if (shouldShowDailySummaryPopup && !popupAlreadyShown) {
                        popupAlreadyShown = true;
                        shouldShowDailySummaryPopup = false;
                        showDailySummaryPopup(habits, finalCompletedCount, finalDayStreak);
                    }
                });
            }
        }).start();
    }

    private void showDailySummaryPopup(List<Habit> habits, int completedCount, int dayStreak) {
        if (!isAdded()) {
            return;
        }

        ArrayList<String> completedHabitLines = new ArrayList<>();

        for (Habit habit : habits) {
            if (habit.isCompletedToday()) {
                String emoji = habit.getIconEmoji() == null || habit.getIconEmoji().trim().isEmpty()
                        ? "✅"
                        : habit.getIconEmoji();

                completedHabitLines.add(
                        emoji + " " + habit.getTitle()
                                + "  •  streak " + habit.getCurrentStreak() + " ngày"
                );
            }
        }

        DailySummaryDialogFragment dialog = DailySummaryDialogFragment.newInstance(
                completedCount,
                habits.size(),
                dayStreak,
                completedHabitLines
        );

        dialog.show(getParentFragmentManager(), "daily_summary_dialog");
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

            if (existingLog == null) {
                HabitLog newLog = new HabitLog(
                        habit.getId(),
                        today,
                        newCurrentValue,
                        targetValue,
                        isChecked,
                        isChecked ? now : null,
                        null,
                        Constants.COMPLETION_METHOD_MANUAL
                );
                db.habitLogDao().insert(newLog);
            } else {
                existingLog.setCurrentValue(newCurrentValue);
                existingLog.setTargetValue(targetValue);
                existingLog.setCompleted(isChecked);
                existingLog.setCompletedAt(isChecked ? now : null);
                existingLog.setCompletionMethod(Constants.COMPLETION_METHOD_MANUAL);
                db.habitLogDao().update(existingLog);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    loadHabitsFromDatabase();
                    Toast.makeText(
                            requireContext(),
                            isChecked ? "Đã đánh dấu hoàn thành" : "Đã bỏ đánh dấu",
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }
        }).start();
    }

    private void updateCounterHabit(Habit habit, int position, int delta, boolean resetToZero) {
        if (habit == null || position == RecyclerView.NO_POSITION) {
            return;
        }

        new Thread(() -> {
            String today = getTodayDate();
            String now = getCurrentDateTime();

            HabitLog existingLog = db.habitLogDao().getLogByHabitAndDate(habit.getId(), today);

            int targetValue = habit.getSafeTargetValue();
            int currentValue = existingLog != null ? existingLog.getCurrentValue() : 0;

            int newValue;
            if (resetToZero) {
                newValue = 0;
            } else {
                newValue = Math.min(targetValue, currentValue + delta);
            }

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
                existingLog.setCompletionMethod(Constants.COMPLETION_METHOD_MANUAL);
                db.habitLogDao().update(existingLog);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    loadHabitsFromDatabase();

                    String message;
                    if (resetToZero) {
                        message = "Đã đặt lại về 0";
                    } else if (isCompleted) {
                        message = "Đã đạt mục tiêu";
                    } else {
                        message = "+1 " + habit.getDisplayUnit();
                    }

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateProgressText(int completedCount, int totalCount) {
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
}