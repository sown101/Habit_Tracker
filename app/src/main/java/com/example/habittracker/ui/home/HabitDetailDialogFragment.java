package com.example.habittracker.ui.home;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.example.habittracker.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HabitDetailDialogFragment extends DialogFragment {

    private static final String ARG_HABIT = "habit_data";

    private Habit habit;
    private AppDatabase db;

    private TextView tvName;
    private TextView tvCategory;
    private TextView tvTarget;
    private TextView tvFrequency;
    private TextView tvReminder;
    private TextView tvTodayStatus;
    private CheckBox cbDetailComplete;
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
        tvCategory = view.findViewById(R.id.tvDetailCategory);
        tvTarget = view.findViewById(R.id.tvDetailTarget);
        tvFrequency = view.findViewById(R.id.tvDetailFrequency);
        tvReminder = view.findViewById(R.id.tvDetailReminder);
        tvTodayStatus = view.findViewById(R.id.tvTodayStatus);
        cbDetailComplete = view.findViewById(R.id.cbDetailComplete);
        btnClose = view.findViewById(R.id.btnDetailClose);

        android.widget.ImageButton btnEditHabit = view.findViewById(R.id.btnEditHabit);
        android.widget.ImageButton btnDeleteHabit = view.findViewById(R.id.btnDeleteHabit);

        if (getArguments() != null) {
            habit = (Habit) getArguments().getSerializable(ARG_HABIT);
        }

        bindStaticHabitInfo();
        loadTodayLogAndBind();

        cbDetailComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }
            updateTodayCompletion(isChecked);
        });

        btnEditHabit.setOnClickListener(v -> {
            if (habit == null) return;

            dismiss();

            AddHabit editBottomSheet = new AddHabit();
            Bundle bundle = new Bundle();
            bundle.putSerializable("EDIT_HABIT", habit);
            editBottomSheet.setArguments(bundle);
            editBottomSheet.show(requireActivity().getSupportFragmentManager(), "EditHabit");
        });

        btnDeleteHabit.setOnClickListener(v -> {
            if (habit == null) return;

            new AlertDialog.Builder(requireContext())
                    .setTitle("Xóa thói quen")
                    .setMessage("Bạn có chắc chắn muốn xóa thói quen '" + habit.getTitle() + "' không? Dữ liệu không thể khôi phục.")
                    .setPositiveButton("Xóa", (dialogInterface, i) -> {
                        new Thread(() -> {
                            db.habitDao().delete(habit);

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "Đã xóa thói quen", Toast.LENGTH_SHORT).show();
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

    private void bindStaticHabitInfo() {
        if (habit == null) {
            return;
        }

        tvName.setText(safeText(habit.getTitle(), "Không có tên"));
        tvCategory.setText(safeText(habit.getCategory(), "Chưa phân loại"));

        String unit = safeText(habit.getUnit(), "");
        String targetText = "🎯 Mục tiêu: " + habit.getTargetValue();
        if (!unit.isEmpty()) {
            targetText += " " + unit;
        }
        tvTarget.setText(targetText);

        tvFrequency.setText("🔄 Tần suất: " + safeText(habit.getFrequency(), "Chưa có"));

        if (habit.isReminderEnabled()
                && habit.getReminderTime() != null
                && !habit.getReminderTime().trim().isEmpty()) {
            tvReminder.setText("⏰ Nhắc nhở: " + habit.getReminderTime());
        } else {
            tvReminder.setText("⏰ Nhắc nhở: Không bật");
        }
    }

    private void loadTodayLogAndBind() {
        if (habit == null) {
            return;
        }

        new Thread(() -> {
            String today = getTodayDate();
            HabitLog todayLog = db.habitLogDao().getLogByHabitAndDate(habit.getId(), today);

            boolean completed = todayLog != null && todayLog.isCompleted();
            int currentValue = todayLog != null ? todayLog.getCurrentValue() : 0;
            int targetValue = habit.getTargetValue();

            habit.setCompletedToday(completed);

            String statusText = completed
                    ? "Trạng thái hôm nay: Đã hoàn thành"
                    : "Trạng thái hôm nay: Chưa hoàn thành";

            String progressText = statusText + " (" + currentValue + "/" + targetValue + ")";

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    cbDetailComplete.setOnCheckedChangeListener(null);
                    cbDetailComplete.setChecked(completed);
                    cbDetailComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (!buttonView.isPressed()) {
                            return;
                        }
                        updateTodayCompletion(isChecked);
                    });

                    tvTodayStatus.setText(progressText);
                });
            }
        }).start();
    }

    private void updateTodayCompletion(boolean isChecked) {
        if (habit == null) {
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
                    String message = isChecked
                            ? "Đã đánh dấu hoàn thành hôm nay"
                            : "Đã bỏ đánh dấu hoàn thành hôm nay";

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().setFragmentResult("refresh_habits", new Bundle());

                    String status = isChecked
                            ? "Trạng thái hôm nay: Đã hoàn thành"
                            : "Trạng thái hôm nay: Chưa hoàn thành";

                    tvTodayStatus.setText(status + " (" +
                            (isChecked ? habit.getTargetValue() : 0) + "/" + habit.getTargetValue() + ")");
                });
            }
        }).start();
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}