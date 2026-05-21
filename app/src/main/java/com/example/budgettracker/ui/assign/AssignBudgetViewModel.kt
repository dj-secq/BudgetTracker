package com.example.budgettracker.ui.assign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgettracker.data.local.entity.BudgetLimit
import com.example.budgettracker.data.local.entity.Category
import com.example.budgettracker.data.local.entity.CategoryType
import com.example.budgettracker.data.repository.BudgetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class AssignBudgetItem(
    val category: Category,
    val limit: Double,
    val spent: Double
)

data class AssignBudgetUiState(
    val budgetItems: List<AssignBudgetItem> = emptyList(),
    val currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
)

@OptIn(ExperimentalCoroutinesApi::class)
class AssignBudgetViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _monthYear = MutableStateFlow(
        Pair(Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.YEAR))
    )

    private val monthlyDataFlow = _monthYear.flatMapLatest { (month, year) ->
        combine(
            repository.getTransactionsForMonth(month, year),
            repository.getBudgetLimitsForMonth(month, year)
        ) { txs, limits ->
            Pair(txs, limits)
        }
    }

    val uiState: StateFlow<AssignBudgetUiState> = combine(
        repository.getAllCategories(),
        monthlyDataFlow,
        _monthYear
    ) { categories: List<Category>, monthlyData: Pair<List<@JvmWildcard Any>, List<BudgetLimit>>, monthYear: Pair<Int, Int> ->
        @Suppress("UNCHECKED_CAST")
        val monthTransactions = monthlyData.first as List<com.example.budgettracker.data.local.entity.Transaction>
        val budgetLimits = monthlyData.second
        val (month, year) = monthYear
        
        val expenseCategories = categories.filter { it.type == CategoryType.EXPENSE }

        val items = expenseCategories.map { category ->
            val limit = budgetLimits.find { it.categoryId == category.id }?.assignedAmount ?: 0.0
            val spent = monthTransactions.filter { it.categoryId == category.id }.sumOf { it.amount }
            AssignBudgetItem(category, limit, spent)
        }

        AssignBudgetUiState(items, month, year)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AssignBudgetUiState())

    fun setMonth(month: Int, year: Int) {
        _monthYear.value = Pair(month, year)
    }

    fun updateBudgetLimit(categoryId: Long, amount: Double) {
        viewModelScope.launch {
            val (month, year) = _monthYear.value
            repository.setBudgetLimit(categoryId, amount, month, year)
        }
    }
}
