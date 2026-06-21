package com.example.util

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log

object PhoneUtils {
    private const val TAG = "PhoneUtils"

    /**
     * Clean phone number of any non-digit chars
     */
    fun cleanNumber(number: String): String {
        return number.filter { it.isDigit() }
    }

    /**
     * Extracts a standard area code from a phone number.
     * Supports US/NANP format (and handles starting +1 or 1).
     * If international or not matching standard NANP, returns first 3 digits as a utility fallback.
     */
    fun extractAreaCode(number: String): String {
        val digits = number.filter { it.isDigit() }
        
        return when {
            digits.length == 11 && digits.startsWith("1") -> digits.substring(1, 4)
            digits.length == 10 -> digits.substring(0, 3)
            digits.length > 3 -> digits.substring(0, 3) // Best-effort fallback
            else -> digits
        }
    }

    /**
     * Queries the system contacts to check if the given phone number exists.
     * Requires READ_CONTACTS permission.
     */
    fun isNumberInContacts(context: Context, phoneNumber: String): Boolean {
        if (phoneNumber.isBlank()) return false
        
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(
                ContactsContract.PhoneLookup.DISPLAY_NAME
            )
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        val name = cursor.getString(nameIndex)
                        Log.d(TAG, "Number $phoneNumber found in contacts as '$name'")
                    }
                    return true
                }
            }
            false
        } catch (e: SecurityException) {
            Log.w(TAG, "READ_CONTACTS permission missing or denied.", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking contact lookup.", e)
            false
        }
    }

    /**
     * Queries the display name for a contact if found.
     */
    fun getContactName(context: Context, phoneNumber: String): String? {
        if (phoneNumber.isBlank()) return null
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        return cursor.getString(nameIndex)
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Attempts to automatically retrieve the device's own telephone line number.
     * Uses TelephonyManager & SubscriptionManager. Requires READ_PHONE_NUMBERS or READ_PHONE_STATE.
     */
    fun getDevicePhoneNumber(context: Context): String? {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_PHONE_NUMBERS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_PHONE_STATE
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? android.telephony.SubscriptionManager
                val subId = android.telephony.SubscriptionManager.getDefaultSubscriptionId()
                if (subId != android.telephony.SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    val number = subscriptionManager?.getPhoneNumber(subId)
                    if (!number.isNullOrBlank()) return number
                }
            }

            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? android.telephony.TelephonyManager
            val line1 = telephonyManager?.line1Number
            if (!line1.isNullOrBlank()) {
                return line1
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException reading phone number", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading phone number", e)
        }
        return null
    }

    /**
     * Checks if notification listener service is enabled for our application package.
     */
    fun isNotificationServiceEnabled(context: Context): Boolean {
        val pkgName = context.packageName
        val flat = android.provider.Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (!flat.isNullOrEmpty()) {
            val names = flat.split(":")
            for (name in names) {
                val cn = android.content.ComponentName.unflattenFromString(name)
                if (cn != null && cn.packageName == pkgName) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Checks if the sender is a saved contact by searching for their display name OR phone number.
     * Ensures contacts are never blocked.
     */
    fun isSenderInContacts(context: Context, identifier: String): Boolean {
        if (identifier.isBlank()) return false

        // 1. Check if the identifier is a display name of a contact in our address book
        try {
            val uri = ContactsContract.Contacts.CONTENT_URI
            val projection = arrayOf(ContactsContract.Contacts._ID)
            val selection = "${ContactsContract.Contacts.DISPLAY_NAME} = ? OR ${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} = ?"
            val selectionArgs = arrayOf(identifier, identifier)
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    Log.d(TAG, "Sender '$identifier' matched a saved contact display name.")
                    return true
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "READ_CONTACTS permission missing for name lookup.", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up contact display name.", e)
        }

        // 2. Fallback: Treat as a number and use phone lookup
        return isNumberInContacts(context, identifier)
    }
}
