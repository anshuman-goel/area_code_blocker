package com.example.ui

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.util.PhoneUtils

class PermissionState(
    val context: Context,
    private val initContacts: Boolean?,
    private val initPhone: Boolean?,
    private val initRole: Boolean?,
    private val initNotif: Boolean?,
    private val permissionLauncher: (Array<String>) -> Unit,
    private val roleLauncher: (Intent) -> Unit,
) {
    var isContactsGranted by mutableStateOf(
        initContacts ?: (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED),
    )
    var isPhoneGranted by mutableStateOf(
        initPhone ?: (ContextCompat.checkSelfPermission(context, PhoneUtils.PHONE_PERMISSION) == PackageManager.PERMISSION_GRANTED),
    )
    var isRoleGranted by mutableStateOf(
        initRole ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
        } else true,
    )
    var isNotificationEnabled by mutableStateOf(
        initNotif ?: PhoneUtils.isNotificationServiceEnabled(context),
    )

    val hasAllRequired get() = isContactsGranted && isRoleGranted && isNotificationEnabled

    fun requestContacts() = permissionLauncher(arrayOf(android.Manifest.permission.READ_CONTACTS))

    fun requestPhone() = permissionLauncher(arrayOf(PhoneUtils.PHONE_PERMISSION))

    fun handlePermissionResult(permissions: Map<String, Boolean>) {
        isContactsGranted = permissions[android.Manifest.permission.READ_CONTACTS] ?: isContactsGranted
        isPhoneGranted = permissions[PhoneUtils.PHONE_PERMISSION] ?: isPhoneGranted
    }

    fun requestRole(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            return if (roleManager?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) == true) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                    roleLauncher(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                } else {
                    isRoleGranted = true
                }
                true
            } else {
                try {
                    Toast.makeText(context, "Call Screening role is not available on this device.", Toast.LENGTH_LONG).show()
                } catch (_: Exception) {
                    // Toasts may not be available in non-UI contexts.
                }
                false
            }
        }
        return false
    }

    fun refreshPermissions() {
        if (initContacts == null) {
            isContactsGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        }
        if (initPhone == null) {
            isPhoneGranted = ContextCompat.checkSelfPermission(context, PhoneUtils.PHONE_PERMISSION) == PackageManager.PERMISSION_GRANTED
        }
        if ((initRole == null) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            isRoleGranted = roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
        }
        if (initNotif == null) {
            isNotificationEnabled = PhoneUtils.isNotificationServiceEnabled(context)
        }
    }
}

@Composable
fun rememberPermissionState(
    initContacts: Boolean? = null,
    initPhone: Boolean? = null,
    initRole: Boolean? = null,
    initNotif: Boolean? = null,
): PermissionState {
    val context = LocalContext.current
    var state by remember { mutableStateOf<PermissionState?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        state?.handlePermissionResult(permissions)
    }

    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            state?.isRoleGranted = roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
        }
    }

    return remember(context) {
        PermissionState(
            context = context,
            initContacts = initContacts,
            initPhone = initPhone,
            initRole = initRole,
            initNotif = initNotif,
            permissionLauncher = permissionLauncher::launch,
            roleLauncher = roleLauncher::launch,
        ).also { state = it }
    }
}
