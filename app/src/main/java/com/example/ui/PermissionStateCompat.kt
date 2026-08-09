package com.example.ui

import androidx.compose.runtime.Composable

@Deprecated("Use PermissionState instead", ReplaceWith("PermissionState"))
typealias DashboardPermissionState = PermissionState

@Deprecated("Use rememberPermissionState instead", ReplaceWith("rememberPermissionState"))
@Composable
fun rememberDashboardPermissionState(
    initContacts: Boolean? = null,
    initPhone: Boolean? = null,
    initRole: Boolean? = null,
    initNotif: Boolean? = null
): PermissionState = rememberPermissionState(initContacts, initPhone, initRole, initNotif)
