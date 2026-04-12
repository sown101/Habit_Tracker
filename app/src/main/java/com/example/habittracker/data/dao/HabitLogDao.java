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

    // Lấy log của 1 habit vào 1 ngày cụ thể
    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId AND log_date = :date LIMIT 1")
    HabitLog getLogByHabitAndDate(int habitId, String date);

    // Lấy tất cả logs của 1 ngày (dùng cho thống kê)
    @Query("SELECT * FROM habit_logs WHERE log_date = :date")
    List<HabitLog> getLogsByDate(String date);

    // Lấy tất cả logs trong khoảng ngày
    @Query("SELECT * FROM habit_logs WHERE log_date BETWEEN :startDate AND :endDate")
    List<HabitLog> getLogsBetweenDates(String startDate, String endDate);

    // Lấy tất cả logs của 1 habit, sắp xếp theo ngày mới nhất trước
    // Dùng để tính streak riêng của từng habit
    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId ORDER BY log_date DESC")
    List<HabitLog> getLogsByHabit(int habitId);

    // Lấy logs hoàn thành của 1 habit trong khoảng ngày (dùng cho heatmap)
    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId AND log_date BETWEEN :startDate AND :endDate ORDER BY log_date ASC")
    List<HabitLog> getLogsByHabitBetweenDates(int habitId, String startDate, String endDate);
}