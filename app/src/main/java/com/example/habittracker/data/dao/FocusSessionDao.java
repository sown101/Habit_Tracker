package com.example.habittracker.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.habittracker.data.model.FocusSession;

import java.util.List;

@Dao
public interface FocusSessionDao {
    @Insert
    long insert(FocusSession session);

    @Update
    void update(FocusSession session);

    @Query("SELECT * FROM focus_sessions WHERE habit_id = :habitId ORDER BY start_time DESC")
    List<FocusSession> getSessionsByHabit(int habitId);

    @Query("SELECT * FROM focus_sessions WHERE id = :id LIMIT 1")
    FocusSession getById(int id);
}
