package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.entity.FocusSessionEntity
import com.example.domain.repository.FocusRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class FocusSessionService : Service() {

    @Inject
    lateinit var repository: FocusRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START -> {
                val durationMinutes = intent.getIntExtra(EXTRA_DURATION, 25)
                startSessionInternal(durationMinutes)
            }
            ACTION_PAUSE -> {
                pauseSessionInternal()
            }
            ACTION_RESUME -> {
                resumeSessionInternal()
            }
            ACTION_STOP -> {
                stopSessionInternal()
            }
        }
        return START_NOT_STICKY
    }

    private fun startSessionInternal(durationMinutes: Int) {
        _selectedDurationMinutes.value = durationMinutes
        _timeLeftSeconds.value = durationMinutes * 60
        _isRunning.value = true
        _isPaused.value = false

        startForeground(NOTIFICATION_ID, buildNotification())
        startTicking()
    }

    private fun pauseSessionInternal() {
        if (_isRunning.value && !_isPaused.value) {
            _isPaused.value = true
            timerJob?.cancel()
            updateNotification()
        }
    }

    private fun resumeSessionInternal() {
        if (_isRunning.value && _isPaused.value) {
            _isPaused.value = false
            startTicking()
            updateNotification()
        }
    }

    private fun stopSessionInternal() {
        timerJob?.cancel()
        _isRunning.value = false
        _isPaused.value = false
        _timeLeftSeconds.value = 0
        stopForeground(true)
        stopSelf()
    }

    private fun startTicking() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (_timeLeftSeconds.value > 0) {
                delay(1000)
                _timeLeftSeconds.value--
                updateNotification()
            }
            onSessionComplete()
        }
    }

    private suspend fun onSessionComplete() {
        val duration = _selectedDurationMinutes.value
        
        // Save to Room Database
        withContext(Dispatchers.IO) {
            repository.insertSession(
                FocusSessionEntity(
                    durationMinutes = duration,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        // Notify UI of completion
        _sessionCompletedEvent.emit(duration)

        // Reset state
        _isRunning.value = false
        _isPaused.value = false
        _timeLeftSeconds.value = 0

        // Play system notification sound / ringtone
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notificationUri)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Send a completion notification and stop foreground
        sendCompletionNotification()
        stopForeground(true)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        val isPaused = _isPaused.value
        val minutes = _timeLeftSeconds.value / 60
        val seconds = _timeLeftSeconds.value % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)

        val title = if (isPaused) "Focus Session Paused" else "Focus Session Active"
        val content = if (isPaused) "Timer: $timeFormatted (Paused)" else "Remaining: $timeFormatted"

        // Intent to launch MainActivity when notification is clicked
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Buttons
        val stopIntent = Intent(this, FocusSessionService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, FocusSessionService::class.java).apply {
            action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            2,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleActionText = if (isPaused) "Resume" else "Pause"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play) // Standard system icon, or fallback
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .addAction(android.R.drawable.ic_media_pause, toggleActionText, togglePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Silent so it doesn't buzz every tick
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun sendCompletionNotification() {
        val duration = _selectedDurationMinutes.value
        val title = "Focus Session Completed!"
        val content = "Awesome job! You successfully completed a $duration-minute Focus Session."

        val contentIntent = PendingIntent.getActivity(
            this,
            3,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(COMPLETION_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Focus Bridge Session"
            val descriptionText = "Notifications for your ongoing and completed Focus Sessions"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                // Remove sound from the continuous ticks channel so it is silent
                setSound(null, null)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        timerJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "focus_session_channel"
        const val NOTIFICATION_ID = 8888
        const val COMPLETION_NOTIFICATION_ID = 8889

        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_PAUSE = "com.example.service.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.service.ACTION_RESUME"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"

        const val EXTRA_DURATION = "com.example.service.EXTRA_DURATION"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _isPaused = MutableStateFlow(false)
        val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

        private val _timeLeftSeconds = MutableStateFlow(0)
        val timeLeftSeconds: StateFlow<Int> = _timeLeftSeconds.asStateFlow()

        private val _selectedDurationMinutes = MutableStateFlow(25)
        val selectedDurationMinutes: StateFlow<Int> = _selectedDurationMinutes.asStateFlow()

        private val _sessionCompletedEvent = MutableSharedFlow<Int>(replay = 0)
        val sessionCompletedEvent = _sessionCompletedEvent

        fun startService(context: Context, durationMinutes: Int) {
            val intent = Intent(context, FocusSessionService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DURATION, durationMinutes)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseSession(context: Context) {
            val intent = Intent(context, FocusSessionService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resumeSession(context: Context) {
            val intent = Intent(context, FocusSessionService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stopSession(context: Context) {
            val intent = Intent(context, FocusSessionService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
