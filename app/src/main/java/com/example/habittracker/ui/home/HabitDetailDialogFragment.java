package com.example.habittracker.ui.home; // Sửa lại package nếu cần

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.habittracker.R;
import com.example.habittracker.data.model.Habit;

public class HabitDetailDialogFragment extends DialogFragment {

    private static final String ARG_HABIT = "habit_data";

    // Hàm chuẩn để truyền dữ liệu vào Fragment
    public static HabitDetailDialogFragment newInstance(Habit habit) {
        HabitDetailDialogFragment fragment = new HabitDetailDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_HABIT, habit); // Truyền Object Habit
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Nạp giao diện
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_habit_detail, null);

        // Ánh xạ View
        TextView tvName = view.findViewById(R.id.tvDetailName);
        TextView tvCategory = view.findViewById(R.id.tvDetailCategory);
        TextView tvTarget = view.findViewById(R.id.tvDetailTarget);
        TextView tvFrequency = view.findViewById(R.id.tvDetailFrequency);
        TextView tvReminder = view.findViewById(R.id.tvDetailReminder);
        Button btnClose = view.findViewById(R.id.btnDetailClose);

        android.widget.ImageButton btnEditHabit = view.findViewById(R.id.btnEditHabit);
        android.widget.ImageButton btnDeleteHabit = view.findViewById(R.id.btnDeleteHabit); // Nút Xóa

        // (Nếu bạn đã làm bước 3: Thêm tvDetailStreak vào XML thì bỏ comment 2 dòng dưới đây)
        // TextView tvStreak = view.findViewById(R.id.tvDetailStreak);

        // 1. LẤY BIẾN HABIT TỪ BUNDLE (Giải quyết lỗi báo đỏ scope)
        Habit tempHabit = null;
        if (getArguments() != null) {
            tempHabit = (Habit) getArguments().getSerializable(ARG_HABIT);
        }

        // Gán vào một biến "final" để các nút Sửa, Xóa bên dưới có thể dùng được
        final Habit habit = tempHabit;

        // 2. ĐỔ DỮ LIỆU LÊN MÀN HÌNH
        if (habit != null) {
            tvName.setText(habit.getTitle());
            tvCategory.setText(habit.getCategory());
            tvTarget.setText("🎯 Mục tiêu: " + habit.getTargetValue() + " " + habit.getUnit());
            tvFrequency.setText("🔄 Tần suất: " + habit.getFrequency());

            if (habit.isReminderEnabled() && !habit.getReminderTime().isEmpty()) {
                tvReminder.setText("⏰ Nhắc nhở: " + habit.getReminderTime());
            } else {
                tvReminder.setText("⏰ Nhắc nhở: Không bật");
            }

            // (Nếu bạn đã làm bước 3: Thêm Streak thì bỏ comment dòng dưới đây)
            // tvStreak.setText("🔥 Chuỗi: " + habit.getCurrentStreak() + " ngày");
        }

        // 3. XỬ LÝ NÚT SỬA
        btnEditHabit.setOnClickListener(v -> {
            if (habit == null) return;

            dismiss(); // Tắt popup chi tiết này đi

            // Mở BottomSheet AddHabit lên và truyền Object habit này qua đó
            com.example.habittracker.ui.home.AddHabit editBottomSheet = new com.example.habittracker.ui.home.AddHabit();
            Bundle bundle = new Bundle();
            bundle.putSerializable("EDIT_HABIT", habit); // Không còn bị lỗi đỏ nữa!
            editBottomSheet.setArguments(bundle);
            editBottomSheet.show(requireActivity().getSupportFragmentManager(), "EditHabit");
        });

        // 4. XỬ LÝ NÚT XÓA (Tạo hộp thoại xác nhận chuẩn bài giảng Chương 4)
        btnDeleteHabit.setOnClickListener(v -> {
            if (habit == null) return;

            new AlertDialog.Builder(requireContext())
                    .setTitle("Xóa thói quen")
                    .setMessage("Bạn có chắc chắn muốn xóa thói quen '" + habit.getTitle() + "' không? Dữ liệu không thể khôi phục.")
                    .setPositiveButton("Xóa", (dialogInterface, i) -> {
                        // Chạy luồng ngầm để xóa khỏi Database
                        new Thread(() -> {
                            com.example.habittracker.data.db.AppDatabase.getInstance(requireContext())
                                    .habitDao().delete(habit);

                            // Cập nhật giao diện trên luồng chính
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    android.widget.Toast.makeText(getContext(), "Đã xóa thói quen", android.widget.Toast.LENGTH_SHORT).show();

                                    // Phát tín hiệu cho HomeFragment tự động load lại danh sách
                                    getParentFragmentManager().setFragmentResult("refresh_habits", new Bundle());

                                    // Đóng cái popup chi tiết lại
                                    dismiss();
                                });
                            }
                        }).start();
                    })
                    .setNegativeButton("Hủy", null) // Bấm hủy thì đóng hộp thoại
                    .show();
        });

        // 5. XỬ LÝ NÚT ĐÓNG
        btnClose.setOnClickListener(v -> dismiss());

        // 6. KHỞI TẠO DIALOG (Xoá viền trắng mặc định để lộ nền bo góc)
        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setView(view)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        return dialog;
    }

    // Giữ cho dialog hiện lên cách đều 2 cạnh bên (90% width)
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}