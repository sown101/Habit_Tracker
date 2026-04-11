package com.example.habittracker.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "habits",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("user_id")}
)
public class Habit implements Serializable {

    public static final String TYPE_COMPLETE = "COMPLETE";
    public static final String TYPE_COUNTER = "COUNTER";

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @NonNull
    private String title;

    private String description;
    private String category;

    @ColumnInfo(name = "habit_type")
    private String habitType;

    @ColumnInfo(name = "target_value")
    private int targetValue;

    private String unit;

    @ColumnInfo(name = "frequency_type")
    private String frequencyType;

    @ColumnInfo(name = "reminder_enabled")
    private boolean reminderEnabled;

    @ColumnInfo(name = "reminder_time")
    private String reminderTime;

    @ColumnInfo(name = "allow_shake_complete")
    private boolean allowShakeComplete;

    @ColumnInfo(name = "enable_focus_session")
    private boolean enableFocusSession;

    @ColumnInfo(name = "session_duration_minutes")
    private int sessionDurationMinutes;

    @ColumnInfo(name = "is_active")
    private boolean isActive;

    @ColumnInfo(name = "created_at")
    private String createdAt;

    @ColumnInfo(name = "updated_at")
    private String updatedAt;

    @Ignore
    private boolean isCompletedToday;

    @Ignore
    private int currentValueToday;

    public Habit(int userId,
                 @NonNull String title,
                 String description,
                 String category,
                 String habitType,
                 int targetValue,
                 String unit,
                 String frequencyType,
                 boolean reminderEnabled,
                 String reminderTime,
                 boolean allowShakeComplete,
                 boolean enableFocusSession,
                 int sessionDurationMinutes,
                 boolean isActive,
                 String createdAt,
                 String updatedAt) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.habitType = habitType;
        this.targetValue = targetValue;
        this.unit = unit;
        this.frequencyType = frequencyType;
        this.reminderEnabled = reminderEnabled;
        this.reminderTime = reminderTime;
        this.allowShakeComplete = allowShakeComplete;
        this.enableFocusSession = enableFocusSession;
        this.sessionDurationMinutes = sessionDurationMinutes;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getHabitType() {
        return habitType;
    }

    public void setHabitType(String habitType) {
        this.habitType = habitType;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(int targetValue) {
        this.targetValue = targetValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(String frequencyType) {
        this.frequencyType = frequencyType;
    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isAllowShakeComplete() {
        return allowShakeComplete;
    }

    public void setAllowShakeComplete(boolean allowShakeComplete) {
        this.allowShakeComplete = allowShakeComplete;
    }

    public boolean isEnableFocusSession() {
        return enableFocusSession;
    }

    public void setEnableFocusSession(boolean enableFocusSession) {
        this.enableFocusSession = enableFocusSession;
    }

    public int getSessionDurationMinutes() {
        return sessionDurationMinutes;
    }

    public void setSessionDurationMinutes(int sessionDurationMinutes) {
        this.sessionDurationMinutes = sessionDurationMinutes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isCompletedToday() {
        return isCompletedToday;
    }

    public void setCompletedToday(boolean completedToday) {
        isCompletedToday = completedToday;
    }

    public int getCurrentValueToday() {
        return currentValueToday;
    }

    public void setCurrentValueToday(int currentValueToday) {
        this.currentValueToday = currentValueToday;
    }

    public String getFrequency() {
        return frequencyType;
    }

    public boolean isCounterHabit() {
        return TYPE_COUNTER.equalsIgnoreCase(habitType);
    }

    public boolean isCompleteHabit() {
        if (habitType == null || habitType.trim().isEmpty()) {
            return true;
        }
        return TYPE_COMPLETE.equalsIgnoreCase(habitType)
                || "CHECKBOX".equalsIgnoreCase(habitType)
                || "REGULAR".equalsIgnoreCase(habitType);
    }

    public String getNormalizedHabitType() {
        return isCounterHabit() ? TYPE_COUNTER : TYPE_COMPLETE;
    }

    public String getDisplayUnit() {
        if (unit == null || unit.trim().isEmpty()) {
            return isCounterHabit() ? "lần" : "";
        }
        return unit.trim();
    }

    public int getSafeTargetValue() {
        return targetValue <= 0 ? 1 : targetValue;
    }

    public String getDisplayProgressText() {
        if (isCounterHabit()) {
            return currentValueToday + "/" + getSafeTargetValue() + " " + getDisplayUnit();
        }
        return isCompletedToday ? "Hoàn thành" : "Chưa hoàn thành";
    }
}