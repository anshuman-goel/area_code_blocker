package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.BlockerApplication
import com.example.data.entity.BlockedLog
import com.example.util.PhoneUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsBlockerReceiver : BroadcastReceiver() {
    private val TAG = "SmsBlockerReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        Log.d(TAG, "Incoming SMS received trigger")

        val messages = try {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing SMS intent", e)
            null
        }

        if (messages.isNullOrEmpty()) return

        val firstMessage = messages[0]
        val rawSender = firstMessage.originatingAddress ?: ""
        
        if (rawSender.isBlank()) return

        val isInContacts = PhoneUtils.isNumberInContacts(context, rawSender)
        if (isInContacts) {
            Log.d(TAG, "SMS sender $rawSender is in contacts. Pass-through.")
            return
        }

        val areaCode = PhoneUtils.extractAreaCode(rawSender)
        val fullBody = messages.joinToString(separator = "") { it.messageBody ?: "" }

        val app = context.applicationContext as BlockerApplication
        val repository = app.repository

        CoroutineScope(Dispatchers.IO).launch {
            val blockedCodes = repository.getBlockedAreaCodesList().map { it.areaCode }
            val blockedKeywords = repository.getBlockedKeywordsList().map { it.keyword }
            
            val containsBlockedKeyword = blockedKeywords.any { keyword ->
                fullBody.contains(keyword, ignoreCase = true)
            }
            val isAreaCodeBlocked = blockedCodes.contains(areaCode)

            if (isAreaCodeBlocked || containsBlockedKeyword) {
                val blockReason = if (containsBlockedKeyword) "Blocked Content (Keyword)" else "Unknown (Blocked Area Code)"
                Log.d(TAG, "SMS blocked. Reason: $blockReason from $rawSender")
                
                repository.insertLog(
                    BlockedLog(
                        phoneNumber = rawSender,
                        areaCode = areaCode,
                        messageBody = fullBody,
                        type = "SMS",
                        senderName = blockReason
                    )
                )
            }
        }
    }
}
