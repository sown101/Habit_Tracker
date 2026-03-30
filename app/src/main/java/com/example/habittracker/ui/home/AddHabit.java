package com.example.habittracker.ui.home;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.habittracker.R;

public class AddHabit  extends BottomSheetDialogFragment {
    @Override
    public int getTheme() {
        // Ép hệ thống phải dùng cái Theme bo góc mà bạn đã tạo trong themes.xml
        return R.style.Theme_HabitTracker_BottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp giao diện XML vừa tạo
        View view = inflater.inflate(R.layout.layout_bottom_sheet_add, container, false);

        EditText edtHabitName = view.findViewById(R.id.edtHabitName);
        Button btnSaveHabit = view.findViewById(R.id.btnSaveHabit);

        // Bắt sự kiện khi bấm nút Lưu
        btnSaveHabit.setOnClickListener(v -> {
            String habitName = edtHabitName.getText().toString();
            if (habitName.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên thói quen!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Đã lưu: " + habitName, Toast.LENGTH_SHORT).show();
                dismiss(); // Lệnh này giúp đóng Bottom Sheet lại
            }
        });
        // Ánh xạ cái TextView hiển thị giờ
        TextView txtReminderTime = view.findViewById(R.id.txtReminderTime);

        // Bắt sự kiện khi người dùng bấm vào chữ "07:00 AM"
        txtReminderTime.setOnClickListener(v -> {
            // Lấy giờ hiện tại
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(java.util.Calendar.MINUTE);

            // TẠO ĐỒNG HỒ MATERIAL 3 XỊN SÒ
            com.google.android.material.timepicker.MaterialTimePicker materialTimePicker =
                    new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                            .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_12H) // Dùng định dạng 12h (AM/PM)
                            .setHour(currentHour)
                            .setMinute(currentMinute)
                            .setTitleText("Chọn giờ nhắc nhở")
                            .setInputMode(com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK) // Mặc định hiển thị mặt đồng hồ xoay
                            .build();

            // Xử lý khi người dùng chọn giờ xong và bấm OK
            materialTimePicker.addOnPositiveButtonClickListener(dialog -> {
                int pickedHour = materialTimePicker.getHour();
                int pickedMinute = materialTimePicker.getMinute();

                // Chuyển đổi sang 12h (AM/PM) như cũ
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
            // Ánh xạ Spinner và ChipGroup chọn ngày
            android.widget.Spinner spinnerFrequency = view.findViewById(R.id.spinnerFrequency);
            com.google.android.material.chip.ChipGroup chipGroupDays = view.findViewById(R.id.chipGroupDays);

            // Bắt sự kiện khi người dùng chọn Tần suất khác nhau
            spinnerFrequency.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {

                    // So sánh bằng vị trí (position): 1 chính là "Hàng tuần"
                    if (position == 1) {
                        // Hiện thanh chọn ngày lên
                        chipGroupDays.setVisibility(View.VISIBLE);
                    } else {
                        // Ẩn thanh chọn ngày đi
                        chipGroupDays.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    // Không làm gì cả
                }

        });

        // --- 3. GOM DỮ LIỆU KHI BẤM NÚT TẠO ---
        android.widget.Button btnCreateHabit = view.findViewById(R.id.btnSaveHabit);

        // Ánh xạ thêm 2 ô mình vừa làm và các nút công tắc
        android.widget.EditText edtTargetValue = view.findViewById(R.id.edtTargetValue);
        android.widget.EditText edtTargetUnit = view.findViewById(R.id.edtTargetUnit);
        com.google.android.material.switchmaterial.SwitchMaterial switchShake = view.findViewById(R.id.switchShake);
        com.google.android.material.switchmaterial.SwitchMaterial switchFocus = view.findViewById(R.id.switchFocus);

        btnCreateHabit.setOnClickListener(v -> {
            // Lấy text từ các ô
            String name = edtHabitName.getText().toString().trim();
            String valueStr = edtTargetValue.getText().toString().trim();
            String unit = edtTargetUnit.getText().toString().trim();
            String reminderTime = txtReminderTime.getText().toString();
            String frequency = spinnerFrequency.getSelectedItem().toString();

            // Bắt lỗi nếu người dùng bỏ trống Tên hoặc Giá trị
            if (name.isEmpty() || valueStr.isEmpty() || unit.isEmpty()) {
                android.widget.Toast.makeText(getContext(), "Vui lòng nhập đủ tên, số và đơn vị mục tiêu!", android.widget.Toast.LENGTH_SHORT).show();
                return; // Dừng lại không làm tiếp
            }

            int targetValue = Integer.parseInt(valueStr);
            boolean isShake = switchShake.isChecked();
            boolean isFocus = switchFocus.isChecked();

            // Mượn Constructor dài ngoằng của bạn để tạo Đối tượng Habit
            // (Tạm thời để category là "Khác" và habitType là "Regular")
            com.example.habittracker.data.model.Habit newHabit = new com.example.habittracker.data.model.Habit(
                    1, // userId tạm
                    name,
                    "", // description
                    "Khác", // category
                    "Regular", // habitType
                    targetValue,
                    unit,
                    frequency,
                    true, // reminderEnabled
                    reminderTime,
                    isShake,
                    isFocus,
                    0, // sessionDurationMinutes
                    true, // isActive
                    "2026-03-30", // createdAt tạm
                    "2026-03-30"  // updatedAt tạm
            );

            // Báo thành công
            android.widget.Toast.makeText(getContext(), "Đã tạo: " + newHabit.getTitle() + " (" + newHabit.getTargetValue() + " " + newHabit.getUnit() + ")", android.widget.Toast.LENGTH_LONG).show();

            // ... (Phần code gom dữ liệu và tạo đối tượng newHabit giữ nguyên ở trên) ...

            // 1. Lấy instance của Database và DAO
            // (Lưu ý: Bạn kiểm tra lại tên hàm getInstance() trong file AppDatabase của nhóm nhé)
            com.example.habittracker.data.db.AppDatabase db = com.example.habittracker.data.db.AppDatabase.getInstance(requireContext());
            com.example.habittracker.data.dao.HabitDao habitDao = db.habitDao();

            // 2. Tạo một luồng chạy ngầm (Background Thread) để lưu dữ liệu
            new Thread(() -> {
                try {
                    // Lệnh insert vào database (Kiểm tra lại tên hàm trong HabitDao của bạn, thường là insert() hoặc addHabit())
                    habitDao.insert(newHabit);

                    // 3. Sau khi lưu xong, phải quay lại Main Thread để cập nhật giao diện (tắt BottomSheet, hiện thông báo)
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            android.widget.Toast.makeText(getContext(), "Đã lưu thói quen thành công!", android.widget.Toast.LENGTH_SHORT).show();

                            getParentFragmentManager().setFragmentResult("refresh_habits", new Bundle());
                            dismiss(); // Đóng cửa sổ trượt

                            // TODO: Chỗ này sau này chúng ta sẽ gọi lệnh để HomeFragment load lại danh sách thói quen
                        });
                    }
                } catch (Exception e) {
                    // Bắt lỗi nếu quá trình lưu database gặp trục trặc
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            android.widget.Toast.makeText(getContext(), "Lỗi khi lưu Database: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                        });
                    }
                }
            }).start(); // Bắt đầu chạy luồng ngầm
        }); // Kết thúc sự kiện click của nút


        return view;
    }
}