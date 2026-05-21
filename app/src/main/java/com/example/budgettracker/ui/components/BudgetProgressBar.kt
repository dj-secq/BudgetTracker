package com.example.budgettracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgettracker.ui.theme.EmeraldGreen
import com.example.budgettracker.ui.utils.CurrencyUtils

@Composable
fun BudgetProgressBar(
    categoryName: String,
    spent: Double,
    limit: Double,
    baseColor: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    val progress = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1f) else 0f
    val isOverBudget = spent > limit
    
    // Choose colors
    val activeColor = if (isOverBudget) MaterialTheme.colorScheme.error else baseColor
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = activeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = categoryName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            
                val remaining = limit - spent
                if (isOverBudget) {
                    Text(
                        text = "Over: ${CurrencyUtils.formatAmount(Math.abs(remaining))}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "Left: ${CurrencyUtils.formatAmount(remaining)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = EmeraldGreen
                    )
                }
            }
        
            Spacer(modifier = Modifier.height(8.dp))
        
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(trackColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress)
                        .fillMaxHeight()
                        .background(activeColor)
                )
            }
        
            Spacer(modifier = Modifier.height(4.dp))
        
            Text(
                text = "Spent: ${CurrencyUtils.formatAmount(spent)} of ${CurrencyUtils.formatAmount(limit)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
