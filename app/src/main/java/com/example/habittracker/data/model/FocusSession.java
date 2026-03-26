package com.example.habittracker.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "focus_sessions",
        foreignKeys = @ForeignKey(
                entity = Habit.class,
                parentColumns = "id",
                childColumns = "habit_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("habit_id")}
)

public class FocusSession {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "habit_id")
    private int habitId;

    @ColumnInfo(name = "start_time")
    private String startTime;

    @ColumnInfo(name = "end_time")
    private String endTime;

    @ColumnInfo(name = "planned_duration_minutes")
    private int plannedDurationMinutes;

    @ColumnInfo(name = "actual_duration_minutes")
    private int actualDurationMinutes;

    private String status;

    @ColumnInfo(name = "created_at")
    private String createdAt;

    public FocusSession(int habitId,
                        String startTime,
                        String endTime,
                        int plannedDurationMinutes,
                        int actualDurationMinutes,
                        String status,
                        String createdAt) {
        this.habitId = habitId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.plannedDurationMinutes = plannedDurationMinutes;
        this.actualDurationMinutes = actualDurationMinutes;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getPlannedDurationMinutes() {
        return plannedDurationMinutes;
    }

    public void setPlannedDurationMinutes(int plannedDurationMinutes) {
        this.plannedDurationMinutes = plannedDurationMinutes;
    }

    public int getActualDurationMinutes() {
        return actualDurationMinutes;
    }

    public void setActualDurationMinutes(int actualDurationMinutes) {
        this.actualDurationMinutes = actualDurationMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
