package com.example.habittracker.ui.home;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.habittracker.R;
import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.data.model.HabitLog;
import com.example.habittracker.utils.DailyCompletionUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HabitDetailDialogFragment extends DialogFragment {

    private static final String ARG_HABIT = "habit_data";

    private Habit habit;
    private AppDatabase db;

    // Views thông tin habit
    private TextView tvName;
    private TextView tvIconEmoji;
    private View viewIconBg;
    private TextView tvFrequency;
    private TextView tvReminder;

    // Views thống kê streak riêng
    private TextView tvCurrentStreak;
    private TextView tvLongestStreak;
    private TextView tvTotalCompleted;

    // Calendar heatmap (lưới các ô màu theo ngày)
    private GridLayout gridHeatmap;

    private Button btnClose;

    public static HabitDetailDialogFragment newInstance(Habit habit) {
        HabitDetailDialogFragment fragment = new HabitDetailDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_HABIT, habit);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_habit_detail, null);

        db = AppDatabase.getInstance(requireContext());

        tvName = view.findViewById(R.id.tvDetailName);
        tvIconEmoji = view.findViewById(R.id.tvDetailIcon);
        viewIconBg = view.findViewById(R.id.viewDetailIconBg);
        tvFrequency = view.findViewById(R.id.tvDetailFrequency);
        tvReminder = view.findViewById(R.id.tvDetailReminder);
        tvCurrentStreak = view.findViewById(R.id.tvDetailCurrentStreak);
        tvLongestStreak = view.findViewById(R.id.tvDetailLongestStreak);
        tvTotalCompleted = view.findViewById(R.id.tvDetailTotalCompleted);
        gridHeatmap = view.findViewById(R.id.gridHeatmap);
        btnClose = view.findViewById(R.id.btnDetailClose);

        ImageButton btnEditHabit = view.findViewById(R.id.btnEditHabit);
        ImageButton btnDeleteHabit = view.findViewById(R.id.btnDeleteHabit);

        if (getArguments() != null) {
            habit = (Habit) getArguments().getSerializable(ARG_HABIT);
        }

        bindStaticInfo();
        loadStats();

        // Nút edit: mở form chỉnh sửa
        btnEditHabit.setOnClickListener(v -> {
            if (habit == null) return;
            dismiss();
            AddHabit editSheet = new AddHabit();
            Bundle bundle = new Bundle();
            bundle.putSerializable("EDIT_HABIT", habit);
            editSheet.setArguments(bundle);
            editSheet.show(requireActivity().getSupportFragmentManager(), "EditHabit");
        });

        // Nút xóa: hỏi xác nhận rồi xóa
        btnDeleteHabit.setOnClickListener(v -> {
            if (habit == null) return;
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xóa thói quen")
                    .setMessage("Bạn có chắc muốn xóa '" + habit.getTitle() + "' không?")
                    .setPositiveButton("Xóa", (d, i) -> {
                        new Thread(() -> {
                            db.habitDao().delete(habit);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
                                    getParentFragmentManager().setFragmentResult("refresh_habits", new Bundle());
                                    dismiss();
                                });
                            }
                        }).start();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        btnClose.setOnClickListener(v -> dismiss());

        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setView(view)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        android.app.Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // 1. Ép chiều rộng và chiều cao tràn viền 100%
            int width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
            int height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);

            // 2. Xóa bỏ cái viền mờ bo góc mặc định của hệ thống để nó phẳng lì ra sát mép
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE));
        }
    }

    // Hiển thị thông tin cơ bản của habit (không cần load từ DB)
    private void bindStaticInfo() {
        if (habit == null) return;

        tvName.setText(habit.getTitle());
        tvIconEmoji.setText(habit.getIconEmoji());

        // Set màu nền tròn cho icon
        try {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(Color.parseColor(habit.getColor()));
            viewIconBg.setBackground(bg);
        } catch (Exception ignored) {}

        tvFrequency.setText("🔄 " + habit.getFrequency());

        if (habit.isReminderEnabled() && !TextUtils.isEmpty(habit.getReminderTime())) {
            tvReminder.setText("⏰ " + habit.getReminderTime());
        } else {
            tvReminder.setText("⏰ Không bật nhắc nhở");
        }
    }

    // Load thống kê streak riêng và vẽ heatmap (chạy trên background thread)
    private void loadStats() {
        if (habit == null) return;

        new Thread(() -> {
            // Tính streak riêng của habit này
            int currentStreak = DailyCompletionUtils.calculateHabitCurrentStreak(db, habit.getId());
            int longestStreak = DailyCompletionUtils.calculateHabitLongestStreak(db, habit.getId(), 90);
            int totalCompleted = DailyCompletionUtils.calculateHabitTotalCompleted(db, habit.getId());

            // Lấy dữ liệu cho heatmap: 56 ngày gần nhất (8 tuần)
            boolean[] completedDays = new boolean[56];
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -55); // Bắt đầu từ 55 ngày trước

            for (int i = 0; i < 56; i++) {
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(calendar.getTime());
                HabitLog log = db.habitLogDao().getLogByHabitAndDate(habit.getId(), date);
                completedDays[i] = (log != null && log.isCompleted());
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvCurrentStreak.setText(currentStreak + " ngày");
                    tvLongestStreak.setText(longestStreak + " ngày");
                    tvTotalCompleted.setText(String.valueOf(totalCompleted));
                    drawHeatmap(completedDays);
                });
            }
        }).start();
    }

    /**
     * Vẽ heatmap: lưới 8 tuần x 7 ngày.
     */
    private void drawHeatmap(boolean[] completedDays) {
        if (gridHeatmap == null || getContext() == null) return;

        gridHeatmap.removeAllViews();
        gridHeatmap.setColumnCount(8);  // 8 cột (8 tuần)
        gridHeatmap.setRowCount(7);     // 7 hàng (7 ngày trong tuần)

        String habitColor = (habit != null && !TextUtils.isEmpty(habit.getColor())) ? habit.getColor() : "#39D353";
        float density = requireContext().getResources().getDisplayMetrics().density;

        for (int week = 0; week < 8; week++) {
            for (int day = 0; day < 7; day++) {
                int index = week * 7 + day;

                View cell = new View(requireContext());

                int cellSize = (int) (16 * density);
                int cellMargin = (int) (2 * density);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(cellMargin, cellMargin, cellMargin, cellMargin);
                params.rowSpec = GridLayout.spec(day);
                params.columnSpec = GridLayout.spec(week);
                cell.setLayoutParams(params);

                // TỰ ĐÚC KHUÔN BẰNG JAVA (Không cần file bg_heatmap nữa)
                android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
                drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                drawable.setCornerRadius(3 * density); // Bo góc 3dp siêu mượt

                if (index < completedDays.length && completedDays[index]) {
                    // Ngày hoàn thành: Đổ màu của Habit
                    drawable.setColor(android.graphics.Color.parseColor(habitColor));
                } else {
                    // Ngày lười biếng: Màu xám đậm hơn một chút để nổi lên nền trắng
                    drawable.setColor(android.graphics.Color.parseColor("#E0E0E0"));
                }

                cell.setBackground(drawable);
                gridHeatmap.addView(cell);
            }
        }
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}