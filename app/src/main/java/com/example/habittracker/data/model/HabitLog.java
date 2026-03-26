package com.example.habittracker.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "habit_logs",
        foreignKeys = @ForeignKey(
                entity = Habit.class,
                parentColumns = "id",
                childColumns = "habit_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("habit_id"),
                @Index(value = {"habit_id", "log_date"}, unique = true)
        }
)

public class HabitLog {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "habit_id")
    private int habitId;

    @ColumnInfo(name = "log_date")
    private String logDate;

    @ColumnInfo(name = "current_value")
    private int currentValue;

    @ColumnInfo(name = "target_value")
    private int targetValue;

    @ColumnInfo(name = "is_completed")
    private boolean isCompleted;

    @ColumnInfo(name = "completed_at")
    private String completedAt;

    private String note;

    @ColumnInfo(name = "completion_method")
    private String completionMethod;

    public HabitLog(int habitId,
                    String logDate,
                    int currentValue,
                    int targetValue,
                    boolean isCompleted,
                    String completedAt,
                    String note,
                    String completionMethod) {
        this.habitId = habitId;
        this.logDate = logDate;
        this.currentValue = currentValue;
        this.targetValue = targetValue;
        this.isCompleted = isCompleted;
        this.completedAt = completedAt;
        this.note = note;
        this.completionMethod = completionMethod;
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

    public String getLogDate() {
        return logDate;
    }

    public void setLogDate(String logDate) {
        this.logDate = logDate;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(int targetValue) {
        this.targetValue = targetValue;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCompletionMethod() {
        return completionMethod;
    }

    public void setCompletionMethod(String completionMethod) {
        this.completionMethod = completionMethod;
    }
}
