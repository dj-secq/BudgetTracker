package com.example.budgettracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.budgettracker.data.local.entity.BudgetLimit
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetLimitDao {
    @Query("SELECT * FROM budget_limits")
    fun getAllBudgetLimits(): Flow<List<BudgetLimit>>

    @Query("SELECT * FROM budget_limits WHERE month = :month AND year = :year")
    fun getBudgetLimitsForMonth(month: Int, year: Int): Flow<List<BudgetLimit>>

    @Query("SELECT * FROM budget_limits WHERE categoryId = :categoryId AND month = :month AND year = :year")
    suspend fun getBudgetLimit(categoryId: Long, month: Int, year: Int): BudgetLimit?

    @Query("SELECT SUM(assignedAmount) FROM budget_limits WHERE categoryId = :categoryId AND ((year < :year) OR (year = :year AND month < :month))")
    suspend fun getTotalAssignedBeforeMonth(categoryId: Long, month: Int, year: Int): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetLimit(budgetLimit: BudgetLimit): Long

    @Update
    suspend fun updateBudgetLimit(budgetLimit: BudgetLimit)

    @Delete
    suspend fun deleteBudgetLimit(budgetLimit: BudgetLimit)
}
