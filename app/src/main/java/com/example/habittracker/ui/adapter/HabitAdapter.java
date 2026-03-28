package com.example.habittracker.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;
import com.example.habittracker.data.model.Habit; // Trỏ đúng về file Habit của bạn

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private List<Habit> habitList;

    public HabitAdapter(List<Habit> habitList) {
        this.habitList = habitList;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit currentHabit = habitList.get(position);

        // 1. Set Tiêu đề
        holder.txtHabitTitle.setText(currentHabit.getTitle());

        // 2. Set Thời gian/Mục tiêu (Ví dụ: "1 tiếng" hoặc "20 phút")
        String timeDesc = currentHabit.getTargetValue() + " " + currentHabit.getUnit();
        if (currentHabit.isCompletedToday()) {
            timeDesc += " • Hoàn thành";
        } else {
            timeDesc += " • Chưa hoàn thành";
        }
        holder.txtHabitTime.setText(timeDesc);

        // 3. Đổi Icon dựa vào Category (Thể loại) của thói quen
        String category = currentHabit.getCategory();
        if (category != null) {
            switch (category) {
                case "Học tập":
                    holder.imgHabitIcon.setImageResource(R.drawable.ic_folder);
                    break;
                case "Thể thao":
                    holder.imgHabitIcon.setImageResource(R.drawable.ic_stats);
                    break;
                default:
                    holder.imgHabitIcon.setImageResource(R.drawable.ic_bell);
                    break;
            }
        } else {
            holder.imgHabitIcon.setImageResource(R.drawable.ic_folder);
        }

        // 4. Xử lý Checkbox
        holder.cbHabitComplete.setOnCheckedChangeListener(null); // Xóa bộ lắng nghe cũ
        holder.cbHabitComplete.setChecked(currentHabit.isCompletedToday()); // Set trạng thái

        holder.cbHabitComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentHabit.setCompletedToday(isChecked);
            // Cập nhật lại dòng text "Hoàn thành / Chưa hoàn thành" khi bấm tick
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return habitList == null ? 0 : habitList.size();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHabitIcon;
        TextView txtHabitTitle;
        TextView txtHabitTime;
        CheckBox cbHabitComplete;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHabitIcon = itemView.findViewById(R.id.imgHabitIcon);
            txtHabitTitle = itemView.findViewById(R.id.txtHabitTitle);
            txtHabitTime = itemView.findViewById(R.id.txtHabitTime);
            cbHabitComplete = itemView.findViewById(R.id.cbHabitComplete);
        }
    }
}
