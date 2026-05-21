package com.example.budgettracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: CategoryType,
    val colorArgb: Int,
    val iconName: String? = null
)

enum class CategoryType {
    INCOME, EXPENSE
}

enum class ExpenseClassification {
    NEED, WANT, SAVING, NONE
}
