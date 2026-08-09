package com.example.ui

import androidx.compose.runtime.Composable
import com.example.viewmodel.BlockerViewModel

@Composable
fun BlockerHomeScreen(
    viewModel: BlockerViewModel,
    initContactsGranted: Boolean? = null,
    initPhoneNumbersGranted: Boolean? = null,
    initCallScreeningGranted: Boolean? = null,
    initNotificationListenerGranted: Boolean? = null
) {
    BlockerScreen(
        viewModel = viewModel,
        initContactsGranted = initContactsGranted,
        initPhoneNumbersGranted = initPhoneNumbersGranted,
        initCallScreeningGranted = initCallScreeningGranted,
        initNotificationListenerGranted = initNotificationListenerGranted
    )
}
