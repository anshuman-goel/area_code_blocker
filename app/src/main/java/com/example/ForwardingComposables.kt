package com.example

import androidx.compose.runtime.Composable
import com.example.viewmodel.BlockerViewModel
import com.example.ui.BlockerHomeScreen
import com.example.ui.components.SettingsDialog

@Composable
fun BlockerHomeScreen(
    viewModel: BlockerViewModel,
    initContactsGranted: Boolean? = null,
    initPhoneNumbersGranted: Boolean? = null,
    initCallScreeningGranted: Boolean? = null,
    initNotificationListenerGranted: Boolean? = null
) {
    com.example.ui.BlockerHomeScreen(
        viewModel = viewModel,
        initContactsGranted = initContactsGranted,
        initPhoneNumbersGranted = initPhoneNumbersGranted,
        initCallScreeningGranted = initCallScreeningGranted,
        initNotificationListenerGranted = initNotificationListenerGranted
    )
}

@Composable
fun SettingsDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    viewModel: BlockerViewModel
) {
    com.example.ui.components.SettingsDialog(
        show = show,
        onDismiss = onDismiss,
        viewModel = viewModel
    )
}
