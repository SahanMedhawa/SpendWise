package com.example.spendwise.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.example.spendwise.receivers.DailyReminderReceiver
import com.example.spendwise.utils.NotificationManager
import java.util.*

class DailyReminderService : Service() {
    private lateinit var notificationManager: NotificationManager
    private lateinit var alarmManager: AlarmManager
    private var pendingIntent: PendingIntent? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationManager(this)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_REMINDER -> startDailyReminder()
            ACTION_STOP_REMINDER -> stopDailyReminder()
        }
        return START_STICKY
    }

    private fun startDailyReminder() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20) // 8 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // If the time has already passed today, set it for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(this, DailyReminderReceiver::class.java)
        val newPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            newPendingIntent
        )
        
        pendingIntent = newPendingIntent
    }

    private fun stopDailyReminder() {
        val currentPendingIntent = pendingIntent
        if (currentPendingIntent != null) {
            alarmManager.cancel(currentPendingIntent)
            pendingIntent = null
        }
        notificationManager.cancelDailyReminder()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDailyReminder()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_REMINDER = "com.example.spendwise.START_REMINDER"
        const val ACTION_STOP_REMINDER = "com.example.spendwise.STOP_REMINDER"
    }
} 