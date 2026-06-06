package com.example.budgettracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.budgettracker.data.local.dao.AccountDao
import com.example.budgettracker.data.local.dao.BudgetLimitDao
import com.example.budgettracker.data.local.dao.CategoryDao
import com.example.budgettracker.data.local.dao.SavingsGoalDao
import com.example.budgettracker.data.local.dao.TransactionDao
import com.example.budgettracker.data.local.entity.Account
import com.example.budgettracker.data.local.entity.BudgetLimit
import com.example.budgettracker.data.local.entity.Category
import com.example.budgettracker.data.local.entity.SavingsGoal
import com.example.budgettracker.data.local.entity.Transaction
import com.example.budgettracker.data.local.entity.RecurringTransaction
import com.example.budgettracker.data.local.dao.RecurringTransactionDao

@Database(
    entities = [
        Account::class,
        Category::class,
        BudgetLimit::class,
        Transaction::class,
        SavingsGoal::class,
        RecurringTransaction::class
    ],
    version = 6,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetLimitDao(): BudgetLimitDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun accountDao(): AccountDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
}
