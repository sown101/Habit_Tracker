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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;
import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.data.model.HabitLog;
import com.example.habittracker.ui.adapter.HabitAdapter; // Đã import HabitAdapter của trang chủ
import com.example.habittracker.ui.timer.TimerHabitDialogFragment;
import com.example.habittracker.utils.Constants;
import com.example.habittracker.utils.DailyCompletionUtils;
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

    // 1. ĐỔI SANG DÙNG HABIT ADAPTER CỦA TRANG CHỦ
    private HabitAdapter adapter;
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

        // 2. KHỞI TẠO ADAPTER VÀ LẮNG NGHE TẤT CẢ SỰ KIỆN (CHECKBOX, +/-, TIMER)
        adapter = new HabitAdapter(
                new ArrayList<>(),
                this::handleCalendarHabitChecked, // Xử lý khi tick hoàn thành
                new HabitAdapter.OnCounterActionListener() { // Xử lý khi bấm +/-
                    @Override
                    public void onCounterPlus(Habit habit, int position) {
                        handleCounterAction(habit, position, true);
                    }

                    @Override
                    public void onCounterMinus(Habit habit, int position) {
                        handleCounterAction(habit, position, false);
                    }
                },
                new HabitAdapter.OnTimerActionListener() { // Xử lý khi bấm Timer
                    @Override
                    public void onTimerClick(Habit habit, int position) {
                        TimerHabitDialogFragment dialog = TimerHabitDialogFragment.newInstance(habit.getId());
                        dialog.show(getChildFragmentManager(), "timer_dialog");
                    }
                }
        );
        rvCalendarHabits.setAdapter(adapter);

        db = AppDatabase.getInstance(requireContext());

        setupCalendar();
        loadCalendarStatus();
        loadHabitsForSelectedDate(selectedDate);

        // 3. LẮNG NGHE TÍN HIỆU TỪ ĐỒNG HỒ (Giống y hệt màn hình Home)
        getParentFragmentManager().setFragmentResultListener("refresh_habits", getViewLifecycleOwner(), (requestKey, result) -> {
            loadHabitsForSelectedDate(selectedDate);
            loadCalendarStatus();
        });

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
            greenDates.clear();
            redDates.clear();

            LocalDate start = YearMonth.now().minusMonths(12).atDay(1);
            LocalDate end = YearMonth.now().plusMonths(12).atEndOfMonth();

            LocalDate date = start;
            while (!date.isAfter(end)) {
                List<Habit> habitsForDate = DailyCompletionUtils.getHabitsActiveOnDate(
                        db,
                        userId,
                        date.toString()
                );

                if (!habitsForDate.isEmpty()) {
                    boolean isPerfect = true;

                    for (Habit habit : habitsForDate) {
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
                    } else {
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
            List<Habit> habits = DailyCompletionUtils.getHabitsActiveOnDate(
                    db,
                    userId,
                    date.toString()
            );

            int completedCount = 0;
            for (Habit habit : habits) {
                // Lấy log đúng theo ngày đang chọn trên lịch
                HabitLog log = db.habitLogDao().getLogByHabitAndDate(habit.getId(), date.toString());
                boolean completed = log != null && log.isCompleted();
                habit.setCompletedToday(completed);

                // Nếu là Counter Habit, ép thêm CurrentValue để Adapter hiển thị đúng (ví dụ 5/10)
                if (log != null && habit.isCounterHabit()) {
                    habit.setCurrentValueToday(log.getCurrentValue());
                } else if (log == null && habit.isCounterHabit()) {
                    habit.setCurrentValueToday(0);
                }

                if (completed) {
                    completedCount++;
                }
            }

            String summary;
            if (habits.isEmpty()) {
                summary = "Ngày này chưa có thói quen nào.";
            } else if (completedCount == habits.size()) {
                summary = "Tuyệt vời! Tất cả thói quen đã hoàn thành.";
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
                        selectedDate.toString(), // Lưu đúng vào ngày đang chọn trên lịch
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
                    // Cập nhật lại UI và trạng thái lịch
                    loadHabitsForSelectedDate(selectedDate);
                    loadCalendarStatus();
                });
            }
        }).start();
    }

    // 4. HÀM MỚI: XỬ LÝ KHI BẤM NÚT CỘNG/TRỪ CHO LOẠI TRACK = AMOUNT
    private void handleCounterAction(Habit habit, int position, boolean isPlus) {
        new Thread(() -> {
            HabitLog log = db.habitLogDao().getLogByHabitAndDate(habit.getId(), selectedDate.toString());
            int currentValue = (log != null) ? log.getCurrentValue() : 0;
            int targetValue = habit.getSafeTargetValue();

            if (isPlus) {
                currentValue = Math.min(currentValue + 1, targetValue);
            } else {
                currentValue = Math.max(currentValue - 1, 0);
            }

            boolean isCompleted = (currentValue >= targetValue);

            if (log == null) {
                log = new HabitLog(habit.getId(), selectedDate.toString(), currentValue, targetValue,
                        isCompleted, isCompleted ? getCurrentDateTime() : null, null, Constants.COMPLETION_METHOD_MANUAL);
                db.habitLogDao().insert(log);
            } else {
                log.setCurrentValue(currentValue);
                log.setCompleted(isCompleted);
                log.setCompletedAt(isCompleted ? getCurrentDateTime() : null);
                db.habitLogDao().update(log);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
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