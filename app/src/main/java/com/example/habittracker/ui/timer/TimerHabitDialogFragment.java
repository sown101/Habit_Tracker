package com.example.habittracker.ui.timer;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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

public class TimerHabitDialogFragment extends DialogFragment {

    private static final String ARG_HABIT_ID = "arg_habit_id";

    private TextView txtHabitTitle;
    private TextView txtTimer;
    private TextView txtTimerHint;
    private ImageButton btnStartPause;
    private ImageButton btnStop;
    private ImageButton btnClose;

    private Habit habit;
    private CountDownTimer countDownTimer;

    private long totalMillis = 0L;
    private long remainingMillis = 0L;
    private boolean isRunning = false;

    private SharedPreferences timerPrefs;
    private AppDatabase db;

    public static TimerHabitDialogFragment newInstance(int habitId) {
        TimerHabitDialogFragment fragment = new TimerHabitDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_HABIT_ID, habitId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_timer_habit, null, false);

        txtHabitTitle = view.findViewById(R.id.txtHabitTitle);
        txtTimer = view.findViewById(R.id.txtTimer);
        txtTimerHint = view.findViewById(R.id.txtTimerHint);
        btnStartPause = view.findViewById(R.id.btnStartPause);
        btnStop = view.findViewById(R.id.btnStop);
        btnClose = view.findViewById(R.id.btnClose);

        timerPrefs = requireContext().getSharedPreferences(Constants.PREF_TIMER, requireContext().MODE_PRIVATE);
        db = AppDatabase.getInstance(requireContext());

        int habitId = getArguments() != null ? getArguments().getInt(ARG_HABIT_ID, -1) : -1;
        if (habitId == -1) {
            dismissAllowingStateLoss();
        } else {
            loadHabit(habitId);
        }

        btnStartPause.setOnClickListener(v -> {
            if (habit == null) return;

            if (isRunning) {
                pauseTimerAndSave();
            } else {
                startTimer();
            }
        });

        btnStop.setOnClickListener(v -> {
            stopAndResetTimer();
            dismissAllowingStateLoss();
        });

        btnClose.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimerAndSave();
            }
            dismissAllowingStateLoss();
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        return dialog;
    }

    private void loadHabit(int habitId) {
        new Thread(() -> {
            habit = db.habitDao().getHabitById(habitId);

            if (habit == null) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::dismissAllowingStateLoss);
                }
                return;
            }

            totalMillis = habit.getSafeTargetValue() * 60L * 1000L;
            long savedRemaining = timerPrefs.getLong(getRemainingKey(habit.getId()), -1L);

            if (savedRemaining > 0 && savedRemaining <= totalMillis) {
                remainingMillis = savedRemaining;
            } else {
                remainingMillis = totalMillis;
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    txtHabitTitle.setText(habit.getTitle());
                    txtTimerHint.setText("Mục tiêu: " + habit.getSafeTargetValue() + " phút");
                    updateTimerText();
                    updateStartPauseIcon();
                });
            }
        }).start();
    }

    private void startTimer() {
        if (habit == null) return;

        if (remainingMillis <= 0) {
            remainingMillis = totalMillis;
        }

        isRunning = true;
        updateStartPauseIcon();

        countDownTimer = new CountDownTimer(remainingMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMillis = millisUntilFinished;
                saveRemainingTime();
                updateTimerText();
            }

            @Override
            public void onFinish() {
                remainingMillis = 0L;
                isRunning = false;
                clearSavedTimer();
                updateTimerText();
                updateStartPauseIcon();
                completeHabitByTimer();
            }
        };
        countDownTimer.start();
    }

    private void pauseTimerAndSave() {
        isRunning = false;
        updateStartPauseIcon();

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        saveRemainingTime();
    }

    private void stopAndResetTimer() {
        isRunning = false;
        updateStartPauseIcon();

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        remainingMillis = totalMillis;
        clearSavedTimer();
        updateTimerText();
    }

    private void saveRemainingTime() {
        if (habit == null) return;

        timerPrefs.edit()
                .putLong(getRemainingKey(habit.getId()), remainingMillis)
                .apply();
    }

    private void clearSavedTimer() {
        if (habit == null) return;

        timerPrefs.edit()
                .remove(getRemainingKey(habit.getId()))
                .apply();
    }

    private String getRemainingKey(int habitId) {
        return "timer_remaining_" + habitId;
    }

    private void updateTimerText() {
        long totalSeconds = Math.max(0, remainingMillis) / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        txtTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void updateStartPauseIcon() {
        if (isRunning) {
            btnStartPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            btnStartPause.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void completeHabitByTimer() {
        new Thread(() -> {
            if (habit == null) return;

            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            HabitLog existingLog = db.habitLogDao().getLogByHabitAndDate(habit.getId(), today);

            if (existingLog == null) {
                HabitLog newLog = new HabitLog(
                        habit.getId(),
                        today,
                        habit.getTargetValue(),
                        habit.getTargetValue(),
                        true,
                        now,
                        "Completed by timer",
                        Constants.COMPLETION_METHOD_TIMER
                );
                db.habitLogDao().insert(newLog);
            } else {
                existingLog.setCurrentValue(habit.getTargetValue());
                existingLog.setTargetValue(habit.getTargetValue());
                existingLog.setCompleted(true);
                existingLog.setCompletedAt(now);
                existingLog.setCompletionMethod(Constants.COMPLETION_METHOD_TIMER);
                db.habitLogDao().update(existingLog);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Bundle result = new Bundle();
                    result.putBoolean("timer_completed", true);
                    getParentFragmentManager().setFragmentResult("refresh_habits", result);
                    dismissAllowingStateLoss();
                });
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isRunning) {
            pauseTimerAndSave();
        }
    }

    @Override
    public void onDestroyView() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        super.onDestroyView();
    }
}