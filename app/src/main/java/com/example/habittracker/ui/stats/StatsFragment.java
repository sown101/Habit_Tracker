package com.example.habittracker.ui.stats;

import android.graphics.Color;
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
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.data.model.HabitLog;
import com.example.habittracker.utils.SessionManager;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * Màn hình thống kê — biểu đồ 7 ngày gần nhất + các chỉ số tổng kết.
 *
 * Cần thêm dependency MPAndroidChart vào build.gradle.kts:
 *   implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
 * Và trong settings.gradle.kts (phần repositories):
 *   maven { url = uri("https://jitpack.io") }
 *
 * Thanh — phần Stats
 */
public class StatsFragment extends Fragment {

    // --- Views ---
    private BarChart barChart;
    private TextView tvCurrentStreak;
    private TextView tvCompletionRate;
    private TextView tvTotalHabits;
    private TextView tvBestDay;

    // --- Database ---
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

        // SessionManager của team là static — gọi thẳng, không cần new
        db = AppDatabase.getInstance(requireContext());

        barChart         = view.findViewById(R.id.barChart);
        tvCurrentStreak  = view.findViewById(R.id.tvCurrentStreak);
        tvCompletionRate = view.findViewById(R.id.tvCompletionRate);
        tvTotalHabits    = view.findViewById(R.id.tvTotalHabits);
        tvBestDay        = view.findViewById(R.id.tvBestDay);

        setupBarChart();
        loadStatsData();
    }

    /**
     * Cấu hình giao diện biểu đồ — gọi 1 lần trước khi đổ data vào.
     * Tắt những thứ mặc định xấu (description, legend, zoom),
     * chỉnh trục Y từ 0–100 vì đơn vị là %.
     */
    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);

        // Trục Y trái: 0% đến 100%, hiện 5 mốc
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setAxisMaximum(100f);
        barChart.getAxisLeft().setLabelCount(5);

        // Ẩn trục Y bên phải cho gọn
        barChart.getAxisRight().setEnabled(false);

        // Trục X ở dưới, bỏ lưới dọc
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
    }

    /**
     * Đọc DB trên background thread, tính toán số liệu,
     * rồi quay lại UI thread để cập nhật biểu đồ và các TextView.
     */
    private void loadStatsData() {
        // SessionManager là static — truyền context vào trực tiếp
        int userId = SessionManager.getUserId(requireContext());

        Executors.newSingleThreadExecutor().execute(() -> {

            // --- BACKGROUND THREAD ---

            List<String> last7Days = getLast7Days();

            // Dùng đúng tên hàm trong HabitDao của team
            List<Habit> habits = db.habitDao().getAllActiveHabitsByUser(userId);
            int totalHabits    = habits.size();

            float[] rates       = new float[7];
            float totalRate     = 0f;
            float bestRate      = 0f;
            String bestDayLabel = "--";
            String[] dayLabels  = new String[7];

            for (int i = 0; i < 7; i++) {
                String dateStr = last7Days.get(i);

                // Chuyển "2024-01-15" → "15/01" để hiện trên trục X
                String[] parts = dateStr.split("-");
                dayLabels[i]   = parts[2] + "/" + parts[1];

                if (totalHabits == 0) continue;

                int completedCount = 0;
                for (Habit habit : habits) {
                    // Dùng getter vì Habit dùng private field + getter/setter
                    HabitLog log = db.habitLogDao()
                            .getLogByHabitAndDate(habit.getId(), dateStr);

                    // isCompleted() trả về boolean, không phải int
                    if (log != null && log.isCompleted()) {
                        completedCount++;
                    }
                }

                float rate  = (float) completedCount / totalHabits * 100f;
                rates[i]    = rate;
                totalRate  += rate;

                if (rate > bestRate) {
                    bestRate     = rate;
                    bestDayLabel = dayLabels[i];
                }
            }

            float avgRate = (totalHabits > 0) ? totalRate / 7f : 0f;
            int streak    = calculateStreak(habits, last7Days);

            // Chuẩn bị data cho biểu đồ
            List<BarEntry> entries = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                entries.add(new BarEntry(i, rates[i]));
            }

            // Biến trong lambda phải là final
            final String[] finalLabels  = dayLabels;
            final float finalAvg        = avgRate;
            final int finalStreak       = streak;
            final int finalTotal        = totalHabits;
            final String finalBestDay   = bestDayLabel;
            final List<BarEntry> finalE = entries;

            // --- Quay về UI thread để vẽ ---
            requireActivity().runOnUiThread(() -> {
                drawBarChart(finalE, finalLabels);
                tvCurrentStreak.setText(finalStreak + " 🔥");
                tvCompletionRate.setText(Math.round(finalAvg) + "%");
                tvTotalHabits.setText(String.valueOf(finalTotal));
                tvBestDay.setText(finalBestDay);
            });
        });
    }

    /**
     * Đưa dữ liệu vào BarChart và vẽ.
     * Tách ra hàm riêng cho dễ đọc hơn.
     */
    private void drawBarChart(List<BarEntry> entries, String[] dayLabels) {
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(Color.parseColor("#4FC3F7")); // xanh dương nhạt
        dataSet.setValueTextColor(Color.parseColor("#1A1A2E"));
        dataSet.setValueTextSize(10f);

        // Hiện "75%" thay vì "75.0" mặc định của thư viện
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return Math.round(value) + "%";
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dayLabels));
        barChart.invalidate(); // trigger vẽ lại
    }

    /**
     * Tính streak: đếm số ngày liên tiếp từ hôm nay trở về trước
     * mà có ít nhất 1 habit hoàn thành. Gặp ngày đứt chuỗi thì dừng.
     */
    private int calculateStreak(List<Habit> habits, List<String> last7Days) {
        int streak = 0;
        // index 6 = hôm nay, index 0 = 6 ngày trước
        for (int i = 6; i >= 0; i--) {
            String dateStr     = last7Days.get(i);
            boolean hasAnydone = false;

            for (Habit habit : habits) {
                HabitLog log = db.habitLogDao()
                        .getLogByHabitAndDate(habit.getId(), dateStr);
                if (log != null && log.isCompleted()) {
                    hasAnydone = true;
                    break;
                }
            }

            if (hasAnydone) {
                streak++;
            } else {
                break; // đứt chuỗi rồi, không tính tiếp
            }
        }
        return streak;
    }

    /**
     * Tạo list 7 ngày gần nhất theo format "yyyy-MM-dd".
     * index 0 = 6 ngày trước, index 6 = hôm nay.
     */
    private List<String> getLast7Days() {
        List<String> days    = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal         = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6); // lùi về 6 ngày trước

        for (int i = 0; i < 7; i++) {
            days.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return days;
    }
}
