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
        
        if (isBlockingActive.value) {
            serviceScope.launch {
                if (isEmergencyBypassActive()) {
                    return@launch
                }
                
                // 1. App Blocking
                if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    val packageName = event.packageName?.toString()
                    if (packageName != null && packageName != this@FocusBlockedAccessibilityService.packageName) {
                        val isBlocked = blockedAppRepository.isAppBlocked(packageName)
                        if (isBlocked) {
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
                        val isBlocked = blockedWebsiteRepository.isWebsiteBlocked(detectedDomain)
                        if (isBlocked) {
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
