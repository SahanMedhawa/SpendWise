package com.example.spendwise.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.spendwise.MainActivity
import com.example.spendwise.R
import com.example.spendwise.data.AppDatabase
import com.example.spendwise.workers.DailyReminderWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val database = AppDatabase.getDatabase(context)
    private val settingsDao = database.settingsDao()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val channelId = "spendwise_channel"
    private val dailyReminderId = 1
    private val budgetAlertId = 2

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SpendWise Notifications"
            val descriptionText = "Budget alerts and daily reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showBudgetAlert(budgetName: String, category: String, remaining: Double) {
        coroutineScope.launch {
            val notificationsEnabled = settingsDao.getSetting("notifications_enabled")?.value?.toBoolean() ?: true
            val budgetAlertsEnabled = settingsDao.getSetting("budget_alerts_enabled")?.value?.toBoolean() ?: true

            if (notificationsEnabled && budgetAlertsEnabled) {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Budget Alert")
                    .setContentText("$budgetName ($category) is running low. Remaining: $remaining")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(budgetAlertId, notification)
            }
        }
    }

    fun showDailyReminder() {
        coroutineScope.launch {
            val notificationsEnabled = settingsDao.getSetting("notifications_enabled")?.value?.toBoolean() ?: true
            val expenseRemindersEnabled = settingsDao.getSetting("expense_reminders_enabled")?.value?.toBoolean() ?: true

            if (notificationsEnabled && expenseRemindersEnabled) {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Expense Reminder")
                    .setContentText("Don't forget to log your expenses today!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(dailyReminderId, notification)
            }
        }
    }

    fun cancelDailyReminder() {
        notificationManager.cancel(dailyReminderId)
    }

    fun updateNotificationSettings() {
        coroutineScope.launch {
            val notificationsEnabled = settingsDao.getSetting("notifications_enabled")?.value?.toBoolean() ?: true
            if (notificationsEnabled) {
                if (settingsDao.getSetting("expense_reminders_enabled")?.value?.toBoolean() ?: true) {
                    startDailyReminder()
                } else {
                    stopDailyReminder()
                }
            } else {
                stopDailyReminder()
            }
        }
    }

    private fun startDailyReminder() {
        DailyReminderWorker.schedule(context)
    }

    private fun stopDailyReminder() {
        DailyReminderWorker.cancel(context)
        cancelDailyReminder()
    }

    companion object {
        private const val CHANNEL_ID = "spendwise_channel"
        private const val BUDGET_ALERT_ID = 1
        private const val EXPENSE_REMINDER_ID = 2
    }
} 