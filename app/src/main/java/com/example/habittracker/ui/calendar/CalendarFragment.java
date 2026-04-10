package com.example.habittracker.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;
import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.data.model.HabitLog;
import com.example.habittracker.utils.Constants;
import com.example.habittracker.utils.SessionManager;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private TextView tvMonthYear;
    private TextView tvSelectedDate;
    private TextView tvDaySummary;
    private RecyclerView rvCalendarHabits;
    private ImageView btnPreviousMonth;
    private ImageView btnNextMonth;

    private CalendarHabitAdapter adapter;
    private AppDatabase db;

    private LocalDate selectedDate = LocalDate.now();
    private final List<LocalDate> greenDates = new ArrayList<>();
    private final List<LocalDate> redDates = new ArrayList<>();

    public CalendarFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        tvDaySummary = view.findViewById(R.id.tvDaySummary);
        rvCalendarHabits = view.findViewById(R.id.rvCalendarHabits);
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);

        rvCalendarHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CalendarHabitAdapter(new ArrayList<>(), this::handleCalendarHabitChecked);
        rvCalendarHabits.setAdapter(adapter);

        db = AppDatabase.getInstance(requireContext());

        setupCalendar();
        loadCalendarStatus();
        loadHabitsForSelectedDate(selectedDate);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCalendarStatus();
        loadHabitsForSelectedDate(selectedDate);
    }

    private void setupCalendar() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(12);
        YearMonth endMonth = currentMonth.plusMonths(12);

        calendarView.setup(startMonth, endMonth, DayOfWeek.MONDAY);
        calendarView.scrollToMonth(currentMonth);

        calendarView.setMonthScrollListener(month -> {
            String monthText = month.getYearMonth().getMonth()
                    .getDisplayName(TextStyle.FULL, Locale.getDefault());
            tvMonthYear.setText(capitalize(monthText) + " " + month.getYearMonth().getYear());
            return null;
        });

        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay data) {
                container.day = data;

                if (data.getPosition() == DayPosition.MonthDate) {
                    container.tvDayText.setVisibility(View.VISIBLE);
                    container.tvDayText.setText(String.valueOf(data.getDate().getDayOfMonth()));

                    if (data.getDate().equals(selectedDate)) {
                        container.tvDayText.setBackgroundResource(R.drawable.bg_calendar_day_selected);
                        container.tvDayText.setTextColor(0xFFFFFFFF);
                    } else if (greenDates.contains(data.getDate())) {
                        container.tvDayText.setBackgroundResource(R.drawable.bg_calendar_day_green);
                        container.tvDayText.setTextColor(0xFF1B5E20);
                    } else if (redDates.contains(data.getDate())) {
                        container.tvDayText.setBackgroundResource(R.drawable.bg_calendar_day_red);
                        container.tvDayText.setTextColor(0xFFB71C1C);
                    } else {
                        container.tvDayText.setBackground(null);
                        container.tvDayText.setTextColor(0xFF111111);
                    }

                    container.view.setOnClickListener(v -> {
                        selectedDate = data.getDate();
                        updateSelectedDateText(selectedDate);
                        loadHabitsForSelectedDate(selectedDate);
                        calendarView.notifyCalendarChanged();
                    });
                } else {
                    container.tvDayText.setText("");
                    container.tvDayText.setBackground(null);
                    container.view.setOnClickListener(null);
                }
            }
        });

        updateSelectedDateText(selectedDate);

        btnPreviousMonth.setOnClickListener(v -> calendarView.smoothScrollToMonth(
                calendarView.findFirstVisibleMonth().getYearMonth().minusMonths(1)
        ));

        btnNextMonth.setOnClickListener(v -> calendarView.smoothScrollToMonth(
                calendarView.findFirstVisibleMonth().getYearMonth().plusMonths(1)
        ));
    }

    private void loadCalendarStatus() {
        int userId = SessionManager.getUserId(requireContext());
        if (userId == -1) {
            return;
        }

        new Thread(() -> {
            List<Habit> habits = db.habitDao().getAllActiveHabitsByUser(userId);

            greenDates.clear();
            redDates.clear();

            LocalDate start = YearMonth.now().minusMonths(12).atDay(1);
            LocalDate end = YearMonth.now().plusMonths(12).atEndOfMonth();

            LocalDate date = start;
            while (!date.isAfter(end)) {
                if (!habits.isEmpty()) {
                    boolean isPerfect = true;
                    boolean hasAnyLogOrHabit = !habits.isEmpty();

                    for (Habit habit : habits) {
                        HabitLog log = db.habitLogDao().getLogByHabitAndDate(
                                habit.getId(),
                                date.toString()
                        );
                        if (log == null || !log.isCompleted()) {
                            isPerfect = false;
                        }
                    }

                    if (isPerfect) {
                        greenDates.add(date);
                    } else if (hasAnyLogOrHabit) {
                        redDates.add(date);
                    }
                }

                date = date.plusDays(1);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> calendarView.notifyCalendarChanged());
            }
        }).start();
    }

    private void loadHabitsForSelectedDate(LocalDate date) {
        int userId = SessionManager.getUserId(requireContext());

        if (userId == -1) {
            Toast.makeText(requireContext(), "Không tìm thấy phiên đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            List<Habit> habits = db.habitDao().getAllActiveHabitsByUser(userId);

            int completedCount = 0;
            for (Habit habit : habits) {
                HabitLog log = db.habitLogDao().getLogByHabitAndDate(habit.getId(), date.toString());
                boolean completed = log != null && log.isCompleted();
                habit.setCompletedToday(completed);
                if (completed) {
                    completedCount++;
                }
            }

            String summary;
            if (habits.isEmpty()) {
                summary = "Ngày này chưa có thói quen nào.";
            } else if (completedCount == habits.size()) {
                summary = "Tất cả thói quen đã hoàn thành.";
            } else {
                summary = "Đã hoàn thành " + completedCount + "/" + habits.size() + " thói quen.";
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.updateData(habits);
                    tvDaySummary.setText(summary);
                });
            }
        }).start();
    }

    private void handleCalendarHabitChecked(Habit habit, boolean isChecked, int position) {
        if (habit == null || position == RecyclerView.NO_POSITION) {
            return;
        }

        new Thread(() -> {
            HabitLog existingLog = db.habitLogDao().getLogByHabitAndDate(habit.getId(), selectedDate.toString());

            if (existingLog == null) {
                HabitLog newLog = new HabitLog(
                        habit.getId(),
                        selectedDate.toString(),
                        isChecked ? habit.getTargetValue() : 0,
                        habit.getTargetValue(),
                        isChecked,
                        isChecked ? getCurrentDateTime() : null,
                        null,
                        Constants.COMPLETION_METHOD_MANUAL
                );
                db.habitLogDao().insert(newLog);
            } else {
                existingLog.setCurrentValue(isChecked ? habit.getTargetValue() : 0);
                existingLog.setTargetValue(habit.getTargetValue());
                existingLog.setCompleted(isChecked);
                existingLog.setCompletedAt(isChecked ? getCurrentDateTime() : null);
                existingLog.setCompletionMethod(Constants.COMPLETION_METHOD_MANUAL);
                db.habitLogDao().update(existingLog);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.updateHabitState(position, isChecked);
                    loadHabitsForSelectedDate(selectedDate);
                    loadCalendarStatus();
                });
            }
        }).start();
    }

    private void updateSelectedDateText(LocalDate date) {
        String text = "Ngày đã chọn: " +
                date.getDayOfMonth() + "/" + date.getMonthValue() + "/" + date.getYear();
        tvSelectedDate.setText(text);
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.substring(0, 1).toUpperCase(Locale.getDefault()) + text.substring(1);
    }

    private static class DayViewContainer extends ViewContainer {
        final View view;
        final TextView tvDayText;
        CalendarDay day;

        DayViewContainer(@NonNull View view) {
            super(view);
            this.view = view;
            this.tvDayText = view.findViewById(R.id.tvDayText);
        }
    }
}