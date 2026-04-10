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

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId AND log_date = :date LIMIT 1")
    HabitLog getLogByHabitAndDate(int habitId, String date);

    @Query("SELECT * FROM habit_logs WHERE log_date = :date")
    List<HabitLog> getLogsByDate(String date);

    @Query("SELECT * FROM habit_logs WHERE log_date BETWEEN :startDate AND :endDate")
    List<HabitLog> getLogsBetweenDates(String startDate, String endDate);

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId ORDER BY log_date DESC")
    List<HabitLog> getLogsByHabit(int habitId);
}