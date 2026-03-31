package com.example.habittracker.ui.calendar;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.habittracker.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private Spinner spinnerMonthYear;
    private ImageButton btnPrev, btnNext;
    private TextView tvSelectedDate;

    //thiết lập giới hạn năm cho droplist
    private final int START_YEAR = 2020;
    private final int END_YEAR = 2035;
    private List<String> monthYearList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        //ánh xạ view
        calendarView = findViewById(R.id.calendarView);
        spinnerMonthYear = findViewById(R.id.spinnerMonthYear);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);

        //tạo dữ liệu cho droplist
        setupSpinnerData();

        //sự kiện ng dùng chọn từ droplist
        spinnerMonthYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //quy đổi từ vị trí ra tháng năm
                int year = START_YEAR + (position / 12);
                int month = position % 12;

                //cập nhật view
                Calendar tempCalendar = Calendar.getInstance();
                tempCalendar.set(year, month, 1);
                calendarView.setDate(tempCalendar.getTimeInMillis(), true, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //nút điều hướng
        btnPrev.setOnClickListener(v -> {
            int currentPos = spinnerMonthYear.getSelectedItemPosition();
            if (currentPos > 0) {
                spinnerMonthYear.setSelection(currentPos - 1); // Lùi 1 tháng
            }
        });

        btnNext.setOnClickListener(v -> {
            int currentPos = spinnerMonthYear.getSelectedItemPosition();
            if (currentPos < monthYearList.size() - 1) {
                spinnerMonthYear.setSelection(currentPos + 1); // Tiến 1 tháng
            }
        });

        //sự kiện ng dùng bấm vào một ngày trên lịch
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String dateText = dayOfMonth + "/" + (month + 1) + "/" + year;
            tvSelectedDate.setText("Ngày được chọn: " + dateText);

            //đồng bộ ngược lại spinner
            int position = (year - START_YEAR) * 12 + month;
            if (position >= 0 && position < monthYearList.size()) {
                spinnerMonthYear.setSelection(position);
            }
        });
    }

    private void setupSpinnerData() {
        monthYearList = new ArrayList<>();

        //tạo danh sách tháng năm
        for (int y = START_YEAR; y <= END_YEAR; y++) {
            for (int m = 1; m <= 12; m++) {
                monthYearList.add("Tháng " + m + " - " + y);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_centered, monthYearList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonthYear.setAdapter(adapter);

        //đặt giá trị mặc định lúc mở app là tháng năm hiện tại
        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH);

        int currentPosition = (currentYear - START_YEAR) * 12 + currentMonth;
        if (currentPosition >= 0 && currentPosition < monthYearList.size()) {
            spinnerMonthYear.setSelection(currentPosition);
        }
    }
}