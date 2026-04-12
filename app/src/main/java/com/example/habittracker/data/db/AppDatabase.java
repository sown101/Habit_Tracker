package com.example.habittracker.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.habittracker.data.dao.HabitDao;
import com.example.habittracker.data.dao.HabitLogDao;
import com.example.habittracker.data.dao.ReminderDao;
import com.example.habittracker.data.model.Habit;
import com.example.habittracker.data.model.HabitLog;
import com.example.habittracker.data.model.Reminder;
import com.example.habittracker.utils.Constants;

// Tăng version lên 3 vì thêm 2 cột mới: icon_emoji và color vào bảng habits
// fallbackToDestructiveMigration() sẽ xóa và tạo lại DB nếu version thay đổi
@Database(
        entities = {
                Habit.class,
                HabitLog.class,
                Reminder.class
        },
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract HabitDao habitDao();
    public abstract HabitLogDao habitLogDao();
    public abstract ReminderDao reminderDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    Constants.DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration() // Xóa DB cũ khi đổi version
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}