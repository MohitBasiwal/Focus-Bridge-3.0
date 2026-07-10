package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientBackground
import com.example.ui.viewmodel.AppBlockerViewModel
import com.example.ui.viewmodel.AppInfo

data class WhitelistedApp(val id: String, val name: String, val category: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val initialAllowed: Boolean)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingsScreen(
    viewModel: AppBlockerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var startAnims by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnims = true
        viewModel.loadInstalledApps(context)
    }

    // Real-time Blocked Apps States
    val blockedApps by viewModel.blockedApps.collectAsStateWithLifecycle()
    val isBlockingActive by viewModel.isBlockingActive.collectAsStateWithLifecycle()
    val isServiceConnected by viewModel.isAccessibilityServiceConnected.collectAsStateWithLifecycle()
    val filteredApps by viewModel.filteredInstalledApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoadingApps by viewModel.isLoadingApps.collectAsStateWithLifecycle()

    var showAppPickerDialog by remember { mutableStateOf(false) }

    // 1. Interactive States
    var strictModeEnabled by remember { mutableStateOf(true) }
    var breakRemindersEnabled by remember { mutableStateOf(false) }
    var autoPauseThresholdMinutes by remember { mutableFloatStateOf(10f) }

    // 2. Soundscape State
    val soundscapes = listOf("None", "Forest Rain", "Deep Space Cafe", "Binaural Focus")
    var selectedSoundscape by remember { mutableStateOf(soundscapes[2]) }

    // 3. Interactive whitelisted apps
    val whitelistedApps = remember {
        mutableStateListOf(
            WhitelistedApp("1", "Slack", "Work", Icons.Default.Chat, true),
            WhitelistedApp("2", "Gmail", "Communication", Icons.Default.Email, true),
            WhitelistedApp("3", "GitHub", "Coding", Icons.Default.Terminal, true),
            WhitelistedApp("4", "YouTube", "Entertainment", Icons.Default.PlayCircle, false),
            WhitelistedApp("5", "Instagram", "Social", Icons.Default.PhotoCamera, false)
        )
    }

    // 4. Double check Dialog for Reset Focus History
    var showResetDialog by remember { mutableStateOf(false) }

    GradientBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 40.dp, bottom = 110.dp)
            ) {
                // Header
                item {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Text(
                            text = "Focus Preferences",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tailor your uninterrupted workspace experience",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                // SECTION 1: Focus Engine Settings
                item {
                    val alpha by animateFloatAsState(
                        targetValue = if (startAnims) 1f else 0f,
                        animationSpec = tween(500, delayMillis = 150),
                        label = "EngineAlpha"
                    )
                    val slideY by animateDpAsState(
                        targetValue = if (startAnims) 0.dp else 20.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "EngineSlide"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(alpha)
                            .offset(y = slideY)
                    ) {
                        Text(
                            text = "Focus Engine",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                // Strict Mode Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Strict Blocking Mode",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Block whitelisted social applications tightly",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                    Switch(
                                        checked = strictModeEnabled,
                                        onCheckedChange = { strictModeEnabled = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        )
                                    )
                                }

                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                                // Break Reminders Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Break Reminders",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Trigger soft breaks at session finish",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                    Switch(
                                        checked = breakRemindersEnabled,
                                        onCheckedChange = { breakRemindersEnabled = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.secondary,
                                            checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                        )
                                    )
                                }

                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                                // Auto Pause range slider
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Idle Auto-Pause",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${autoPauseThresholdMinutes.toInt()} mins",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                    Text(
                                        text = "Pause timer if user state goes inactive",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Slider(
                                        value = autoPauseThresholdMinutes,
                                        onValueChange = { autoPauseThresholdMinutes = it },
                                        valueRange = 2f..30f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // SECTION 2: Ambient Soundscape pill selector
                item {
                    val alpha by animateFloatAsState(
                        targetValue = if (startAnims) 1f else 0f,
                        animationSpec = tween(500, delayMillis = 300),
                        label = "SoundAlpha"
                    )
                    val slideY by animateDpAsState(
                        targetValue = if (startAnims) 0.dp else 20.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "SoundSlide"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(alpha)
                            .offset(y = slideY)
                    ) {
                        Text(
                            text = "Ambient Soundscape",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                soundscapes.forEach { sound ->
                                    val isSelected = selectedSoundscape == sound
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedSoundscape = sound }
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                                else Color.Transparent
                                            )
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Default.MusicNote else Icons.Default.PlayArrow,
                                                contentDescription = "Soundscape",
                                                tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                            Text(
                                                text = sound,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.secondary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 3: Real App Blocking Control & App Picker
                item {
                    val alpha by animateFloatAsState(
                        targetValue = if (startAnims) 1f else 0f,
                        animationSpec = tween(500, delayMillis = 450),
                        label = "AppsAlpha"
                    )
                    val slideY by animateDpAsState(
                        targetValue = if (startAnims) 0.dp else 20.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "AppsSlide"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(alpha)
                            .offset(y = slideY)
                    ) {
                        Text(
                            text = "App Blocker & Shield",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // 3A. Accessibility Connection Warning / Connection Onboarding
                        if (!isServiceConnected) {
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                cornerRadius = 24.dp,
                                borderWidth = 1.5.dp,
                                elevation = 16.dp
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Shield warning",
                                            tint = com.example.ui.theme.SoftVioletAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Accessibility Access Required",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = com.example.ui.theme.TextPrimaryWhite
                                        )
                                    }
                                    Text(
                                        text = "To monitor when distracting apps are opened and display the premium focus barrier, you must enable the Focus Bridge Accessibility Service in your device's settings.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = com.example.ui.theme.TextSecondaryGray,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = { viewModel.openAccessibilitySettings(context) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = com.example.ui.theme.SoftVioletAccent,
                                            contentColor = com.example.ui.theme.TextPrimaryWhite
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Activate Accessibility Service", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            // Service is connected onboarding badge
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                cornerRadius = 24.dp,
                                borderWidth = 1.dp,
                                elevation = 8.dp
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VerifiedUser,
                                            contentDescription = "Connected",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "App Shield Active",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = com.example.ui.theme.TextPrimaryWhite
                                        )
                                        Text(
                                            text = "Accessibility monitoring is fully active.",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = com.example.ui.theme.TextSecondaryGray
                                        )
                                    }
                                }
                            }
                        }

                        // 3B. Session blocker active controls
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 24.dp
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Enable App Blocking",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = com.example.ui.theme.TextPrimaryWhite
                                        )
                                        Text(
                                            text = "Block selected apps during focus sessions",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = com.example.ui.theme.TextSecondaryGray
                                        )
                                    }
                                    Switch(
                                        checked = isBlockingActive,
                                        onCheckedChange = { viewModel.setBlockingActive(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = com.example.ui.theme.SoftVioletAccent,
                                            checkedTrackColor = com.example.ui.theme.SoftVioletAccent.copy(alpha = 0.3f),
                                            uncheckedThumbColor = Color.Gray,
                                            uncheckedTrackColor = Color.DarkGray
                                        )
                                    )
                                }

                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                                // Selected Apps list
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "Protected Apps",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = com.example.ui.theme.TextPrimaryWhite
                                    )

                                    if (blockedApps.isEmpty()) {
                                        Text(
                                            text = "No apps blocked. Tap below to select apps you want to shield.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = com.example.ui.theme.TextSecondaryGray,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    } else {
                                        blockedApps.forEach { app ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(com.example.ui.theme.SoftVioletAccent.copy(alpha = 0.1f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Block,
                                                            contentDescription = "Blocked App",
                                                            tint = com.example.ui.theme.SoftVioletAccent,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    Column {
                                                        Text(
                                                            text = app.appName,
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = com.example.ui.theme.TextPrimaryWhite
                                                        )
                                                        Text(
                                                            text = app.packageName,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = com.example.ui.theme.TextSecondaryGray.copy(alpha = 0.7f)
                                                        )
                                                    }
                                                }
                                                IconButton(
                                                    onClick = { viewModel.toggleAppBlock(app.packageName, app.appName, true) }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete",
                                                        tint = Color(0xFFEF4444).copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Button to open full-screen app picker dialog
                                    Button(
                                        onClick = { showAppPickerDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = com.example.ui.theme.IndigoGlassLight,
                                            contentColor = com.example.ui.theme.LavenderHighlight
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, com.example.ui.theme.GlassBorderLowOpacity),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Select Apps to Block", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION: Emergency Unlock settings
                item {
                    val alpha by animateFloatAsState(
                        targetValue = if (startAnims) 1f else 0f,
                        animationSpec = tween(500, delayMillis = 500),
                        label = "EmergencyAlpha"
                    )
                    val slideY by animateDpAsState(
                        targetValue = if (startAnims) 0.dp else 20.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "EmergencySlide"
                    )

                    val cooldownVal by viewModel.cooldownMinutes.collectAsStateWithLifecycle()
                    val historyList by viewModel.unlockHistory.collectAsStateWithLifecycle()

                    // Count remaining unlocks for today
                    val startOfToday = remember {
                        val calendar = java.util.Calendar.getInstance()
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        calendar.set(java.util.Calendar.MINUTE, 0)
                        calendar.set(java.util.Calendar.SECOND, 0)
                        calendar.set(java.util.Calendar.MILLISECOND, 0)
                        calendar.timeInMillis
                    }
                    val unlocksTodayCount = remember(historyList) {
                        historyList.count { it.timestamp >= startOfToday }
                    }
                    val remainingUnlocks = (3 - unlocksTodayCount).coerceAtLeast(0)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(alpha)
                            .offset(y = slideY),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Emergency Unlock Module",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 24.dp
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                // Remaining Unlocks Indicator
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Remaining Unlocks Today",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = com.example.ui.theme.TextPrimaryWhite
                                        )
                                        Text(
                                            text = "Daily limit is 3 uses to prevent focus erosion.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = com.example.ui.theme.TextSecondaryGray
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (remainingUnlocks > 0) com.example.ui.theme.SoftVioletAccent.copy(alpha = 0.2f)
                                                else Color(0xFFEF4444).copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$remainingUnlocks",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (remainingUnlocks > 0) com.example.ui.theme.LavenderHighlight else Color(0xFFEF4444)
                                        )
                                    }
                                }

                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                                // Configurable Cooldown (Slider)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Bypass Cooldown Duration",
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                color = com.example.ui.theme.TextPrimaryWhite
                                            )
                                            Text(
                                                text = "How long the block shield is suspended.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = com.example.ui.theme.TextSecondaryGray
                                            )
                                        }
                                        Text(
                                            text = "$cooldownVal mins",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = com.example.ui.theme.LavenderHighlight,
                                            modifier = Modifier
                                                .background(com.example.ui.theme.IndigoGlassLight, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    Slider(
                                        value = cooldownVal.toFloat(),
                                        onValueChange = { viewModel.setCooldownMinutes(it.toInt()) },
                                        valueRange = 5f..120f,
                                        steps = 22, // Increments of 5 mins (5, 10, 15, ..., 120)
                                        colors = SliderDefaults.colors(
                                            thumbColor = com.example.ui.theme.SoftVioletAccent,
                                            activeTrackColor = com.example.ui.theme.SoftVioletAccent,
                                            inactiveTrackColor = Color.DarkGray
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (historyList.isNotEmpty()) {
                                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                                    // Usage history lists
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Bypass Usage History",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = com.example.ui.theme.TextPrimaryWhite
                                        )

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 150.dp)
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            historyList.take(10).forEach { item ->
                                                val dateStr = remember(item.timestamp) {
                                                    val df = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                                                    df.format(java.util.Date(item.timestamp))
                                                }
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.History,
                                                            contentDescription = "History",
                                                            tint = com.example.ui.theme.LavenderHighlight.copy(alpha = 0.7f),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Text(
                                                            text = dateStr,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = com.example.ui.theme.TextPrimaryWhite
                                                        )
                                                    }
                                                    Text(
                                                        text = "${item.durationMinutes}m duration",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = com.example.ui.theme.TextSecondaryGray
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

                // SECTION 4: Danger zone reset button
                item {
                    val alpha by animateFloatAsState(
                        targetValue = if (startAnims) 1f else 0f,
                        animationSpec = tween(500, delayMillis = 600),
                        label = "ResetAlpha"
                    )
                    val slideY by animateDpAsState(
                        targetValue = if (startAnims) 0.dp else 20.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "ResetSlide"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(alpha)
                            .offset(y = slideY)
                    ) {
                        Text(
                            text = "Maintenance",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Reset focus timers and daily statistics",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                                OutlinedButton(
                                    onClick = { showResetDialog = true },
                                    border = BorderStroke(1.2.dp, Color(0xFFEF4444)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Reset")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Reset Statistics", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }

                        // App Version metadata text
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Focus Bridge v1.2.0-Alpha\nSecurely sandbox with elegance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Confirm Reset Dialogue Box
            if (showResetDialog) {
                Dialog(onDismissRequest = { showResetDialog = false }) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        cornerRadius = 24.dp,
                        elevation = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "Confirm Clear Status",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Are you sure you want to delete all historical logs? This action is irreversible.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { showResetDialog = false },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSystemInDarkTheme()) Color(0x1AFFFFFF) else Color(0x0F000000),
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text("Cancel")
                                }

                                Button(
                                    onClick = {
                                        // Reset operations
                                        showResetDialog = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEF4444)
                                    )
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }

            // App Picker Dialogue Box
            if (showAppPickerDialog) {
                Dialog(onDismissRequest = { showAppPickerDialog = false }) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                            .padding(8.dp),
                        cornerRadius = 28.dp,
                        elevation = 24.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Shield Applications",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = com.example.ui.theme.TextPrimaryWhite
                                )
                                IconButton(onClick = { showAppPickerDialog = false }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = com.example.ui.theme.TextPrimaryWhite
                                    )
                                }
                            }

                            // Search bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Search installed apps...", color = com.example.ui.theme.TextSecondaryGray) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = com.example.ui.theme.SoftVioletAccent) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = com.example.ui.theme.SoftVioletAccent,
                                    unfocusedBorderColor = com.example.ui.theme.GlassBorderLowOpacity,
                                    focusedContainerColor = com.example.ui.theme.IndigoGlass,
                                    unfocusedContainerColor = com.example.ui.theme.IndigoGlass,
                                    focusedTextColor = com.example.ui.theme.TextPrimaryWhite,
                                    unfocusedTextColor = com.example.ui.theme.TextPrimaryWhite
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            // Apps List
                            if (isLoadingApps) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = com.example.ui.theme.SoftVioletAccent)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(filteredApps) { app ->
                                        val isBlocked = blockedApps.any { it.packageName == app.packageName }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    viewModel.toggleAppBlock(app.packageName, app.appName, isBlocked)
                                                }
                                                .background(
                                                    if (isBlocked) com.example.ui.theme.SoftVioletAccent.copy(alpha = 0.1f)
                                                    else Color.Transparent
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
                                                // App Icon rendering with robust fallback
                                                if (app.icon != null) {
                                                    AndroidView(
                                                        factory = { ctx ->
                                                            android.widget.ImageView(ctx).apply {
                                                                setImageDrawable(app.icon)
                                                            }
                                                        },
                                                        modifier = Modifier.size(36.dp)
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.White.copy(alpha = 0.1f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Android,
                                                            contentDescription = "App Icon Placeholder",
                                                            tint = com.example.ui.theme.TextSecondaryGray
                                                        )
                                                    }
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = app.appName,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = com.example.ui.theme.TextPrimaryWhite,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = app.packageName,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = com.example.ui.theme.TextSecondaryGray.copy(alpha = 0.6f),
                                                        maxLines = 1
                                                    )
                                                }
                                            }

                                            Checkbox(
                                                checked = isBlocked,
                                                onCheckedChange = {
                                                    viewModel.toggleAppBlock(app.packageName, app.appName, isBlocked)
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = com.example.ui.theme.SoftVioletAccent,
                                                    checkmarkColor = Color.White
                                                )
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

