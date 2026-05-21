package com.example.budgettracker.data.repository

import com.example.budgettracker.data.local.dao.AccountDao
import com.example.budgettracker.data.local.dao.BudgetLimitDao
import com.example.budgettracker.data.local.dao.CategoryDao
import com.example.budgettracker.data.local.dao.SavingsGoalDao
import com.example.budgettracker.data.local.dao.TransactionDao
import com.example.budgettracker.data.local.entity.Account
import com.example.budgettracker.data.local.entity.BudgetLimit
import com.example.budgettracker.data.local.entity.Category
import com.example.budgettracker.data.local.entity.CategoryType
import com.example.budgettracker.data.local.entity.SavingsGoal
import com.example.budgettracker.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class BudgetRepository(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val budgetLimitDao: BudgetLimitDao,
    private val transactionDao: TransactionDao,
    private val savingsGoalDao: SavingsGoalDao
) {
    // Accounts
    fun getAllAccounts(): Flow<List<Account>> = accountDao.getAllAccounts()
    suspend fun getAccountById(id: Long): Account? = accountDao.getAccountById(id)
    suspend fun insertAccount(account: Account): Long = accountDao.insertAccount(account)
    suspend fun updateAccount(account: Account) = accountDao.updateAccount(account)
    suspend fun deleteAccount(account: Account) = accountDao.deleteAccount(account)

    // Categories
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    fun getCategoriesByType(type: CategoryType): Flow<List<Category>> = categoryDao.getCategoriesByType(type)
    
    suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)

    // Budget Limits
    fun getBudgetLimitsForMonth(month: Int, year: Int): Flow<List<BudgetLimit>> =
        budgetLimitDao.getBudgetLimitsForMonth(month, year)

    suspend fun setBudgetLimit(categoryId: Long, amount: Double, month: Int, year: Int) {
        val existing = budgetLimitDao.getBudgetLimit(categoryId, month, year)
        if (existing != null) {
            budgetLimitDao.updateBudgetLimit(existing.copy(assignedAmount = amount))
        } else {
            budgetLimitDao.insertBudgetLimit(
                BudgetLimit(
                    categoryId = categoryId,
                    assignedAmount = amount,
                    month = month,
                    year = year
                )
            )
        }
    }

    // Transactions
    fun getRecentTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    
    fun getTransactionsForMonth(month: Int, year: Int): Flow<List<Transaction>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startDate = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endDate = calendar.timeInMillis
        return transactionDao.getTransactionsBetweenDates(startDate, endDate)
    }

    suspend fun hasTransactionsForCategory(categoryId: Long): Boolean {
        val transactions = transactionDao.getTransactionsByCategory(categoryId).first()
        return transactions.isNotEmpty()
    }
    
    fun getTotalSpentByCategory(categoryId: Long, month: Int, year: Int): Flow<Double?> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1) // Calendar months are 0-indexed
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startDate = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endDate = calendar.timeInMillis
        
        return transactionDao.getTotalAmountByCategoryAndDateRange(categoryId, startDate, endDate)
    }

    suspend fun insertTransaction(transaction: Transaction): Long {
        val category = categoryDao.getCategoryById(transaction.categoryId)
        val account = accountDao.getAccountById(transaction.accountId)
        if (category != null && account != null) {
            val updatedBalance = if (category.type == CategoryType.INCOME) {
                account.balance + transaction.amount
            } else {
                account.balance - transaction.amount
            }
            accountDao.updateAccount(account.copy(balance = updatedBalance))
        }
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        val category = categoryDao.getCategoryById(transaction.categoryId)
        val account = accountDao.getAccountById(transaction.accountId)
        if (category != null && account != null) {
            val updatedBalance = if (category.type == CategoryType.INCOME) {
                account.balance - transaction.amount
            } else {
                account.balance + transaction.amount
            }
            accountDao.updateAccount(account.copy(balance = updatedBalance))
        }
        transactionDao.deleteTransaction(transaction)
    }
    
    // Savings Goals
    fun getAllGoals(): Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoals()
    suspend fun insertGoal(goal: SavingsGoal): Long = savingsGoalDao.insertGoal(goal)
    suspend fun updateGoal(goal: SavingsGoal) = savingsGoalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: SavingsGoal) = savingsGoalDao.deleteGoal(goal)
}
