package com.example.budgettracker.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.budgettracker.R
import com.example.budgettracker.ui.components.BarChartData
import com.example.budgettracker.ui.components.HorizontalBarChart
import com.example.budgettracker.ui.components.MonthPicker
import com.example.budgettracker.ui.components.PieChart
import com.example.budgettracker.ui.components.PieChartData
import com.example.budgettracker.ui.theme.CategoryColors
import com.example.budgettracker.ui.theme.EmeraldGreen
import com.example.budgettracker.ui.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tab_analytics), fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp)
        ) {
            MonthPicker(
                currentMonth = uiState.currentMonth,
                currentYear = uiState.currentYear,
                onMonthChanged = { m, y -> viewModel.setMonth(m, y) }
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.totalExpenses == 0.0 && uiState.totalIncome == 0.0) {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No data this month",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                    Text(
                        "Add transactions to see analytics",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Dashboard Cards
                    items(uiState.bucketStats) { stat ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = stat.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Goal: ${CurrencyUtils.formatAmount(stat.goal)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Actual: ${CurrencyUtils.formatAmount(stat.actual)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val isUnderBudget = stat.net >= 0
                                Text(
                                    text = "Net: ${if (isUnderBudget) "+" else "-"}${CurrencyUtils.formatAmount(Math.abs(stat.net))}",
                                    color = if (isUnderBudget) EmeraldGreen else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Pie Chart for spending breakdown
                    if (uiState.categorySpending.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Spending Breakdown",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val pieData = uiState.categorySpending.sortedByDescending { it.totalSpent }.map { item ->
                                val color = CategoryColors[(item.category.id % CategoryColors.size).toInt()]
                                PieChartData(
                                    label = item.category.name,
                                    value = item.totalSpent,
                                    color = color
                                )
                            }
                            
                            PieChart(
                                data = pieData,
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .padding(vertical = 8.dp)
                            )
                            
                            // Legend
                            Spacer(modifier = Modifier.height(8.dp))
                            pieData.forEach { item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .padding(end = 0.dp)
                                    ) {
                                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawCircle(color = item.color)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.label,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = CurrencyUtils.formatAmount(item.value),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "Spending by Category",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState.categorySpending.isEmpty()) {
                            Text("No spending data this month.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val barData = uiState.categorySpending.sortedByDescending { it.totalSpent }.map { item ->
                                val color = CategoryColors[(item.category.id % CategoryColors.size).toInt()]
                                val percentage = if (uiState.totalExpenses > 0) (item.totalSpent / uiState.totalExpenses) * 100 else 0.0
                                BarChartData(
                                    label = item.category.name,
                                    value = item.totalSpent,
                                    color = color,
                                    percentageText = "${String.format(java.util.Locale.getDefault(), "%.1f", percentage)}%"
                                )
                            }

                            HorizontalBarChart(
                                data = barData,
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
