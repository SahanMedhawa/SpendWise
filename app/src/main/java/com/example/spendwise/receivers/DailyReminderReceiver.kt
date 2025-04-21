package com.example.spendwise.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.spendwise.utils.NotificationManager

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = NotificationManager(context)
        notificationManager.showDailyReminder()
    }
} 