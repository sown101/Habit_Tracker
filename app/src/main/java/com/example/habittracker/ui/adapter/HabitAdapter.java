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
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.ui.home.HabitDetailDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    public interface OnHabitCheckedChangeListener {
        void onHabitCheckedChanged(Habit habit, boolean isChecked, int position);
    }

    private final List<Habit> habitList = new ArrayList<>();
    private final OnHabitCheckedChangeListener checkedChangeListener;

    public HabitAdapter(List<Habit> habits, OnHabitCheckedChangeListener listener) {
        if (habits != null) {
            habitList.addAll(habits);
        }
        this.checkedChangeListener = listener;
    }

    public void updateData(List<Habit> newHabits) {
        habitList.clear();
        if (newHabits != null) {
            habitList.addAll(newHabits);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit currentHabit = habitList.get(position);

        holder.txtHabitTitle.setText(currentHabit.getTitle());

        String unit = currentHabit.getUnit() == null ? "" : currentHabit.getUnit();
        String statusText = currentHabit.isCompletedToday() ? "Hoàn thành" : "Chưa hoàn thành";
        String infoText = currentHabit.getTargetValue() + " " + unit + " • " + statusText;
        holder.txtHabitTime.setText(infoText.trim());

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

        holder.cbHabitComplete.setOnCheckedChangeListener(null);
        holder.cbHabitComplete.setChecked(currentHabit.isCompletedToday());

        holder.cbHabitComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkedChangeListener != null) {
                checkedChangeListener.onHabitCheckedChanged(currentHabit, isChecked, holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            androidx.fragment.app.FragmentManager fragmentManager =
                    ((androidx.appcompat.app.AppCompatActivity) holder.itemView.getContext())
                            .getSupportFragmentManager();

            HabitDetailDialogFragment dialog = HabitDetailDialogFragment.newInstance(currentHabit);
            dialog.show(fragmentManager, "HabitDetailDialog");
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public void updateHabitCheckedState(int position, boolean isChecked) {
        if (position >= 0 && position < habitList.size()) {
            habitList.get(position).setCompletedToday(isChecked);
            notifyItemChanged(position);
        }
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