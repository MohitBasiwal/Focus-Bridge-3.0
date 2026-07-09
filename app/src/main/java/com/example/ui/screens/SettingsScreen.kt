package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientBackground

data class WhitelistedApp(val id: String, val name: String, val category: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val initialAllowed: Boolean)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingsScreen() {
    var startAnims by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnims = true
    }

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

                // SECTION 3: App Blocking profilewhitelists
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
                            text = "App Whitelist",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                whitelistedApps.forEachIndexed { index, app ->
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
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (app.initialAllowed) Color(0xFF10B981).copy(alpha = 0.1f)
                                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = app.icon,
                                                    contentDescription = app.name,
                                                    tint = if (app.initialAllowed) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = app.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = app.category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                                )
                                            }
                                        }

                                        // Whitelist selection Checkbox
                                        val isAllowed = app.initialAllowed
                                        Checkbox(
                                            checked = isAllowed,
                                            onCheckedChange = { isChecked ->
                                                whitelistedApps[index] = app.copy(initialAllowed = isChecked)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFF10B981),
                                                checkmarkColor = Color.White
                                            )
                                        )
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
        }
    }
}

