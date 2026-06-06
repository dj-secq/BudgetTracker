package com.example.budgettracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgettracker.data.local.entity.Account
import com.example.budgettracker.data.local.entity.Category
import com.example.budgettracker.data.local.entity.RecurringTransaction
import com.example.budgettracker.data.repository.BudgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecurringTransactionItem(
    val recurringTransaction: RecurringTransaction,
    val category: Category?,
    val account: Account?
)

class RecurringTransactionsViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    val recurringTransactions: StateFlow<List<RecurringTransactionItem>> = combine(
        repository.getAllRecurringTransactions(),
        repository.getAllCategories(),
        repository.getAllAccounts()
    ) { transactions, categories, accounts ->
        transactions.map { tx ->
            RecurringTransactionItem(
                recurringTransaction = tx,
                category = categories.find { it.id == tx.categoryId },
                account = accounts.find { it.id == tx.accountId }
            )
        }.sortedBy { it.recurringTransaction.nextRunTime }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteRecurringTransaction(transaction: RecurringTransaction) {
        viewModelScope.launch {
            repository.deleteRecurringTransaction(transaction)
        }
    }
}
