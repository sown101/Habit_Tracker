package com.example.habittracker.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.habittracker.data.db.AppDatabase;
import com.example.habittracker.utils.SessionManager;

public class DailyResetWorker extends Worker {

    public DailyResetWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        int userId = SessionManager.getUserId(context);

        if (userId == -1) {
            return Result.success();
        }

        AppDatabase db = AppDatabase.getInstance(context);

        // Bản hiện tại giữ worker này nhẹ để đúng kiểu app Habit Tracker:
        // - không tạo log trống tự động
        // - không reset cứng dữ liệu DB
        // - chỉ là điểm mở rộng cho ngày mới
        //
        // Sau này bạn có thể thêm:
        // - gửi thông báo "bắt đầu ngày mới"
        // - sync dữ liệu
        // - chuẩn bị summary / cleanup

        return Result.success();
    }
}