package com.aistudio.areacodeblocker.ui

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.PermissionState
import com.example.util.PhoneUtils
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class PermissionStateTest {

    @Test
    @Config(sdk = [24, 25])
    fun legacySdk_requestsReadPhoneState_andHandlesItsResult() {
        assertEquals(Manifest.permission.READ_PHONE_STATE, PhoneUtils.PHONE_PERMISSION)

        var requestedPermissions: Array<String>? = null
        val state = createState { requestedPermissions = it }

        state.requestPhone()
        assertArrayEquals(arrayOf(Manifest.permission.READ_PHONE_STATE), requestedPermissions)

        state.handlePermissionResult(mapOf(Manifest.permission.READ_PHONE_STATE to true))
        assertTrue(state.isPhoneGranted)
    }

    @Test
    @Config(sdk = [26, 36])
    fun modernSdk_requestsReadPhoneNumbers_andHandlesItsResult() {
        assertEquals(Manifest.permission.READ_PHONE_NUMBERS, PhoneUtils.PHONE_PERMISSION)

        var requestedPermissions: Array<String>? = null
        val state = createState { requestedPermissions = it }

        state.requestPhone()
        assertArrayEquals(arrayOf(Manifest.permission.READ_PHONE_NUMBERS), requestedPermissions)

        state.handlePermissionResult(mapOf(Manifest.permission.READ_PHONE_NUMBERS to true))
        assertTrue(state.isPhoneGranted)
    }

    @Test
    @Config(sdk = [24])
    fun legacySdk_ignoresModernPermissionResult() {
        val state = createState()

        state.handlePermissionResult(mapOf(Manifest.permission.READ_PHONE_NUMBERS to true))

        assertFalse(state.isPhoneGranted)
    }

    @Test
    @Config(sdk = [26])
    fun modernSdk_ignoresLegacyPermissionResult() {
        val state = createState()

        state.handlePermissionResult(mapOf(Manifest.permission.READ_PHONE_STATE to true))

        assertFalse(state.isPhoneGranted)
    }

    private fun createState(
        permissionLauncher: (Array<String>) -> Unit = {},
    ): PermissionState {
        val context: Context = ApplicationProvider.getApplicationContext()
        return PermissionState(
            context = context,
            initContacts = false,
            initPhone = false,
            initRole = false,
            initNotif = false,
            permissionLauncher = permissionLauncher,
        ) {}
    }
}
