package com.example.budgettracker

import android.app.Application
import com.example.budgettracker.di.AppContainer
import com.example.budgettracker.di.DefaultAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BudgetTrackerApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        
        CoroutineScope(Dispatchers.IO).launch {
            com.example.budgettracker.data.seedDatabaseIfEmpty(container.budgetRepository)
        }
    }
}
