package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.BlockedAppEntity
import com.example.domain.repository.BlockedAppRepository
import com.example.service.FocusBlockedAccessibilityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)

@HiltViewModel
class AppBlockerViewModel @Inject constructor(
    private val repository: BlockedAppRepository,
    private val websiteRepository: com.example.domain.repository.BlockedWebsiteRepository,
    private val preferenceManager: com.example.data.datastore.PreferenceManager,
    private val unlockRepository: com.example.domain.repository.EmergencyUnlockRepository,
    private val timetableRepository: com.example.domain.repository.TimetableRepository
) : ViewModel() {

    val isAnyBlockActive: StateFlow<Boolean> = flow {
        while (true) {
            val isManual = isBlockingActive.value
            val isSession = com.example.service.FocusSessionService.isRunning.value
            
            val calendar = java.util.Calendar.getInstance()
            val dayOfWeekInt = calendar.get(java.util.Calendar.DAY_OF_WEEK)
            val currentDayStr = when (dayOfWeekInt) {
                java.util.Calendar.MONDAY -> "Mon"
                java.util.Calendar.TUESDAY -> "Tue"
                java.util.Calendar.WEDNESDAY -> "Wed"
                java.util.Calendar.THURSDAY -> "Thu"
                java.util.Calendar.FRIDAY -> "Fri"
                java.util.Calendar.SATURDAY -> "Sat"
                java.util.Calendar.SUNDAY -> "Sun"
                else -> ""
            }
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = calendar.get(java.util.Calendar.MINUTE)
            val currentTimeVal = hour * 60 + minute

            var isSchedule = false
            try {
                val subjects = timetableRepository.getAllSubjectsSync()
                isSchedule = subjects.any { s ->
                    val sDays = s.dayOfWeek.split(",")
                    if (sDays.contains(currentDayStr)) {
                        val sParts = s.startTime.split(":")
                        val sTime = sParts[0].toInt() * 60 + sParts[1].toInt()
                        val eParts = s.endTime.split(":")
                        val eTime = eParts[0].toInt() * 60 + eParts[1].toInt()
                        currentTimeVal in sTime..eTime
                    } else false
                }
            } catch (e: Exception) {
                // ignore
            }

            emit(isManual || isSession || isSchedule)
            kotlinx.coroutines.delay(1000)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val blockedWebsites: StateFlow<List<com.example.data.entity.BlockedWebsiteEntity>> = websiteRepository.getAllBlockedWebsites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleWebsiteBlock(domain: String, isCurrentlyBlocked: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyBlocked) {
                websiteRepository.deleteBlockedWebsite(com.example.data.entity.BlockedWebsiteEntity(domain))
            } else {
                websiteRepository.insertBlockedWebsite(com.example.data.entity.BlockedWebsiteEntity(domain))
            }
        }
    }

    val isOnboardingCompleted: StateFlow<Boolean> = preferenceManager.isOnboardingCompleted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isStrictBlockingEnabled: StateFlow<Boolean> = preferenceManager.isStrictBlockingEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setStrictBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setStrictBlockingEnabled(enabled)
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            preferenceManager.setOnboardingCompleted(completed)
        }
    }

    // Cooldown setting and history exposed via Flow
    val cooldownMinutes: StateFlow<Int> = preferenceManager.cooldownMinutes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 30
        )

    val unlockHistory: StateFlow<List<com.example.data.entity.EmergencyUnlockEntity>> = unlockRepository.getAllUnlocks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setCooldownMinutes(minutes: Int) {
        viewModelScope.launch {
            preferenceManager.setCooldownMinutes(minutes)
        }
    }

    // List of blocked app entities from DB
    val blockedApps: StateFlow<List<BlockedAppEntity>> = repository.getAllBlockedApps()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // State for installed apps list loaded from device
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    // Loading state for installed apps
    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    // Search query for filtering installed apps
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered installed apps based on search query
    val filteredInstalledApps: StateFlow<List<AppInfo>> = combine(
        _installedApps,
        _searchQuery
    ) { apps, query ->
        if (query.isBlank()) {
            apps
        } else {
            apps.filter { it.appName.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Blocking active/inactive state synced with service
    val isBlockingActive: StateFlow<Boolean> = FocusBlockedAccessibilityService.isBlockingActive

    // Accessibility Service connection state
    val isAccessibilityServiceConnected: StateFlow<Boolean> = FocusBlockedAccessibilityService.isServiceConnected

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Load installed apps with launcher intents
    fun loadInstalledApps(context: Context) {
        if (_installedApps.value.isNotEmpty() && !_isLoadingApps.value) return
        
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingApps.value = true
            try {
                val pm = context.packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(intent, 0)
                val apps = resolveInfos.mapNotNull { info ->
                    val packageName = info.activityInfo.packageName
                    if (packageName == context.packageName) return@mapNotNull null
                    val appName = info.loadLabel(pm).toString()
                    val icon = try {
                        info.loadIcon(pm)
                    } catch (e: Exception) {
                        null
                    }
                    AppInfo(packageName, appName, icon)
                }.distinctBy { it.packageName }.sortedBy { it.appName }

                _installedApps.value = apps
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingApps.value = false
            }
        }
    }

    // Toggle app block status
    fun toggleAppBlock(packageName: String, appName: String, isCurrentlyBlocked: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyBlocked) {
                repository.deleteBlockedApp(BlockedAppEntity(packageName, appName))
            } else {
                repository.insertBlockedApp(BlockedAppEntity(packageName, appName))
            }
        }
    }

    // Start/Stop blocking session
    fun setBlockingActive(active: Boolean) {
        FocusBlockedAccessibilityService.setBlockingActive(active)
    }

    // Guide the user to accessibility settings
    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
