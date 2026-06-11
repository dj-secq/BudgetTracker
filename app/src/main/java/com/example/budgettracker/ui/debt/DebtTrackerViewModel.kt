package com.example.budgettracker.ui.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgettracker.data.local.entity.Debt
import com.example.budgettracker.data.local.entity.DebtType
import com.example.budgettracker.data.repository.BudgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DebtTrackerViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    val debts: StateFlow<List<Debt>> = repository.getAllDebts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDebt(personName: String, amount: Double, type: DebtType, note: String) {
        viewModelScope.launch {
            val debt = Debt(
                personName = personName,
                amount = amount,
                type = type,
                date = System.currentTimeMillis(),
                isPaid = false,
                note = note
            )
            repository.insertDebt(debt)
        }
    }

    fun toggleDebtStatus(debt: Debt) {
        viewModelScope.launch {
            repository.updateDebt(debt.copy(isPaid = !debt.isPaid))
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }
}
