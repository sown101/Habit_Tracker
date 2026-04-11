package com.example.habittracker.ui.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.ui.home.HabitDetailDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    public interface OnHabitCheckedChangeListener {
        void onHabitCheckedChanged(Habit habit, boolean isChecked, int position);
    }

    public interface OnCounterActionListener {
        void onCounterPlus(Habit habit, int position);
        void onCounterMinus(Habit habit, int position);
    }

    private final List<Habit> habitList;
    private final OnHabitCheckedChangeListener checkedChangeListener;
    private final OnCounterActionListener counterActionListener;

    public HabitAdapter(List<Habit> habitList,
                        OnHabitCheckedChangeListener checkedChangeListener,
                        OnCounterActionListener counterActionListener) {
        this.habitList = habitList != null ? habitList : new ArrayList<>();
        this.checkedChangeListener = checkedChangeListener;
        this.counterActionListener = counterActionListener;
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
        holder.bind(habitList.get(position));
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public void updateData(List<Habit> newHabits) {
        habitList.clear();
        if (newHabits != null) {
            habitList.addAll(newHabits);
        }
        notifyDataSetChanged();
    }

    class HabitViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgHabitIcon;
        private final TextView txtHabitTitle;
        private final TextView txtHabitTime;
        private final CheckBox cbHabitComplete;

        private final LinearLayout layoutCompleteAction;
        private final LinearLayout layoutCounterAction;
        private final TextView btnMinus;
        private final TextView btnPlus;
        private final TextView txtCounterValue;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHabitIcon = itemView.findViewById(R.id.imgHabitIcon);
            txtHabitTitle = itemView.findViewById(R.id.txtHabitTitle);
            txtHabitTime = itemView.findViewById(R.id.txtHabitTime);
            cbHabitComplete = itemView.findViewById(R.id.cbHabitComplete);

            layoutCompleteAction = itemView.findViewById(R.id.layoutCompleteAction);
            layoutCounterAction = itemView.findViewById(R.id.layoutCounterAction);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            txtCounterValue = itemView.findViewById(R.id.txtCounterValue);
        }

        void bind(Habit habit) {
            txtHabitTitle.setText(habit.getTitle());
            txtHabitTime.setText(buildSubtitle(habit));

            if (habit.isCounterHabit()) {
                bindCounterHabit(habit);
            } else {
                bindCompleteHabit(habit);
            }

            updateVisualState(habit);

            itemView.setOnClickListener(v -> {
                if (itemView.getContext() instanceof FragmentActivity) {
                    FragmentActivity activity = (FragmentActivity) itemView.getContext();
                    HabitDetailDialogFragment dialog = HabitDetailDialogFragment.newInstance(habit);
                    dialog.show(activity.getSupportFragmentManager(), "habit_detail_dialog");
                }
            });
        }

        private void bindCompleteHabit(Habit habit) {
            layoutCompleteAction.setVisibility(View.VISIBLE);
            layoutCounterAction.setVisibility(View.GONE);

            cbHabitComplete.setOnCheckedChangeListener(null);
            cbHabitComplete.setChecked(habit.isCompletedToday());
            cbHabitComplete.setContentDescription("Đánh dấu hoàn thành");

            cbHabitComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }

                if (checkedChangeListener != null) {
                    checkedChangeListener.onHabitCheckedChanged(habit, isChecked, position);
                }
            });
        }

        private void bindCounterHabit(Habit habit) {
            layoutCompleteAction.setVisibility(View.GONE);
            layoutCounterAction.setVisibility(View.VISIBLE);

            int current = Math.max(0, habit.getCurrentValueToday());
            int target = habit.getSafeTargetValue();
            txtCounterValue.setText(String.format(Locale.getDefault(), "%d/%d", current, target));

            btnMinus.setAlpha(current > 0 ? 1f : 0.4f);

            btnPlus.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                if (counterActionListener != null) {
                    counterActionListener.onCounterPlus(habit, position);
                }
            });

            btnMinus.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                if (counterActionListener != null) {
                    counterActionListener.onCounterMinus(habit, position);
                }
            });
        }

        private String buildSubtitle(Habit habit) {
            String frequency = safeText(habit.getFrequency());
            String unit = safeText(habit.getDisplayUnit());

            if (habit.isCounterHabit()) {
                int current = Math.max(0, habit.getCurrentValueToday());
                int target = habit.getSafeTargetValue();

                String progress = String.format(
                        Locale.getDefault(),
                        "%d/%d %s",
                        current,
                        target,
                        unit
                ).trim();

                if (!TextUtils.isEmpty(frequency)) {
                    if (habit.isCompletedToday()) {
                        return progress + " • " + frequency + " • Đã đạt mục tiêu";
                    }
                    return progress + " • " + frequency;
                } else {
                    if (habit.isCompletedToday()) {
                        return progress + " • Đã đạt mục tiêu";
                    }
                    return progress;
                }
            } else {
                if (!TextUtils.isEmpty(frequency)) {
                    return frequency + (habit.isCompletedToday() ? " • Hoàn thành" : " • Chưa hoàn thành");
                }
                return habit.isCompletedToday() ? "Hoàn thành" : "Chưa hoàn thành";
            }
        }

        private void updateVisualState(Habit habit) {
            if (habit.isCompletedToday()) {
                txtHabitTitle.setAlpha(0.75f);
                txtHabitTime.setAlpha(0.75f);
                imgHabitIcon.setAlpha(0.75f);
                txtHabitTime.setTextColor(Color.parseColor("#2E7D32"));
            } else {
                txtHabitTitle.setAlpha(1f);
                txtHabitTime.setAlpha(1f);
                imgHabitIcon.setAlpha(1f);
                txtHabitTime.setTextColor(Color.parseColor("#757575"));
            }
        }

        private String safeText(String value) {
            return value == null ? "" : value.trim();
        }
    }
}