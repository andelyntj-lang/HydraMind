package com.example.uas_pemrograman_mobile;

import android.content.Context;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class WaterReminderHelper {

    private static final String WORK_TAG = "water_reminder_work";

    public static void scheduleWaterReminder(Context context) {
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                WaterReminderWorker.class,
                1, TimeUnit.HOURS // Interval 1 jam
        )
        .addTag(WORK_TAG)
        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP, // Tetap gunakan yang lama jika sudah ada
                periodicWorkRequest
        );
    }
    
    public static void cancelWaterReminder(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG);
    }
}
