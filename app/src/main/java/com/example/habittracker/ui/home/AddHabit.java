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

        return view;
    }
}