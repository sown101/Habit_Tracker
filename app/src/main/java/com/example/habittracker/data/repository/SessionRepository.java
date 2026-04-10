package com.example.habittracker.data.repository;

import android.content.Context;

import com.example.habittracker.data.dao.FocusSessionDao;
import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.data.model.FocusSession;

import java.util.List;

public class SessionRepository {

    private final FocusSessionDao focusSessionDao;

    public SessionRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        focusSessionDao = db.focusSessionDao();
    }

    public long insert(FocusSession session) {
        return focusSessionDao.insert(session);
    }

    public void update(FocusSession session) {
        focusSessionDao.update(session);
    }

    public List<FocusSession> getSessionsByHabit(int habitId) {
        return focusSessionDao.getSessionsByHabit(habitId);
    }
}