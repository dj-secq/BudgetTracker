package com.example.budgettracker.ui.debt

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgettracker.data.local.entity.Debt
import com.example.budgettracker.data.local.entity.DebtType
import com.example.budgettracker.ui.theme.EmeraldGreen
import com.example.budgettracker.ui.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtTrackerScreen(
    viewModel: DebtTrackerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val debts by viewModel.debts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debt Tracker") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Debt")
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            val totalOwedToYou = debts.filter { it.type == DebtType.LENT && !it.isPaid }.sumOf { it.amount }
            val totalYouOwe = debts.filter { it.type == DebtType.BORROWED && !it.isPaid }.sumOf { it.amount }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Owed to you", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 14.sp)
                        Text(CurrencyUtils.formatAmount(totalOwedToYou), fontWeight = FontWeight.Bold, color = EmeraldGreen, fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("You owe", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 14.sp)
                        Text(CurrencyUtils.formatAmount(totalYouOwe), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 18.sp)
                    }
                }
            }

            if (debts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No debts tracked yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(debts) { debt ->
                        DebtItem(
                            debt = debt,
                            onToggleStatus = { viewModel.toggleDebtStatus(debt) },
                            onDelete = { viewModel.deleteDebt(debt) }
                        )
                    }
                }
            }
        }
        
        if (showAddDialog) {
            AddDebtDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, amount, type, note ->
                    viewModel.addDebt(name, amount, type, note)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun DebtItem(debt: Debt, onToggleStatus: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleStatus)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val actionText = if (debt.type == DebtType.LENT) "Lent to" else "Borrowed from"
                Text(
                    text = "$actionText ${debt.personName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textDecoration = if (debt.isPaid) TextDecoration.LineThrough else null,
                    color = if (debt.isPaid) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                if (debt.note.isNotEmpty()) {
                    Text(text = debt.note, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(debt.date)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                val color = if (debt.isPaid) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            else if (debt.type == DebtType.LENT) EmeraldGreen else MaterialTheme.colorScheme.error
                Text(
                    text = CurrencyUtils.formatAmount(debt.amount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = color,
                    textDecoration = if (debt.isPaid) TextDecoration.LineThrough else null
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, DebtType, String) -> Unit
) {
    var personName by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(DebtType.LENT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Debt") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("Person Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == DebtType.LENT, onClick = { type = DebtType.LENT })
                        Text("I Lent")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == DebtType.BORROWED, onClick = { type = DebtType.BORROWED })
                        Text("I Borrowed")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (personName.isNotBlank() && amount != null && amount > 0) {
                        onAdd(personName, amount, type, note)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
