package com.example.budgettracker.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.budgettracker.ui.model.TransactionUiItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgettracker.ui.utils.CategoryIconHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    
    var transactionToDelete by remember { mutableStateOf<com.example.budgettracker.data.local.entity.Transaction?>(null) }
    
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val groupedTransactions = remember(uiState.transactions) {
        uiState.transactions.groupBy {
            SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(it.transaction.timestamp))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (uiState.filterCategoryIds.isNotEmpty() || uiState.filterAccountId != null || uiState.startDate != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        if (uiState.transactions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (uiState.filterCategoryIds.isNotEmpty() || uiState.filterAccountId != null || uiState.startDate != null)
                            "No transactions match your filters." 
                        else "No transactions yet.", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupedTransactions.forEach { (date, transactions) ->
                    item {
                        Text(
                            text = date,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(transactions, key = { it.transaction.id }) { item ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    transactionToDelete = item.transaction
                                    false
                                } else {
                                    false
                                }
                            }
                        )
                        
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) MaterialTheme.colorScheme.error else Color.Transparent
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Transaction",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = CategoryIconHelper.getIconForCategory(item.categoryName),
                                        contentDescription = item.categoryName,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.transaction.note.ifEmpty { item.categoryName }, 
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.categoryName,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 12.sp
                                            )
                                            if (item.transaction.classification != com.example.budgettracker.data.local.entity.ExpenseClassification.NONE) {
                                                Text(
                                                    text = " • ${item.transaction.classification.name}",
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    val isIncome = item.categoryType == com.example.budgettracker.data.local.entity.CategoryType.INCOME
                                    Text(
                                        text = "${if(isIncome) "+" else "-"}${com.example.budgettracker.ui.utils.CurrencyUtils.formatAmount(Math.abs(item.transaction.amount))}", 
                                        color = if (isIncome) com.example.budgettracker.ui.theme.EmeraldGreen else MaterialTheme.colorScheme.onSurface, 
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    transactionToDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.deleteTransaction(transaction)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFilterSheet) {
        var tempCategoryIds by remember { mutableStateOf(uiState.filterCategoryIds) }
        var tempAccountId by remember { mutableStateOf(uiState.filterAccountId) }
        var tempStartDate by remember { mutableStateOf(uiState.startDate) }
        var tempEndDate by remember { mutableStateOf(uiState.endDate) }

        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filters", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { 
                        tempCategoryIds = emptySet()
                        tempAccountId = null
                        tempStartDate = null
                        tempEndDate = null
                        viewModel.clearFilters()
                    }) {
                        Text("Clear All")
                    }
                }

                Text("Category", fontWeight = FontWeight.Bold)
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = tempCategoryIds.isEmpty(),
                            onClick = { tempCategoryIds = emptySet() },
                            label = { Text("All") }
                        )
                    }
                    items(uiState.categories) { category ->
                        FilterChip(
                            selected = tempCategoryIds.contains(category.id),
                            onClick = { 
                                tempCategoryIds = if (tempCategoryIds.contains(category.id)) {
                                    tempCategoryIds - category.id
                                } else {
                                    tempCategoryIds + category.id
                                }
                            },
                            label = { Text(category.name) }
                        )
                    }
                }

                Text("Account", fontWeight = FontWeight.Bold)
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = tempAccountId == null,
                            onClick = { tempAccountId = null },
                            label = { Text("All") }
                        )
                    }
                    items(uiState.accounts) { account ->
                        FilterChip(
                            selected = tempAccountId == account.id,
                            onClick = { tempAccountId = account.id },
                            label = { Text(account.name) }
                        )
                    }
                }

                Text("Date Range", fontWeight = FontWeight.Bold)
                
                var showStartDatePicker by remember { mutableStateOf(false) }
                var showEndDatePicker by remember { mutableStateOf(false) }

                val startState = rememberDatePickerState(initialSelectedDateMillis = tempStartDate)
                val endState = rememberDatePickerState(initialSelectedDateMillis = tempEndDate)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(tempStartDate?.let { 
                            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(it))
                        } ?: "Start Date")
                    }

                    Text("to")

                    OutlinedButton(
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(tempEndDate?.let { 
                            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(it))
                        } ?: "End Date")
                    }
                }

                if (showStartDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showStartDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                tempStartDate = startState.selectedDateMillis
                                showStartDatePicker = false
                            }) { Text("OK") }
                        }
                    ) { DatePicker(state = startState) }
                }

                if (showEndDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showEndDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                tempEndDate = endState.selectedDateMillis
                                showEndDatePicker = false
                            }) { Text("OK") }
                        }
                    ) { DatePicker(state = endState) }
                }

                Button(
                    onClick = { 
                        viewModel.applyFilters(tempCategoryIds, tempAccountId, tempStartDate, tempEndDate)
                        showFilterSheet = false 
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}
