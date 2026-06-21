package com.example.service

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.BlockerApplication
import com.example.data.entity.BlockedLog
import com.example.util.PhoneUtils
import kotlinx.coroutines.runBlocking

@RequiresApi(Build.VERSION_CODES.Q)
class CallBlockerScreeningService : CallScreeningService() {
    private val TAG = "CallScreeningService"

    override fun onScreenCall(callDetails: Call.Details) {
        val handle = callDetails.handle ?: return
        val rawNumber = handle.schemeSpecificPart ?: ""
        
        Log.d(TAG, "Incoming call screened: $rawNumber")

        if (rawNumber.isBlank()) {
            respondWithAllow(callDetails)
            return
        }

        // 1. Check if the number is in contacts
        val isInContacts = PhoneUtils.isNumberInContacts(this, rawNumber)
        if (isInContacts) {
            Log.d(TAG, "Caller is in contacts. Allowing the call: $rawNumber")
            respondWithAllow(callDetails)
            return
        }

        // 2. Extract area code
        val areaCode = PhoneUtils.extractAreaCode(rawNumber)
        Log.d(TAG, "Extracted area code: $areaCode from number: $rawNumber")

        // 3. Load blocked records and check matches
        val app = application as BlockerApplication
        val repository = app.repository

        var shouldBlock = false
        runBlocking {
            val blockedCodes = repository.getBlockedAreaCodesList().map { it.areaCode }
            if (blockedCodes.contains(areaCode)) {
                shouldBlock = true
                Log.d(TAG, "Blocking call from area code $areaCode ($rawNumber)")
                repository.insertLog(
                    BlockedLog(
                        phoneNumber = rawNumber,
                        areaCode = areaCode,
                        messageBody = null,
                        type = "CALL",
                        senderName = "Unknown (Blocked Area Code)"
                    )
                )
            }
        }

        if (shouldBlock) {
            respondWithBlock(callDetails)
        } else {
            respondWithAllow(callDetails)
        }
    }

    private fun respondWithAllow(callDetails: Call.Details) {
        val response = CallResponse.Builder().build()
        respondToCall(callDetails, response)
    }

    private fun respondWithBlock(callDetails: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(true)
            .build()
        respondToCall(callDetails, response)
    }
}
