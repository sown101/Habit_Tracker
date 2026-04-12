package com.example.habittracker.ui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.ui.home.HabitDetailDialogFragment;
import com.example.habittracker.utils.Constants;

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

    public interface OnTimerActionListener {
        void onTimerClick(Habit habit, int position);
    }

    private final List<Habit> habitList;
    private final OnHabitCheckedChangeListener checkedChangeListener;
    private final OnCounterActionListener counterActionListener;
    private final OnTimerActionListener timerActionListener;

    public HabitAdapter(List<Habit> habitList,
                        OnHabitCheckedChangeListener checkedChangeListener,
                        OnCounterActionListener counterActionListener,
                        OnTimerActionListener timerActionListener) {
        this.habitList = habitList != null ? habitList : new ArrayList<>();
        this.checkedChangeListener = checkedChangeListener;
        this.counterActionListener = counterActionListener;
        this.timerActionListener = timerActionListener;
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

        private final FrameLayout layoutActionContainer;
        private final LinearLayout layoutCompleteAction;
        private final LinearLayout layoutCounterAction;
        private final LinearLayout layoutTimerAction;

        private final TextView btnMinus;
        private final TextView btnPlus;
        private final TextView txtCounterValue;

        private final TextView btnStartTimer;
        private final TextView txtTimerValue;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHabitIcon = itemView.findViewById(R.id.imgHabitIcon);
            txtHabitTitle = itemView.findViewById(R.id.txtHabitTitle);
            txtHabitTime = itemView.findViewById(R.id.txtHabitTime);
            cbHabitComplete = itemView.findViewById(R.id.cbHabitComplete);

            layoutActionContainer = itemView.findViewById(R.id.layoutActionContainer);
            layoutCompleteAction = itemView.findViewById(R.id.layoutCompleteAction);
            layoutCounterAction = itemView.findViewById(R.id.layoutCounterAction);
            layoutTimerAction = itemView.findViewById(R.id.layoutTimerAction);

            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            txtCounterValue = itemView.findViewById(R.id.txtCounterValue);

            btnStartTimer = itemView.findViewById(R.id.btnStartTimer);
            txtTimerValue = itemView.findViewById(R.id.txtTimerValue);
        }

        void bind(Habit habit) {
            txtHabitTitle.setText(habit.getTitle());
            txtHabitTime.setText(buildSubtitle(habit));

            if (habit.isTimerHabit()) {
                bindTimerHabit(habit);
            } else if (habit.isCounterHabit()) {
                bindCounterHabit(habit);
            } else {
                bindTaskHabit(habit);
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

        private void bindTaskHabit(Habit habit) {
            layoutCompleteAction.setVisibility(View.VISIBLE);
            layoutCounterAction.setVisibility(View.GONE);
            layoutTimerAction.setVisibility(View.GONE);

            cbHabitComplete.setOnCheckedChangeListener(null);
            cbHabitComplete.setChecked(habit.isCompletedToday());

            cbHabitComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                if (checkedChangeListener != null) {
                    checkedChangeListener.onHabitCheckedChanged(habit, isChecked, position);
                }
            });
        }

        private void bindCounterHabit(Habit habit) {
            layoutCompleteAction.setVisibility(View.GONE);
            layoutCounterAction.setVisibility(View.VISIBLE);
            layoutTimerAction.setVisibility(View.GONE);

            int current = Math.max(0, habit.getCurrentValueToday());
            int target = habit.getSafeTargetValue();
            txtCounterValue.setText(String.format(Locale.getDefault(), "%d/%d", current, target));
            btnMinus.setAlpha(current > 0 ? 1f : 0.4f);

            btnPlus.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;
                if (counterActionListener != null) {
                    counterActionListener.onCounterPlus(habit, position);
                }
            });

            btnMinus.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;
                if (counterActionListener != null) {
                    counterActionListener.onCounterMinus(habit, position);
                }
            });
        }

        private void bindTimerHabit(Habit habit) {
            layoutCompleteAction.setVisibility(View.GONE);
            layoutCounterAction.setVisibility(View.GONE);
            layoutTimerAction.setVisibility(View.VISIBLE);

            txtTimerValue.setText(getTimerPreview(itemView.getContext(), habit));

            btnStartTimer.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;
                if (timerActionListener != null) {
                    timerActionListener.onTimerClick(habit, position);
                }
            });
        }

        private String buildSubtitle(Habit habit) {
            String frequency = safeText(habit.getFrequency());

            if (habit.isTimerHabit()) {
                String timerText = formatMinutesToShort(habit.getSafeTargetValue());
                return TextUtils.isEmpty(frequency) ? timerText : timerText + " • " + frequency;
            }

            if (habit.isCounterHabit()) {
                int current = Math.max(0, habit.getCurrentValueToday());
                int target = habit.getSafeTargetValue();
                String unit = safeText(habit.getDisplayUnit());

                String progress = String.format(
                        Locale.getDefault(),
                        "%d/%d %s",
                        current,
                        target,
                        unit
                ).trim();

                if (!TextUtils.isEmpty(frequency)) {
                    return habit.isCompletedToday()
                            ? progress + " • " + frequency + " • Đã đạt mục tiêu"
                            : progress + " • " + frequency;
                }
                return habit.isCompletedToday() ? progress + " • Đã đạt mục tiêu" : progress;
            }

            if (!TextUtils.isEmpty(frequency)) {
                return frequency + (habit.isCompletedToday() ? " • Hoàn thành" : " • Chưa hoàn thành");
            }
            return habit.isCompletedToday() ? "Hoàn thành" : "Chưa hoàn thành";
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

        private String formatMinutesToShort(int minutes) {
            return String.format(Locale.getDefault(), "%02d:00", Math.max(0, minutes));
        }

        private String getTimerPreview(Context context, Habit habit) {
            SharedPreferences prefs =
                    context.getSharedPreferences(Constants.PREF_TIMER, Context.MODE_PRIVATE);

            long defaultMillis = habit.getSafeTargetValue() * 60L * 1000L;
            long remaining = prefs.getLong("timer_remaining_" + habit.getId(), defaultMillis);

            if (remaining <= 0) {
                remaining = defaultMillis;
            }

            long totalSeconds = remaining / 1000;
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;

            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }
}