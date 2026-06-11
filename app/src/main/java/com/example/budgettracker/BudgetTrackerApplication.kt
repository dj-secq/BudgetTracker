package com.example.budgettracker

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.budgettracker.di.AppContainer
import com.example.budgettracker.di.DefaultAppContainer
import com.example.budgettracker.worker.RecurringTransactionWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class BudgetTrackerApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        
        // Schedule Recurring Transactions Worker
        val constraints = Constraints.Builder()
            // .setRequiresBatteryNotLow(true) // Don't block entirely for demo purposes
            .build()
            
        val recurringWorkRequest = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "recurring_transactions_work",
            ExistingPeriodicWorkPolicy.KEEP,
            recurringWorkRequest
        )
        
        // Schedule Daily Reminder Worker at 8 PM
        val currentDate = java.util.Calendar.getInstance()
        val dueDate = java.util.Calendar.getInstance()
        
        // Set Execution around 20:00:00 (8 PM)
        dueDate.set(java.util.Calendar.HOUR_OF_DAY, 20)
        dueDate.set(java.util.Calendar.MINUTE, 0)
        dueDate.set(java.util.Calendar.SECOND, 0)
        
        if (dueDate.before(currentDate)) {
            dueDate.add(java.util.Calendar.HOUR_OF_DAY, 24)
        }
        
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        
        val dailyReminderRequest = PeriodicWorkRequestBuilder<com.example.budgettracker.worker.DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reminder_work",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyReminderRequest
        )
        
        CoroutineScope(Dispatchers.IO).launch {
            com.example.budgettracker.data.seedDatabaseIfEmpty(container.budgetRepository)
        }
    }
}
