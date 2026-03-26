package com.example.habittracker.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.habittracker.data.dao.FocusSessionDao;
import com.example.habittracker.data.dao.HabitDao;
import com.example.habittracker.data.dao.HabitLogDao;
import com.example.habittracker.data.dao.ReminderDao;
import com.example.habittracker.data.dao.UserDao;
import com.example.habittracker.data.model.FocusSession;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.data.model.HabitLog;
import com.example.habittracker.data.model.Reminder;
import com.example.habittracker.data.model.User;
import com.example.habittracker.utils.Constants;

@Database(
        entities = {
                User.class,
                Habit.class,
                HabitLog.class,
                Reminder.class,
                FocusSession.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    public abstract UserDao userDao();
    public abstract HabitDao habitDao();
    public abstract HabitLogDao habitLogDao();
    public abstract ReminderDao reminderDao();
    public abstract FocusSessionDao focusSessionDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    Constants.DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
