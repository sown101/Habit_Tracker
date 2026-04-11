package com.example.habittracker.ui.home;

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
import com.example.habittracker.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
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

    private static final String LABEL_COMPLETE = "Complete habit";
    private static final String LABEL_COUNTER = "Counter habit";
    private static final String LABEL_DAILY = "Hàng ngày";
    private static final String LABEL_WEEKLY = "Hàng tuần";

    private EditText edtHabitName;
    private EditText edtTargetValue;
    private EditText edtTargetUnit;
    private TextView txtReminderTime;
    private TextView tvSheetTitle;
    private TextView tvWeeklyDaysLabel;
    private TextView tvTargetTitle;
    private TextView tvTargetHint;
    private Button btnSaveHabit;
    private Spinner spinnerFrequency;
    private Spinner spinnerHabitType;

    private SwitchMaterial switchReminder;
    private SwitchMaterial switchShake;
    private SwitchMaterial switchFocus;

    private ChipGroup chipGroupCategory;
    private ChipGroup chipGroupDays;
    private LinearLayout layoutReminderTime;
    private LinearLayout layoutCounterFields;

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
        setupSpinners();
        initEvents();

        if (getArguments() != null && getArguments().containsKey("EDIT_HABIT")) {
            habitToEdit = (Habit) getArguments().getSerializable("EDIT_HABIT");
        }

        if (habitToEdit != null) {
            bindHabitToForm(habitToEdit);
        } else {
            spinnerHabitType.setSelection(0);
            spinnerFrequency.setSelection(0);
            updateFormByHabitType();
            updateReminderVisibility();
            updateWeeklyDaysVisibility();
        }

        btnSaveHabit.setOnClickListener(v -> saveHabit());

        return view;
    }

    private void initViews(View view) {
        edtHabitName = view.findViewById(R.id.edtHabitName);
        edtTargetValue = view.findViewById(R.id.edtTargetValue);
        edtTargetUnit = view.findViewById(R.id.edtTargetUnit);
        txtReminderTime = view.findViewById(R.id.txtReminderTime);
        tvSheetTitle = view.findViewById(R.id.tvSheetTitle);
        tvWeeklyDaysLabel = view.findViewById(R.id.tvWeeklyDaysLabel);
        tvTargetTitle = view.findViewById(R.id.tvTargetTitle);
        tvTargetHint = view.findViewById(R.id.tvTargetHint);
        btnSaveHabit = view.findViewById(R.id.btnSaveHabit);
        spinnerFrequency = view.findViewById(R.id.spinnerFrequency);
        spinnerHabitType = view.findViewById(R.id.spinnerHabitType);

        switchReminder = view.findViewById(R.id.switchReminder);
        switchShake = view.findViewById(R.id.switchShake);
        switchFocus = view.findViewById(R.id.switchFocus);

        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        chipGroupDays = view.findViewById(R.id.chipGroupDays);
        layoutReminderTime = view.findViewById(R.id.layoutReminderTime);
        layoutCounterFields = view.findViewById(R.id.layoutCounterFields);
    }

    private void setupSpinners() {
        ArrayAdapter<String> habitTypeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{LABEL_COMPLETE, LABEL_COUNTER}
        );
        habitTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHabitType.setAdapter(habitTypeAdapter);

        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{LABEL_DAILY, LABEL_WEEKLY}
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

        spinnerHabitType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateFormByHabitType();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void updateReminderVisibility() {
        layoutReminderTime.setVisibility(switchReminder.isChecked() ? View.VISIBLE : View.GONE);
    }

    private void updateWeeklyDaysVisibility() {
        String selected = spinnerFrequency.getSelectedItem() != null
                ? spinnerFrequency.getSelectedItem().toString()
                : "";

        boolean isWeekly = LABEL_WEEKLY.equals(selected);
        tvWeeklyDaysLabel.setVisibility(isWeekly ? View.VISIBLE : View.GONE);
        chipGroupDays.setVisibility(isWeekly ? View.VISIBLE : View.GONE);
    }

    private void updateFormByHabitType() {
        boolean isCounter = isCounterTypeSelected();

        layoutCounterFields.setVisibility(isCounter ? View.VISIBLE : View.GONE);

        if (isCounter) {
            tvTargetTitle.setText("Mục tiêu trong ngày");
            tvTargetHint.setText("Ví dụ: 8 cốc, 6000 bước, 4 phiên");
            switchShake.setChecked(false);
            switchShake.setEnabled(false);
            switchShake.setAlpha(0.5f);
        } else {
            tvTargetTitle.setText("Thiết lập complete habit");
            tvTargetHint.setText("Complete habit chỉ cần đánh dấu hoàn thành trong ngày.");
            switchShake.setEnabled(true);
            switchShake.setAlpha(1f);
        }
    }

    private boolean isCounterTypeSelected() {
        Object selected = spinnerHabitType.getSelectedItem();
        return selected != null && LABEL_COUNTER.equals(selected.toString());
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

        if (habit.isCounterHabit()) {
            spinnerHabitType.setSelection(1);
            edtTargetValue.setText(String.valueOf(habit.getSafeTargetValue()));
            edtTargetUnit.setText(habit.getDisplayUnit());
        } else {
            spinnerHabitType.setSelection(0);
            edtTargetValue.setText("");
            edtTargetUnit.setText("");
        }

        switchShake.setChecked(habit.isAllowShakeComplete());
        switchFocus.setChecked(habit.isEnableFocusSession());

        boolean reminderEnabled = habit.isReminderEnabled()
                && !TextUtils.isEmpty(habit.getReminderTime());
        switchReminder.setChecked(reminderEnabled);

        if (reminderEnabled) {
            txtReminderTime.setText(habit.getReminderTime());
        } else {
            txtReminderTime.setText("Chọn giờ");
        }

        String frequencyType = habit.getFrequencyType();
        if (LABEL_WEEKLY.equalsIgnoreCase(frequencyType) || "WEEKLY".equalsIgnoreCase(frequencyType)) {
            spinnerFrequency.setSelection(1);
        } else {
            spinnerFrequency.setSelection(0);
        }

        String category = habit.getCategory();
        for (int i = 0; i < chipGroupCategory.getChildCount(); i++) {
            View child = chipGroupCategory.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.getText().toString().equals(category)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }

        updateFormByHabitType();
        updateReminderVisibility();
        updateWeeklyDaysVisibility();
    }

    private void saveHabit() {
        String name = edtHabitName.getText().toString().trim();
        String selectedHabitType = isCounterTypeSelected() ? Habit.TYPE_COUNTER : Habit.TYPE_COMPLETE;
        String valueStr = edtTargetValue.getText().toString().trim();
        String unitStr = edtTargetUnit.getText().toString().trim();
        String frequency = spinnerFrequency.getSelectedItem() != null
                ? spinnerFrequency.getSelectedItem().toString()
                : LABEL_DAILY;
        String reminderTime = txtReminderTime.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên thói quen", Toast.LENGTH_SHORT).show();
            return;
        }

        int targetValue;
        String unit;

        if (Habit.TYPE_COUNTER.equals(selectedHabitType)) {
            if (valueStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập mục tiêu cho counter habit", Toast.LENGTH_SHORT).show();
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

            unit = unitStr;
        } else {
            targetValue = 1;
            unit = "lần";
        }

        int userId = SessionManager.getUserId(requireContext());
        if (userId == -1) {
            Toast.makeText(getContext(), "Không tìm thấy phiên đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isShake = switchShake.isChecked() && Habit.TYPE_COMPLETE.equals(selectedHabitType);
        boolean isFocus = switchFocus.isChecked();
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
            habitToEdit.setCategory(category);
            habitToEdit.setHabitType(selectedHabitType);
            habitToEdit.setTargetValue(targetValue);
            habitToEdit.setUnit(unit);
            habitToEdit.setFrequencyType(frequency);
            habitToEdit.setReminderEnabled(isReminderEnabled);
            habitToEdit.setReminderTime(isReminderEnabled ? reminderTime : "");
            habitToEdit.setAllowShakeComplete(isShake);
            habitToEdit.setEnableFocusSession(isFocus);
            habitToEdit.setSessionDurationMinutes(isFocus ? 25 : 0);
            habitToEdit.setUpdatedAt(now);

            habitToSave = habitToEdit;
        } else {
            isUpdating = false;

            habitToSave = new Habit(
                    userId,
                    name,
                    "",
                    category,
                    selectedHabitType,
                    targetValue,
                    unit,
                    frequency,
                    isReminderEnabled,
                    isReminderEnabled ? reminderTime : "",
                    isShake,
                    isFocus,
                    isFocus ? 25 : 0,
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