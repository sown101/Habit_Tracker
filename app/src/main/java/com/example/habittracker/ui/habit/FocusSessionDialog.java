package com.example.habittracker.ui.habit;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.habittracker.R;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.service.FocusSessionService;

public class FocusSessionDialog extends DialogFragment {

    private static final String ARG_HABIT = "arg_habit";

    private Habit habit;

    public static FocusSessionDialog newInstance(Habit habit) {
        FocusSessionDialog dialog = new FocusSessionDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_HABIT, habit);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_focus_session, null);

        if (getArguments() != null) {
            habit = (Habit) getArguments().getSerializable(ARG_HABIT);
        }

        TextView tvFocusTitle = view.findViewById(R.id.tvFocusTitle);
        TextView tvFocusHabitName = view.findViewById(R.id.tvFocusHabitName);
        TextView tvFocusDuration = view.findViewById(R.id.tvFocusDuration);
        Button btnStartFocus = view.findViewById(R.id.btnStartFocus);
        Button btnCancelFocus = view.findViewById(R.id.btnCancelFocus);

        int durationMinutes = 25;
        if (habit != null && habit.getSessionDurationMinutes() > 0) {
            durationMinutes = habit.getSessionDurationMinutes();
        }

        tvFocusTitle.setText("Bắt đầu Focus Session");
        tvFocusHabitName.setText(habit != null ? habit.getTitle() : "Không có thói quen");
        tvFocusDuration.setText("Thời lượng: " + durationMinutes + " phút");

        int finalDurationMinutes = durationMinutes;
        btnStartFocus.setOnClickListener(v -> {
            if (habit == null) {
                Toast.makeText(requireContext(), "Không tìm thấy thói quen", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(habit.getTitle())) {
                Toast.makeText(requireContext(), "Tên thói quen không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(requireContext(), FocusSessionService.class);
            intent.setAction(FocusSessionService.ACTION_START);
            intent.putExtra(FocusSessionService.EXTRA_HABIT_ID, habit.getId());
            intent.putExtra(FocusSessionService.EXTRA_HABIT_TITLE, habit.getTitle());
            intent.putExtra(FocusSessionService.EXTRA_DURATION_MINUTES, finalDurationMinutes);

            androidx.core.content.ContextCompat.startForegroundService(requireContext(), intent);

            Toast.makeText(requireContext(), "Đã bắt đầu Focus Session", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        btnCancelFocus.setOnClickListener(v -> dismiss());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        return dialog;
    }
}