package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entity.BlockedAreaCode
import com.example.data.entity.BlockedKeyword

internal fun activeRuleCount(
    blockedAreaCodes: List<BlockedAreaCode>,
    blockedKeywords: List<BlockedKeyword>,
): Int {
    val regionalRules = blockedAreaCodes.mapNotNull { it.regionLabel }.distinct().size
    val manualRules = blockedAreaCodes
        .filter { it.regionLabel == null }
        .distinctBy { it.areaCode }
        .size
    val keywordRules = blockedKeywords.distinctBy { it.keyword }.size
    return regionalRules + manualRules + keywordRules
}

@Composable
fun StatisticsGrid(
    callsCount: Int,
    textsCount: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("statistics_grid"),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Calls Box
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.calls_block_log),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = callsCount.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Texts Box
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.texts_filtered),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = textsCount.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ProtectionStatusBanner(
    isContactsGranted: Boolean,
    isRoleGranted: Boolean,
    blockedAreaCodes: List<BlockedAreaCode>,
    blockedKeywords: List<BlockedKeyword>,
    onRemoveAreaCode: (String) -> Unit,
    onRemoveRegion: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    onRequestPermissions: () -> Unit,
    onRequestRole: () -> Unit,
) {
    val allActive = isContactsGranted && isRoleGranted
    val statusDesc = if (allActive) {
        stringResource(R.string.protection_status_active)
    } else if (isRoleGranted) {
        stringResource(R.string.protection_status_contacts_needed)
    } else {
        stringResource(R.string.protection_status_role_needed)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Dynamic Counter
            val totalActiveRules = activeRuleCount(blockedAreaCodes, blockedKeywords)
            
            Text(
                text = pluralStringResource(R.plurals.active_rules, totalActiveRules, totalActiveRules),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 44.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-1).sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Current status tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isRoleGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRoleGranted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRoleGranted) stringResource(R.string.active_screening) else stringResource(R.string.shield_offline),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRoleGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle Description
            Text(
                text = statusDesc,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            if (!isRoleGranted) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRequestRole,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.activate_call_screening), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            } else if (!isContactsGranted) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRequestPermissions,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.grant_system_permissions), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (blockedAreaCodes.isNotEmpty() || blockedKeywords.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Top,
                ) {
                    val groupedAreaCodes = blockedAreaCodes.groupBy {
                        it.regionLabel ?: "manual_${it.areaCode}"
                    }

                    groupedAreaCodes.forEach { (_, codes) ->
                        val firstCode = codes.first()
                        val isRegional = firstCode.regionLabel != null
                        val displayText = firstCode.regionLabel ?: firstCode.areaCode

                        RuleChip(
                            label = displayText,
                            contentDescription = stringResource(R.string.delete_code),
                            onRemove = {
                                if (isRegional) {
                                    onRemoveRegion(requireNotNull(firstCode.regionLabel))
                                } else {
                                    onRemoveAreaCode(firstCode.areaCode)
                                }
                            },
                        )
                    }

                    blockedKeywords.distinctBy { it.keyword }.forEach { blockedKeyword ->
                        RuleChip(
                            label = blockedKeyword.keyword,
                            contentDescription = stringResource(
                                R.string.delete_keyword_named,
                                blockedKeyword.keyword,
                            ),
                            onRemove = { onRemoveKeyword(blockedKeyword.keyword) },
                            isKeyword = true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleChip(
    label: String,
    contentDescription: String,
    onRemove: () -> Unit,
    isKeyword: Boolean = false,
) {
    val containerColor = if (isKeyword) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    }
    val contentColor = if (isKeyword) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(CircleShape)
            .background(containerColor)
            .border(
                width = 1.dp,
                color = contentColor.copy(alpha = 0.25f),
                shape = CircleShape,
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onRemove),
            )
        }
    }
}
