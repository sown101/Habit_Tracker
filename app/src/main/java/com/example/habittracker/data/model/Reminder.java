package com.example.habittracker.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "reminders",
        foreignKeys = @ForeignKey(
                entity = Habit.class,
                parentColumns = "id",
                childColumns = "habit_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("habit_id")}
)

public class Reminder {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "habit_id")
    private int habitId;

    @ColumnInfo(name = "remind_time")
    private String remindTime;

    @ColumnInfo(name = "days_of_week")
    private String daysOfWeek;

    @ColumnInfo(name = "is_enabled")
    private boolean isEnabled;

    @ColumnInfo(name = "request_code")
    private int requestCode;

    public Reminder(int habitId, String remindTime, String daysOfWeek, boolean isEnabled, int requestCode) {
        this.habitId = habitId;
        this.remindTime = remindTime;
        this.daysOfWeek = daysOfWeek;
        this.isEnabled = isEnabled;
        this.requestCode = requestCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getHabitId() {
        return habitId;
    }

    public void setHabitId(int habitId) {
        this.habitId = habitId;
    }

    public String getRemindTime() {
        return remindTime;
    }

    public void setRemindTime(String remindTime) {
        this.remindTime = remindTime;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }
}
