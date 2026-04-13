package com.example.habittracker.ui.settings;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.habittracker.R;
import com.example.habittracker.utils.SettingsManager;
import com.example.habittracker.worker.WorkerScheduler;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private SwitchMaterial switchNotificationSetting;
    private LinearLayout layoutDailySummaryTime;
    private LinearLayout layoutDisplayName;
    private TextView tvDailySummaryTimeValue;
    private TextView tvDisplayNameValue;

    public SettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchNotificationSetting = view.findViewById(R.id.switchNotificationSetting);
        layoutDailySummaryTime = view.findViewById(R.id.layoutDailySummaryTime);
        layoutDisplayName = view.findViewById(R.id.layoutDisplayName);
        tvDailySummaryTimeValue = view.findViewById(R.id.tvDailySummaryTimeValue);
        tvDisplayNameValue = view.findViewById(R.id.tvDisplayNameValue);

        boolean notificationsEnabled = SettingsManager.isNotificationEnabled(requireContext());
        switchNotificationSetting.setChecked(notificationsEnabled);

        updateSummaryTimeText();
        updateDisplayNameText();
        updateSummaryTimeEnabledState(notificationsEnabled);

        switchNotificationSetting.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsManager.setNotificationEnabled(requireContext(), isChecked);
            updateSummaryTimeEnabledState(isChecked);

            if (isChecked) {
                WorkerScheduler.scheduleSummaryWorker(requireContext());
                Toast.makeText(requireContext(), "Đã bật thông báo", Toast.LENGTH_SHORT).show();
            } else {
                WorkerScheduler.cancelSummaryWorker(requireContext());
                Toast.makeText(requireContext(), "Đã tắt thông báo tổng kết", Toast.LENGTH_SHORT).show();
            }
        });

        layoutDailySummaryTime.setOnClickListener(v -> {
            if (!SettingsManager.isNotificationEnabled(requireContext())) {
                Toast.makeText(
                        requireContext(),
                        "Hãy bật thông báo trước để chọn giờ tổng kết",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            showSummaryTimePicker();
        });

        layoutDisplayName.setOnClickListener(v -> showDisplayNameDialog());
    }

    private void showSummaryTimePicker() {
        int currentHour = SettingsManager.getDailySummaryHour(requireContext());
        int currentMinute = SettingsManager.getDailySummaryMinute(requireContext());
        boolean is24Hour = DateFormat.is24HourFormat(requireContext());

        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    SettingsManager.setDailySummaryTime(requireContext(), hourOfDay, minute);
                    updateSummaryTimeText();
                    WorkerScheduler.scheduleSummaryWorker(requireContext());

                    Toast.makeText(
                            requireContext(),
                            "Đã cập nhật giờ tổng kết: " + formatTime(hourOfDay, minute),
                            Toast.LENGTH_SHORT
                    ).show();
                },
                currentHour,
                currentMinute,
                is24Hour
        );

        dialog.show();
    }

    private void showDisplayNameDialog() {
        EditText editText = new EditText(requireContext());
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editText.setHint("Nhập tên hiển thị");
        editText.setText(SettingsManager.getDisplayName(requireContext()));
        editText.setSelection(editText.getText().length());
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});

        int padding = dpToPx(20);
        editText.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(requireContext())
                .setTitle("Tên hiển thị")
                .setView(editText)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    SettingsManager.setDisplayName(requireContext(), name);
                    updateDisplayNameText();

                    Toast.makeText(
                            requireContext(),
                            name.isEmpty() ? "Đã xóa tên hiển thị" : "Đã lưu tên hiển thị",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .show();
    }

    private void updateSummaryTimeText() {
        int hour = SettingsManager.getDailySummaryHour(requireContext());
        int minute = SettingsManager.getDailySummaryMinute(requireContext());
        tvDailySummaryTimeValue.setText(formatTime(hour, minute));
    }

    private void updateDisplayNameText() {
        String displayName = SettingsManager.getDisplayName(requireContext());
        if (displayName == null || displayName.trim().isEmpty()) {
            tvDisplayNameValue.setText("Chưa đặt");
        } else {
            tvDisplayNameValue.setText(displayName.trim());
        }
    }

    private void updateSummaryTimeEnabledState(boolean enabled) {
        layoutDailySummaryTime.setEnabled(enabled);
        layoutDailySummaryTime.setAlpha(enabled ? 1f : 0.5f);
        tvDailySummaryTimeValue.setAlpha(enabled ? 1f : 0.6f);
    }

    private String formatTime(int hour, int minute) {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}