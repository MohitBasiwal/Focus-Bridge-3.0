package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.domain.repository.BlockedAppRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FocusBlockedAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var blockedAppRepository: BlockedAppRepository

    @Inject
    lateinit var blockedWebsiteRepository: com.example.domain.repository.BlockedWebsiteRepository

    @Inject
    lateinit var preferenceManager: com.example.data.datastore.PreferenceManager

    @Inject
    lateinit var timetableRepository: com.example.domain.repository.TimetableRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private suspend fun isEmergencyBypassActive(): Boolean {
        return try {
            val lastUnlock = preferenceManager.lastUnlockTime.first() ?: 0L
            val cooldown = preferenceManager.cooldownMinutes.first()
            if (lastUnlock <= 0L) return false
            val now = System.currentTimeMillis()
            val elapsed = now - lastUnlock
            elapsed < (cooldown.toLong() * 60 * 1000)
        } catch (e: Exception) {
            Log.e("AccessibilityService", "Error checking bypass status", e)
            false
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        serviceScope.launch {
            if (isEmergencyBypassActive()) {
                return@launch
            }

            // 1. Check if there is an active timetable schedule right now
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

            var activeSubject: com.example.data.entity.TimetableSubjectEntity? = null
            try {
                val subjects = timetableRepository.getAllSubjectsSync()
                activeSubject = subjects.find { s ->
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
                Log.e("AccessibilityService", "Error resolving active timetable subject", e)
            }

            // If a schedule is active, or manual blocking is active
            val isScheduleActive = activeSubject != null
            val isManualBlockingActive = isBlockingActive.value

            if (isScheduleActive || isManualBlockingActive) {
                // Determine block criteria
                val blockAppsSet = if (isScheduleActive && activeSubject?.blockedApps?.isNotBlank() == true) {
                    activeSubject.blockedApps.split(",").toSet()
                } else emptySet()

                val blockWebsitesSet = if (isScheduleActive && activeSubject?.blockedWebsites?.isNotBlank() == true) {
                    activeSubject.blockedWebsites.split(",").toSet()
                } else emptySet()

                // 1. App Blocking
                if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    val packageName = event.packageName?.toString()
                    if (packageName != null && packageName != this@FocusBlockedAccessibilityService.packageName) {
                        var shouldBlock = false
                        if (isScheduleActive) {
                            if (blockAppsSet.isNotEmpty()) {
                                // Specific list defined for active timetable block
                                if (blockAppsSet.contains(packageName)) {
                                    shouldBlock = true
                                }
                            } else {
                                // If active timetable block has NO specific list, block global list
                                if (blockedAppRepository.isAppBlocked(packageName)) {
                                    shouldBlock = true
                                }
                            }
                        } else if (isManualBlockingActive) {
                            // Block global list
                            if (blockedAppRepository.isAppBlocked(packageName)) {
                                    shouldBlock = true
                            }
                        }

                        if (shouldBlock) {
                            launchBlockOverlay(packageName)
                            return@launch
                        }
                    }
                }

                // 2. Website Blocking
                if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
                    event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                    
                    val rootNode = rootInActiveWindow
                    val detectedDomain = findUrlInNode(rootNode)
                    if (detectedDomain != null) {
                        var shouldBlockWeb = false
                        if (isScheduleActive) {
                            if (blockWebsitesSet.isNotEmpty()) {
                                // Specific websites defined for active timetable block
                                if (blockWebsitesSet.contains(detectedDomain)) {
                                    shouldBlockWeb = true
                                }
                            } else {
                                // If active timetable block has NO specific list, block global list
                                if (blockedWebsiteRepository.isWebsiteBlocked(detectedDomain)) {
                                    shouldBlockWeb = true
                                }
                            }
                        } else if (isManualBlockingActive) {
                            // Block global list
                            if (blockedWebsiteRepository.isWebsiteBlocked(detectedDomain)) {
                                shouldBlockWeb = true
                            }
                        }

                        if (shouldBlockWeb) {
                            launchWebsiteBlockOverlay(detectedDomain)
                        }
                    }
                }
            }
        }
    }

    private fun findUrlInNode(node: android.view.accessibility.AccessibilityNodeInfo?): String? {
        if (node == null) return null
        
        val viewId = node.viewIdResourceName
        val text = node.text?.toString()
        
        if (viewId != null && (
            viewId.endsWith("url_bar") || 
            viewId.endsWith("url_edit_text") || 
            viewId.endsWith("address_bar") || 
            viewId.endsWith("search_box") || 
            viewId.endsWith("search_src_text")
        )) {
            if (!text.isNullOrBlank()) {
                return extractDomain(text)
            }
        }
        
        if (!text.isNullOrBlank() && (
            text.startsWith("http://") || 
            text.startsWith("https://") || 
            text.contains(".com") || 
            text.contains(".org") || 
            text.contains(".net") || 
            text.contains(".edu") || 
            text.contains(".co.in")
        )) {
            return extractDomain(text)
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val found = findUrlInNode(child)
            if (found != null) {
                return found
            }
        }
        return null
    }

    private fun extractDomain(url: String): String {
        var cleanUrl = url.trim().lowercase()
        if (cleanUrl.startsWith("http://")) {
            cleanUrl = cleanUrl.substring(7)
        } else if (cleanUrl.startsWith("https://")) {
            cleanUrl = cleanUrl.substring(8)
        }
        if (cleanUrl.startsWith("www.")) {
            cleanUrl = cleanUrl.substring(4)
        }
        val slashIdx = cleanUrl.indexOf('/')
        if (slashIdx != -1) {
            cleanUrl = cleanUrl.substring(0, slashIdx)
        }
        val colonIdx = cleanUrl.indexOf(':')
        if (colonIdx != -1) {
            cleanUrl = cleanUrl.substring(0, colonIdx)
        }
        return cleanUrl
    }

    private fun launchBlockOverlay(blockedPackage: String) {
        val intent = Intent().apply {
            setClassName(this@FocusBlockedAccessibilityService, "com.example.ui.screens.BlockedOverlayActivity")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("BLOCKED_PACKAGE_NAME", blockedPackage)
        }
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        mainHandler.post {
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("AccessibilityService", "Failed to launch blocking overlay", e)
            }
        }
    }

    private fun launchWebsiteBlockOverlay(domain: String) {
        val intent = Intent().apply {
            setClassName(this@FocusBlockedAccessibilityService, "com.example.ui.screens.BlockedOverlayActivity")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("BLOCKED_WEBSITE_DOMAIN", domain)
        }
        performGlobalAction(GLOBAL_ACTION_HOME)
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        mainHandler.post {
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("AccessibilityService", "Failed to launch website blocking overlay", e)
            }
        }
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceConnected.value = true
        Log.d("AccessibilityService", "FocusBlockedAccessibilityService Connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceConnected.value = false
        Log.d("AccessibilityService", "FocusBlockedAccessibilityService Destroyed")
    }

    companion object {
        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        private val _isBlockingActive = MutableStateFlow(false)
        val isBlockingActive: StateFlow<Boolean> = _isBlockingActive.asStateFlow()

        fun setBlockingActive(active: Boolean) {
            _isBlockingActive.value = active
        }
    }
}
