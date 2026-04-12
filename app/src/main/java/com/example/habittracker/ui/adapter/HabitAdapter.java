package com.example.habittracker.ui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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

        private final CardView cardHabit;
        private final TextView txtIconEmoji;
        private final View viewIconBg;
        private final TextView txtHabitTitle;
        private final TextView txtHabitStreak;
        private final TextView txtHabitSub;

        private final FrameLayout layoutActionContainer;
        private final LinearLayout layoutCompleteAction;
        private final LinearLayout layoutCounterAction;
        private final LinearLayout layoutTimerAction;

        private final CheckBox cbHabitComplete;

        private final TextView btnPlus;
        private final TextView txtCounterValue;

        private final TextView btnStartTimer;
        private final TextView txtTimerValue;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);

            cardHabit = (CardView) itemView;

            txtIconEmoji = itemView.findViewById(R.id.txtIconEmoji);
            viewIconBg = itemView.findViewById(R.id.viewIconBg);
            txtHabitTitle = itemView.findViewById(R.id.txtHabitTitle);
            txtHabitStreak = itemView.findViewById(R.id.txtHabitStreak);
            txtHabitSub = itemView.findViewById(R.id.txtHabitSub);

            layoutActionContainer = itemView.findViewById(R.id.layoutActionContainer);
            layoutCompleteAction = itemView.findViewById(R.id.layoutCompleteAction);
            layoutCounterAction = itemView.findViewById(R.id.layoutCounterAction);
            layoutTimerAction = itemView.findViewById(R.id.layoutTimerAction);

            cbHabitComplete = itemView.findViewById(R.id.cbHabitComplete);

            btnPlus = itemView.findViewById(R.id.btnPlus);
            txtCounterValue = itemView.findViewById(R.id.txtCounterValue);

            btnStartTimer = itemView.findViewById(R.id.btnStartTimer);
            txtTimerValue = itemView.findViewById(R.id.txtTimerValue);
        }

        void bind(Habit habit) {
            txtIconEmoji.setText(habit.getIconEmoji());
            txtHabitTitle.setText(habit.getTitle());

            int streak = habit.getCurrentStreak();
            txtHabitStreak.setText("🔥 " + streak + (streak <= 1 ? " Day" : " Days"));

            txtHabitSub.setText(buildSubtitle(habit));

            setIconBackground(habit.getColor());
            setCardBackground(habit.getColor(), habit.isCompletedToday());

            if (habit.isTimerHabit()) {
                bindTimerHabit(habit);
            } else if (habit.isCounterHabit()) {
                bindCounterHabit(habit);
            } else {
                bindTaskHabit(habit);
            }

            float contentAlpha = habit.isCompletedToday() ? 0.75f : 1f;
            txtHabitTitle.setAlpha(contentAlpha);
            txtHabitSub.setAlpha(contentAlpha);
            txtHabitStreak.setAlpha(contentAlpha);

            itemView.setOnClickListener(v -> {
                Context context = itemView.getContext();
                if (context instanceof FragmentActivity) {
                    HabitDetailDialogFragment dialog =
                            HabitDetailDialogFragment.newInstance(habit);
                    dialog.show(((FragmentActivity) context).getSupportFragmentManager(),
                            "habit_detail_dialog");
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

            txtCounterValue.setText(String.format(
                    Locale.getDefault(),
                    "%d / %d %s",
                    current,
                    target,
                    habit.getDisplayUnit()
            ));

            btnPlus.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                if (counterActionListener != null) {
                    counterActionListener.onCounterPlus(habit, position);
                }
            });
        }

        private void bindTimerHabit(Habit habit) {
            layoutCompleteAction.setVisibility(View.GONE);
            layoutCounterAction.setVisibility(View.GONE);
            layoutTimerAction.setVisibility(View.VISIBLE);

            txtTimerValue.setText(getTimerProgressText(itemView.getContext(), habit));

            View.OnClickListener openTimerClick = v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                if (timerActionListener != null) {
                    timerActionListener.onTimerClick(habit, position);
                }
            };

            btnStartTimer.setOnClickListener(openTimerClick);
            txtTimerValue.setOnClickListener(openTimerClick);
        }

        private String buildSubtitle(Habit habit) {
            String frequency = mapFrequency(habit.getFrequencyType());

            if (habit.isTimerHabit()) {
                return frequency;
            }

            if (habit.isCounterHabit()) {
                int current = Math.max(0, habit.getCurrentValueToday());
                int target = habit.getSafeTargetValue();
                return frequency + " • " + current + "/" + target + " " + habit.getDisplayUnit();
            }

            return frequency;
        }

        private String getTimerProgressText(Context context, Habit habit) {
            SharedPreferences prefs = context.getSharedPreferences(
                    Constants.PREF_TIMER,
                    Context.MODE_PRIVATE
            );

            long totalMillis = habit.getSafeTargetValue() * 60L * 1000L;
            long remainingMillis = prefs.getLong("timer_remaining_" + habit.getId(), totalMillis);
            remainingMillis = Math.max(0L, Math.min(remainingMillis, totalMillis));

            long elapsedMillis = totalMillis - remainingMillis;

            return formatMinuteSecond(elapsedMillis) + " / " + formatMinuteSecond(totalMillis);
        }

        private String formatMinuteSecond(long millis) {
            long totalSeconds = Math.max(0L, millis / 1000L);
            long minutes = totalSeconds / 60L;
            long seconds = totalSeconds % 60L;
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }

        private String mapFrequency(String frequencyType) {
            if (Constants.FREQUENCY_WEEKLY.equalsIgnoreCase(frequencyType)) {
                return "Weekly";
            }
            if (Constants.FREQUENCY_MONTHLY.equalsIgnoreCase(frequencyType)) {
                return "Monthly";
            }
            return "Today";
        }

        private void setIconBackground(String colorHex) {
            try {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.OVAL);
                drawable.setColor(Color.parseColor(colorHex));
                viewIconBg.setBackground(drawable);
            } catch (Exception e) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.OVAL);
                drawable.setColor(Color.parseColor("#4CAF50"));
                viewIconBg.setBackground(drawable);
            }
        }

        private void setCardBackground(String colorHex, boolean completed) {
            try {
                int baseColor = Color.parseColor(colorHex);

                int r = (int) (Color.red(baseColor) * 0.16f);
                int g = (int) (Color.green(baseColor) * 0.16f);
                int b = (int) (Color.blue(baseColor) * 0.16f);

                if (completed) {
                    r = Math.min(255, r + 6);
                    g = Math.min(255, g + 14);
                    b = Math.min(255, b + 6);
                }

                cardHabit.setCardBackgroundColor(Color.rgb(r, g, b));
            } catch (Exception e) {
                cardHabit.setCardBackgroundColor(Color.parseColor("#161616"));
            }
        }
    }
}