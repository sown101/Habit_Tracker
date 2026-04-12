package com.example.habittracker.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.habittracker.utils.Constants;

import java.io.Serializable;

@Entity(
        tableName = "habits",
        indices = {@Index("user_id")}
)
public class Habit implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @NonNull
    private String title;

    private String description;
    private String category;

    // icon dạng emoji, ví dụ "💪", "📚", "🏃"
    @ColumnInfo(name = "icon_emoji")
    private String iconEmoji;

    // màu nền của icon, dạng hex string ví dụ "#4CAF50"
    @ColumnInfo(name = "color")
    private String color;

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

    @ColumnInfo(name = "is_active")
    private boolean isActive;

    @ColumnInfo(name = "created_at")
    private String createdAt;

    @ColumnInfo(name = "updated_at")
    private String updatedAt;

    // Các field tạm thời (không lưu vào DB), dùng để hiển thị UI
    @Ignore
    private boolean isCompletedToday;

    @Ignore
    private int currentValueToday;

    @Ignore
    private int currentStreak; // streak riêng của từng habit

    public Habit(int userId,
                 @NonNull String title,
                 String description,
                 String category,
                 String iconEmoji,
                 String color,
                 String habitType,
                 int targetValue,
                 String unit,
                 String frequencyType,
                 boolean reminderEnabled,
                 String reminderTime,
                 boolean isActive,
                 String createdAt,
                 String updatedAt) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.iconEmoji = iconEmoji;
        this.color = color;
        this.habitType = habitType;
        this.targetValue = targetValue;
        this.unit = unit;
        this.frequencyType = frequencyType;
        this.reminderEnabled = reminderEnabled;
        this.reminderTime = reminderTime;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    @NonNull
    public String getTitle() { return title; }
    public void setTitle(@NonNull String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category == null ? "" : category; }
    public void setCategory(String category) { this.category = category; }

    public String getIconEmoji() {
        return iconEmoji == null || iconEmoji.isEmpty() ? "⭐" : iconEmoji;
    }
    public void setIconEmoji(String iconEmoji) { this.iconEmoji = iconEmoji; }

    public String getColor() {
        return color == null || color.isEmpty() ? "#4CAF50" : color;
    }
    public void setColor(String color) { this.color = color; }

    public String getHabitType() {
        return habitType == null ? Constants.HABIT_TYPE_CHECKBOX : habitType;
    }
    public void setHabitType(String habitType) { this.habitType = habitType; }

    public int getTargetValue() { return targetValue; }
    public void setTargetValue(int targetValue) { this.targetValue = targetValue; }

    public String getUnit() { return unit == null ? "" : unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getFrequencyType() {
        return frequencyType == null ? Constants.FREQUENCY_DAILY : frequencyType;
    }
    public void setFrequencyType(String frequencyType) { this.frequencyType = frequencyType; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }

    public String getReminderTime() { return reminderTime == null ? "" : reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt == null ? "" : createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt == null ? "" : updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isCompletedToday() { return isCompletedToday; }
    public void setCompletedToday(boolean completedToday) { isCompletedToday = completedToday; }

    public int getCurrentValueToday() { return currentValueToday; }
    public void setCurrentValueToday(int currentValueToday) { this.currentValueToday = currentValueToday; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    // --- Helper methods ---

    public String getFrequency() { return getFrequencyType(); }

    public boolean isTaskHabit() {
        return Constants.HABIT_TYPE_CHECKBOX.equalsIgnoreCase(getHabitType());
    }

    public boolean isCounterHabit() {
        return Constants.HABIT_TYPE_COUNTER.equalsIgnoreCase(getHabitType());
    }

    public boolean isTimerHabit() {
        return Constants.HABIT_TYPE_TIMER.equalsIgnoreCase(getHabitType());
    }

    public int getSafeTargetValue() {
        return targetValue <= 0 ? 1 : targetValue;
    }

    public String getDisplayUnit() {
        if (isTaskHabit()) return "lần";
        if (isTimerHabit()) return "phút";
        String rawUnit = getUnit().trim();
        return rawUnit.isEmpty() ? "lần" : rawUnit;
    }
}