package com.example.habittracker.ui.timer;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.habittracker.R;
import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.data.model.HabitLog;
import com.example.habittracker.utils.Constants;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimerHabitDialogFragment extends DialogFragment {

    private static final String ARG_HABIT_ID = "arg_habit_id";

    private View btnCloseTimer;
    private View btnStopTimer;
    private View timerCircleWrap;
    private View viewTimerDot;
    private View viewHabitIconBg;

    private TextView tvHabitName;
    private TextView tvTimerHint;
    private TextView tvTimer;
    private TextView tvTimerProgress;
    private TextView txtHabitIconEmoji;

    private Habit habit;
    private AppDatabase db;
    private SharedPreferences timerPrefs;
    private CountDownTimer countDownTimer;

    private long totalMillis = 0L;
    private long remainingMillis = 0L;
    private boolean isRunning = false;

    public static TimerHabitDialogFragment newInstance(int habitId) {
        TimerHabitDialogFragment fragment = new TimerHabitDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_HABIT_ID, habitId);
        fragment.setArguments(args);
        return fragment;
    }

    public TimerHabitDialogFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_Material3_Dark_NoActionBar);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_timer_habit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);

        db = AppDatabase.getInstance(requireContext());
        timerPrefs = requireContext().getSharedPreferences(Constants.PREF_TIMER, requireContext().MODE_PRIVATE);

        setupClicks();

        int habitId = getArguments() != null ? getArguments().getInt(ARG_HABIT_ID, -1) : -1;
        if (habitId == -1) {
            dismissAllowingStateLoss();
            return;
        }

        loadHabit(habitId);
    }

    private void bindViews(View view) {
        btnCloseTimer = view.findViewById(R.id.btnCloseTimer);
        btnStopTimer = view.findViewById(R.id.btnStopTimer);
        timerCircleWrap = view.findViewById(R.id.timerCircleWrap);
        viewTimerDot = view.findViewById(R.id.viewTimerDot);
        viewHabitIconBg = view.findViewById(R.id.viewHabitIconBg);

        tvHabitName = view.findViewById(R.id.tvHabitName);
        tvTimerHint = view.findViewById(R.id.tvTimerHint);
        tvTimer = view.findViewById(R.id.tvTimer);
        tvTimerProgress = view.findViewById(R.id.tvTimerProgress);
        txtHabitIconEmoji = view.findViewById(R.id.txtHabitIconEmoji);
    }

    private void setupClicks() {
        btnCloseTimer.setOnClickListener(v -> {
            pauseTimerOnly();
            dismissAllowingStateLoss();
        });

        btnStopTimer.setOnClickListener(v -> {
            stopAndResetTimer();
            dismissAllowingStateLoss();
        });

        timerCircleWrap.setOnClickListener(v -> togglePauseResume());
        tvTimer.setOnClickListener(v -> togglePauseResume());
    }

    private void togglePauseResume() {
        if (habit == null) return;

        if (isRunning) {
            pauseTimerOnly();
        } else {
            startTimer();
        }
    }

    private void loadHabit(int habitId) {
        new Thread(() -> {
            Habit loadedHabit = db.habitDao().getHabitById(habitId);

            if (loadedHabit == null) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::dismissAllowingStateLoss);
                }
                return;
            }

            habit = loadedHabit;
            totalMillis = habit.getSafeTargetValue() * 60L * 1000L;

            long savedRemaining = timerPrefs.getLong(getRemainingKey(habit.getId()), totalMillis);
            remainingMillis = Math.max(0L, Math.min(savedRemaining, totalMillis));

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    bindHabitData();
                    updateTimerViews();
                    startTimer();
                });
            }
        }).start();
    }

    private void bindHabitData() {
        if (habit == null) return;

        tvHabitName.setText(habit.getTitle());
        txtHabitIconEmoji.setText(habit.getIconEmoji());

        applyIconBackgroundColor(habit.getColor());

        if (habit.isCompletedToday()) {
            tvTimerHint.setText("Hôm nay đã hoàn thành");
        } else {
            tvTimerHint.setText("Chạm vòng tròn để tạm dừng");
        }
    }

    private void applyIconBackgroundColor(String colorHex) {
        try {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(dp(24));
            bg.setColor(adjustAlpha(Color.parseColor(colorHex), 0.45f));
            viewHabitIconBg.setBackground(bg);
        } catch (Exception e) {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(dp(24));
            bg.setColor(Color.parseColor("#6A4152"));
            viewHabitIconBg.setBackground(bg);
        }
    }

    private void startTimer() {
        if (habit == null) return;

        if (remainingMillis <= 0L) {
            remainingMillis = totalMillis;
        }

        cancelCurrentTimer();
        isRunning = true;
        tvTimerHint.setText("Chạm vòng tròn để tạm dừng");

        countDownTimer = new CountDownTimer(remainingMillis, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMillis = millisUntilFinished;
                saveRemainingTime();
                updateTimerViews();
            }

            @Override
            public void onFinish() {
                remainingMillis = 0L;
                isRunning = false;
                clearSavedTimer();
                updateTimerViews();
                tvTimerHint.setText("Hoàn thành");
                completeHabitByTimer();
            }
        };
        countDownTimer.start();
    }

    private void pauseTimerOnly() {
        isRunning = false;
        cancelCurrentTimer();
        saveRemainingTime();
        updateTimerViews();
        tvTimerHint.setText("Đã tạm dừng - chạm vòng tròn để tiếp tục");
    }

    private void stopAndResetTimer() {
        isRunning = false;
        cancelCurrentTimer();
        remainingMillis = totalMillis;
        clearSavedTimer();
        updateTimerViews();
    }

    private void cancelCurrentTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
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

    private void updateTimerViews() {
        long elapsedMillis = Math.max(0L, totalMillis - remainingMillis);

        tvTimer.setText(formatHms(remainingMillis));
        tvTimerProgress.setText(formatMs(elapsedMillis) + " / " + formatMs(totalMillis));

        updateCircleProgress(elapsedMillis, totalMillis);
    }

    private void updateCircleProgress(long elapsedMillis, long totalMillis) {
        if (viewTimerDot == null || totalMillis <= 0) return;

        float progress = Math.max(0f, Math.min(1f, (float) elapsedMillis / (float) totalMillis));

        timerCircleWrap.post(() -> {
            int size = timerCircleWrap.getWidth();
            if (size <= 0) return;

            float radius = size / 2f;
            float strokeOffset = dp(18);
            float orbitRadius = radius - strokeOffset;

            double angle = Math.toRadians((progress * 360f) - 90f);

            float centerX = radius;
            float centerY = radius;

            float dotCenterX = centerX + (float) (orbitRadius * Math.cos(angle));
            float dotCenterY = centerY + (float) (orbitRadius * Math.sin(angle));

            float dotHalfWidth = viewTimerDot.getWidth() / 2f;
            float dotHalfHeight = viewTimerDot.getHeight() / 2f;

            viewTimerDot.animate()
                    .x(dotCenterX - dotHalfWidth)
                    .y(dotCenterY - dotHalfHeight)
                    .setDuration(300)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            viewTimerDot.setAlpha(isRunning ? 1f : 0.55f);
            tvTimer.setAlpha(isRunning ? 1f : 0.9f);
        });
    }

    private void completeHabitByTimer() {
        new Thread(() -> {
            if (habit == null) return;

            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            HabitLog existingLog = db.habitLogDao().getLogByHabitAndDate(habit.getId(), today);
            int targetValue = habit.getSafeTargetValue();

            if (existingLog == null) {
                HabitLog newLog = new HabitLog(
                        habit.getId(),
                        today,
                        targetValue,
                        targetValue,
                        true,
                        now,
                        "Completed by timer",
                        Constants.COMPLETION_METHOD_TIMER
                );
                db.habitLogDao().insert(newLog);
            } else {
                existingLog.setCurrentValue(targetValue);
                existingLog.setTargetValue(targetValue);
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

    private String formatHms(long millis) {
        long totalSeconds = Math.max(0L, millis / 1000L);
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String formatMs(long millis) {
        long totalSeconds = Math.max(0L, millis / 1000L);
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    private int dp(int value) {
        return (int) (value * requireContext().getResources().getDisplayMetrics().density);
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isRunning) {
            pauseTimerOnly();
        }
    }

    @Override
    public void onDestroyView() {
        cancelCurrentTimer();
        super.onDestroyView();
    }
}