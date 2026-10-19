package com.example.service

import android.content.Context
import androidx.core.content.edit

object SmsFilterPreferences {
    private const val PREFERENCES_NAME = "blocker_prefs"
    private const val KEY_SILENCE_BLOCKED_AREA_CODES = "silence_sms_from_blocked_area_codes"

    fun isAreaCodeSilencingEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SILENCE_BLOCKED_AREA_CODES, true)
    }

    fun setAreaCodeSilencingEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_SILENCE_BLOCKED_AREA_CODES, enabled)
        }
    }
}
