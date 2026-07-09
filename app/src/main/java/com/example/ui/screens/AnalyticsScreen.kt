package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientBackground

data class DayTrend(val dayName: String, val minutes: Float, val isTargetAchieved: Boolean)
data class CategoryStat(val name: String, val percentage: Float, val color: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun AnalyticsScreen() {
    // 1. Staggered animation triggers
    var startAnims by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnims = true
    }

    // 2. Filter tabs: Day / Week / Month
    var selectedFilter by remember { mutableStateOf("Week") }

    // 3. Interactive weekly trend data
    val weeklyData = remember {
        listOf(
            DayTrend("M", 120f, true),
            DayTrend("T", 180f, true),
            DayTrend("W", 90f, false),
            DayTrend("T", 210f, true),
            DayTrend("F", 150f, true),
            DayTrend("S", 60f, false),
            DayTrend("S", 45f, false)
        )
    }
    var selectedDayIndex by remember { mutableIntStateOf(3) } // Default Thursday active

    // 4. Category statistics
    val categories = remember {
        listOf(
            CategoryStat("Coding", 0.45f, Color(0xFF06B6D4), Icons.Default.Code),
            CategoryStat("Study", 0.30f, Color(0xFF8B5CF6), Icons.Default.MenuBook),
            CategoryStat("Health", 0.15f, Color(0xFF10B981), Icons.Default.Favorite),
            CategoryStat("Admin", 0.10f, Color(0xFFF59E0B), Icons.Default.Work)
        )
    }

    GradientBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 40.dp, bottom = 110.dp)
        ) {
            // Header / Filter layout
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Flow Analytics",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Track your focus metrics over time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    // Glassy segmented control filter
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSystemInDarkTheme()) Color(0x11FFFFFF) else Color(0x0F000000))
                            .padding(2.dp)
                    ) {
                        listOf("Week", "Month").forEach { tab ->
                            val isTabSelected = selectedFilter == tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isTabSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedFilter = tab }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tab,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isTabSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // Key Stats Grid Row
            item {
                val alpha by animateFloatAsState(
                    targetValue = if (startAnims) 1f else 0f,
                    animationSpec = tween(500, delayMillis = 150),
                    label = "KeyStatsAlpha"
                )
                val slideY by animateDpAsState(
                    targetValue = if (startAnims) 0.dp else 20.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "KeyStatsSlide"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alpha)
                        .offset(y = slideY),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tile 1
                    GlassCard(modifier = Modifier.weight(1f), cornerRadius = 16.dp) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Focus Hours",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Total Focus",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "14.5 hrs",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Tile 2
                    GlassCard(modifier = Modifier.weight(1f), cornerRadius = 16.dp) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Average Session",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Avg Session",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "42 mins",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Tile 3
                    GlassCard(modifier = Modifier.weight(1f), cornerRadius = 16.dp) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Score",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Flow Score",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "94%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Interactive Weekly Trend custom Canvas Bar Chart
            item {
                val alpha by animateFloatAsState(
                    targetValue = if (startAnims) 1f else 0f,
                    animationSpec = tween(500, delayMillis = 300),
                    label = "ChartAlpha"
                )
                val slideY by animateDpAsState(
                    targetValue = if (startAnims) 0.dp else 20.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "ChartSlide"
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alpha)
                        .offset(y = slideY),
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Trend",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Target: 120m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        // Custom Bar Chart using Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            val activePrimaryColor = MaterialTheme.colorScheme.primary
                            val activeSecondaryColor = MaterialTheme.colorScheme.secondary
                            val neutralGray = if (isSystemInDarkTheme()) Color(0x11FFFFFF) else Color(0x0E000000)

                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                weeklyData.forEachIndexed { index, trend ->
                                    val isSelected = selectedDayIndex == index
                                    // Animated height fraction
                                    val heightFraction by animateFloatAsState(
                                        targetValue = if (startAnims) trend.minutes / 240f else 0f,
                                        animationSpec = tween(800, delayMillis = index * 80, easing = EaseOutBack),
                                        label = "BarHeightAnim"
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                selectedDayIndex = index
                                            }
                                    ) {
                                        // Bar representation with custom canvas drawing
                                        Box(
                                            modifier = Modifier
                                                .width(18.dp)
                                                .height(120.dp)
                                        ) {
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                val barHeight = size.height * heightFraction
                                                val barWidth = size.width
                                                val topOffset = size.height - barHeight

                                                // Background slot
                                                drawRoundRect(
                                                    color = neutralGray,
                                                    size = size,
                                                    cornerRadius = CornerRadius(10f, 10f)
                                                )

                                                // Filling bar
                                                val barBrush = Brush.verticalGradient(
                                                    colors = if (isSelected) {
                                                        listOf(activeSecondaryColor, activePrimaryColor)
                                                    } else {
                                                        listOf(
                                                            activePrimaryColor.copy(alpha = 0.6f),
                                                            activePrimaryColor.copy(alpha = 0.3f)
                                                        )
                                                    }
                                                )
                                                drawRoundRect(
                                                    brush = barBrush,
                                                    topLeft = Offset(0f, topOffset),
                                                    size = Size(barWidth, barHeight),
                                                    cornerRadius = CornerRadius(10f, 10f)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = trend.dayName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) activePrimaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Daily Drill down summary for the selected day in bar chart
            item {
                val selectedDayTrend = weeklyData.getOrNull(selectedDayIndex)
                if (selectedDayTrend != null) {
                    val dayFullNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                    val dayNameLong = dayFullNames.getOrElse(selectedDayIndex) { "Selected Day" }

                    val alpha by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = tween(400),
                        label = "DayDetailAlpha"
                    )

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(alpha),
                        cornerRadius = 20.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = dayNameLong,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = if (selectedDayTrend.isTargetAchieved) Icons.Default.CheckCircle else Icons.Default.HourglassBottom,
                                        contentDescription = "Status",
                                        tint = if (selectedDayTrend.isTargetAchieved) Color(0xFF10B981) else Color(0xFFF59E0B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (selectedDayTrend.isTargetAchieved) "Daily Target Completed" else "Target Incomplete",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${selectedDayTrend.minutes.toInt()} mins",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "Focused Block",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }

            // Section: Focus categories distribution segment charts
            item {
                val alpha by animateFloatAsState(
                    targetValue = if (startAnims) 1f else 0f,
                    animationSpec = tween(500, delayMillis = 450),
                    label = "CategoryAlpha"
                )
                val slideY by animateDpAsState(
                    targetValue = if (startAnims) 0.dp else 20.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "CategorySlide"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alpha)
                        .offset(y = slideY)
                ) {
                    Text(
                        text = "Focus Distribution",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Circular donut segmented ring chart
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(110.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val isDark = isSystemInDarkTheme()
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        var currentAngle = -90f
                                        val bgRingColor = if (isDark) Color(0x11FFFFFF) else Color(0x0D000000)

                                        // Background donut ring
                                        drawCircle(
                                            color = bgRingColor,
                                            radius = size.minDimension / 2 - 8.dp.toPx(),
                                            style = Stroke(width = 6.dp.toPx())
                                        )

                                        // Dynamic Arc segments for category percentages
                                        categories.forEach { cat ->
                                            val sweepAngle = 360f * cat.percentage
                                            drawArc(
                                                color = cat.color,
                                                startAngle = currentAngle,
                                                sweepAngle = sweepAngle,
                                                useCenter = false,
                                                style = Stroke(
                                                    width = 8.dp.toPx(),
                                                    cap = StrokeCap.Round
                                                ),
                                                topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
                                                size = size.copy(
                                                    width = size.width - 16.dp.toPx(),
                                                    height = size.height - 16.dp.toPx()
                                                )
                                            )
                                            currentAngle += sweepAngle
                                        }
                                    }

                                    // Centered core stat
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "4",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Sectors",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                // Legend of Category indicators
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    categories.forEach { category ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(category.color)
                                            )
                                            Text(
                                                text = category.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "${(category.percentage * 100).toInt()}%",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

