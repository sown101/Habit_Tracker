package com.example.habittracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.habittracker.R;
import com.example.habittracker.data.model.Habit;

public class AddHabit extends BottomSheetDialogFragment {

    @Override
    public int getTheme() {
        return R.style.Theme_HabitTracker_BottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_bottom_sheet_add, container, false);

        // --- 1. ÁNH XẠ TOÀN BỘ CÁC VIEW TỪ GIAO DIỆN ---
        EditText edtHabitName = view.findViewById(R.id.edtHabitName);
        EditText edtTargetValue = view.findViewById(R.id.edtTargetValue);
        EditText edtTargetUnit = view.findViewById(R.id.edtTargetUnit);
        TextView txtReminderTime = view.findViewById(R.id.txtReminderTime);
        Button btnSaveHabit = view.findViewById(R.id.btnSaveHabit);
        android.widget.Spinner spinnerFrequency = view.findViewById(R.id.spinnerFrequency);

        com.google.android.material.switchmaterial.SwitchMaterial switchShake = view.findViewById(R.id.switchShake);
        com.google.android.material.switchmaterial.SwitchMaterial switchFocus = view.findViewById(R.id.switchFocus);
        com.google.android.material.chip.ChipGroup chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        com.google.android.material.chip.ChipGroup chipGroupDays = view.findViewById(R.id.chipGroupDays);

        // --- 2. XỬ LÝ SỰ KIỆN ĐỒNG HỒ VÀ SPINNER ---
        txtReminderTime.setOnClickListener(v -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(java.util.Calendar.MINUTE);

            com.google.android.material.timepicker.MaterialTimePicker materialTimePicker =
                    new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                            .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_12H)
                            .setHour(currentHour)
                            .setMinute(currentMinute)
                            .setTitleText("Chọn giờ nhắc nhở")
                            .setInputMode(com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK)
                            .build();

            materialTimePicker.addOnPositiveButtonClickListener(dialog -> {
                int pickedHour = materialTimePicker.getHour();
                int pickedMinute = materialTimePicker.getMinute();
                String amPm;
                int hour12;
                if (pickedHour >= 12) {
                    amPm = "PM";
                    hour12 = (pickedHour == 12) ? 12 : pickedHour - 12;
                } else {
                    amPm = "AM";
                    hour12 = (pickedHour == 0) ? 12 : pickedHour;
                }
                String formattedTime = String.format(java.util.Locale.getDefault(), "%02d:%02d %s", hour12, pickedMinute, amPm);
                txtReminderTime.setText(formattedTime);
            });
            materialTimePicker.show(getChildFragmentManager(), "MATERIAL_TIME_PICKER");
        });

        spinnerFrequency.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    chipGroupDays.setVisibility(View.VISIBLE);
                } else {
                    chipGroupDays.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // --- 3. KIỂM TRA CHẾ ĐỘ SỬA VÀ ĐỔ DỮ LIỆU CŨ LÊN FORM ---
        Habit habitToEdit = null;
        if (getArguments() != null && getArguments().containsKey("EDIT_HABIT")) {
            habitToEdit = (Habit) getArguments().getSerializable("EDIT_HABIT");
        }

        if (habitToEdit != null) {
            btnSaveHabit.setText("Cập nhật"); // Đổi tên nút
            TextView tvSheetTitle = view.findViewById(R.id.tvSheetTitle);
            tvSheetTitle.setText("Cập nhật thói quen");
            edtHabitName.setText(habitToEdit.getTitle());
            edtTargetValue.setText(String.valueOf(habitToEdit.getTargetValue()));
            edtTargetUnit.setText(habitToEdit.getUnit());

            // Đã dùng đúng hàm của bạn: isAllowShakeComplete và isEnableFocusSession
            switchShake.setChecked(habitToEdit.isAllowShakeComplete());
            switchFocus.setChecked(habitToEdit.isEnableFocusSession());

            if (habitToEdit.isReminderEnabled() && habitToEdit.getReminderTime() != null && !habitToEdit.getReminderTime().isEmpty()) {
                txtReminderTime.setText(habitToEdit.getReminderTime());
            }

            if ("Hàng tuần".equals(habitToEdit.getFrequencyType())) {
                spinnerFrequency.setSelection(1);
                chipGroupDays.setVisibility(View.VISIBLE);
            } else {
                spinnerFrequency.setSelection(0);
                chipGroupDays.setVisibility(View.GONE);
            }

            for (int i = 0; i < chipGroupCategory.getChildCount(); i++) {
                com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) chipGroupCategory.getChildAt(i);
                if (chip.getText().toString().equals(habitToEdit.getCategory())) {
                    chip.setChecked(true);
                    break;
                }
            }
        }

        // --- 4. XỬ LÝ LƯU HOẶC CẬP NHẬT KHI BẤM NÚT ---
        final Habit finalHabitToEdit = habitToEdit;

        btnSaveHabit.setOnClickListener(v -> {
            // 4.1 Lấy dữ liệu trên form
            String name = edtHabitName.getText().toString().trim();
            String valueStr = edtTargetValue.getText().toString().trim();
            String unit = edtTargetUnit.getText().toString().trim();
            String reminderTime = txtReminderTime.getText().toString();
            String frequency = spinnerFrequency.getSelectedItem().toString();

            if (name.isEmpty() || valueStr.isEmpty() || unit.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ tên, số và đơn vị mục tiêu!", Toast.LENGTH_SHORT).show();
                return;
            }

            int targetValue = Integer.parseInt(valueStr);
            boolean isShake = switchShake.isChecked();
            boolean isFocus = switchFocus.isChecked();

            String category = "Khác";
            int selectedChipId = chipGroupCategory.getCheckedChipId();
            if (selectedChipId != View.NO_ID) {
                com.google.android.material.chip.Chip selectedChip = view.findViewById(selectedChipId);
                category = selectedChip.getText().toString();
            }

            // 4.2 Cập nhật Object trên Main Thread
            final Habit habitToSave;
            final boolean isUpdating;

            if (finalHabitToEdit != null) {
                isUpdating = true;
                finalHabitToEdit.setTitle(name);
                finalHabitToEdit.setCategory(category);
                finalHabitToEdit.setTargetValue(targetValue);
                finalHabitToEdit.setUnit(unit);
                finalHabitToEdit.setFrequencyType(frequency);
                finalHabitToEdit.setReminderTime(reminderTime);
                finalHabitToEdit.setAllowShakeComplete(isShake);
                finalHabitToEdit.setEnableFocusSession(isFocus);

                habitToSave = finalHabitToEdit;
            } else {
                isUpdating = false;
                habitToSave = new Habit(
                        1, name, "", category, "Regular", targetValue, unit, frequency,
                        true, reminderTime, isShake, isFocus, 0, true, "2026-04-02", "2026-04-02"
                );
            }

            // 4.3 Chạy luồng ngầm (Background Thread) để thao tác Database
            new Thread(() -> {
                try {
                    com.example.habittracker.data.db.AppDatabase db = com.example.habittracker.data.db.AppDatabase.getInstance(requireContext());

                    if (isUpdating) {
                        db.habitDao().update(habitToSave);
                    } else {
                        db.habitDao().insert(habitToSave);
                    }

                    // 4.4 Trở về luồng chính cập nhật giao diện
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            String msg = isUpdating ? "Đã cập nhật thói quen!" : "Đã lưu thói quen thành công!";
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();

                            // Phát tín hiệu tải lại màn hình Home
                            getParentFragmentManager().setFragmentResult("refresh_habits", new Bundle());
                            dismiss();
                        });
                    }
                } catch (Exception e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Lỗi DB: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            }).start();
        });

        return view;
    }
}