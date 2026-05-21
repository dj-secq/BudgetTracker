package com.example.budgettracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

data class PieChartData(
    val label: String,
    val value: Double,
    val color: Color
)

@Composable
fun PieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier,
    donutHoleRatio: Float = 0.5f
) {
    val total = data.sumOf { it.value }.toFloat()
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = (size.width / 2) * (1f - donutHoleRatio)
            val outerRadius = size.width / 2
            val radius = outerRadius - strokeWidth / 2
            val sizeToDraw = Size(radius * 2, radius * 2)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            var startAngle = -90f

            data.forEach { slice ->
                val sweepAngle = if (total > 0) (slice.value.toFloat() / total) * 360f else 0f
                val animatedSweep = sweepAngle * animatedProgress.value

                if (animatedSweep > 0) {
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = sizeToDraw,
                        style = Stroke(width = strokeWidth)
                    )
                }
                startAngle += sweepAngle
            }
            
            // Draw empty state if no data
            if (total == 0f || data.isEmpty()) {
                drawArc(
                    color = Color.Gray.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f * animatedProgress.value,
                    useCenter = false,
                    topLeft = topLeft,
                    size = sizeToDraw,
                    style = Stroke(width = strokeWidth)
                )
            }
        }
    }
}
