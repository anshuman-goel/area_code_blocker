package com.example.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.BlockerApplication
import com.example.data.entity.BlockedLog
import com.example.util.PhoneUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsNotificationListenerService : NotificationListenerService() {
    private val TAG = "SmsNotificationListener"

    companion object {
        @Volatile
        var isServiceRunning = false
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceRunning = true
        Log.d(TAG, "SMS Notification Shield service connected / active.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isServiceRunning = false
        Log.d(TAG, "SMS Notification Shield service disconnected.")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName ?: return
        
        // Target typical messaging apps, especially the default system SMS consumer client and stock messaging apps
        val isMessagingApp = packageName.equals("com.google.android.apps.messaging", ignoreCase = true) || 
                             packageName.equals("com.android.mms", ignoreCase = true) || 
                             packageName.contains("sms", ignoreCase = true) ||
                             packageName.contains("messaging", ignoreCase = true)
        
        if (!isMessagingApp) return

        val extras = sbn.notification?.extras ?: return
        
        // Get sender title, checking multiple possible titles for deep compatibility with modern messaging styles
        val senderTitle = (extras.getCharSequence(Notification.EXTRA_TITLE) ?: 
                           extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE) ?: 
                           extras.getCharSequence(Notification.EXTRA_SUB_TEXT) ?: 
                           "").toString().trim()
                           
        // Get message text, with multiple big-text and subtext fallback options
        val messageText = (extras.getCharSequence(Notification.EXTRA_TEXT) ?: 
                           extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: 
                           "").toString().trim()

        if (senderTitle.isBlank()) return

        Log.d(TAG, "Intercepted message notification from $packageName. Sender: '$senderTitle', Content: '$messageText'")

        // Check if the sender is a contact. If so, we never block them
        val isInContacts = PhoneUtils.isSenderInContacts(applicationContext, senderTitle)
        if (isInContacts) {
            Log.d(TAG, "Sender '$senderTitle' is in contacts. Skipping block checks.")
            return
        }

        val app = applicationContext as BlockerApplication
        val repository = app.repository

        CoroutineScope(Dispatchers.IO).launch {
            val blockedCodes = repository.getBlockedAreaCodesList().map { it.areaCode }
            val blockedKeywords = repository.getBlockedKeywordsList().map { it.keyword }

            val areaCode = PhoneUtils.extractAreaCode(senderTitle)
            
            // Check keyword block (checks both the text body AND the sender title)
            val matchedKeyword = blockedKeywords.firstOrNull { keyword ->
                messageText.contains(keyword, ignoreCase = true) || 
                senderTitle.contains(keyword, ignoreCase = true)
            }
            
            // Check area code block (only relevant if senderTitle looks like a phone number)
            val cleanTitle = PhoneUtils.cleanNumber(senderTitle)
            val isAreaCodeBlocked = cleanTitle.length >= 7 && blockedCodes.contains(areaCode)

            if (isAreaCodeBlocked || matchedKeyword != null) {
                val blockReason = if (matchedKeyword != null) {
                    "Notification Filtered (Keyword: '$matchedKeyword')"
                } else {
                    "Notification Filtered (Area Code: $areaCode)"
                }
                
                Log.d(TAG, "Keyword/AreaCode match detected: blocking notification from '$senderTitle'. Reason: $blockReason")

                // 1. Cancel the system notification in real-time so it disappears from status-bar/lockscreen
                try {
                    cancelNotification(sbn.key)
                    Log.d(TAG, "Successfully dismissed notification for key: ${sbn.key}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error trying to cancel notification", e)
                }

                // 2. Insert into Blocked Logs so user can view blocked SMS in the main logs dashboard
                repository.insertLog(
                    BlockedLog(
                        phoneNumber = senderTitle,
                        areaCode = areaCode,
                        messageBody = messageText,
                        type = "SMS (Silenced)",
                        senderName = blockReason
                    )
                )
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Safe override to monitor removal if needed
    }
}
