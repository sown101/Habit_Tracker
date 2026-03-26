package com.example.habittracker.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.habittracker.data.model.Reminder;

import java.util.List;

@Dao
public interface ReminderDao {
    @Insert
    long insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("SELECT * FROM reminders WHERE habit_id = :habitId")
    List<Reminder> getRemindersByHabit(int habitId);
}
