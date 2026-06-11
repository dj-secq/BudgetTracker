package com.example.budgettracker.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgettracker.data.local.entity.Account
import com.example.budgettracker.data.local.entity.Category
import com.example.budgettracker.data.local.entity.CategoryType
import com.example.budgettracker.data.local.entity.ExpenseClassification
import com.example.budgettracker.data.local.entity.Transaction
import com.example.budgettracker.data.repository.BudgetRepository
import com.example.budgettracker.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class EditTransactionViewModel(
    private val repository: BudgetRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<Account>> = repository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSaving = MutableStateFlow(false)
    val transactionFlow = MutableStateFlow<Transaction?>(null)
    
    val showOverBudgetWarning = MutableStateFlow<Pair<Double, Boolean>?>(null)
    val showBucketWarning = MutableStateFlow<Pair<String, Double>?>(null)

    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            val tx = repository.getTransactionById(transactionId)
            transactionFlow.value = tx
        }
    }

    fun onConfirmSave(
        transactionId: Long,
        accountId: Long,
        categoryId: Long,
        amount: Double,
        note: String,
        timestamp: Long,
        classification: ExpenseClassification,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val oldTransaction = transactionFlow.value ?: return@launch
            
            // Limit checks
            val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            
            // Note: When editing, checking budget limits is slightly complex because we need to subtract the old amount if the category is the same.
            // For simplicity, we fetch the current spent, subtract old amount (if same category), add new amount, and check against limit.
            val limits = repository.getBudgetLimitsForMonth(month, year).first()
            var limit = limits.find { it.categoryId == categoryId }?.assignedAmount ?: 0.0
            val prefs = preferencesRepository.generalPreferencesFlow.first()
            
            if (limit > 0) {
                if (prefs.rolloverBudgetsEnabled) {
                    limit += repository.getRolloverAmount(categoryId, month, year)
                }
                val currentSpent = repository.getTotalSpentByCategory(categoryId, month, year).first() ?: 0.0
                // Adjust spent if the old transaction was in the same month/year and category
                val oldCalendar = Calendar.getInstance().apply { timeInMillis = oldTransaction.timestamp }
                val oldMonth = oldCalendar.get(Calendar.MONTH) + 1
                val oldYear = oldCalendar.get(Calendar.YEAR)
                
                var adjustedSpent = currentSpent
                if (oldMonth == month && oldYear == year && oldTransaction.categoryId == categoryId) {
                    adjustedSpent -= oldTransaction.amount
                }
                
                if (adjustedSpent + amount > limit) {
                    showOverBudgetWarning.value = Pair((adjustedSpent + amount) - limit, prefs.strictLimitsEnabled)
                    return@launch
                }
            }

            // 2. Check Bucket Limit (Needs/Wants/Savings)
            if (classification != ExpenseClassification.NONE) {
                val transactions = repository.getTransactionsForMonth(month, year).first()
                val incomeCategories = repository.getAllCategories().first().filter { it.type == CategoryType.INCOME }
                var totalIncome = transactions.filter { tx -> incomeCategories.any { it.id == tx.categoryId } }.sumOf { it.amount }
                
                // Adjust total income if the old transaction was an income and in the same month
                val oldCalendar = Calendar.getInstance().apply { timeInMillis = oldTransaction.timestamp }
                if (oldCalendar.get(Calendar.MONTH) + 1 == month && oldCalendar.get(Calendar.YEAR) == year) {
                    if (incomeCategories.any { it.id == oldTransaction.categoryId }) {
                        totalIncome -= oldTransaction.amount
                    }
                }
                
                // If the new transaction is an income, add it to the total
                if (incomeCategories.any { it.id == categoryId }) {
                    totalIncome += amount
                }

                if (totalIncome > 0 && !incomeCategories.any { it.id == categoryId }) {
                    val rule = preferencesRepository.budgetRulePreferencesFlow.first()
                    val bucketPercent = when (classification) {
                        ExpenseClassification.NEED -> rule.needsPercent
                        ExpenseClassification.WANT -> rule.wantsPercent
                        ExpenseClassification.SAVING -> rule.savingsPercent
                        else -> 0
                    }
                    val bucketLimit = totalIncome * (bucketPercent / 100.0)
                    
                    var bucketSpent = transactions.filter { it.classification == classification }.sumOf { it.amount }
                    if (oldCalendar.get(Calendar.MONTH) + 1 == month && oldCalendar.get(Calendar.YEAR) == year && oldTransaction.classification == classification) {
                        bucketSpent -= oldTransaction.amount
                    }
                    
                    if (bucketSpent + amount > bucketLimit) {
                        showBucketWarning.value = Pair(classification.name, (bucketSpent + amount) - bucketLimit)
                        return@launch
                    }
                }
            }

            isSaving.value = true
            val updatedTransaction = oldTransaction.copy(
                accountId = accountId,
                categoryId = categoryId,
                amount = amount,
                note = note,
                timestamp = timestamp,
                classification = classification
            )
            repository.updateTransaction(updatedTransaction)
            isSaving.value = false
            onComplete()
        }
    }

    fun saveTransactionWithoutLimits(
        transactionId: Long,
        accountId: Long,
        categoryId: Long,
        amount: Double,
        note: String,
        timestamp: Long,
        classification: ExpenseClassification,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val oldTransaction = transactionFlow.value ?: return@launch
            isSaving.value = true
            val updatedTransaction = oldTransaction.copy(
                accountId = accountId,
                categoryId = categoryId,
                amount = amount,
                note = note,
                timestamp = timestamp,
                classification = classification
            )
            repository.updateTransaction(updatedTransaction)
            isSaving.value = false
            onComplete()
        }
    }

    fun dismissWarnings() {
        showOverBudgetWarning.value = null
        showBucketWarning.value = null
    }
}
