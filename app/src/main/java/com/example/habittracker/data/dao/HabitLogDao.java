package com.example.habittracker.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.habittracker.data.model.HabitLog;

import java.util.List;

@Dao
public interface HabitLogDao {
    @Insert
    long insert(HabitLog habitLog);

    @Update
    void update(HabitLog habitLog);

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId AND log_date = :logDate LIMIT 1")
    HabitLog getLogByHabitAndDate(int habitId, String logDate);

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId ORDER BY log_date DESC")
    List<HabitLog> getLogsByHabit(int habitId);

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habit_id = :habitId AND is_completed = 1")
    int getCompletedCount(int habitId);
}
