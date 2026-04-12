package com.example.habittracker.ui.home;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class AddHabit extends BottomSheetDialogFragment {

    private static final String[] ICONS = {
            "💪", "📚", "🏃", "🧘", "💧", "🥗", "😴", "🎯",
            "🎵", "✍️", "🌿", "❤️", "🧹", "💊", "🚴", "🧠"
    };

    private static final String[] COLORS = {
            "#4CAF50", "#FF9800", "#9C27B0", "#3F51B5",
            "#2196F3", "#673AB7", "#009688", "#3949AB",
            "#1565C0", "#E91E63", "#F44336", "#795548"
    };

    private String selectedIcon;
    private String selectedColor;

    private EditText edtHabitName;
    private EditText edtHabitDescription;
    private EditText edtTargetValue;
    private EditText edtTargetUnit;
    private EditText edtTimerMinutes;

    private TextView txtReminderTime;
    private TextView tvSheetTitle;
    private Button btnSaveHabit;

    private TextView txtSelectedIcon;
    private View viewSelectedIconBg;
    private LinearLayout iconPickerGrid;
    private LinearLayout colorPickerRow;

    private SwitchMaterial switchReminder;

    private ChipGroup chipGroupDays;
    private TextView tvWeeklyDaysLabel;

    private MaterialButtonToggleGroup toggleTrackType;
    private MaterialButton btnTrackTask;
    private MaterialButton btnTrackAmount;
    private MaterialButton btnTrackTime;

    private MaterialButtonToggleGroup toggleRepeat;
    private MaterialButton btnRepeatDaily;
    private MaterialButton btnRepeatWeekly;
    private MaterialButton btnRepeatMonthly;

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

        selectedIcon = ICONS[new Random().nextInt(ICONS.length)];
        selectedColor = COLORS[0];

        initViews(view);
        setupIconPicker();
        setupColorPicker();
        setupRepeatToggle();
        initEvents();

        if (getArguments() != null && getArguments().containsKey("EDIT_HABIT")) {
            habitToEdit = (Habit) getArguments().getSerializable("EDIT_HABIT");
        }

        if (habitToEdit != null) {
            bindHabitToForm(habitToEdit);
        } else {
            txtSelectedIcon.setText(selectedIcon);
            setIconPreviewColor(selectedColor);

            btnTrackTask.setChecked(true);
            btnRepeatDaily.setChecked(true);

            updateTrackTypeUI();
            updateTrackButtonsUI();
            updateRepeatUI();
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
        btnSaveHabit = view.findViewById(R.id.btnSaveHabit);

        txtSelectedIcon = view.findViewById(R.id.txtSelectedIcon);
        viewSelectedIconBg = view.findViewById(R.id.viewSelectedIconBg);
        iconPickerGrid = view.findViewById(R.id.iconPickerGrid);
        colorPickerRow = view.findViewById(R.id.colorPickerRow);

        switchReminder = view.findViewById(R.id.switchReminder);
        chipGroupDays = view.findViewById(R.id.chipGroupDays);
        tvWeeklyDaysLabel = view.findViewById(R.id.tvWeeklyDaysLabel);

        toggleTrackType = view.findViewById(R.id.toggleTrackType);
        btnTrackTask = view.findViewById(R.id.btnTrackTask);
        btnTrackAmount = view.findViewById(R.id.btnTrackAmount);
        btnTrackTime = view.findViewById(R.id.btnTrackTime);

        toggleRepeat = view.findViewById(R.id.toggleRepeat);
        btnRepeatDaily = view.findViewById(R.id.btnRepeatDaily);
        btnRepeatWeekly = view.findViewById(R.id.btnRepeatWeekly);
        btnRepeatMonthly = view.findViewById(R.id.btnRepeatMonthly);

        layoutReminderTime = view.findViewById(R.id.layoutReminderTime);
        layoutAmountFields = view.findViewById(R.id.layoutAmountFields);
        layoutTimerFields = view.findViewById(R.id.layoutTimerFields);
    }

    private void setupIconPicker() {
        if (iconPickerGrid == null) return;

        iconPickerGrid.removeAllViews();
        iconPickerGrid.setVisibility(View.GONE);

        LinearLayout currentRow = null;

        for (int i = 0; i < ICONS.length; i++) {
            if (i % 4 == 0) {
                currentRow = new LinearLayout(requireContext());
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                iconPickerGrid.addView(currentRow);
            }

            final String icon = ICONS[i];

            TextView iconView = new TextView(requireContext());
            iconView.setText(icon);
            iconView.setTextSize(24f);
            iconView.setGravity(Gravity.CENTER);

            int size = dp(56);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, size, 1f);
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            iconView.setLayoutParams(params);

            iconView.setOnClickListener(v -> {
                selectedIcon = icon;
                txtSelectedIcon.setText(selectedIcon);
                updateIconPickerSelection();
                iconPickerGrid.setVisibility(View.GONE);
            });

            if (currentRow != null) {
                currentRow.addView(iconView);
            }
        }

        txtSelectedIcon.setText(selectedIcon);
        setIconPreviewColor(selectedColor);

        View.OnClickListener previewClick = v -> toggleIconGrid();
        txtSelectedIcon.setOnClickListener(previewClick);
        viewSelectedIconBg.setOnClickListener(previewClick);
    }

    private void toggleIconGrid() {
        if (iconPickerGrid == null) return;
        iconPickerGrid.setVisibility(
                iconPickerGrid.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
        );
    }

    private void updateIconPickerSelection() {
        for (int i = 0; i < iconPickerGrid.getChildCount(); i++) {
            View rowView = iconPickerGrid.getChildAt(i);
            if (!(rowView instanceof LinearLayout)) continue;

            LinearLayout row = (LinearLayout) rowView;
            for (int j = 0; j < row.getChildCount(); j++) {
                View child = row.getChildAt(j);
                if (!(child instanceof TextView)) continue;

                TextView tv = (TextView) child;
                boolean selected = selectedIcon.equals(tv.getText().toString());

                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(dp(14));
                bg.setColor(selected ? Color.parseColor("#202020") : Color.TRANSPARENT);
                if (selected) {
                    bg.setStroke(dp(2), Color.parseColor(selectedColor));
                }

                tv.setBackground(bg);
            }
        }
    }

    private void setupColorPicker() {
        if (colorPickerRow == null) return;

        colorPickerRow.removeAllViews();

        for (String color : COLORS) {
            View colorDot = new View(requireContext());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(32), dp(32));
            params.setMargins(0, 0, dp(10), 0);
            colorDot.setLayoutParams(params);

            setColorDotDrawable(colorDot, color, color.equals(selectedColor));

            colorDot.setOnClickListener(v -> {
                selectedColor = color;
                setIconPreviewColor(selectedColor);
                updateColorPickerSelection();
                updateIconPickerSelection();
            });

            colorPickerRow.addView(colorDot);
        }

        setIconPreviewColor(selectedColor);
    }

    private void updateColorPickerSelection() {
        for (int i = 0; i < colorPickerRow.getChildCount(); i++) {
            View child = colorPickerRow.getChildAt(i);
            setColorDotDrawable(child, COLORS[i], COLORS[i].equals(selectedColor));
        }
    }

    private void setColorDotDrawable(View target, String color, boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.parseColor(color));
        if (selected) {
            drawable.setStroke(dp(3), Color.WHITE);
        }
        target.setBackground(drawable);
    }

    private void setIconPreviewColor(String colorHex) {
        try {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(Color.parseColor(colorHex));
            viewSelectedIconBg.setBackground(bg);
        } catch (Exception ignored) {
        }
    }

    private void setupRepeatToggle() {
        if (toggleRepeat == null) return;
        toggleRepeat.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            updateRepeatUI();
            updateWeeklyDaysVisibility();
        });
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

        toggleTrackType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            updateTrackTypeUI();
            updateTrackButtonsUI();
        });
    }

    private void updateTrackTypeUI() {
        if (btnTrackTask.isChecked()) {
            layoutAmountFields.setVisibility(View.GONE);
            layoutTimerFields.setVisibility(View.GONE);
        } else if (btnTrackAmount.isChecked()) {
            layoutAmountFields.setVisibility(View.VISIBLE);
            layoutTimerFields.setVisibility(View.GONE);
        } else {
            layoutAmountFields.setVisibility(View.GONE);
            layoutTimerFields.setVisibility(View.VISIBLE);
        }
    }

    private void updateTrackButtonsUI() {
        updateSingleTrackButton(btnTrackTask, btnTrackTask.isChecked());
        updateSingleTrackButton(btnTrackAmount, btnTrackAmount.isChecked());
        updateSingleTrackButton(btnTrackTime, btnTrackTime.isChecked());
    }

    private void updateRepeatUI() {
        updateSingleTrackButton(btnRepeatDaily, btnRepeatDaily.isChecked());
        updateSingleTrackButton(btnRepeatWeekly, btnRepeatWeekly.isChecked());
        updateSingleTrackButton(btnRepeatMonthly, btnRepeatMonthly.isChecked());
    }

    private void updateSingleTrackButton(MaterialButton button, boolean selected) {
        if (button == null) return;

        if (selected) {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1E6F3A")));
            button.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#1E6F3A")));
            button.setTextColor(Color.WHITE);
        } else {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#111111")));
            button.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#444444")));
            button.setTextColor(Color.parseColor("#BBBBBB"));
        }
    }

    private void updateReminderVisibility() {
        layoutReminderTime.setVisibility(switchReminder.isChecked() ? View.VISIBLE : View.GONE);
    }

    private void updateWeeklyDaysVisibility() {
        boolean isWeekly = btnRepeatWeekly != null && btnRepeatWeekly.isChecked();
        tvWeeklyDaysLabel.setVisibility(isWeekly ? View.VISIBLE : View.GONE);
        chipGroupDays.setVisibility(isWeekly ? View.VISIBLE : View.GONE);
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(calendar.get(Calendar.MINUTE))
                .setTitleText("Chọn giờ nhắc")
                .build();

        picker.addOnPositiveButtonClickListener(dialog -> {
            String formattedTime = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    picker.getHour(),
                    picker.getMinute()
            );
            txtReminderTime.setText(formattedTime);
        });

        picker.show(getChildFragmentManager(), "TIME_PICKER");
    }

    private void bindHabitToForm(Habit habit) {
        tvSheetTitle.setText("Cập nhật thói quen");
        btnSaveHabit.setText("Cập nhật");

        edtHabitName.setText(habit.getTitle());
        edtHabitDescription.setText(habit.getDescription());

        selectedIcon = habit.getIconEmoji();
        selectedColor = habit.getColor();

        txtSelectedIcon.setText(selectedIcon);
        setIconPreviewColor(selectedColor);
        updateColorPickerSelection();
        updateIconPickerSelection();

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

        boolean reminderEnabled = habit.isReminderEnabled() && !TextUtils.isEmpty(habit.getReminderTime());
        switchReminder.setChecked(reminderEnabled);
        txtReminderTime.setText(reminderEnabled ? habit.getReminderTime() : "Chọn giờ");

        String freq = habit.getFrequencyType();
        if (Constants.FREQUENCY_WEEKLY.equalsIgnoreCase(freq)) {
            btnRepeatWeekly.setChecked(true);
        } else if (Constants.FREQUENCY_MONTHLY.equalsIgnoreCase(freq)) {
            btnRepeatMonthly.setChecked(true);
        } else {
            btnRepeatDaily.setChecked(true);
        }

        updateTrackTypeUI();
        updateTrackButtonsUI();
        updateRepeatUI();
        updateReminderVisibility();
        updateWeeklyDaysVisibility();
    }

    private void saveHabit() {
        String name = edtHabitName.getText().toString().trim();
        if (name.isEmpty()) {
            toast("Vui lòng nhập tên thói quen");
            return;
        }

        String habitType;
        int targetValue;
        String unit;

        if (btnTrackTask.isChecked()) {
            habitType = Constants.HABIT_TYPE_CHECKBOX;
            targetValue = 1;
            unit = "lần";
        } else if (btnTrackAmount.isChecked()) {
            String valueStr = edtTargetValue.getText().toString().trim();
            String unitStr = edtTargetUnit.getText().toString().trim();

            if (valueStr.isEmpty()) {
                toast("Vui lòng nhập mục tiêu");
                return;
            }

            try {
                targetValue = Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                toast("Giá trị không hợp lệ");
                return;
            }

            if (targetValue <= 0) {
                toast("Mục tiêu phải lớn hơn 0");
                return;
            }

            if (unitStr.isEmpty()) {
                toast("Vui lòng nhập đơn vị");
                return;
            }

            habitType = Constants.HABIT_TYPE_COUNTER;
            unit = unitStr;
        } else {
            String timerStr = edtTimerMinutes.getText().toString().trim();

            if (timerStr.isEmpty()) {
                toast("Vui lòng nhập số phút");
                return;
            }

            try {
                targetValue = Integer.parseInt(timerStr);
            } catch (NumberFormatException e) {
                toast("Số phút không hợp lệ");
                return;
            }

            if (targetValue <= 0) {
                toast("Số phút phải lớn hơn 0");
                return;
            }

            habitType = Constants.HABIT_TYPE_TIMER;
            unit = "phút";
        }

        String frequency;
        if (btnRepeatWeekly.isChecked()) {
            frequency = Constants.FREQUENCY_WEEKLY;
        } else if (btnRepeatMonthly.isChecked()) {
            frequency = Constants.FREQUENCY_MONTHLY;
        } else {
            frequency = Constants.FREQUENCY_DAILY;
        }

        int userId = SessionManager.getUserId(requireContext());
        boolean reminderEnabled = switchReminder.isChecked();
        String reminderTime = txtReminderTime.getText().toString().trim();
        String description = edtHabitDescription.getText().toString().trim();
        String daysOfWeek = getSelectedDaysOfWeek();

        if (reminderEnabled && (reminderTime.isEmpty() || "Chọn giờ".equals(reminderTime))) {
            toast("Vui lòng chọn giờ nhắc nhở");
            return;
        }

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new java.util.Date());

        final Habit habitToSave;
        final boolean isUpdating;

        if (habitToEdit != null) {
            isUpdating = true;
            habitToEdit.setUserId(userId);
            habitToEdit.setTitle(name);
            habitToEdit.setDescription(description);
            habitToEdit.setIconEmoji(selectedIcon);
            habitToEdit.setColor(selectedColor);
            habitToEdit.setHabitType(habitType);
            habitToEdit.setTargetValue(targetValue);
            habitToEdit.setUnit(unit);
            habitToEdit.setFrequencyType(frequency);
            habitToEdit.setReminderEnabled(reminderEnabled);
            habitToEdit.setReminderTime(reminderEnabled ? reminderTime : "");
            habitToEdit.setUpdatedAt(now);
            habitToSave = habitToEdit;
        } else {
            isUpdating = false;
            habitToSave = new Habit(
                    userId,
                    name,
                    description,
                    "Khác",
                    selectedIcon,
                    selectedColor,
                    habitType,
                    targetValue,
                    unit,
                    frequency,
                    reminderEnabled,
                    reminderEnabled ? reminderTime : "",
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

                if (habitToSave.isReminderEnabled() && !TextUtils.isEmpty(habitToSave.getReminderTime())) {
                    int requestCode = oldReminder != null ? oldReminder.getRequestCode() : habitToSave.getId();

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
                        toast(isUpdating ? "Đã cập nhật thói quen" : "Đã tạo thói quen");
                        getParentFragmentManager().setFragmentResult("refresh_habits", new Bundle());
                        dismiss();
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            toast("Lỗi: " + e.getMessage())
                    );
                }
            }
        }).start();
    }

    private String getSelectedDaysOfWeek() {
        if (chipGroupDays.getVisibility() != View.VISIBLE) return "";

        java.util.List<String> selectedDays = new java.util.ArrayList<>();
        for (int i = 0; i < chipGroupDays.getChildCount(); i++) {
            View child = chipGroupDays.getChildAt(i);
            if (child instanceof Chip && ((Chip) child).isChecked()) {
                selectedDays.add(((Chip) child).getText().toString());
            }
        }
        return TextUtils.join(",", selectedDays);
    }

    private int[] parseReminderTime(String timeText) {
        if (timeText == null || timeText.trim().isEmpty()) return null;

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", Locale.US);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(timeText.trim()));
            return new int[]{
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE)
            };
        } catch (Exception ignored) {
        }

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", Locale.US);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(timeText.trim()));
            return new int[]{
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE)
            };
        } catch (Exception ignored) {
        }

        return null;
    }

    private void toast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int value) {
        return (int) (value * requireContext().getResources().getDisplayMetrics().density);
    }
}