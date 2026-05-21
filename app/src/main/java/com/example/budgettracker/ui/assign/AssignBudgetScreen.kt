package com.example.budgettracker.ui.assign

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgettracker.ui.components.MonthPicker

import com.example.budgettracker.ui.utils.CategoryIconHelper
import com.example.budgettracker.ui.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignBudgetScreen(
    viewModel: AssignBudgetViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var categoryToEdit by remember { mutableStateOf<Long?>(null) }
    var editAmount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assign Budget") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header — Month picker only
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MonthPicker(
                    currentMonth = uiState.currentMonth,
                    currentYear = uiState.currentYear,
                    onMonthChanged = { m, y -> viewModel.setMonth(m, y) }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(uiState.budgetItems) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                categoryToEdit = item.category.id
                                editAmount = if (item.limit > 0) item.limit.toString() else ""
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = CategoryIconHelper.getIconForCategory(item.category.name),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(item.category.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("Spent: ${CurrencyUtils.formatAmount(item.spent)}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Limit: ${CurrencyUtils.formatAmount(item.limit)}", fontWeight = FontWeight.Bold)
                                Icon(Icons.Filled.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    categoryToEdit?.let { categoryId ->
        AlertDialog(
            onDismissRequest = { categoryToEdit = null },
            title = { Text("Assign Budget") },
            text = {
                OutlinedTextField(
                    value = editAmount,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) editAmount = it },
                    label = { Text("Amount") },
                    prefix = { Text("₱") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = editAmount.toDoubleOrNull() ?: 0.0
                    viewModel.updateBudgetLimit(categoryId, amount)
                    categoryToEdit = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
