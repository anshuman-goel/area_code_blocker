package com.example.service

import android.app.Notification
import android.app.Person
import android.os.Build
import android.os.Bundle
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

    companion object {
        private const val TAG = "SmsNotificationListener"
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
        
        val senderTitle = (extras.getCharSequence(Notification.EXTRA_TITLE)
            ?: extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)
            ?: extras.getCharSequence(Notification.EXTRA_SUB_TEXT)
            ?: "").toString().trim()

        val messageText = (extras.getCharSequence(Notification.EXTRA_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: "").toString().trim()

        val senderIdentifiers = buildList {
            if (senderTitle.isNotBlank()) add(senderTitle)
            messagingStyleSenders(extras).forEach(::add)
        }.distinct()

        if (senderIdentifiers.isEmpty()) return

        Log.d(
            TAG,
            "Intercepted message notification from $packageName. " +
                "Senders: $senderIdentifiers, Content: '$messageText'",
        )

        // Contacts are always allowed. MessagingStyle sender data is checked as well as the title.
        if (senderIdentifiers.any { PhoneUtils.isSenderInContacts(applicationContext, it) }) {
            Log.d(TAG, "A sender is in contacts. Skipping block checks.")
            return
        }

        val app = applicationContext as BlockerApplication
        val repository = app.repository

        CoroutineScope(Dispatchers.IO).launch {
            val match = SmsNotificationFilter.findMatch(
                senderIdentifiers = senderIdentifiers,
                messageText = messageText,
                blockedAreaCodes = repository.getBlockedAreaCodesList().map { it.areaCode },
                blockedKeywords = repository.getBlockedKeywordsList().map { it.keyword },
            ) ?: return@launch

            val sender = senderIdentifiers.first()
            val blockReason = if (match.keyword != null) {
                "Notification Filtered (Keyword: '${match.keyword}')"
            } else {
                "Notification Filtered (Area Code: ${match.areaCode})"
            }

            Log.d(TAG, "SMS match detected: silencing '$sender'. Reason: $blockReason")

            try {
                cancelNotification(sbn.key)
                Log.d(TAG, "Successfully dismissed notification for key: ${sbn.key}")
            } catch (e: Exception) {
                Log.e(TAG, "Error trying to cancel notification", e)
            }

            repository.insertLog(
                BlockedLog(
                    phoneNumber = sender,
                    areaCode = match.areaCode,
                    messageBody = messageText,
                    type = "SMS (Silenced)",
                    senderName = blockReason,
                ),
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun messagingStyleSenders(extras: Bundle): List<String> {
        return extras.getParcelableArray(Notification.EXTRA_MESSAGES)
            .orEmpty()
            .mapNotNull { it as? Bundle }
            .mapNotNull { message ->
                val sender = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    (message.getParcelable("sender_person") as? Person)?.name
                        ?: message.getCharSequence("sender")
                } else {
                    message.getCharSequence("sender")
                }
                sender?.toString()?.trim()?.takeIf(String::isNotBlank)
            }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Safe override to monitor removal if needed
    }
}
