package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.viewmodel.BlockerViewModel

fun LazyListScope.rulesTab(
    viewModel: BlockerViewModel,
    onTriggerPhonePermission: () -> Unit
) {
    item {
        val inputPhone by viewModel.inputPhoneNumber.collectAsStateWithLifecycle()
        val extractedAreaCode by viewModel.extractedAreaCode.collectAsStateWithLifecycle()
        PhoneExtractionCard(
            inputPhone = inputPhone,
            extractedAreaCode = extractedAreaCode,
            onValueChange = { viewModel.updateInputPhoneNumber(it) },
            onAddClick = { viewModel.addAreaCode(it) }
        )
    }

    item {
        val context = LocalContext.current
        val userOwnNumber by viewModel.userOwnNumber.collectAsStateWithLifecycle()
        val blockedAreaCodes by viewModel.blockedAreaCodes.collectAsStateWithLifecycle()
        
        OwnNumberCard(
            userOwnNumber = userOwnNumber,
            onValueChange = { viewModel.setUserOwnNumber(it) },
            onAutoDetectClick = {
                val detected = viewModel.tryAutoDetectPhoneNumber(context)
                if (detected) {
                    Toast.makeText(context, "Successfully detected!", Toast.LENGTH_SHORT).show()
                } else {
                    onTriggerPhonePermission()
                    if (viewModel.tryAutoDetectPhoneNumber(context)) {
                        Toast.makeText(context, "Successfully detected!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Could not auto-retrieve number.", Toast.LENGTH_LONG).show()
                    }
                }
            },
            ownAreaCode = viewModel.extractUserOwnAreaCode(),
            isAlreadyBlocked = blockedAreaCodes.any { it.areaCode == viewModel.extractUserOwnAreaCode() },
            onBlockOwnAreaClick = { viewModel.addAreaCode(it) }
        )
    }

    item {
        RegionalBlockCard(
            onBlockAreaCodes = { codes, label ->
                viewModel.addAreaCodes(codes, label)
            }
        )
    }

    item {
        ManualAreaCodeCard(
            onAddClick = { viewModel.addAreaCode(it) }
        )
    }

    item {
        val blockedKeywords by viewModel.blockedKeywords.collectAsStateWithLifecycle()
        SmsKeywordCard(
            blockedKeywords = blockedKeywords,
            onAddKeyword = { viewModel.addKeyword(it) },
            onRemoveKeyword = { viewModel.removeKeyword(it) }
        )
    }
}
