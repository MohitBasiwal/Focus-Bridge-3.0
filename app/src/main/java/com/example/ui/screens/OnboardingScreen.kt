package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientBackground
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppBlockerViewModel

sealed class OnboardingPage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    object Welcome : OnboardingPage(
        title = "Welcome to Focus Bridge",
        subtitle = "Connect your intentions and protect your attention span with deep flow blocks.",
        icon = Icons.Default.HourglassEmpty,
        accentColor = SoftVioletAccent
    )

    object Accessibility : OnboardingPage(
        title = "Accessibility Bridge",
        subtitle = "Focus Bridge uses an Accessibility Service to detect when distracting apps are opened and read browser URL bars to enforce website blocks.",
        icon = Icons.Default.Security,
        accentColor = SoftVioletAccent
    )

    object Overlay : OnboardingPage(
        title = "Draw Over Apps",
        subtitle = "This permission is required to display our high-priority purple glass overlay block screen immediately when a distraction is accessed.",
        icon = Icons.Default.Layers,
        accentColor = LavenderHighlight
    )

    object Microphone : OnboardingPage(
        title = "Speech Challenge",
        subtitle = "Microphone access is used solely to verify your vocal speech challenge, acting as a smart offline barrier when trying to bypass focus blocks.",
        icon = Icons.Default.Mic,
        accentColor = SoftVioletAccent
    )

    object Notifications : OnboardingPage(
        title = "Alerts & Reminders",
        subtitle = "Receive reminders about active sessions, block triggers, and timers to support your study flow without cloud distraction.",
        icon = Icons.Default.Notifications,
        accentColor = LavenderHighlight
    )

    object Battery : OnboardingPage(
        title = "Background Shield",
        subtitle = "Bypass aggressive system battery optimization to ensure the background monitor service remains active during your deep study sessions.",
        icon = Icons.Default.BatteryChargingFull,
        accentColor = SoftVioletAccent
    )

    object Setup : OnboardingPage(
        title = "Setup Your Goal",
        subtitle = "Set your daily focus intention and customize your initial distraction list.",
        icon = Icons.Default.Settings,
        accentColor = LavenderHighlight
    )

    object Complete : OnboardingPage(
        title = "Calibration Complete",
        subtitle = "Your focus bridge is now fully calibrated. Ready to cross into distraction-free flow?",
        icon = Icons.Default.DoneAll,
        accentColor = SoftVioletAccent
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AppBlockerViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = LocalContext.current
    val pages = remember {
        listOf(
            OnboardingPage.Welcome,
            OnboardingPage.Accessibility,
            OnboardingPage.Overlay,
            OnboardingPage.Microphone,
            OnboardingPage.Notifications,
            OnboardingPage.Battery,
            OnboardingPage.Setup,
            OnboardingPage.Complete
        )
    }

    var currentPageIndex by remember { mutableIntStateOf(0) }
    val currentPage = pages[currentPageIndex]

    // Live permission states
    val isAccessibilityConnected by viewModel.isAccessibilityServiceConnected.collectAsStateWithLifecycle()
    var isOverlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var isMicGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isNotifGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    // Refresh overlay and permission checks on resume/re-entry
    LaunchedEffect(currentPageIndex) {
        isOverlayGranted = Settings.canDrawOverlays(context)
        isMicGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isNotifGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Load installed apps when reaching the Setup step
    LaunchedEffect(currentPageIndex) {
        if (currentPage is OnboardingPage.Setup) {
            viewModel.loadInstalledApps(context)
        }
    }

    // Request launch triggers
    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isMicGranted = granted
    }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isNotifGranted = granted
    }

    GradientBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Header Step Indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pages.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    brush = if (index <= currentPageIndex) {
                                        Brush.linearGradient(listOf(SoftVioletAccent, LavenderHighlight))
                                    } else {
                                        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.15f)))
                                    }
                                )
                        )
                    }
                }

                // 2. Central Multi-Step Content Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = currentPage,
                        transitionSpec = {
                            if (currentPageIndex > pages.indexOf(initialState)) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut())
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut())
                            }
                        },
                        label = "OnboardingContent"
                    ) { targetPage ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            cornerRadius = 28.dp,
                            elevation = 16.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Glowing Page Icon Badge
                                Box(
                                    modifier = Modifier
                                        .size(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(
                                                        targetPage.accentColor.copy(alpha = 0.25f),
                                                        Color.Transparent
                                                    )
                                                )
                                            )
                                    )
                                    Icon(
                                        imageVector = targetPage.icon,
                                        contentDescription = targetPage.title,
                                        tint = targetPage.accentColor,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = targetPage.title,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimaryWhite,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = targetPage.subtitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                    color = TextSecondaryGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // STEP-SPECIFIC CUSTOM ACTIONS
                                when (targetPage) {
                                    is OnboardingPage.Accessibility -> {
                                        PermissionIndicator(
                                            isGranted = isAccessibilityConnected,
                                            grantedLabel = "Accessibility Connected",
                                            deniedLabel = "Accessibility Disconnected"
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = { viewModel.openAccessibilitySettings(context) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SoftVioletAccent),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Connect Accessibility", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    is OnboardingPage.Overlay -> {
                                        PermissionIndicator(
                                            isGranted = isOverlayGranted,
                                            grantedLabel = "Overlay Permission Active",
                                            deniedLabel = "Overlay Permission Required"
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = {
                                                val intent = Intent(
                                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                    Uri.parse("package:${context.packageName}")
                                                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SoftVioletAccent),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Grant Overlay Permission", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    is OnboardingPage.Microphone -> {
                                        PermissionIndicator(
                                            isGranted = isMicGranted,
                                            grantedLabel = "Microphone Calibrated",
                                            deniedLabel = "Microphone Action Required"
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = { micLauncher.launch(android.Manifest.permission.RECORD_AUDIO) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SoftVioletAccent),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Authorize Microphone", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    is OnboardingPage.Notifications -> {
                                        PermissionIndicator(
                                            isGranted = isNotifGranted,
                                            grantedLabel = "Notifications Active",
                                            deniedLabel = "Notifications Action Required"
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                                }
                                            },
                                            enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                                            colors = ButtonDefaults.buttonColors(containerColor = SoftVioletAccent),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Grant Notification Authorization", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    is OnboardingPage.Battery -> {
                                        Button(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    context.startActivity(intent)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SoftVioletAccent),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Open Battery Settings", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    is OnboardingPage.Setup -> {
                                        InteractiveGoalAndAppSelector(viewModel = viewModel)
                                    }

                                    is OnboardingPage.Complete -> {
                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .background(SoftVioletAccent.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Success",
                                                tint = SoftVioletAccent,
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }

                // 3. Bottom Navigation Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    if (currentPageIndex > 0) {
                        OutlinedButton(
                            onClick = { currentPageIndex-- },
                            border = BorderStroke(1.dp, GlassBorderLowOpacity),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimaryWhite),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.width(100.dp)
                        ) {
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(100.dp))
                    }

                    // Next/Complete button
                    Button(
                        onClick = {
                            if (currentPageIndex < pages.size - 1) {
                                currentPageIndex++
                            } else {
                                viewModel.setOnboardingCompleted(true)
                                onNavigateToHome()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentPage is OnboardingPage.Complete) SoftVioletAccent else Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text(
                            text = if (currentPage is OnboardingPage.Complete) "Enter Bridge" else "Next",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionIndicator(
    isGranted: Boolean,
    grantedLabel: String,
    deniedLabel: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .background(
                if (isGranted) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape)
        )
        Text(
            text = if (isGranted) grantedLabel else deniedLabel,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444)
        )
    }
}

@Composable
fun InteractiveGoalAndAppSelector(
    viewModel: AppBlockerViewModel
) {
    val dailyGoalMinutes by viewModel.cooldownMinutes.collectAsStateWithLifecycle() // Reusing as setting state
    val blockedApps by viewModel.blockedApps.collectAsStateWithLifecycle()
    val installedApps by viewModel.filteredInstalledApps.collectAsStateWithLifecycle()
    val isLoadingApps by viewModel.isLoadingApps.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Study Goal, 1: Block Apps

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tab Headers
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            contentColor = LavenderHighlight,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = SoftVioletAccent
                )
            }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Study Goal", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Block Apps", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeTab == 0) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${dailyGoalMinutes} Minutes",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = SoftVioletAccent
                )
                Text(
                    text = "Daily Target Intention",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryGray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = dailyGoalMinutes.toFloat(),
                    onValueChange = { viewModel.setCooldownMinutes(it.toInt()) },
                    valueRange = 10f..180f,
                    steps = 17,
                    colors = SliderDefaults.colors(
                        activeTrackColor = SoftVioletAccent,
                        thumbColor = SoftVioletAccent
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            if (isLoadingApps) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SoftVioletAccent)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(installedApps.take(15)) { app ->
                        val isCurrentlyBlocked = blockedApps.any { it.packageName == app.packageName }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.toggleAppBlock(
                                        app.packageName,
                                        app.appName,
                                        isCurrentlyBlocked
                                    )
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                )
                                Text(
                                    text = app.appName,
                                    color = TextPrimaryWhite,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            Checkbox(
                                checked = isCurrentlyBlocked,
                                onCheckedChange = {
                                    viewModel.toggleAppBlock(
                                        app.packageName,
                                        app.appName,
                                        isCurrentlyBlocked
                                    )
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = SoftVioletAccent,
                                    uncheckedColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
