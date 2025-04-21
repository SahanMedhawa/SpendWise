package com.example.spendwise.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.example.spendwise.MainActivity
import com.example.spendwise.R
import com.example.spendwise.workers.DailyReminderWorker

class NotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
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

    fun showBudgetAlert(category: String, spent: Double, budget: Double, percentage: Double) {
        if (!areNotificationsEnabled() || !areBudgetAlertsEnabled()) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val remaining = budget - spent
        val message = when {
            percentage >= 100 -> "⚠️ Budget Exceeded! You've spent $spent of $budget in $category"
            percentage >= 90 -> "⚠️ Budget Warning! You've spent $spent of $budget in $category. Only $remaining left!"
            percentage >= 75 -> "⚠️ Budget Alert! You've spent $spent of $budget in $category. $remaining remaining."
            else -> "Budget Update: You've spent $spent of $budget in $category. $remaining remaining."
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Budget Alert: $category")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(budgetAlertId, notification)
    }

    fun showDailyReminder() {
        if (!areNotificationsEnabled() || !areExpenseRemindersEnabled()) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Daily Expense Reminder")
            .setContentText("Don't forget to record your expenses for today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(dailyReminderId, notification)
    }

    fun cancelDailyReminder() {
        notificationManager.cancel(dailyReminderId)
    }

    fun updateNotificationSettings() {
        if (areNotificationsEnabled()) {
            if (areExpenseRemindersEnabled()) {
                startDailyReminder()
            } else {
                stopDailyReminder()
            }
        } else {
            stopDailyReminder()
        }
    }

    private fun startDailyReminder() {
        DailyReminderWorker.schedule(context)
    }

    private fun stopDailyReminder() {
        DailyReminderWorker.cancel(context)
        cancelDailyReminder()
    }

    private fun areNotificationsEnabled(): Boolean {
        return preferences.getBoolean("notifications_enabled", true)
    }

    private fun areBudgetAlertsEnabled(): Boolean {
        return preferences.getBoolean("budget_alerts_enabled", true)
    }

    private fun areExpenseRemindersEnabled(): Boolean {
        return preferences.getBoolean("expense_reminders_enabled", true)
    }
} 