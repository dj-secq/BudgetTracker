package com.example.budgettracker.di

import android.content.Context
import androidx.room.Room
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.budgettracker.data.local.AppDatabase
import com.example.budgettracker.data.repository.BudgetRepository
import com.example.budgettracker.data.repository.UserPreferencesRepository

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

interface AppContainer {
    val database: AppDatabase
    val budgetRepository: BudgetRepository
    val userPreferencesRepository: UserPreferencesRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "budget_tracker_db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    override val budgetRepository: BudgetRepository by lazy {
        BudgetRepository(
            accountDao = database.accountDao(),
            categoryDao = database.categoryDao(),
            budgetLimitDao = database.budgetLimitDao(),
            transactionDao = database.transactionDao(),
            savingsGoalDao = database.savingsGoalDao()
        )
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.dataStore)
    }
}
