package com.example.spendwise.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.spendwise.utils.NotificationManager
import java.util.Calendar

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val notificationManager = NotificationManager(applicationContext)
        notificationManager.showDailyReminder()
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 20) // 8 PM
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            // If the time has already passed today, set it for tomorrow
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val delay = calendar.timeInMillis - System.currentTimeMillis()

            val workRequest = androidx.work.OneTimeWorkRequestBuilder<DailyReminderWorker>()
                .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build()

            androidx.work.WorkManager.getInstance(context)
                .enqueue(workRequest)
        }

        fun cancel(context: Context) {
            androidx.work.WorkManager.getInstance(context).cancelAllWork()
        }
    }
} 