package com.example.habittracker.ui.home;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.habittracker.R;
import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.data.model.Reminder;
import com.example.habittracker.notifications.NotificationScheduler;
import com.example.habittracker.utils.Constants;
import com.example.habittracker.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddHabit extends BottomSheetDialogFragment {

    private EditText edtHabitName;
    private EditText edtHabitDescription;
    private EditText edtTargetValue;
    private EditText edtTargetUnit;
    private EditText edtTimerMinutes;

    private TextView txtReminderTime;
    private TextView tvSheetTitle;
    private TextView tvWeeklyDaysLabel;
    private Button btnSaveHabit;

    private Spinner spinnerFrequency;
    private SwitchMaterial switchReminder;

    private ChipGroup chipGroupCategory;
    private ChipGroup chipGroupDays;

    private MaterialButtonToggleGroup toggleTrackType;
    private MaterialButton btnTrackTask;
    private MaterialButton btnTrackAmount;
    private MaterialButton btnTrackTime;

    private LinearLayout layoutReminderTime;
    private LinearLayout layoutAmountFields;
    private LinearLayout layoutTimerFields;

    private Habit habitToEdit;

    @Override
    public int getTheme() {
        return R.style.Theme_HabitTracker_BottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_bottom_sheet_add, container, false);

        initViews(view);
        setupFrequencySpinner();
        initEvents();

        if (getArguments() != null && getArguments().containsKey("EDIT_HABIT")) {
            habitToEdit = (Habit) getArguments().getSerializable("EDIT_HABIT");
        }

        if (habitToEdit != null) {
            bindHabitToForm(habitToEdit);
        } else {
            btnTrackTask.setChecked(true);
            spinnerFrequency.setSelection(0);
            updateTrackTypeUI();
            updateTrackButtonsUI();
            updateReminderVisibility();
            updateWeeklyDaysVisibility();
        }

        btnSaveHabit.setOnClickListener(v -> saveHabit());

        return view;
    }

    private void initViews(View view) {
        edtHabitName = view.findViewById(R.id.edtHabitName);
        edtHabitDescription = view.findViewById(R.id.edtHabitDescription);
        edtTargetValue = view.findViewById(R.id.edtTargetValue);
        edtTargetUnit = view.findViewById(R.id.edtTargetUnit);
        edtTimerMinutes = view.findViewById(R.id.edtTimerMinutes);

        txtReminderTime = view.findViewById(R.id.txtReminderTime);
        tvSheetTitle = view.findViewById(R.id.tvSheetTitle);
        tvWeeklyDaysLabel = view.findViewById(R.id.tvWeeklyDaysLabel);
        btnSaveHabit = view.findViewById(R.id.btnSaveHabit);

        spinnerFrequency = view.findViewById(R.id.spinnerFrequency);
        switchReminder = view.findViewById(R.id.switchReminder);

        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        chipGroupDays = view.findViewById(R.id.chipGroupDays);

        toggleTrackType = view.findViewById(R.id.toggleTrackType);
        btnTrackTask = view.findViewById(R.id.btnTrackTask);
        btnTrackAmount = view.findViewById(R.id.btnTrackAmount);
        btnTrackTime = view.findViewById(R.id.btnTrackTime);

        layoutReminderTime = view.findViewById(R.id.layoutReminderTime);
        layoutAmountFields = view.findViewById(R.id.layoutAmountFields);
        layoutTimerFields = view.findViewById(R.id.layoutTimerFields);
    }

    private void setupFrequencySpinner() {
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{
                        Constants.FREQUENCY_DAILY,
                        Constants.FREQUENCY_WEEKLY,
                        Constants.FREQUENCY_MONTHLY
                }
        );
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(frequencyAdapter);
    }

    private void initEvents() {
        txtReminderTime.setOnClickListener(v -> {
            if (switchReminder.isChecked()) {
                showTimePicker();
            }
        });

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                txtReminderTime.setText("Chọn giờ");
            }
            updateReminderVisibility();
        });

        spinnerFrequency.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateWeeklyDaysVisibility();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        toggleTrackType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            updateTrackTypeUI();
            updateTrackButtonsUI();
        });
    }

    private void updateTrackTypeUI() {
        if (isTaskTypeSelected()) {
            layoutAmountFields.setVisibility(View.GONE);
            layoutTimerFields.setVisibility(View.GONE);
        } else if (isAmountTypeSelected()) {
            layoutAmountFields.setVisibility(View.VISIBLE);
            layoutTimerFields.setVisibility(View.GONE);
        } else if (isTimeTypeSelected()) {
            layoutAmountFields.setVisibility(View.GONE);
            layoutTimerFields.setVisibility(View.VISIBLE);
        }
    }

    private void updateTrackButtonsUI() {
        setTrackButtonStyle(btnTrackTask, btnTrackTask.isChecked());
        setTrackButtonStyle(btnTrackAmount, btnTrackAmount.isChecked());
        setTrackButtonStyle(btnTrackTime, btnTrackTime.isChecked());
    }

    private void setTrackButtonStyle(MaterialButton button, boolean selected) {
        if (selected) {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            button.setTextColor(Color.WHITE);
            button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EEEEEE")));
            button.setTextColor(Color.parseColor("#222222"));
            button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        }
    }

    private boolean isTaskTypeSelected() {
        return btnTrackTask.isChecked();
    }

    private boolean isAmountTypeSelected() {
        return btnTrackAmount.isChecked();
    }

    private boolean isTimeTypeSelected() {
        return btnTrackTime.isChecked();
    }

    private void updateReminderVisibility() {
        layoutReminderTime.setVisibility(switchReminder.isChecked() ? View.VISIBLE : View.GONE);
    }

    private void updateWeeklyDaysVisibility() {
        String selected = spinnerFrequency.getSelectedItem() != null
                ? spinnerFrequency.getSelectedItem().toString()
                : "";

        boolean isWeekly = Constants.FREQUENCY_WEEKLY.equals(selected);
        tvWeeklyDaysLabel.setVisibility(isWeekly ? View.VISIBLE : View.GONE);
        chipGroupDays.setVisibility(isWeekly ? View.VISIBLE : View.GONE);
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(currentHour)
                .setMinute(currentMinute)
                .setTitleText("Chọn giờ nhắc nhở")
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build();

        picker.addOnPositiveButtonClickListener(dialog -> {
            int pickedHour = picker.getHour();
            int pickedMinute = picker.getMinute();

            String amPm;
            int hour12;
            if (pickedHour >= 12) {
                amPm = "PM";
                hour12 = (pickedHour == 12) ? 12 : pickedHour - 12;
            } else {
                amPm = "AM";
                hour12 = (pickedHour == 0) ? 12 : pickedHour;
            }

            String formattedTime = String.format(
                    Locale.getDefault(),
                    "%02d:%02d %s",
                    hour12,
                    pickedMinute,
                    amPm
            );
            txtReminderTime.setText(formattedTime);
        });

        picker.show(getChildFragmentManager(), "MATERIAL_TIME_PICKER");
    }

    private void bindHabitToForm(Habit habit) {
        tvSheetTitle.setText("Cập nhật thói quen");
        btnSaveHabit.setText("Cập nhật");

        edtHabitName.setText(habit.getTitle());
        edtHabitDescription.setText(habit.getDescription());

        if (habit.isTaskHabit()) {
            btnTrackTask.setChecked(true);
        } else if (habit.isCounterHabit()) {
            btnTrackAmount.setChecked(true);
            edtTargetValue.setText(String.valueOf(habit.getSafeTargetValue()));
            edtTargetUnit.setText(habit.getDisplayUnit());
        } else if (habit.isTimerHabit()) {
            btnTrackTime.setChecked(true);
            edtTimerMinutes.setText(String.valueOf(habit.getSafeTargetValue()));
        }

        boolean reminderEnabled = habit.isReminderEnabled()
                && !TextUtils.isEmpty(habit.getReminderTime());
        switchReminder.setChecked(reminderEnabled);

        if (reminderEnabled) {
            txtReminderTime.setText(habit.getReminderTime());
        } else {
            txtReminderTime.setText("Chọn giờ");
        }

        String frequencyType = habit.getFrequencyType();
        if (Constants.FREQUENCY_WEEKLY.equalsIgnoreCase(frequencyType)) {
            spinnerFrequency.setSelection(1);
        } else if (Constants.FREQUENCY_MONTHLY.equalsIgnoreCase(frequencyType)) {
            spinnerFrequency.setSelection(2);
        } else {
            spinnerFrequency.setSelection(0);
        }

        String category = habit.getCategory();
        for (int i = 0; i < chipGroupCategory.getChildCount(); i++) {
            View child = chipGroupCategory.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.getText().toString().equalsIgnoreCase(category)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }

        updateTrackTypeUI();
        updateTrackButtonsUI();
        updateReminderVisibility();
        updateWeeklyDaysVisibility();
    }

    private void saveHabit() {
        String name = edtHabitName.getText().toString().trim();
        String description = edtHabitDescription.getText().toString().trim();
        String frequency = spinnerFrequency.getSelectedItem() != null
                ? spinnerFrequency.getSelectedItem().toString()
                : Constants.FREQUENCY_DAILY;
        String reminderTime = txtReminderTime.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên thói quen", Toast.LENGTH_SHORT).show();
            return;
        }

        String habitType;
        int targetValue;
        String unit;

        if (isTaskTypeSelected()) {
            habitType = Constants.HABIT_TYPE_CHECKBOX;
            targetValue = 1;
            unit = "lần";
        } else if (isAmountTypeSelected()) {
            String valueStr = edtTargetValue.getText().toString().trim();
            String unitStr = edtTargetUnit.getText().toString().trim();

            if (valueStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập mục tiêu cho Amount", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                targetValue = Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Giá trị mục tiêu không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (targetValue <= 0) {
                Toast.makeText(getContext(), "Mục tiêu phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (unitStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đơn vị", Toast.LENGTH_SHORT).show();
                return;
            }

            habitType = Constants.HABIT_TYPE_COUNTER;
            unit = unitStr;
        } else {
            String timerStr = edtTimerMinutes.getText().toString().trim();

            if (timerStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập số phút cho Time habit", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                targetValue = Integer.parseInt(timerStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số phút không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (targetValue <= 0) {
                Toast.makeText(getContext(), "Số phút phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }

            habitType = Constants.HABIT_TYPE_TIMER;
            unit = "phút";
        }

        int userId = SessionManager.getUserId(requireContext());
        boolean isReminderEnabled = switchReminder.isChecked();

        if (isReminderEnabled && (TextUtils.isEmpty(reminderTime) || "Chọn giờ".equals(reminderTime))) {
            Toast.makeText(getContext(), "Vui lòng chọn giờ nhắc nhở", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = getSelectedCategory();
        String daysOfWeek = getSelectedDaysOfWeek();
        String now = getCurrentDateTime();

        final Habit habitToSave;
        final boolean isUpdating;

        if (habitToEdit != null) {
            isUpdating = true;

            habitToEdit.setUserId(userId);
            habitToEdit.setTitle(name);
            habitToEdit.setDescription(description);
            habitToEdit.setCategory(category);
            habitToEdit.setHabitType(habitType);
            habitToEdit.setTargetValue(targetValue);
            habitToEdit.setUnit(unit);
            habitToEdit.setFrequencyType(frequency);
            habitToEdit.setReminderEnabled(isReminderEnabled);
            habitToEdit.setReminderTime(isReminderEnabled ? reminderTime : "");
            habitToEdit.setUpdatedAt(now);

            habitToSave = habitToEdit;
        } else {
            isUpdating = false;

            habitToSave = new Habit(
                    userId,
                    name,
                    description,
                    category,
                    habitType,
                    targetValue,
                    unit,
                    frequency,
                    isReminderEnabled,
                    isReminderEnabled ? reminderTime : "",
                    true,
                    now,
                    now
            );
        }

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(requireContext());

                if (isUpdating) {
                    db.habitDao().update(habitToSave);
                } else {
                    long insertedId = db.habitDao().insert(habitToSave);
                    habitToSave.setId((int) insertedId);
                }

                Reminder oldReminder = db.reminderDao().getReminderByHabitId(habitToSave.getId());

                if (habitToSave.isReminderEnabled()
                        && !TextUtils.isEmpty(habitToSave.getReminderTime())) {

                    int requestCode = (oldReminder != null)
                            ? oldReminder.getRequestCode()
                            : habitToSave.getId();

                    Reminder reminder = new Reminder(
                            habitToSave.getId(),
                            habitToSave.getReminderTime(),
                            daysOfWeek,
                            true,
                            requestCode
                    );

                    if (oldReminder == null) {
                        db.reminderDao().insert(reminder);
                    } else {
                        reminder.setId(oldReminder.getId());
                        db.reminderDao().update(reminder);
                    }

                    int[] hourMinute = parseReminderTime(habitToSave.getReminderTime());
                    if (hourMinute != null) {
                        NotificationScheduler.cancelReminder(requireContext(), requestCode);
                        NotificationScheduler.scheduleReminder(
                                requireContext(),
                                habitToSave.getId(),
                                habitToSave.getTitle(),
                                hourMinute[0],
                                hourMinute[1],
                                requestCode
                        );
                    }

                } else {
                    if (oldReminder != null) {
                        NotificationScheduler.cancelReminder(requireContext(), oldReminder.getRequestCode());
                        db.reminderDao().delete(oldReminder);
                    }
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(
                                getContext(),
                                isUpdating ? "Đã cập nhật thói quen" : "Đã tạo thói quen",
                                Toast.LENGTH_SHORT
                        ).show();

                        getParentFragmentManager().setFragmentResult("refresh_habits", new Bundle());
                        dismiss();
                    });
                }

            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    getContext(),
                                    "Lỗi khi lưu: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                }
            }
        }).start();
    }

    private String getSelectedCategory() {
        int selectedChipId = chipGroupCategory.getCheckedChipId();
        if (selectedChipId != View.NO_ID) {
            Chip selectedChip = chipGroupCategory.findViewById(selectedChipId);
            if (selectedChip != null) {
                return selectedChip.getText().toString();
            }
        }
        return "Khác";
    }

    private String getSelectedDaysOfWeek() {
        if (chipGroupDays.getVisibility() != View.VISIBLE) {
            return "";
        }

        List<String> selectedDays = new ArrayList<>();
        for (int i = 0; i < chipGroupDays.getChildCount(); i++) {
            View child = chipGroupDays.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked()) {
                    selectedDays.add(chip.getText().toString());
                }
            }
        }

        return TextUtils.join(",", selectedDays);
    }

    private int[] parseReminderTime(String timeText) {
        if (timeText == null || timeText.trim().isEmpty()) {
            return null;
        }

        String[] patterns = {"hh:mm a", "HH:mm"};

        for (String pattern : patterns) {
            try {
                java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat(pattern, java.util.Locale.US);
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.setTime(sdf.parse(timeText.trim()));

                return new int[]{
                        calendar.get(java.util.Calendar.HOUR_OF_DAY),
                        calendar.get(java.util.Calendar.MINUTE)
                };
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new java.util.Date());
    }
}