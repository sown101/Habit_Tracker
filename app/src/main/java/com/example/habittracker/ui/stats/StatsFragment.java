package com.example.habittracker.ui.stats;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.habittracker.R;
import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.data.model.HabitLog;
import com.example.habittracker.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class StatsFragment extends Fragment {

    private TextView tvTotalHabits;
    private TextView tvCompletedToday;
    private TextView tvCompletionRate;
    private TextView tvCurrentStreak;
    private TextView tvLongestStreak;
    private TextView tvProgressPercent;
    private TextView tvWeeklySummary;
    private ProgressBar progressWeekly;

    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getInstance(requireContext());

        tvTotalHabits = view.findViewById(R.id.tvTotalHabits);
        tvCompletedToday = view.findViewById(R.id.tvCompletedToday);
        tvCompletionRate = view.findViewById(R.id.tvCompletionRate);
        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak);
        tvLongestStreak = view.findViewById(R.id.tvLongestStreak);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        tvWeeklySummary = view.findViewById(R.id.tvWeeklySummary);
        progressWeekly = view.findViewById(R.id.progressWeekly);

        loadStats();
    }

    private void loadStats() {
        int userId = SessionManager.getUserId(requireContext());

        Executors.newSingleThreadExecutor().execute(() -> {
            if (userId == -1) {
                updateUi(0, 0, 0, 0, 0, "");
                return;
            }

            List<Habit> habits = db.habitDao().getAllActiveHabitsByUser(userId);
            int totalHabits = habits.size();

            String today = getDateString(0);
            int completedToday = countCompletedHabitsByDate(habits, today);

            List<String> last7Days = getLastNDays(7);
            int totalCompletedIn7Days = 0;
            StringBuilder weeklyBuilder = new StringBuilder();

            List<Boolean> completionFlags30Days = new ArrayList<>();
            List<String> last30Days = getLastNDays(30);

            for (String date : last30Days) {
                int completedCount = countCompletedHabitsByDate(habits, date);
                completionFlags30Days.add(completedCount > 0);
            }

            for (String date : last7Days) {
                int completedCount = countCompletedHabitsByDate(habits, date);
                totalCompletedIn7Days += completedCount;

                String displayDate = convertToDisplayDate(date);
                weeklyBuilder.append(displayDate)
                        .append(": ")
                        .append(completedCount)
                        .append("/")
                        .append(totalHabits)
                        .append(" habit hoàn thành");

                if (!date.equals(last7Days.get(last7Days.size() - 1))) {
                    weeklyBuilder.append("\n");
                }
            }

            int completionRate = 0;
            if (totalHabits > 0) {
                completionRate = Math.round((totalCompletedIn7Days * 100f) / (7f * totalHabits));
            }

            int currentStreak = calculateCurrentStreak(completionFlags30Days);
            int longestStreak = calculateLongestStreak(completionFlags30Days);

            updateUi(
                    totalHabits,
                    completedToday,
                    completionRate,
                    currentStreak,
                    longestStreak,
                    weeklyBuilder.toString()
            );
        });
    }

    private int countCompletedHabitsByDate(List<Habit> habits, String date) {
        int count = 0;

        for (Habit habit : habits) {
            HabitLog log = db.habitLogDao().getLogByHabitAndDate(habit.getId(), date);
            if (log != null && log.isCompleted()) {
                count++;
            }
        }

        return count;
    }

    private void updateUi(int totalHabits,
                          int completedToday,
                          int completionRate,
                          int currentStreak,
                          int longestStreak,
                          String weeklySummary) {

        if (!isAdded()) {
            return;
        }

        requireActivity().runOnUiThread(() -> {
            tvTotalHabits.setText(String.valueOf(totalHabits));
            tvCompletedToday.setText(String.valueOf(completedToday));
            tvCompletionRate.setText(completionRate + "%");
            tvCurrentStreak.setText(currentStreak + " ngày");
            tvLongestStreak.setText(longestStreak + " ngày");
            tvProgressPercent.setText(completionRate + "%");
            progressWeekly.setProgress(completionRate);

            if (TextUtils.isEmpty(weeklySummary)) {
                tvWeeklySummary.setText("Chưa có dữ liệu thống kê.");
            } else {
                tvWeeklySummary.setText(weeklySummary);
            }
        });
    }

    private List<String> getLastNDays(int numberOfDays) {
        List<String> days = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -(numberOfDays - 1));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < numberOfDays; i++) {
            days.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return days;
    }

    private String getDateString(int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, offset);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private String convertToDisplayDate(String dbDate) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd/MM", Locale.getDefault());
            return output.format(input.parse(dbDate));
        } catch (Exception e) {
            return dbDate;
        }
    }

    private int calculateCurrentStreak(List<Boolean> flags) {
        int streak = 0;

        for (int i = flags.size() - 1; i >= 0; i--) {
            if (flags.get(i)) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }

    private int calculateLongestStreak(List<Boolean> flags) {
        int longest = 0;
        int current = 0;

        for (Boolean flag : flags) {
            if (flag) {
                current++;
                if (current > longest) {
                    longest = current;
                }
            } else {
                current = 0;
            }
        }

        return longest;
    }
}