package com.example.habittracker.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.habittracker.data.model.User;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);
}
