package com.example.habittracker.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;
import com.example.habittracker.data.model.Habit;

import java.util.ArrayList;
import java.util.List;

public class CalendarHabitAdapter extends RecyclerView.Adapter<CalendarHabitAdapter.HabitViewHolder> {

    public interface OnCalendarHabitCheckedListener {
        void onCheckedChanged(Habit habit, boolean isChecked, int position);
    }

    private final List<Habit> habitList = new ArrayList<>();
    private final OnCalendarHabitCheckedListener listener;

    public CalendarHabitAdapter(List<Habit> habits, OnCalendarHabitCheckedListener listener) {
        if (habits != null) {
            habitList.addAll(habits);
        }
        this.listener = listener;
    }

    public void updateData(List<Habit> newHabits) {
        habitList.clear();
        if (newHabits != null) {
            habitList.addAll(newHabits);
        }
        notifyDataSetChanged();
    }

    public void updateHabitState(int position, boolean isChecked) {
        if (position >= 0 && position < habitList.size()) {
            habitList.get(position).setCompletedToday(isChecked);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habitList.get(position);

        holder.tvCalendarHabitName.setText(habit.getTitle());

        String unit = habit.getUnit() == null ? "" : habit.getUnit();
        holder.tvCalendarHabitSub.setText((habit.getTargetValue() + " " + unit).trim());

        holder.cbCalendarHabitDone.setOnCheckedChangeListener(null);
        holder.cbCalendarHabitDone.setChecked(habit.isCompletedToday());

        if (habit.isCompletedToday()) {
            holder.tvCalendarHabitStatus.setText("Đã hoàn thành");
            holder.tvCalendarHabitStatus.setTextColor(0xFF2E7D32);
        } else {
            holder.tvCalendarHabitStatus.setText("Chưa hoàn thành");
            holder.tvCalendarHabitStatus.setTextColor(0xFFC62828);
        }

        holder.cbCalendarHabitDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onCheckedChanged(habit, isChecked, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView tvCalendarHabitName;
        TextView tvCalendarHabitSub;
        TextView tvCalendarHabitStatus;
        CheckBox cbCalendarHabitDone;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCalendarHabitName = itemView.findViewById(R.id.tvCalendarHabitName);
            tvCalendarHabitSub = itemView.findViewById(R.id.tvCalendarHabitSub);
            tvCalendarHabitStatus = itemView.findViewById(R.id.tvCalendarHabitStatus);
            cbCalendarHabitDone = itemView.findViewById(R.id.cbCalendarHabitDone);
        }
    }
}