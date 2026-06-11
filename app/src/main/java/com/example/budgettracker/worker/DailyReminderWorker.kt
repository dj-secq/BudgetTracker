package com.example.budgettracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.budgettracker.BudgetTrackerApplication
import kotlinx.coroutines.flow.first

class DailyReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Only run if daily reminders are enabled
        val app = context.applicationContext as BudgetTrackerApplication
        val preferencesRepository = app.container.userPreferencesRepository
        val prefs = preferencesRepository.generalPreferencesFlow.first()
        
        if (!prefs.dailyRemindersEnabled) {
            return Result.success()
        }

        // Check if there are any transactions today
        val repository = app.container.budgetRepository
        val transactions = repository.getRecentTransactions().first()
        
        val today = java.util.Calendar.getInstance()
        val hasTransactionsToday = transactions.any {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
            cal.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR)
        }

        if (!hasTransactionsToday) {
            showNotification()
        }

        return Result.success()
    }

    private fun showNotification() {
        val channelId = "daily_reminder_channel"
        val notificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Reminders"
            val descriptionText = "Reminders to log your expenses"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app's icon
            .setContentTitle("Budget Tracker Reminder")
            .setContentText("You haven't logged any transactions today. Keep your budget up to date!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Requesting notification permission is required for Android 13+ in the UI, 
        // assuming it's granted here for the worker
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
