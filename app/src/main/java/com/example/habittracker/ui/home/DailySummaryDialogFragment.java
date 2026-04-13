package com.example.habittracker.ui.home;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;

import java.util.ArrayList;

public class DailySummaryDialogFragment extends DialogFragment {

    private static final String ARG_COMPLETED_COUNT = "arg_completed_count";
    private static final String ARG_TOTAL_COUNT = "arg_total_count";
    private static final String ARG_DAY_STREAK = "arg_day_streak";
    private static final String ARG_ITEMS = "arg_items";

    public static DailySummaryDialogFragment newInstance(
            int completedCount,
            int totalCount,
            int dayStreak,
            ArrayList<String> items
    ) {
        DailySummaryDialogFragment fragment = new DailySummaryDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COMPLETED_COUNT, completedCount);
        args.putInt(ARG_TOTAL_COUNT, totalCount);
        args.putInt(ARG_DAY_STREAK, dayStreak);
        args.putStringArrayList(ARG_ITEMS, items);
        fragment.setArguments(args);
        return fragment;
    }

    public DailySummaryDialogFragment() {
    }

    @Override
    public int getTheme() {
        return R.style.Theme_HabitTracker_DailySummaryDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_daily_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvSubtitle = view.findViewById(R.id.tvSubtitle);
        TextView tvProgress = view.findViewById(R.id.tvProgress);
        TextView tvDayStreak = view.findViewById(R.id.tvDayStreak);
        TextView tvEmpty = view.findViewById(R.id.tvEmpty);
        ImageView btnClose = view.findViewById(R.id.btnClose);
        RecyclerView rvSummaryHabits = view.findViewById(R.id.rvSummaryHabits);

        int completedCount = 0;
        int totalCount = 0;
        int dayStreak = 0;
        ArrayList<String> items = new ArrayList<>();

        if (getArguments() != null) {
            completedCount = getArguments().getInt(ARG_COMPLETED_COUNT, 0);
            totalCount = getArguments().getInt(ARG_TOTAL_COUNT, 0);
            dayStreak = getArguments().getInt(ARG_DAY_STREAK, 0);

            ArrayList<String> argItems = getArguments().getStringArrayList(ARG_ITEMS);
            if (argItems != null) {
                items = argItems;
            }
        }

        tvTitle.setText("Chúc mừng bạn 🎉");

        if (completedCount > 0) {
            tvSubtitle.setText("Hôm nay bạn đã làm rất tốt, tiếp tục giữ phong độ nhé");
        } else {
            tvSubtitle.setText("Đừng lo, ngày mai mình làm lại thật tốt nhé");
        }

        tvProgress.setText(completedCount + "/" + totalCount + " habit hoàn thành");
        tvDayStreak.setText(dayStreak + " ngày");

        rvSummaryHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSummaryHabits.setAdapter(new DailySummaryHabitAdapter(items));

        if (items.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvSummaryHabits.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvSummaryHabits.setVisibility(View.VISIBLE);
        }

        btnClose.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.92);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}