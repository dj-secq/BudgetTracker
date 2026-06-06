package com.example.budgettracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.budgettracker.BudgetTrackerApplication
import com.example.budgettracker.data.local.entity.Frequency
import com.example.budgettracker.data.local.entity.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class RecurringTransactionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val app = applicationContext as BudgetTrackerApplication
            val repository = app.container.budgetRepository
            
            val currentTime = System.currentTimeMillis()
            val dueTransactions = repository.getDueRecurringTransactions(currentTime)
            
            for (recurring in dueTransactions) {
                // Create the actual transaction
                val transaction = Transaction(
                    accountId = recurring.accountId,
                    categoryId = recurring.categoryId,
                    amount = recurring.amount,
                    note = recurring.note,
                    timestamp = recurring.nextRunTime, // Log it exactly when it was due
                    classification = recurring.classification
                )
                repository.insertTransaction(transaction)
                
                // Calculate next run time
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = recurring.nextRunTime
                }
                
                when (recurring.frequency) {
                    Frequency.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                    Frequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    Frequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                    Frequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
                }
                
                val nextRunTime = calendar.timeInMillis
                
                // Catch up if we missed multiple cycles
                var updatedNextRunTime = nextRunTime
                while (updatedNextRunTime <= currentTime) {
                    calendar.timeInMillis = updatedNextRunTime
                    when (recurring.frequency) {
                        Frequency.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                        Frequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                        Frequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                        Frequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
                    }
                    updatedNextRunTime = calendar.timeInMillis
                    
                    // Insert missed occurrences too!
                    repository.insertTransaction(
                        transaction.copy(timestamp = updatedNextRunTime, id = 0)
                    )
                }
                
                // Update the recurring transaction
                repository.updateRecurringTransaction(
                    recurring.copy(nextRunTime = updatedNextRunTime)
                )
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
