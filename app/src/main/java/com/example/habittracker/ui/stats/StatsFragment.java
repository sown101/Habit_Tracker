package com.example.habittracker.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.habittracker.R;
import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.utils.DailyCompletionUtils;
import com.example.habittracker.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatsFragment extends Fragment {

    private TextView tvCurrentStreak;
    private TextView tvLongestStreak;
    private TextView tvStatsSummary;
    private BarChart barChartWeekly;

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

        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak);
        tvLongestStreak = view.findViewById(R.id.tvLongestStreak);
        tvStatsSummary = view.findViewById(R.id.tvStatsSummary);
        barChartWeekly = view.findViewById(R.id.barChartWeekly);

        loadStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        int userId = SessionManager.getUserId(requireContext());
        if (userId == -1) {
            return;
        }

        new Thread(() -> {
            int currentStreak = DailyCompletionUtils.calculateCurrentDayStreak(db, userId);
            int longestStreak = DailyCompletionUtils.calculateLongestDayStreak(db, userId, 90);

            List<BarEntry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -6);

            int perfectDays = 0;

            for (int i = 0; i < 7; i++) {
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(calendar.getTime());
                String label = new SimpleDateFormat("dd/MM", Locale.getDefault())
                        .format(calendar.getTime());

                int totalHabits = DailyCompletionUtils.getTotalHabitsForDay(db, userId);
                int completed = DailyCompletionUtils.getCompletedCountForDay(db, userId, date);

                float percent = totalHabits == 0 ? 0 : (completed * 100f / totalHabits);
                entries.add(new BarEntry(i, percent));
                labels.add(label);

                if (DailyCompletionUtils.isPerfectDay(db, userId, date)) {
                    perfectDays++;
                }

                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            String summary = "Chuỗi hiện tại được tính theo ngày hoàn hảo.\n"
                    + "Một ngày chỉ được cộng streak khi tất cả habit trong ngày đều hoàn thành.\n"
                    + "Số ngày hoàn hảo trong 7 ngày gần nhất: " + perfectDays;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvCurrentStreak.setText(String.valueOf(currentStreak));
                    tvLongestStreak.setText(String.valueOf(longestStreak));
                    tvStatsSummary.setText(summary);
                    renderBarChart(entries, labels);
                });
            }
        }).start();
    }

    private void renderBarChart(List<BarEntry> entries, List<String> labels) {
        BarDataSet dataSet = new BarDataSet(entries, "Tỷ lệ hoàn thành (%)");
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.55f);

        barChartWeekly.setData(data);
        barChartWeekly.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChartWeekly.getXAxis().setGranularity(1f);
        barChartWeekly.getXAxis().setLabelCount(labels.size());
        barChartWeekly.getAxisRight().setEnabled(false);
        barChartWeekly.getAxisLeft().setAxisMinimum(0f);
        barChartWeekly.getAxisLeft().setAxisMaximum(100f);
        barChartWeekly.getLegend().setEnabled(true);

        Description description = new Description();
        description.setText("");
        barChartWeekly.setDescription(description);

        barChartWeekly.invalidate();
    }
}