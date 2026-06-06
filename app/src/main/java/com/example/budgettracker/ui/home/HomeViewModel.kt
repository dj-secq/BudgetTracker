package com.example.budgettracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope


import com.example.budgettracker.data.local.entity.Account
import com.example.budgettracker.data.local.entity.BudgetLimit
import com.example.budgettracker.data.local.entity.Category
import com.example.budgettracker.data.local.entity.CategoryType
import com.example.budgettracker.data.local.entity.Transaction
import com.example.budgettracker.data.repository.BudgetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class BudgetItem(
    val category: Category,
    val limit: Double,
    val spent: Double
)

data class HomeUiState(
    val accounts: List<Account> = emptyList(),
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val budgetItems: List<BudgetItem> = emptyList(),
    val currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
)

@OptIn(ExperimentalCoroutinesApi::class)

class HomeViewModel(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _monthYear = MutableStateFlow(
        Pair(Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.YEAR))
    )

    private val monthlyDataFlow = _monthYear.flatMapLatest { (month, year) ->
        combine(
            budgetRepository.getTransactionsForMonth(month, year),
            budgetRepository.getBudgetLimitsForMonth(month, year)
        ) { txs, limits ->
            Pair(txs, limits)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        budgetRepository.getAllAccounts(),
        budgetRepository.getAllCategories(),
        monthlyDataFlow,
        _monthYear
    ) { accounts: List<Account>, categories: List<Category>, monthlyData: Pair<List<Transaction>, List<BudgetLimit>>, monthYear: Pair<Int, Int> ->
        
        val (monthTransactions, budgetLimits) = monthlyData
        val (month, year) = monthYear
        
        val incomeCategories = categories.filter { it.type == CategoryType.INCOME }
        val expenseCategories = categories.filter { it.type == CategoryType.EXPENSE }
        
        // Use monthly transactions for income/expense/budget calculations
        val totalIncome = monthTransactions.filter { tx -> 
            incomeCategories.any { it.id == tx.categoryId } 
        }.sumOf { it.amount }

        val totalExpenses = monthTransactions.filter { tx -> 
            expenseCategories.any { it.id == tx.categoryId } 
        }.sumOf { it.amount }
        
        // Sum all account balances
        val totalBalance = accounts.sumOf { it.balance }
        
        // Budget items use monthly spending
        val items = expenseCategories.map { category ->
            val limit = budgetLimits.find { it.categoryId == category.id }?.assignedAmount ?: 0.0
            val spent = monthTransactions.filter { it.categoryId == category.id }.sumOf { it.amount }
            BudgetItem(category, limit, spent)
        }
        
        HomeUiState(
            accounts = accounts,
            totalBalance = totalBalance,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            budgetItems = items,
            currentMonth = month,
            currentYear = year
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun setMonth(month: Int, year: Int) {
        _monthYear.value = Pair(month, year)
    }
}
