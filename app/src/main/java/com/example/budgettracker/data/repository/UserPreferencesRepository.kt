package com.example.budgettracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

data class BudgetRulePreferences(
    val needsPercent: Int,
    val wantsPercent: Int,
    val savingsPercent: Int,
    val themeMode: ThemeMode
)

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val NEEDS_PERCENT = intPreferencesKey("needs_percent")
        val WANTS_PERCENT = intPreferencesKey("wants_percent")
        val SAVINGS_PERCENT = intPreferencesKey("savings_percent")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val budgetRulePreferencesFlow: Flow<BudgetRulePreferences> = dataStore.data
        .map { preferences ->
            val needs = preferences[PreferencesKeys.NEEDS_PERCENT] ?: 50
            val wants = preferences[PreferencesKeys.WANTS_PERCENT] ?: 30
            val savings = preferences[PreferencesKeys.SAVINGS_PERCENT] ?: 20
            val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = try { ThemeMode.valueOf(themeModeStr) } catch (e: Exception) { ThemeMode.SYSTEM }
            BudgetRulePreferences(needs, wants, savings, themeMode)
        }

    suspend fun updateBudgetRule(needs: Int, wants: Int, savings: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NEEDS_PERCENT] = needs
            preferences[PreferencesKeys.WANTS_PERCENT] = wants
            preferences[PreferencesKeys.SAVINGS_PERCENT] = savings
        }
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }
}
