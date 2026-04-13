package com.example.habittracker.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.habittracker.utils.SettingsManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class WorkerScheduler {

    private static final String UNIQUE_SUMMARY_WORK = "unique_daily_summary_work";
    private static final String UNIQUE_RESET_WORK = "unique_daily_reset_work";

    private WorkerScheduler() {
    }

    public static void scheduleAll(@NonNull Context context) {
        scheduleSummaryWorker(context);
    }

    public static void scheduleSummaryWorker(@NonNull Context context) {
        if (!SettingsManager.isNotificationEnabled(context)) {
            cancelSummaryWorker(context);
            return;
        }

        int summaryHour = SettingsManager.getDailySummaryHour(context);
        int summaryMinute = SettingsManager.getDailySummaryMinute(context);

        long initialDelay = calculateInitialDelay(summaryHour, summaryMinute);

        PeriodicWorkRequest summaryRequest =
                new PeriodicWorkRequest.Builder(SummaryWorker.class, 24, TimeUnit.HOURS)
                        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_SUMMARY_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                summaryRequest
        );
    }

    public static void cancelSummaryWorker(@NonNull Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_SUMMARY_WORK);
    }

    private static long calculateInitialDelay(int targetHour, int targetMinute) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();

        target.set(Calendar.HOUR_OF_DAY, targetHour);
        target.set(Calendar.MINUTE, targetMinute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        if (target.getTimeInMillis() <= now.getTimeInMillis()) {
            target.add(Calendar.DAY_OF_YEAR, 1);
        }

        return target.getTimeInMillis() - now.getTimeInMillis();
    }
}