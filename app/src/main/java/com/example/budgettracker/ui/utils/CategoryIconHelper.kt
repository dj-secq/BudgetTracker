package com.example.budgettracker.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    fun getIconForCategory(categoryName: String): ImageVector {
        return when (categoryName.lowercase()) {
            "groceries" -> Icons.Filled.ShoppingCart
            "food", "dining", "restaurant" -> Icons.Filled.Restaurant
            "rent", "housing", "mortgage" -> Icons.Filled.Home
            "entertainment", "fun", "movies" -> Icons.Filled.Movie
            "transport", "car", "gas" -> Icons.Filled.DirectionsCar
            "salary", "income", "paycheck" -> Icons.Filled.AttachMoney
            "utilities", "electric", "water" -> Icons.Filled.Bolt
            "health", "medical", "pharmacy" -> Icons.Filled.LocalHospital
            "shopping", "clothes" -> Icons.Filled.LocalMall
            "education", "school", "tuition" -> Icons.Filled.School
            "insurance", "protection" -> Icons.Filled.Security
            "investments", "stock", "crypto" -> Icons.AutoMirrored.Filled.TrendingUp
            "gifts", "donation", "present" -> Icons.Filled.CardGiftcard
            "personal care", "beauty", "spa" -> Icons.Filled.Face
            "travel", "flight", "vacation" -> Icons.Filled.Flight
            "subscription", "streaming", "gym" -> Icons.Filled.Subscriptions
            "maintenance", "repair", "service" -> Icons.Filled.Build
            "savings", "fund" -> Icons.Filled.Savings
            "loan", "debt", "repayment" -> Icons.Filled.Payments
            "allowance" -> Icons.Filled.Wallet
            "food" -> Icons.Filled.Restaurant
            "lost" -> Icons.Filled.SearchOff
            "rent income" -> Icons.Filled.Apartment
            "bonus" -> Icons.Filled.Star
            else -> Icons.Filled.List
        }
    }
}
