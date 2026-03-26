package com.example.habittracker.data.repository;

import android.content.Context;

import com.example.habittracker.data.dao.UserDao;
import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.User;
import com.example.habittracker.utils.PasswordUtils;

public class AuthRepository {
    private final UserDao userDao;

    public AuthRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        userDao = database.userDao();
    }

    public boolean isEmailExists(String email) {
        User existingUser = userDao.getUserByEmail(email);
        return existingUser != null;
    }

    public long register(String fullName, String email, String plainPassword) {
        if (isEmailExists(email)) {
            return -1;
        }

        String hashedPassword = PasswordUtils.hashPassword(plainPassword);

        User user = new User(
                fullName,
                email,
                hashedPassword,
                String.valueOf(System.currentTimeMillis())
        );

        return userDao.insert(user);
    }

    public User login(String email, String plainPassword) {
        String hashedPassword = PasswordUtils.hashPassword(plainPassword);
        return userDao.login(email, hashedPassword);
    }
}
