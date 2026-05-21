package com.example.budgettracker.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.budgettracker.R
import com.example.budgettracker.data.repository.ThemeMode
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToWallets: () -> Unit,
    onNavigateToCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    val budgetRule by viewModel.budgetRule.collectAsState()

    var needs by remember(budgetRule) { mutableFloatStateOf(budgetRule?.needsPercent?.toFloat() ?: 50f) }
    var wants by remember(budgetRule) { mutableFloatStateOf(budgetRule?.wantsPercent?.toFloat() ?: 30f) }
    var savings by remember(budgetRule) { mutableFloatStateOf(budgetRule?.savingsPercent?.toFloat() ?: 20f) }

    val total = (needs + wants + savings).roundToInt()
    val isValid = total == 100

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Text(
                text = stringResource(R.string.appearance_header),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Theme Selector
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = budgetRule?.themeMode == mode,
                        onClick = { viewModel.updateThemeMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size)
                    ) {
                        Text(mode.name.lowercase().capitalize())
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.data_management_header),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Wallet Management Card
            Card(
                onClick = onNavigateToWallets,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Wallets", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(stringResource(R.string.manage_wallets), fontWeight = FontWeight.Medium)
                }
            }

            // Category Management Card
            Card(
                onClick = onNavigateToCategories,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Category, contentDescription = "Categories", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(stringResource(R.string.manage_categories), fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.budgeting_rule_header),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text("Adjust your target allocations. Must equal 100%.", color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Needs Slider
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Needs", fontWeight = FontWeight.Bold)
                    Text("${needs.roundToInt()}%")
                }
                Slider(
                    value = needs,
                    onValueChange = { needs = it },
                    valueRange = 0f..100f,
                    steps = 100
                )
            }

            // Wants Slider
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Wants", fontWeight = FontWeight.Bold)
                    Text("${wants.roundToInt()}%")
                }
                Slider(
                    value = wants,
                    onValueChange = { wants = it },
                    valueRange = 0f..100f,
                    steps = 100
                )
            }

            // Savings Slider
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Savings", fontWeight = FontWeight.Bold)
                    Text("${savings.roundToInt()}%")
                }
                Slider(
                    value = savings,
                    onValueChange = { savings = it },
                    valueRange = 0f..100f,
                    steps = 100
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.updateRule(needs.roundToInt(), wants.roundToInt(), savings.roundToInt()) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isValid
            ) {
                Text(
                    if (isValid) "Save Rule" else "Total is $total%, must be 100%",
                    fontSize = 16.sp
                )
            }
        }
    }
}
