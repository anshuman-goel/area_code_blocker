package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.SmsNotificationListenerService
import com.example.util.PhoneUtils
import com.example.viewmodel.BlockerViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsGroup(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    viewModel: BlockerViewModel,
) {
    val context = LocalContext.current
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
            ),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .testTag("settings_dialog_surface"),
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close settings",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SettingsGroup(
                        title = "App Appearance",
                        icon = Icons.Default.Settings,
                    ) {
                        Text(
                            text = "Select your preferred visual style. Choose light mode for daytime clarity, dark mode for eye relaxation, or synchronize with your device system configurations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val currentThemeChoice by viewModel.appTheme.collectAsStateWithLifecycle()
                        val themes = listOf("System", "Light", "Dark")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            themes.forEach { theme ->
                                val isSelected = currentThemeChoice == theme
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            viewModel.setAppTheme(theme)
                                        }
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (theme) {
                                            "System" -> "System"
                                            else -> theme
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    SettingsGroup(
                        title = "SMS Notification Guard (Modern System Setup)",
                        icon = Icons.Default.Notifications,
                    ) {
                        Text(
                            text = "On newer system versions, the default system messaging client receives and stores message notifications. Our background Notification Guard intercepts in real-time, silences, and clears spam message alerts matching your rules.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        var isNotificationSettingAllowed by remember {
                            mutableStateOf(PhoneUtils.isNotificationServiceEnabled(context))
                        }
                        val isListenerCurrentlyActive = SmsNotificationListenerService.isServiceRunning

                        LaunchedEffect(show) {
                            isNotificationSettingAllowed = PhoneUtils.isNotificationServiceEnabled(context)
                        }

                        // State Row 1: System Permission Status
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "System Access Status",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isNotificationSettingAllowed) "Granted in system preferences" else "Awaiting system approval",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                        context.startActivity(intent)
                                        Toast.makeText(context, "Locate 'Area Code Blocker' and toggle Notification Access", Toast.LENGTH_LONG).show()
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Could not open settings automatically. Search 'Notification Access' in device settings.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isNotificationSettingAllowed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                    contentColor = if (isNotificationSettingAllowed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = if (isNotificationSettingAllowed) "Authorized" else "Grant",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // State Row 2: Deep Binder Connection (Live Status)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (isListenerCurrentlyActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isListenerCurrentlyActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isListenerCurrentlyActive) "Shield Active & Guarding" else "Shield Suspended",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isListenerCurrentlyActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = if (isListenerCurrentlyActive) "Currently listening for and dismissing text spam alerts." else "Authorize system settings, or cycle setting Off/On to force activation.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    SettingsGroup(
                        title = "Data Sources & Attributions",
                        icon = Icons.Default.Info,
                    ) {
                        Text(
                            text = "Area code and regional data is sourced from Wikipedia and is licensed under the Creative Commons Attribution-ShareAlike License.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        
                        val intent1 = remember { Intent(Intent.ACTION_VIEW, "https://en.wikipedia.org/wiki/List_of_North_American_Numbering_Plan_area_codes".toUri()) }
                        val intent2 = remember { Intent(Intent.ACTION_VIEW, "https://en.wikipedia.org/wiki/North_American_Numbering_Plan#Countries_and_territories".toUri()) }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = {
                                    try {
                                        context.startActivity(intent1)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "No web browser found to open link.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Area Code Master List", fontSize = 12.sp)
                            }
                            TextButton(
                                onClick = {
                                    try {
                                        context.startActivity(intent2)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "No web browser found to open link.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Countries & Territories", fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }
        )
    }
}
