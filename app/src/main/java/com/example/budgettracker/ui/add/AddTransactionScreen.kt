package com.example.budgettracker.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.res.stringResource
import com.example.budgettracker.R
import com.example.budgettracker.data.local.entity.CategoryType
import com.example.budgettracker.data.local.entity.ExpenseClassification
import com.example.budgettracker.ui.theme.CategoryColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val haptic = LocalHapticFeedback.current

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var transactionType by remember { mutableStateOf(CategoryType.EXPENSE) }
    var selectedClassification by remember { mutableStateOf(ExpenseClassification.NONE) }
    
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
    
    val accounts by viewModel.accounts.collectAsState()
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    
    val overBudgetWarning by viewModel.showOverBudgetWarning.collectAsState()
    val bucketWarning by viewModel.showBucketWarning.collectAsState()
    
    val filteredCategories = categories.filter { it.type == transactionType }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_transaction_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) amount = it },
                label = { Text(stringResource(R.string.amount_label)) },
                prefix = { Text("₱") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold)
            )

            // Transaction Type Toggle
            TabRow(
                selectedTabIndex = if (transactionType == CategoryType.EXPENSE) 0 else 1,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = transactionType == CategoryType.EXPENSE,
                    onClick = { transactionType = CategoryType.EXPENSE; selectedCategoryId = null },
                    text = { Text("Expense", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = transactionType == CategoryType.INCOME,
                    onClick = { transactionType = CategoryType.INCOME; selectedCategoryId = null },
                    text = { Text("Income", fontWeight = FontWeight.Bold) }
                )
            }

            // Account Selection
            Text(stringResource(R.string.account_label), fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(accounts) { account ->
                    val isSelected = selectedAccountId == account.id
                    val color = Color(account.colorArgb)
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedAccountId = account.id }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = account.name,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Category Selection
            Text(stringResource(R.string.category_label), fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val chunkedCategories = filteredCategories.chunked(3)
                chunkedCategories.forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowCategories.forEach { category ->
                            val isSelected = selectedCategoryId == category.id
                            val color = CategoryColors[(category.id % CategoryColors.size).toInt()]
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable { selectedCategoryId = category.id }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = com.example.budgettracker.ui.utils.CategoryIconHelper.getIconForCategory(category.name),
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = category.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        // Fill empty spots if row is not full
                        repeat(3 - rowCategories.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Classification Selection (Only for Expenses)
            if (transactionType == CategoryType.EXPENSE) {
                Text("Classification", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val classifications = listOf(
                        ExpenseClassification.NEED,
                        ExpenseClassification.WANT,
                        ExpenseClassification.SAVING,
                        ExpenseClassification.NONE
                    )
                    classifications.forEach { clazz ->
                        FilterChip(
                            selected = selectedClassification == clazz,
                            onClick = { selectedClassification = clazz },
                            label = { Text(clazz.name) }
                        )
                    }
                }
            }
            
            // Date Selection
            val formattedDate = remember(selectedDateMillis) {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDateMillis))
            }
            OutlinedTextField(
                value = formattedDate,
                onValueChange = { },
                label = { Text(stringResource(R.string.date_label)) },
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                selectedDateMillis = it
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(stringResource(R.string.cancel_button))
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.note_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    if (parsedAmount != null && selectedCategoryId != null && selectedAccountId != null) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (transactionType == CategoryType.EXPENSE) {
                            viewModel.onConfirmSave(
                                selectedAccountId!!,
                                selectedCategoryId!!,
                                parsedAmount,
                                note,
                                selectedDateMillis,
                                selectedClassification
                            ) {
                                onNavigateBack()
                            }
                        } else {
                            viewModel.saveTransaction(
                                selectedAccountId!!,
                                selectedCategoryId!!,
                                parsedAmount,
                                note,
                                selectedDateMillis,
                                ExpenseClassification.NONE
                            ) {
                                onNavigateBack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = amount.isNotEmpty() && selectedCategoryId != null && selectedAccountId != null && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.save_transaction), fontSize = 18.sp, color = Color.Black)
            }
        }
    }

    // Bucket warning dialog
    bucketWarning?.let { (bucketName, excess) ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissWarnings() },
            title = { Text("$bucketName Cap Warning") },
            text = { Text("This transaction will put you ₱${String.format(Locale.getDefault(), "%.2f", excess)} over your planned $bucketName allocation. Do you still want to save it?") },
            confirmButton = {
                TextButton(onClick = {
                    val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                    viewModel.saveTransaction(
                        selectedAccountId!!,
                        selectedCategoryId!!,
                        parsedAmount,
                        note,
                        selectedDateMillis,
                        selectedClassification
                    ) {
                        viewModel.dismissWarnings()
                        onNavigateBack()
                    }
                }) {
                    Text(stringResource(R.string.yes_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissWarnings() }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    // Over budget warning dialog
    overBudgetWarning?.let { excess ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissWarnings() },
            title = { Text("Over Budget Warning") },
            text = { Text("This transaction will put you ₱${String.format(Locale.getDefault(), "%.2f", excess)} over your budget for this category. Do you still want to save it?") },
            confirmButton = {
                TextButton(onClick = {
                    val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                    viewModel.saveTransaction(
                        selectedAccountId!!,
                        selectedCategoryId!!,
                        parsedAmount,
                        note,
                        selectedDateMillis,
                        selectedClassification
                    ) {
                        viewModel.dismissWarnings()
                        onNavigateBack()
                    }
                }) {
                    Text(stringResource(R.string.yes_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissWarnings() }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    // Handle back navigation on success (when not using manual callback)
    LaunchedEffect(isSaving) {
        if (!isSaving && overBudgetWarning == null && amount.isEmpty() && selectedCategoryId == null) {
            // This is a bit tricky since we don't have a clear "success" state other than the callback
            // For now, the onConfirmSave and saveTransaction with callback should handle it.
        }
    }
}
