package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientBackground
import kotlinx.coroutines.delay
import java.util.Calendar

// Data classes for interactive UI state
data class QuickAction(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val activeColor: Color
)

data class TimetableBlock(
    val id: Int,
    val title: String,
    val timeRange: String,
    val durationMin: Int,
    val category: String,
    val initialCompleted: Boolean = false
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen() {
    // Staggered entry anim state
    var startAnimations by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimations = true
    }

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    // 1. Live Interactive Timer Integration with Foreground Service
    val context = LocalContext.current
    val isServiceRunning by com.example.service.FocusSessionService.isRunning.collectAsStateWithLifecycle()
    val isServicePaused by com.example.service.FocusSessionService.isPaused.collectAsStateWithLifecycle()
    val serviceTimeLeftSeconds by com.example.service.FocusSessionService.timeLeftSeconds.collectAsStateWithLifecycle()
    val serviceSelectedDurationMinutes by com.example.service.FocusSessionService.selectedDurationMinutes.collectAsStateWithLifecycle()

    var selectedDurationMinutes by remember { mutableIntStateOf(25) }
    
    // Effective states depending on whether the service is running or not
    val isTimerRunning = isServiceRunning && !isServicePaused
    val activeSelectedMinutes = if (isServiceRunning) serviceSelectedDurationMinutes else selectedDurationMinutes
    val timeLeftSeconds = if (isServiceRunning) serviceTimeLeftSeconds else (selectedDurationMinutes * 60)
    val totalSeconds = activeSelectedMinutes * 60

    // Dialog & Custom selection states
    var showCustomDurationDialog by remember { mutableStateOf(false) }
    var tempCustomMinutes by remember { mutableIntStateOf(45) }
    var showStopConfirmationDialog by remember { mutableStateOf(false) }
    var completedSessionDuration by remember { mutableStateOf<Int?>(null) }

    // Request permissions launcher for Android 13+ (POST_NOTIFICATIONS)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        com.example.service.FocusSessionService.startService(context, selectedDurationMinutes)
    }

    // Collect completion event from service to trigger gorgeous animation overlay
    LaunchedEffect(Unit) {
        com.example.service.FocusSessionService.sessionCompletedEvent.collect { duration ->
            completedSessionDuration = duration
        }
    }

    // Motivational Quotes list
    val quotes = listOf(
        "Focus is a matter of deciding what things you're not going to do." to "John Carmack",
        "Deep work is an indispensable skill in our modern economy." to "Cal Newport",
        "Your focus determines your reality." to "Qui-Gon Jinn",
        "The secret of getting ahead is getting started." to "Mark Twain",
        "It is not that we have a short time to live, but that we waste a lot of it." to "Seneca",
        "Flow is the state of optimal experience." to "Mihaly Csikszentmihalyi",
        "Concentrate all your thoughts upon the work at hand." to "Alexander Graham Bell",
        "Don't watch the clock; do what it does. Keep going." to "Sam Levenson"
    )
    val activeQuote = remember(activeSelectedMinutes) {
        quotes[activeSelectedMinutes % quotes.size]
    }

    // Timer progress (0.0f to 1.0f)
    val timerProgress by animateFloatAsState(
        targetValue = if (totalSeconds > 0) timeLeftSeconds.toFloat() / totalSeconds.toFloat() else 1f,
        animationSpec = tween(500, easing = LinearEasing),
        label = "TimerProgress"
    )

    // Breathing pulse scale animation when timer is running
    val infiniteTransition = rememberInfiniteTransition(label = "BreathingScale")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isTimerRunning) 1.05f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // 2. Interactive Timetable Block Completion
    val timetableList = remember {
        mutableStateListOf(
            TimetableBlock(1, "UI Design Sprint", "09:00 - 10:30", 90, "Creative"),
            TimetableBlock(2, "Kotlin & Compose Refactor", "11:00 - 12:30", 90, "Development"),
            TimetableBlock(3, "Deep Reading: System Architecture", "14:00 - 15:30", 90, "Research"),
            TimetableBlock(4, "Evening Sync & Journal", "17:00 - 17:30", 30, "Admin")
        )
    }
    val completedBlocks = remember { mutableStateMapOf<Int, Boolean>() }

    // Initial load
    LaunchedEffect(Unit) {
        timetableList.forEach { block ->
            completedBlocks[block.id] = block.initialCompleted
        }
    }

    // Calculate dynamic stats
    val completedCount = completedBlocks.values.count { it }
    val totalFocusTimeToday = timetableList.filter { completedBlocks[it.id] == true }.sumOf { it.durationMin }
    val progressFraction = if (timetableList.isNotEmpty()) completedCount.toFloat() / timetableList.size.toFloat() else 0f

    // 3. Interactive Quick Actions Toggles
    val quickActions = remember {
        listOf(
            QuickAction("dnd", "Do Not Disturb", "Mute all notifications", Icons.Default.DoNotDisturbOn, Color(0xFFEF4444)),
            QuickAction("strict", "Strict Blocking", "Auto-lock social apps", Icons.Default.Security, Color(0xFF06B6D4)),
            QuickAction("ambient", "Binaural Beats", "Play focus noise", Icons.Default.MusicNote, Color(0xFF8B5CF6)),
            QuickAction("flip", "Flip to Focus", "Lock on screen down", Icons.Default.ScreenRotation, Color(0xFF10B981))
        )
    }
    val activeQuickActions = remember { mutableStateMapOf<String, Boolean>() }

    // Dynamic Greeting based on current hours
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good morning, Focus Architect"
            in 12..16 -> "Good afternoon, Flow Maker"
            else -> "Good evening, Deep Thinker"
        }
    }

    GradientBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 40.dp, bottom = 110.dp)
        ) {
            // Header / Greeting card
            item {
                val alpha by animateFloatAsState(
                    targetValue = if (startAnimations) 1f else 0f,
                    animationSpec = tween(500, delayMillis = 100),
                    label = "HeaderAlpha"
                )
                val slideY by animateDpAsState(
                    targetValue = if (startAnimations) 0.dp else (-20).dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "HeaderSlide"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .alpha(alpha)
                        .offset(y = slideY)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isTimerRunning) Color(0xFF10B981) else Color(0xFFEF4444))
                                )
                                Text(
                                    text = if (isTimerRunning) "Current Flow Active" else "Ready to Focus",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Premium animated avatar placeholder
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                                .clickable { /* Mini animation easter egg */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // SECTION 1: Focus Timer Card
            item {
                val alpha by animateFloatAsState(
                    targetValue = if (startAnimations) 1f else 0f,
                    animationSpec = tween(500, delayMillis = 250),
                    label = "TimerAlpha"
                )
                val slideY by animateDpAsState(
                    targetValue = if (startAnimations) 0.dp else 30.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "TimerSlide"
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alpha)
                        .offset(y = slideY)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FOCUS ZONE",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Arc & Digital Clock Timer
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .scale(pulseScale),
                            contentAlignment = Alignment.Center
                        ) {
                            val activePrimaryColor = MaterialTheme.colorScheme.primary
                            val isDark = isSystemInDarkTheme()
                            val ringBgColor = if (isDark) Color(0x11FFFFFF) else Color(0x0D000000)

                            // Canvas drawing interactive radial progress rings
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw Background Ring
                                drawCircle(
                                    color = ringBgColor,
                                    radius = size.minDimension / 2 - 12.dp.toPx(),
                                    style = Stroke(width = 8.dp.toPx())
                                )

                                // Draw Live Filling Arc
                                drawArc(
                                    color = activePrimaryColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f * timerProgress,
                                    useCenter = false,
                                    style = Stroke(
                                        width = 10.dp.toPx(),
                                        cap = StrokeCap.Round
                                    ),
                                    topLeft = Offset(12.dp.toPx(), 12.dp.toPx()),
                                    size = size.copy(
                                        width = size.width - 24.dp.toPx(),
                                        height = size.height - 24.dp.toPx()
                                    )
                                )
                            }

                            // Dynamic ticking time output
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val minutes = timeLeftSeconds / 60
                                val seconds = timeLeftSeconds % 60
                                val timeFormatted = String.format("%02d:%02d", minutes, seconds)

                                Text(
                                    text = timeFormatted,
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-1).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isTimerRunning) "STAY FOCUSED" else "READY",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Motivational Quote Box
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "\"${activeQuote.first}\"",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    lineHeight = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "— ${activeQuote.second}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Selector chips (Deep, Short, Long, Custom) with glassy background
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (isSystemInDarkTheme()) Color(0x11FFFFFF) else Color(0x0F000000))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val options = listOf(
                                "Deep" to 25,
                                "Short" to 5,
                                "Long" to 15,
                                if (selectedDurationMinutes != 25 && selectedDurationMinutes != 5 && selectedDurationMinutes != 15) {
                                    "Custom (${selectedDurationMinutes}m)" to selectedDurationMinutes
                                } else {
                                    "Custom" to -1
                                }
                            )

                            options.forEach { (label, duration) ->
                                val isSelected = if (duration == -1) false else selectedDurationMinutes == duration
                                val chipBg = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Transparent
                                }
                                val chipTextColor = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(chipBg)
                                        .clickable(enabled = !isServiceRunning) {
                                            if (duration == -1) {
                                                tempCustomMinutes = selectedDurationMinutes
                                                showCustomDurationDialog = true
                                            } else {
                                                selectedDurationMinutes = duration
                                            }
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = chipTextColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Live Play, Pause, Reset Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Premium Glass Reset Button
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(if (isSystemInDarkTheme()) Color(0x1AFFFFFF) else Color(0x0F000000))
                                    .border(
                                        BorderStroke(1.2.dp, if (isSystemInDarkTheme()) Color(0x28FFFFFF) else Color(0x15000000)),
                                        CircleShape
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        if (isServiceRunning) {
                                            showStopConfirmationDialog = true
                                        } else {
                                            selectedDurationMinutes = 25
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset Timer",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Premium Glass Play/Pause Button
                            val playBtnColor = if (isTimerRunning) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            Box(
                                modifier = Modifier
                                    .height(52.dp)
                                    .width(180.dp)
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(playBtnColor.copy(alpha = 0.22f))
                                    .border(
                                        BorderStroke(1.5.dp, playBtnColor.copy(alpha = 0.5f)),
                                        RoundedCornerShape(26.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        if (isServiceRunning) {
                                            if (isServicePaused) {
                                                com.example.service.FocusSessionService.resumeSession(context)
                                            } else {
                                                com.example.service.FocusSessionService.pauseSession(context)
                                            }
                                        } else {
                                            // Start Session
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                val hasNotificationPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                                    context,
                                                    android.Manifest.permission.POST_NOTIFICATIONS
                                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                                if (!hasNotificationPermission) {
                                                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                                } else {
                                                    com.example.service.FocusSessionService.startService(context, selectedDurationMinutes)
                                                }
                                            } else {
                                                com.example.service.FocusSessionService.startService(context, selectedDurationMinutes)
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Toggle Timer",
                                        tint = playBtnColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isTimerRunning) "Pause Session" else "Start Session",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = playBtnColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2: Daily Progress Summary
            item {
                val alpha by animateFloatAsState(
                    targetValue = if (startAnimations) 1f else 0f,
                    animationSpec = tween(500, delayMillis = 400),
                    label = "ProgressAlpha"
                )
                val slideY by animateDpAsState(
                    targetValue = if (startAnimations) 0.dp else 30.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "ProgressSlide"
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alpha)
                        .offset(y = slideY)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Daily Progress",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(progressFraction * 100).toInt()}% Done",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Animated horizontal progress indicator
                        val progressAnim by animateFloatAsState(
                            targetValue = progressFraction,
                            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "HorizontalProgress"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (isSystemInDarkTheme()) Color(0x11FFFFFF) else Color(0x0D000000))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressAnim)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Focused Today",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "$totalFocusTimeToday mins",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Completed blocks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "$completedCount of ${timetableList.size}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 3: Quick Actions (Grid style layout)
            item {
                val alpha by animateFloatAsState(
                    targetValue = if (startAnimations) 1f else 0f,
                    animationSpec = tween(500, delayMillis = 550),
                    label = "QuickActionsAlpha"
                )
                val slideY by animateDpAsState(
                    targetValue = if (startAnimations) 0.dp else 30.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "QuickActionsSlide"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alpha)
                        .offset(y = slideY)
                ) {
                    Text(
                        text = "Quick Controls",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 2x2 Grid using layout rows
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (i in 0 until quickActions.size step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (j in 0..1) {
                                    val action = quickActions[i + j]
                                    val isActive = activeQuickActions[action.id] == true

                                    // Animated border & glow scaling
                                    val scale by animateFloatAsState(
                                        targetValue = if (isActive) 1.02f else 1.0f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                        label = "ActionScale"
                                    )

                                    val cardBorderColor = if (isActive) {
                                        action.activeColor.copy(alpha = 0.8f)
                                    } else {
                                        Color.Transparent
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .scale(scale)
                                            .clip(RoundedCornerShape(20.dp))
                                            .border(
                                                BorderStroke(
                                                    if (isActive) 2.dp else 0.dp,
                                                    cardBorderColor
                                                ),
                                                RoundedCornerShape(20.dp)
                                            )
                                    ) {
                                        GlassCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                    activeQuickActions[action.id] = !isActive
                                                },
                                            cornerRadius = 20.dp,
                                            elevation = if (isActive) 12.dp else 4.dp
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isActive) action.activeColor.copy(alpha = 0.2f)
                                                            else if (isSystemInDarkTheme()) Color(0x11FFFFFF)
                                                            else Color(0x0F000000)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = action.icon,
                                                        contentDescription = action.title,
                                                        tint = if (isActive) action.activeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = action.title,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = if (isActive) "Enabled" else "Muted",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isActive) action.activeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
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

            // SECTION 4: Today's Interactive Timetable Card
            item {
                val alpha by animateFloatAsState(
                    targetValue = if (startAnimations) 1f else 0f,
                    animationSpec = tween(500, delayMillis = 700),
                    label = "TimetableAlpha"
                )
                val slideY by animateDpAsState(
                    targetValue = if (startAnimations) 0.dp else 30.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "TimetableSlide"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alpha)
                        .offset(y = slideY)
                ) {
                    Text(
                        text = "Today's Timetable",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            timetableList.forEachIndexed { index, block ->
                                val isCompleted = completedBlocks[block.id] == true

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            completedBlocks[block.id] = !isCompleted
                                        }
                                        .background(
                                            if (isCompleted) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Visual bullet icon representing category
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isCompleted) Color(0xFF10B981).copy(alpha = 0.15f)
                                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.AutoMirrored.Rounded.List,
                                                contentDescription = "Block Category",
                                                tint = if (isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = block.title,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    textDecoration = if (isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                                ),
                                                color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = block.timeRange,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(3.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                                                )
                                                Text(
                                                    text = block.category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }

                                    // Premium Custom Glass Checkbox
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(
                                                BorderStroke(
                                                    2.dp,
                                                    if (isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                                ),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .background(
                                                if (isCompleted) Color(0xFF10B981) else Color.Transparent
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Completed Checkmark",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
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

        // 1. Custom Duration Picker Dialog
        if (showCustomDurationDialog) {
            Dialog(onDismissRequest = { showCustomDurationDialog = false }) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    cornerRadius = 28.dp,
                    elevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CUSTOM DURATION",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        val hours = tempCustomMinutes / 60
                        val mins = tempCustomMinutes % 60
                        val durationText = when {
                            hours > 0 && mins > 0 -> "$hours hr $mins min"
                            hours > 0 -> "$hours hr"
                            else -> "$mins min"
                        }
                        
                        Text(
                            text = durationText,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Slider(
                            value = tempCustomMinutes.toFloat(),
                            onValueChange = { tempCustomMinutes = ((it.toInt() / 5) * 5).coerceIn(5, 480) },
                            valueRange = 5f..480f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(if (isSystemInDarkTheme()) Color(0x11FFFFFF) else Color(0x0F000000))
                                    .clickable { showCustomDurationDialog = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Cancel",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                                    .border(
                                        BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                        RoundedCornerShape(24.dp)
                                    )
                                    .clickable {
                                        selectedDurationMinutes = tempCustomMinutes
                                        showCustomDurationDialog = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Apply",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Stop Confirmation Dialog
        if (showStopConfirmationDialog) {
            Dialog(onDismissRequest = { showStopConfirmationDialog = false }) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    cornerRadius = 28.dp,
                    elevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "ABANDON SESSION?",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Stopping now will discard this focus session. It will not be recorded in your history.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(if (isSystemInDarkTheme()) Color(0x11FFFFFF) else Color(0x0F000000))
                                    .clickable { showStopConfirmationDialog = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Keep Going",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.22f))
                                    .border(
                                        BorderStroke(1.2.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                        RoundedCornerShape(24.dp)
                                    )
                                    .clickable {
                                        com.example.service.FocusSessionService.stopSession(context)
                                        showStopConfirmationDialog = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Stop",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Session Completion Celebration Dialog
        if (completedSessionDuration != null) {
            val duration = completedSessionDuration ?: 25
            val dialogTransition = rememberInfiniteTransition(label = "DialogCelebration")
            val rotateCelebration by dialogTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(8000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "Rotate"
            )
            val scaleCelebration by dialogTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Scale"
            )

            Dialog(onDismissRequest = { completedSessionDuration = null }) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    cornerRadius = 32.dp,
                    elevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .scale(scaleCelebration),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0x2210B981),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.minDimension / 1.4f
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF10B981),
                                modifier = Modifier
                                    .size(56.dp)
                                    .rotate(rotateCelebration)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Text(
                            text = "SESSION COMPLETE!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = Color(0xFF10B981),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Awesome work! You completed a $duration-minute focus block.",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Your flow session has been successfully logged to your local journal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                .border(
                                    BorderStroke(1.2.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                                    RoundedCornerShape(24.dp)
                                )
                                .clickable { completedSessionDuration = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Return to Flow",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }
        }

    }
}

