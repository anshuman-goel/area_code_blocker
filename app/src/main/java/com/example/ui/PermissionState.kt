package com.example.ui

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.widget.Toast
import com.example.util.PhoneUtils

class PermissionState(
    val context: Context,
    private val initContacts: Boolean?,
    private val initPhone: Boolean?,
    private val initRole: Boolean?,
    private val initNotif: Boolean?,
    private val permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    private val roleLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    var isContactsGranted by mutableStateOf(
        initContacts ?: (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
    )
    var isPhoneGranted by mutableStateOf(
        initPhone ?: (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED)
    )
    var isRoleGranted by mutableStateOf(
        initRole ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
        } else true
    )
    var isNotificationEnabled by mutableStateOf(
        initNotif ?: PhoneUtils.isNotificationServiceEnabled(context)
    )

    val hasAllRequired get() = isContactsGranted && isRoleGranted && isNotificationEnabled

    fun requestContacts() = permissionLauncher.launch(arrayOf(android.Manifest.permission.READ_CONTACTS))
    fun requestPhone() = permissionLauncher.launch(arrayOf(android.Manifest.permission.READ_PHONE_NUMBERS))
    
    fun requestRole(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) == true) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                    roleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                    return true
                } else {
                    // role already held
                    isRoleGranted = true
                    return true
                }
            } else {
                // Role not available on this device - notify user
                try {
                    Toast.makeText(context, "Call Screening role is not available on this device.", Toast.LENGTH_LONG).show()
                } catch (_: Exception) {}
                return false
            }
        }
        return false
    }

    fun refreshPermissions() {
        if (initContacts == null) {
            isContactsGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        }
        if (initPhone == null) {
            isPhoneGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED
        }
        if (initRole == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
    initNotif: Boolean? = null
): PermissionState {
    val context = LocalContext.current
    
    var state by remember { mutableStateOf<PermissionState?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        state?.let { s ->
            s.isContactsGranted = permissions[android.Manifest.permission.READ_CONTACTS] ?: s.isContactsGranted
            s.isPhoneGranted = permissions[android.Manifest.permission.READ_PHONE_NUMBERS] ?: s.isPhoneGranted
        }
    }

    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            state?.isRoleGranted = roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
        }
    }

    return remember(context) {
        PermissionState(context, initContacts, initPhone, initRole, initNotif, permissionLauncher, roleLauncher).also { state = it }
    }
}
