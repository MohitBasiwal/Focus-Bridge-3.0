package com.example.ui.screens

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import com.example.MainActivity
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientBackground
import com.example.ui.theme.FocusBridgeTheme
import com.example.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Calendar

@AndroidEntryPoint
class BlockedOverlayActivity : ComponentActivity() {

    @Inject
    lateinit var unlockRepository: com.example.domain.repository.EmergencyUnlockRepository

    @Inject
    lateinit var preferenceManager: com.example.data.datastore.PreferenceManager

    @Inject
    lateinit var speechHistoryRepository: com.example.domain.repository.SpeechChallengeHistoryRepository

    @Inject
    lateinit var blockedEventRepository: com.example.domain.repository.BlockedEventRepository

    override fun onResume() {
        super.onResume()
        isShowing = true
    }

    override fun onPause() {
        super.onPause()
        isShowing = false
    }

    companion object {
        var isShowing = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val blockedPackage = intent.getStringExtra("BLOCKED_PACKAGE_NAME")
        val blockedWebsite = intent.getStringExtra("BLOCKED_WEBSITE_DOMAIN")
        
        val isApp = blockedWebsite == null
        val identifier = blockedPackage ?: blockedWebsite ?: "Unknown"
        
        val pm = packageManager
        val displayName = if (blockedWebsite != null) {
            blockedWebsite
        } else {
            try {
                val appInfo = pm.getApplicationInfo(identifier, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                "Distracting App"
            }
        }

        // Log the blocked event immediately in the database for analytics tracking
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val event = com.example.data.entity.BlockedEventEntity(
                    identifier = identifier,
                    displayName = displayName,
                    timestamp = System.currentTimeMillis(),
                    isApp = isApp
                )
                blockedEventRepository.insertBlockedEvent(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        setContent {
            FocusBridgeTheme {
                val scope = rememberCoroutineScope()
                
                // Track remaining unlocks reactive state from local start of today
                val startOfToday = remember {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
                
                val unlocksTodayFlow = remember {
                    unlockRepository.getUnlocksSince(startOfToday)
                }
                val unlocksToday by unlocksTodayFlow.collectAsState(initial = emptyList())
                val remainingUnlocks = remember(unlocksToday) {
                    (3 - unlocksToday.size).coerceAtLeast(0)
                }
                
                val cooldownMinutes by preferenceManager.cooldownMinutes.collectAsState(initial = 30)
                val isStrictEnabled by preferenceManager.isStrictBlockingEnabled.collectAsState(initial = false)

                BlockedOverlayContent(
                    blockedAppName = displayName,
                    onReturnToFocus = {
                        val returnIntent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        startActivity(returnIntent)
                        finish()
                    },
                    remainingUnlocks = remainingUnlocks,
                    cooldownMinutes = cooldownMinutes,
                    isStrictEnabled = isStrictEnabled,
                    onUnlockSuccess = {
                        scope.launch {
                            // Log the success in Room Database
                            val newUnlock = com.example.data.entity.EmergencyUnlockEntity(
                                timestamp = System.currentTimeMillis(),
                                durationMinutes = cooldownMinutes
                            )
                            unlockRepository.insertUnlock(newUnlock)
                            
                            // Persist the last unlock time to preferences
                            preferenceManager.setLastUnlockTime(System.currentTimeMillis())
                            
                            // Success feedback & dismiss activity
                            Toast.makeText(
                                this@BlockedOverlayActivity,
                                "Emergency bypass active for $cooldownMinutes minutes!",
                                Toast.LENGTH_LONG
                            ).show()
                            
                            finish()
                        }
                    },
                    onChallengeSuccess = { accuracy, targetParagraph ->
                        scope.launch {
                            val history = com.example.data.entity.SpeechChallengeHistoryEntity(
                                timestamp = System.currentTimeMillis(),
                                paragraphText = targetParagraph,
                                accuracy = accuracy,
                                isSuccess = true
                            )
                            speechHistoryRepository.insertSpeechChallenge(history)
                        }
                    },
                    onChallengeFail = { accuracy, targetParagraph ->
                        scope.launch {
                            val history = com.example.data.entity.SpeechChallengeHistoryEntity(
                                timestamp = System.currentTimeMillis(),
                                paragraphText = targetParagraph,
                                accuracy = accuracy,
                                isSuccess = false
                            )
                            speechHistoryRepository.insertSpeechChallenge(history)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BlockedOverlayContent(
    blockedAppName: String,
    onReturnToFocus: () -> Unit,
    remainingUnlocks: Int,
    cooldownMinutes: Int,
    isStrictEnabled: Boolean,
    onUnlockSuccess: () -> Unit,
    onChallengeSuccess: (Double, String) -> Unit,
    onChallengeFail: (Double, String) -> Unit
) {
    val context = LocalContext.current
    
    // Dialog state controllers
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showChallengeDialog by remember { mutableStateOf(false) }

    // Core heartbeat pulse animation for warning visual
    val transition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepNavyBg
    ) {
        GradientBackground(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    cornerRadius = 32.dp,
                    elevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Glowing Icon Badge
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .scale(pulseScale),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                SoftVioletAccent.copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                            
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Shield warning",
                                tint = SoftVioletAccent,
                                modifier = Modifier.size(56.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Warning Header
                        Text(
                            text = "FOCUS BLOCK ACTIVE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp
                            ),
                            color = LavenderHighlight,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Body message
                        Text(
                            text = "Take a deep breath.",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimaryWhite,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "You decided to block $blockedAppName to stay focused. Protect your attention span!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 20.sp
                            ),
                            color = TextSecondaryGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Premium Return Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            SoftVioletAccent,
                                            Color(0xFF5140CD)
                                        )
                                    )
                                )
                                .clickable { onReturnToFocus() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Timer icon",
                                    tint = TextPrimaryWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Return to Focus",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextPrimaryWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Emergency Unlock Button (Purple-Themed transparent glass)
                        val canUnlock = remainingUnlocks > 0 && !isStrictEnabled

                        OutlinedButton(
                            onClick = { showConfirmDialog = true },
                            border = BorderStroke(1.2.dp, if (canUnlock) Color(0xFFEF4444).copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.3f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (canUnlock) Color(0xFFEF4444) else Color.Gray
                            ),
                            shape = RoundedCornerShape(28.dp),
                            enabled = canUnlock,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isStrictEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Unlock icon",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isStrictEnabled) {
                                    "Unlock Disabled (Strict Mode)"
                                } else if (remainingUnlocks > 0) {
                                    "Emergency Unlock ($remainingUnlocks/3 left)"
                                } else {
                                    "No Unlocks Remaining Today"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Footnote
                        Text(
                            text = "The screen is your mind's gateway. Keep it clear.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryGray.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // A. Material 3 confirmation dialog
    if (showConfirmDialog) {
        Dialog(onDismissRequest = { showConfirmDialog = false }) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                cornerRadius = 28.dp,
                elevation = 24.dp
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "Initiate Emergency Unlock?",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimaryWhite,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "You have $remainingUnlocks of 3 emergency unlocks available for today. If successful, your focus blocks will be paused for a $cooldownMinutes-minute cooldown.\n\nYou must pass a vocal Speech Challenge to unlock.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showConfirmDialog = false },
                            border = BorderStroke(1.dp, GlassBorderLowOpacity),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimaryWhite),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                showConfirmDialog = false
                                showChallengeDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftVioletAccent),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Start Challenge", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // B. Immersive Speech Challenge dialog
    if (showChallengeDialog) {
        val targetParagraph = remember { 
            com.example.data.speech.ParagraphLibrary.getParagraph(kotlin.random.Random.nextInt(500)) 
        }

        Dialog(onDismissRequest = { showChallengeDialog = false }) {
            SpeechChallengeContent(
                targetParagraph = targetParagraph,
                cooldownMinutes = cooldownMinutes,
                onChallengeSuccess = { accuracy ->
                    showChallengeDialog = false
                    onChallengeSuccess(accuracy, targetParagraph)
                    onUnlockSuccess()
                },
                onChallengeFail = { accuracy ->
                    onChallengeFail(accuracy, targetParagraph)
                },
                onDismiss = {
                    showChallengeDialog = false
                }
            )
        }
    }
}

enum class WordStatus {
    PENDING,
    CORRECT,
    SKIPPED
}

@Composable
fun SpeechChallengeContent(
    targetParagraph: String,
    cooldownMinutes: Int,
    onChallengeSuccess: (Double) -> Unit,
    onChallengeFail: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    // Tokenize paragraph into clean words
    val themeHeader = remember(targetParagraph) {
        if (targetParagraph.startsWith("Theme: ")) {
            targetParagraph.substringBefore("\n\n")
        } else {
            "Theme: Dynamic Mindfulness"
        }
    }
    val cleanTextBody = remember(targetParagraph) {
        if (targetParagraph.startsWith("Theme: ")) {
            targetParagraph.substringAfter("\n\n")
        } else {
            targetParagraph
        }
    }
    val words = remember(cleanTextBody) { 
        cleanTextBody.split(Regex("[\\s\n\r]+")).filter { it.isNotBlank() } 
    }

    // State
    var isListening by remember { mutableStateOf(false) }
    var currentWordIndex by remember { mutableStateOf(0) }
    val wordStatuses = remember(words) { 
        mutableStateListOf<WordStatus>().apply {
            addAll(List(words.size) { WordStatus.PENDING })
        }
    }
    var mistakesCount by remember { mutableIntStateOf(0) }
    var statusMessage by remember { mutableStateOf("Tap the microphone to begin") }
    var lastSpeechTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var isSilentFailed by remember { mutableStateOf(false) }
    var recognizedTextHistory by remember { mutableStateOf("") }
    
    // Fallback/Simulate reading parameters
    var showSimulatorAssist by remember { mutableStateOf(false) }

    // Speech challenge progress computations
    val correctCount = wordStatuses.count { it == WordStatus.CORRECT }
    val progressPercent = if (words.isNotEmpty()) (currentWordIndex.toFloat() / words.size) else 0f
    val accuracy = remember(correctCount, mistakesCount) {
        val totalProcessed = correctCount + mistakesCount
        if (totalProcessed == 0) 100.0 else (correctCount.toDouble() / totalProcessed) * 100.0
    }

    // Speech Recognizer setup
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    fun cleanWord(w: String) = w.lowercase().replace(Regex("[^a-z0-9]"), "")

    fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length

        var distance = IntArray(lhsLength + 1) { it }
        var newDistance = IntArray(lhsLength + 1)

        for (i in 1..rhsLength) {
            newDistance[0] = i
            for (j in 1..lhsLength) {
                val substitutionCost = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                newDistance[j] = minOf(
                    distance[j] + 1,
                    newDistance[j - 1] + 1,
                    distance[j - 1] + substitutionCost
                )
            }
            val temp = distance
            distance = newDistance
            newDistance = temp
        }
        return distance[lhsLength]
    }

    fun isPronunciationSimilar(w1: String, w2: String): Boolean {
        if (w1 == w2) return true
        if (w1.isEmpty() || w2.isEmpty()) return false
        if (w1.contains(w2) && w1.length - w2.length <= 2) return true
        if (w2.contains(w1) && w2.length - w1.length <= 2) return true
        
        val dist = levenshteinDistance(w1, w2)
        val maxLen = maxOf(w1.length, w2.length)
        if (maxLen <= 3) return dist <= 1
        if (maxLen <= 6) return dist <= 2
        return dist <= 3
    }

    val checkSpeechMatch = { spokenText: String ->
        lastSpeechTimestamp = System.currentTimeMillis()
        val targetCleanWords = words.map { cleanWord(it) }
        val spokenWords = spokenText.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }.map { cleanWord(it) }
        
        var targetIdx = 0
        var correct = 0
        var mistakes = 0
        
        // Reset statuses first to compute from spokenText
        for (i in 0 until words.size) {
            wordStatuses[i] = WordStatus.PENDING
        }
        
        for (spokenWord in spokenWords) {
            if (spokenWord.isEmpty()) continue
            if (targetIdx < targetCleanWords.size) {
                val targetWord = targetCleanWords[targetIdx]
                if (isPronunciationSimilar(spokenWord, targetWord)) {
                    wordStatuses[targetIdx] = WordStatus.CORRECT
                    targetIdx++
                    correct++
                } else {
                    mistakes++
                }
            } else {
                mistakes++
            }
        }
        
        val oldIndex = currentWordIndex
        currentWordIndex = targetIdx
        mistakesCount = mistakes
        
        if (currentWordIndex > oldIndex) {
            recognizedTextHistory = spokenText
            statusMessage = "Excellent, keep reading!"
        }

        // Completion condition
        if (currentWordIndex >= words.size || progressPercent >= 0.95f) {
            isListening = false
            try { speechRecognizer.stopListening() } catch(e:Exception){}
            if (accuracy >= 70.0) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onChallengeSuccess(accuracy)
            } else {
                statusMessage = "Accuracy too low (${accuracy.toInt()}%). Tap Mic to retry!"
                onChallengeFail(accuracy)
            }
        }
    }

    // 5-second silence failure detection
    LaunchedEffect(isListening, lastSpeechTimestamp, currentWordIndex) {
        if (isListening && !isSilentFailed) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                val elapsed = System.currentTimeMillis() - lastSpeechTimestamp
                if (elapsed >= 5000) {
                    isSilentFailed = true
                    isListening = false
                    try { speechRecognizer.stopListening() } catch (e: Exception) {}
                    statusMessage = "Challenge failed due to silence! Tap Mic to restart."
                    onChallengeFail(accuracy)
                    break
                }
            }
        }
    }

    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                isSilentFailed = false
                lastSpeechTimestamp = System.currentTimeMillis()
                statusMessage = "Listening clearly..."
            }
            override fun onBeginningOfSpeech() {
                lastSpeechTimestamp = System.currentTimeMillis()
            }
            override fun onRmsChanged(rmsdB: Float) {
                if (rmsdB > 2f) {
                    lastSpeechTimestamp = System.currentTimeMillis()
                }
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }
            override fun onError(error: Int) {
                isListening = false
                val errText = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "Could not match speech. Try again."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout. Tap & speak."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Service busy. Wait a moment."
                    else -> "Tap Microphone to retry."
                }
                statusMessage = errText
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    checkSpeechMatch(matches[0])
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    checkSpeechMatch(matches[0])
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    // Mic Permission
    var micPermissionGranted by remember {
        mutableStateOf(
            android.content.pm.PackageManager.PERMISSION_GRANTED == 
            context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        micPermissionGranted = isGranted
        if (isGranted) {
            Toast.makeText(context, "Audio Permission Granted!", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                speechRecognizer.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Waveform scale animation
    val transition = rememberInfiniteTransition(label = "VoicePulse")
    val waveScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(12.dp),
        cornerRadius = 28.dp,
        elevation = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vocal Challenge",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimaryWhite
                    )
                    Text(
                        text = themeHeader,
                        style = MaterialTheme.typography.labelSmall,
                        color = LavenderHighlight
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextPrimaryWhite)
                }
            }

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Progress", style = MaterialTheme.typography.labelSmall, color = TextSecondaryGray)
                    Text("${(progressPercent * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimaryWhite)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.1f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Accuracy", style = MaterialTheme.typography.labelSmall, color = TextSecondaryGray)
                    Text("${accuracy.toInt()}%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = if (accuracy >= 70.0) Color(0xFF10B981) else Color(0xFFEF4444))
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.1f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Read", style = MaterialTheme.typography.labelSmall, color = TextSecondaryGray)
                    Text("$correctCount/${words.size}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = LavenderHighlight)
                }
            }

            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SoftVioletAccent,
                trackColor = Color.White.copy(alpha = 0.08f)
            )

            // Scrollable text container showing the words and real-time highlighting
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                val scrollState = rememberScrollState()
                
                // Keep scroll in sync with current reading position
                LaunchedEffect(currentWordIndex) {
                    if (words.isNotEmpty()) {
                        val approxLine = (currentWordIndex.toFloat() / words.size) * scrollState.maxValue
                        scrollState.animateScrollTo(approxLine.toInt())
                    }
                }

                androidx.compose.foundation.text.BasicText(
                    text = remember(currentWordIndex, wordStatuses) {
                        androidx.compose.ui.text.buildAnnotatedString {
                            words.forEachIndexed { idx, word ->
                                val status = wordStatuses[idx]
                                val style = when {
                                    idx == currentWordIndex -> androidx.compose.ui.text.SpanStyle(
                                        color = LavenderHighlight,
                                        fontWeight = FontWeight.Black,
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                        fontSize = 17.sp
                                    )
                                    status == WordStatus.CORRECT -> androidx.compose.ui.text.SpanStyle(
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    status == WordStatus.SKIPPED -> androidx.compose.ui.text.SpanStyle(
                                        color = Color(0xFFEF4444).copy(alpha = 0.7f),
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    )
                                    else -> androidx.compose.ui.text.SpanStyle(
                                        color = TextSecondaryGray.copy(alpha = 0.6f)
                                    )
                                }
                                withStyle(style) {
                                    append(word)
                                }
                                append(" ")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                )
            }

            // Status feedback
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isSilentFailed) Color(0xFFEF4444) else LavenderHighlight,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Dynamic controls & simulators
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Microphone FAB button
                    Box(
                        modifier = Modifier
                            .size(70.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isListening) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(waveScale)
                                    .clip(CircleShape)
                                    .background(SoftVioletAccent.copy(alpha = 0.12f))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = if (isListening) {
                                            listOf(Color(0xFFEF4444), Color(0xFFFF5252))
                                        } else {
                                            listOf(SoftVioletAccent, Color(0xFF5140CD))
                                        }
                                    )
                                )
                                .clickable {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    if (!micPermissionGranted) {
                                        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                    } else {
                                        if (isListening) {
                                            isListening = false
                                            try { speechRecognizer.stopListening() } catch(e:Exception){}
                                        } else {
                                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                                                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                                            }
                                            try {
                                                speechRecognizer.setRecognitionListener(recognitionListener)
                                                speechRecognizer.startListening(intent)
                                                isListening = true
                                                isSilentFailed = false
                                                lastSpeechTimestamp = System.currentTimeMillis()
                                                statusMessage = "Speak the paragraph out loud..."
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                statusMessage = "Failed to launch voice service."
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Challenge Mic",
                                tint = TextPrimaryWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Toggle simulator assist button (for easy testing/graceful bypass)
                    IconButton(
                        onClick = { showSimulatorAssist = !showSimulatorAssist },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Simulate",
                            tint = if (showSimulatorAssist) LavenderHighlight else LavenderHighlight.copy(alpha = 0.5f)
                        )
                    }
                }

                // SIMULATOR ASSIST COMPONENT: Helps users bypass if mic fails or in quiet spaces, also makes visual flows highly testable
                AnimatedVisibility(visible = showSimulatorAssist) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🛠️ Verification & Simulator Assist Mode",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LavenderHighlight
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    // Read next 15 words with some randomized skip/correct status
                                    val sizeToRead = 15
                                    val stopIdx = (currentWordIndex + sizeToRead).coerceAtMost(words.size)
                                    for (i in currentWordIndex until stopIdx) {
                                        // 94% correct, 6% skipped simulation
                                        if (kotlin.random.Random.nextFloat() > 0.06f) {
                                            wordStatuses[i] = WordStatus.CORRECT
                                        } else {
                                            wordStatuses[i] = WordStatus.SKIPPED
                                        }
                                    }
                                    currentWordIndex = stopIdx
                                    lastSpeechTimestamp = System.currentTimeMillis()
                                    
                                    if (currentWordIndex >= words.size) {
                                        if (accuracy >= 70.0) {
                                            onChallengeSuccess(accuracy)
                                        } else {
                                            statusMessage = "Challenge completed but accuracy too low. Restarting!"
                                            currentWordIndex = 0
                                            wordStatuses.fill(WordStatus.PENDING)
                                        }
                                    } else {
                                        statusMessage = "Read next 15 words simulated successfully."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftVioletAccent.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Read Next Chunk", style = MaterialTheme.typography.labelSmall, color = TextPrimaryWhite)
                            }

                            Button(
                                onClick = {
                                    // Instantly complete paragraph with 98% accuracy
                                    for (i in 0 until words.size) {
                                        if (kotlin.random.Random.nextFloat() > 0.02f) {
                                            wordStatuses[i] = WordStatus.CORRECT
                                        } else {
                                            wordStatuses[i] = WordStatus.SKIPPED
                                        }
                                    }
                                    currentWordIndex = words.size
                                    onChallengeSuccess(98.0)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Complete Instantly", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981))
                            }
                        }
                    }
                }
            }
        }
    }
}
