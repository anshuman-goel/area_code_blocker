package com.example.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.viewmodel.BlockerViewModel
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BlockerScreen(
    viewModel: BlockerViewModel,
    initContactsGranted: Boolean? = null,
    initPhoneNumbersGranted: Boolean? = null,
    initCallScreeningGranted: Boolean? = null,
    initNotificationListenerGranted: Boolean? = null,
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(
        initContacts = initContactsGranted,
        initPhone = initPhoneNumbersGranted,
        initRole = initCallScreeningGranted,
        initNotif = initNotificationListenerGranted,
    )

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                permissionState.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // UI Data States
    val blockedAreaCodes by viewModel.blockedAreaCodes.collectAsStateWithLifecycle()
    val blockedLogs by viewModel.blockedLogs.collectAsStateWithLifecycle()
    val userOwnNumber by viewModel.userOwnNumber.collectAsStateWithLifecycle()

    LaunchedEffect(permissionState.isPhoneGranted) {
        if (permissionState.isPhoneGranted && userOwnNumber.isBlank()) {
            viewModel.tryAutoDetectPhoneNumber(context)
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(value = false) }
    
    if (!permissionState.hasAllRequired) {
        AppOnboardingScreen(
            isContactsGranted = permissionState.isContactsGranted,
            isPhoneNumbersGranted = permissionState.isPhoneGranted,
            isCallScreeningGranted = permissionState.isRoleGranted,
            isNotificationListenerGranted = permissionState.isNotificationEnabled,
            onRequestContacts = { permissionState.requestContacts() },
            onRequestPhoneIdentity = { permissionState.requestPhone() },
            onRequestCallScreening = { permissionState.requestRole() },
            onRequestNotificationListener = {
                try {
                    context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                    Toast.makeText(context, "Locate 'Area Code Blocker' and toggle Access", Toast.LENGTH_LONG).show()
                } catch (_: Exception) {
                    Toast.makeText(context, "Search 'Notification Access' in settings.", Toast.LENGTH_LONG).show()
                }
            },
        ) {
            Toast.makeText(context, "Welcome! Protection Active.", Toast.LENGTH_SHORT).show()
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AppTopBar { showSettingsDialog = true }
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .imePadding()
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("main_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
            ) {
                item {
                    ProtectionStatusBanner(
                        isContactsGranted = permissionState.isContactsGranted,
                        isRoleGranted = permissionState.isRoleGranted,
                        blockedAreaCodes = blockedAreaCodes,
                        onRemoveAreaCode = { area ->
                            viewModel.removeAreaCode(area)
                            Toast.makeText(context, "Removed $area", Toast.LENGTH_SHORT).show()
                        },
                        onRemoveRegion = { label ->
                            viewModel.removeAreaCodesByRegion(label)
                            Toast.makeText(context, "Removed $label", Toast.LENGTH_SHORT).show()
                        },
                        onRequestPermissions = { permissionState.requestContacts() },
                        onRequestRole = { permissionState.requestRole() },
                    )
                }

                item {
                    StatisticsGrid(
                        callsCount = blockedLogs.count { it.type == "CALL" },
                        textsCount = blockedLogs.count { it.type.startsWith("SMS") },
                    )
                }

                item {
                    TabNavigation(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        logsCount = blockedLogs.size,
                    )
                }

                if (selectedTab == 0) {
                    rulesTab(
                        viewModel = viewModel,
                        onTriggerPhonePermission = { permissionState.requestPhone() },
                    )
                } else {
                    logsTab(
                        blockedLogs = blockedLogs,
                        onClearAll = { viewModel.clearAllLogs() },
                        onDeleteLog = { viewModel.deleteLogById(it) },
                    )
                }
            }
        }
    }

    SettingsDialog(
        show = showSettingsDialog,
        onDismiss = { showSettingsDialog = false },
        viewModel = viewModel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(onSettingsClick: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onSettingsClick() }.padding(4.dp),
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Open settings", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Area Code Blocker", fontWeight = FontWeight.Bold, fontSize = 19.sp, letterSpacing = (-0.5).sp)
                }
                ActiveBadge()
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
fun ActiveBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape).padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = "ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.2.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
    }
}

@Composable
fun TabNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit, logsCount: Int) {
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color(0xFF211F26),
        modifier = Modifier.fillMaxWidth().border(width = (0.5).dp, color = MaterialTheme.colorScheme.outlineVariant).height(72.dp),
        indicator = {},
        divider = {}
    ) {
        TabItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = Icons.Default.Settings,
            label = "Shield & Setup",
            tag = "rules_tab"
        )
        TabItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = Icons.AutoMirrored.Filled.List,
            label = "Logs ($logsCount)",
            tag = "logs_tab"
        )
    }
}

@Composable
fun TabItem(selected: Boolean, onClick: () -> Unit, icon: ImageVector, label: String, tag: String) {
    val bg = if (selected) Color(0xFFE8DEF8) else Color.Transparent
    val contentColor = if (selected) Color(0xFF1D192B) else Color(0xFFCAC4D0)
    
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.testTag(tag).fillMaxHeight(),
        selectedContentColor = contentColor,
        unselectedContentColor = Color(0xFFCAC4D0)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.clip(CircleShape).background(bg).padding(horizontal = 20.dp, vertical = 6.dp)) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = contentColor)
            }
            Spacer(Modifier.height(4.dp))
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (selected) Color(0xFFE5DDF5) else Color(0xFFCAC4D0))
        }
    }
}
