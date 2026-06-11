package com.example.budgettracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.res.stringResource
import com.example.budgettracker.R
import com.example.budgettracker.ui.components.BudgetProgressBar
import com.example.budgettracker.ui.components.MonthPicker
import com.example.budgettracker.data.local.entity.Account
import com.example.budgettracker.data.local.entity.AccountType
import com.example.budgettracker.ui.theme.CategoryColors
import com.example.budgettracker.ui.theme.EmeraldGreen
import com.example.budgettracker.ui.utils.CategoryIconHelper
import com.example.budgettracker.ui.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToAssignBudget: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDebtTracker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val isFabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                expanded = isFabExpanded,
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add Transaction") },
                text = { Text("New") }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .windowInsetsPadding(WindowInsets.statusBars),
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = 24.dp + innerPadding.calculateBottomPadding()
                )
            ) {
                // Header: Settings and Centered Total Balance
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = onNavigateToSettings) {
                                Icon(
                                    imageVector = Icons.Filled.Settings, 
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.total_balance),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = CurrencyUtils.formatAmount(uiState.totalBalance),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Month Picker
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    MonthPicker(
                        currentMonth = uiState.currentMonth,
                        currentYear = uiState.currentYear,
                        onMonthChanged = { m, y -> viewModel.setMonth(m, y) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Income and Expenses Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(stringResource(R.string.monthly_income), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            Text("+${CurrencyUtils.formatAmount(uiState.totalIncome)}", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(stringResource(R.string.monthly_expenses), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            Text("-${CurrencyUtils.formatAmount(uiState.totalExpenses)}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
                
                // Wallets (Accounts)
                if (uiState.accounts.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(uiState.accounts) { account ->
                                AccountCard(account)
                            }
                        }
                    }
                }

                // Gamification Streaks & Debt Tracker
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Gamification Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, contentDescription = "Streaks", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("No-Spend Streaks", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Current: ${uiState.currentStreak} days", color = MaterialTheme.colorScheme.onTertiaryContainer, fontSize = 14.sp)
                                Text("Longest: ${uiState.longestStreak} days", color = MaterialTheme.colorScheme.onTertiaryContainer, fontSize = 14.sp)
                            }
                        }

                        // Debt Tracker Shortcut
                        Card(
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToDebtTracker,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.MoneyOff, contentDescription = "Debts", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Debt Tracker", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Who owes you &", color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 14.sp)
                                Text("Who you owe", color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // Budget Header with assign icon
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.budgets_header),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Assign icon
                        IconButton(onClick = onNavigateToAssignBudget) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = "Assign Budget",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Budget items with category icons
                items(uiState.budgetItems) { item ->
                    val color = CategoryColors[(item.category.id % CategoryColors.size).toInt()]
                    BudgetProgressBar(
                        categoryName = item.category.name,
                        spent = item.spent,
                        limit = item.limit,
                        baseColor = color,
                        icon = CategoryIconHelper.getIconForCategory(item.category.name)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun AccountCard(account: Account) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(account.colorArgb)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.width(160.dp).height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = account.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = when (account.type) {
                        AccountType.CHECKING -> "✓"
                        AccountType.SAVINGS -> "🏦"
                        AccountType.CREDIT -> "💳"
                    },
                    fontSize = 14.sp
                )
            }
            Text(
                text = CurrencyUtils.formatAmount(account.balance),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
