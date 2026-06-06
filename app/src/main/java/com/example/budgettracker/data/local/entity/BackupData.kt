package com.example.budgettracker.data.local.entity

data class BackupData(
    val accounts: List<Account>,
    val categories: List<Category>,
    val transactions: List<Transaction>,
    val budgetLimits: List<BudgetLimit>,
    val savingsGoals: List<SavingsGoal>,
    val recurringTransactions: List<RecurringTransaction>
)
