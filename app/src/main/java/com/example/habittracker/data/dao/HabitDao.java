package com.example.habittracker.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.habittracker.data.model.Habit;

import java.util.List;

@Dao
public interface HabitDao {
    @Insert
    long insert(Habit habit);

    @Update
    void update(Habit habit);

    @Delete
    void delete(Habit habit);

    @Query("SELECT * FROM habits WHERE user_id = :userId AND is_active = 1 ORDER BY created_at DESC")
    List<Habit> getAllActiveHabitsByUser(int userId);

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    Habit getHabitById(int habitId);
}
