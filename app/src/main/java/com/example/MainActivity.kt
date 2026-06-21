package com.example

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.BlockedAreaCode
import com.example.data.entity.BlockedLog
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BlockerViewModel
import com.example.viewmodel.BlockerViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = applicationContext as BlockerApplication
            val viewModel: BlockerViewModel = viewModel(
                factory = BlockerViewModelFactory(app, app.repository)
            )
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()

            MyApplicationTheme(themeSetting = appTheme) {
                BlockerHomeScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockerHomeScreen(viewModel: BlockerViewModel) {
    val context = LocalContext.current

    // Permissions State
    var isContactsPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isSmsPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isPhoneNumbersPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_PHONE_NUMBERS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isCallScreeningRoleGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
            } else {
                true
            }
        )
    }

    // Launchers for permissions
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isContactsPermissionGranted = permissions[android.Manifest.permission.READ_CONTACTS] ?: isContactsPermissionGranted
        isSmsPermissionGranted = permissions[android.Manifest.permission.RECEIVE_SMS] ?: isSmsPermissionGranted
        isPhoneNumbersPermissionGranted = permissions[android.Manifest.permission.READ_PHONE_NUMBERS] ?: isPhoneNumbersPermissionGranted
    }

    val requestRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            isCallScreeningRoleGranted = roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
        }
    }

    fun triggerPermissionRequest() {
        requestPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.READ_PHONE_NUMBERS
            )
        )
    }

    fun triggerCallScreeningRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                    requestRoleLauncher.launch(intent)
                } else {
                    Toast.makeText(context, "Call Screening role is already active!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Call Screening role is not supported on this device.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var isNotificationListenerEnabled by remember {
        mutableStateOf(com.example.util.PhoneUtils.isNotificationServiceEnabled(context))
    }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isContactsPermissionGranted = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
                
                isSmsPermissionGranted = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.RECEIVE_SMS
                ) == PackageManager.PERMISSION_GRANTED
                
                isPhoneNumbersPermissionGranted = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_PHONE_NUMBERS
                ) == PackageManager.PERMISSION_GRANTED

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                    isCallScreeningRoleGranted = roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
                }
                
                isNotificationListenerEnabled = com.example.util.PhoneUtils.isNotificationServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // UI Data States
    val blockedAreaCodes by viewModel.blockedAreaCodes.collectAsStateWithLifecycle()
    val blockedLogs by viewModel.blockedLogs.collectAsStateWithLifecycle()
    val inputPhone by viewModel.inputPhoneNumber.collectAsStateWithLifecycle()
    val extractedAreaCode by viewModel.extractedAreaCode.collectAsStateWithLifecycle()
    val userOwnNumber by viewModel.userOwnNumber.collectAsStateWithLifecycle()

    LaunchedEffect(isPhoneNumbersPermissionGranted) {
        if (isPhoneNumbersPermissionGranted && userOwnNumber.isBlank()) {
            viewModel.tryAutoDetectPhoneNumber(context)
        }
    }

    var selectedTab by remember { mutableStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val hasAllRequiredGranted = isContactsPermissionGranted && 
                               isSmsPermissionGranted && 
                               isCallScreeningRoleGranted && 
                               isNotificationListenerEnabled

    if (!hasAllRequiredGranted) {
        com.example.ui.AppOnboardingScreen(
            isContactsGranted = isContactsPermissionGranted,
            isSmsGranted = isSmsPermissionGranted,
            isCallScreeningGranted = isCallScreeningRoleGranted,
            isNotificationListenerGranted = isNotificationListenerEnabled,
            onRequestContactsAndSms = { triggerPermissionRequest() },
            onRequestCallScreening = { triggerCallScreeningRequest() },
            onRequestNotificationListener = {
                try {
                    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    context.startActivity(intent)
                    Toast.makeText(context, "Locate 'Area Code Blocker' and toggle Notification Access", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Search 'Notification Access' in device settings.", Toast.LENGTH_LONG).show()
                }
            },
            onEnterApp = {
                Toast.makeText(context, "Welcome! Protection Active.", Toast.LENGTH_SHORT).show()
            }
        )
    } else {
        Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showSettingsDialog = true }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Access Settings",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Area Code Blocker",
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Premium Active Status badge from HTML design template
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Status Info Block - Styled like HTML Status Hero
            item {
                ProtectionStatusBanner(
                    isContactsGranted = isContactsPermissionGranted,
                    isSmsGranted = isSmsPermissionGranted,
                    isRoleGranted = isCallScreeningRoleGranted,
                    blockedAreaCodes = blockedAreaCodes,
                    onRemoveAreaCode = { area ->
                        viewModel.removeAreaCode(area)
                        Toast.makeText(context, "Removed $area", Toast.LENGTH_SHORT).show()
                    },
                    onRequestPermissions = { triggerPermissionRequest() },
                    onRequestRole = { triggerCallScreeningRequest() }
                )
            }

            // Statistics Grid directly below status block
            item {
                StatisticsGrid(
                    callsCount = blockedLogs.count { it.type == "CALL" },
                    textsCount = blockedLogs.count { it.type == "SMS" }
                )
            }

            // Tab navigation bar styled like Sophisticated Dark custom navigation footer
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF211F26),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = (0.5).dp, color = MaterialTheme.colorScheme.outlineVariant)
                        .height(72.dp),
                    indicator = {},
                    divider = {}
                ) {
                    val rulesSelected = selectedTab == 0
                    val rulesBg = if (rulesSelected) Color(0xFFE8DEF8) else Color.Transparent
                    val rulesContentColor = if (rulesSelected) Color(0xFF1D192B) else Color(0xFFCAC4D0)

                    Tab(
                        selected = rulesSelected,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.testTag("rules_tab").fillMaxHeight(),
                        selectedContentColor = rulesContentColor,
                        unselectedContentColor = Color(0xFFCAC4D0)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(rulesBg)
                                    .padding(horizontal = 20.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = rulesContentColor
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Shield & Setup",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (rulesSelected) Color(0xFFE5DDF5) else Color(0xFFCAC4D0)
                            )
                        }
                    }

                    val logsSelected = selectedTab == 1
                    val logsBg = if (logsSelected) Color(0xFFE8DEF8) else Color.Transparent
                    val logsContentColor = if (logsSelected) Color(0xFF1D192B) else Color(0xFFCAC4D0)

                    Tab(
                        selected = logsSelected,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.testTag("logs_tab").fillMaxHeight(),
                        selectedContentColor = logsContentColor,
                        unselectedContentColor = Color(0xFFCAC4D0)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(logsBg)
                                    .padding(horizontal = 20.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = logsContentColor
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Logs (${blockedLogs.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (logsSelected) Color(0xFFE5DDF5) else Color(0xFFCAC4D0)
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                // Extract Area Code Block
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Extract from Phone Number",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Input a phone number to extract its area code instantly to block calls/texts from that region.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = inputPhone,
                                    onValueChange = { viewModel.updateInputPhoneNumber(it) },
                                    label = { Text("Enter Phone Number") },
                                    placeholder = { Text("+1 (512) 555-0199") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = {
                                        keyboardController?.hide()
                                    }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("phone_input"),
                                    singleLine = true
                                )

                                if (extractedAreaCode.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "Extracted Area Code",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = extractedAreaCode,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.addAreaCode(extractedAreaCode)
                                                viewModel.updateInputPhoneNumber("")
                                                keyboardController?.hide()
                                                Toast.makeText(context, "Blocked Area Code: $extractedAreaCode", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.testTag("add_extracted_button")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Block")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Profiles Convenience Save Card
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Your Phone Number Link",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Optionally save your own number to quickly suggestions-block your own home area code.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = userOwnNumber,
                                        onValueChange = { viewModel.setUserOwnNumber(it) },
                                        placeholder = { Text("My Number") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = {
                                                    val detected = viewModel.tryAutoDetectPhoneNumber(context)
                                                    if (detected) {
                                                        Toast.makeText(context, "Successfully detected and auto-filled SIM telephone number!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        // Ensure permission is prompted
                                                        triggerPermissionRequest()
                                                        val retryDetected = viewModel.tryAutoDetectPhoneNumber(context)
                                                        if (retryDetected) {
                                                            Toast.makeText(context, "Successfully detected and auto-filled SIM telephone number!", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "Could not automatically retrieve number. Ensure SIM is active or input manually.", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Auto Fill Phone"
                                                )
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Phone,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(onDone = {
                                            keyboardController?.hide()
                                        }),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("own_number_input"),
                                        singleLine = true
                                    )

                                    val ownArea = viewModel.extractUserOwnAreaCode()
                                    if (ownArea.isNotBlank()) {
                                        val isAlreadyBlocked = blockedAreaCodes.any { it.areaCode == ownArea }
                                        Button(
                                            onClick = {
                                                viewModel.addAreaCode(ownArea)
                                                Toast.makeText(context, "Blocked your area code: $ownArea", Toast.LENGTH_SHORT).show()
                                            },
                                            enabled = !isAlreadyBlocked,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                            )
                                        ) {
                                            Text("Block $ownArea")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Custom Single Rule Entry Card
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Add Custom Area Code Prefix",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                var rawAreaInput by remember { mutableStateOf("") }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = rawAreaInput,
                                        onValueChange = { input ->
                                            if (input.all { it.isDigit() } && input.length <= 5) {
                                                rawAreaInput = input
                                            }
                                        },
                                        placeholder = { Text("e.g. 512, 212") },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(onDone = {
                                            keyboardController?.hide()
                                        }),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("manual_area_input"),
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            if (rawAreaInput.isNotBlank()) {
                                                viewModel.addAreaCode(rawAreaInput)
                                                Toast.makeText(context, "Added $rawAreaInput", Toast.LENGTH_SHORT).show()
                                                rawAreaInput = ""
                                                keyboardController?.hide()
                                            }
                                        },
                                        enabled = rawAreaInput.isNotBlank(),
                                        modifier = Modifier.testTag("add_manual_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add")
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Add")
                                    }
                                }
                            }
                        }
                    }
                }

                // Restricted area codes title text section
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Restricted Area Codes (${blockedAreaCodes.size})",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                if (blockedAreaCodes.isEmpty()) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No area codes registered.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = "Extract from a phone number or enter one above to start screening.",
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                blockedAreaCodes.forEach { rule ->
                                    ElevatedAssistChip(
                                        onClick = {},
                                        label = {
                                            Text(
                                                text = "Area Code ${rule.areaCode}",
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Delete code",
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable {
                                                        viewModel.removeAreaCode(rule.areaCode)
                                                        Toast.makeText(context, "Removed ${rule.areaCode}", Toast.LENGTH_SHORT).show()
                                                    }
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }



                // Text Message Keyword Filters Card (New Feature!)
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Blocked SMS Keywords",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Add specific words or phrases. Any incoming text message containing any of these keywords from non-contacts will be filtered.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                var rawKeywordInput by remember { mutableStateOf("") }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = rawKeywordInput,
                                        onValueChange = { rawKeywordInput = it },
                                        placeholder = { Text("e.g. lottery, crypto, claim") },
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(onDone = {
                                            keyboardController?.hide()
                                        }),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("keyword_input"),
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            if (rawKeywordInput.isNotBlank()) {
                                                viewModel.addKeyword(rawKeywordInput)
                                                Toast.makeText(context, "Added keyword: $rawKeywordInput", Toast.LENGTH_SHORT).show()
                                                rawKeywordInput = ""
                                                keyboardController?.hide()
                                            }
                                        },
                                        enabled = rawKeywordInput.isNotBlank(),
                                        modifier = Modifier.testTag("add_keyword_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add")
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Add")
                                    }
                                }

                                val blockedKeywords by viewModel.blockedKeywords.collectAsStateWithLifecycle()
                                if (blockedKeywords.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Active Keywords (${blockedKeywords.size})",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        blockedKeywords.forEach { rule ->
                                            ElevatedAssistChip(
                                                onClick = {},
                                                label = {
                                                    Text(
                                                        text = rule.keyword,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                },
                                                trailingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Delete keyword",
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                            .clickable {
                                                                viewModel.removeKeyword(rule.keyword)
                                                                Toast.makeText(context, "Removed keyword: ${rule.keyword}", Toast.LENGTH_SHORT).show()
                                                            }
                                                    )
                                                },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                ),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


            } else {
                // Block logs history header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filtered Operations Log",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (blockedLogs.isNotEmpty()) {
                            TextButton(
                                onClick = { viewModel.clearAllLogs() },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.testTag("purge_logs_button")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear Log")
                            }
                        }
                    }
                }

                if (blockedLogs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Screening Log Is Clean",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Any calls or messages that match your restricted area codes and are not in your contacts will appear here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(blockedLogs, key = { it.id }) { log ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            BlockedLogCard(
                                log = log,
                                onDelete = { viewModel.deleteLogById(log.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    SettingsDialog(
        show = showSettingsDialog,
        onDismiss = { showSettingsDialog = false },
        viewModel = viewModel
    )
    }
}

@Composable
fun StatisticsGrid(callsCount: Int, textsCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Calls Box
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "CALLS BLOCK LOG",
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$callsCount",
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
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "TEXTS FILTERED",
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$textsCount",
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
    isSmsGranted: Boolean,
    isRoleGranted: Boolean,
    blockedAreaCodes: List<BlockedAreaCode>,
    onRemoveAreaCode: (String) -> Unit,
    onRequestPermissions: () -> Unit,
    onRequestRole: () -> Unit
) {
    val allActive = isContactsGranted && isSmsGranted && isRoleGranted
    val statusDesc = if (allActive) {
        "Your device will screen calls and messages from blocked area codes, cross-checking contacts automatically."
    } else if (isRoleGranted) {
        "Calls from blocked area codes are screened, but SMS & Contacts check require system permissions."
    } else {
        "Activate Call Screening role below to enable automated ring call blocking."
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
            // Label
            Text(
                text = "PROTECTED COVERAGE",
                color = MaterialTheme.colorScheme.outline,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Dynamic Counter
            Text(
                text = "${blockedAreaCodes.size} Active",
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
                    text = if (isRoleGranted) "ACTIVE SCREENING" else "SHIELD OFFLINE",
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
                    Text("Activate Call Screening", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            } else if (!isContactsGranted || !isSmsGranted) {
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
                    Text("Grant System Permissions", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (blockedAreaCodes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                
                // Flow list of active area codes directly inside status hero (like HTML template)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Top
                ) {
                    blockedAreaCodes.forEach { rule ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    shape = CircleShape
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Area ${rule.areaCode}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete code",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            onRemoveAreaCode(rule.areaCode)
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionItemRow(
    title: String,
    desc: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 28.dp, top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (!isGranted) {
            Button(
                onClick = onRequest,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("Authorize", fontSize = 12.sp)
            }
        } else {
            Surface(
                color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Granted",
                    fontSize = 11.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun BlockedLogCard(
    log: BlockedLog,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }
    val timeStr = remember(log.timestamp) { sdf.format(Date(log.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("log_card_${log.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (log.type == "CALL") Icons.Default.Phone else Icons.Default.Send,
                            contentDescription = log.type,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = log.phoneNumber,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Area ${log.areaCode} • ${log.type}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Small incident log indicator dot from Sophisticated Dark design
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete log",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (!log.messageBody.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = log.messageBody,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                fontSize = 10.sp
            )
        }
    }
}

// FlowRow support backport for simple Compose layouts
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        
        var currentX = 0
        var currentY = 0
        var maxRowHeight = 0
        val placedData = mutableListOf<Triple<androidx.compose.ui.layout.Placeable, Int, Int>>()

        val hGap = 8.dp.roundToPx()
        val vGap = 8.dp.roundToPx()

        for (placeable in placeables) {
            if (currentX + placeable.width > layoutWidth) {
                currentX = 0
                currentY += maxRowHeight + vGap
                maxRowHeight = 0
            }
            placedData.add(Triple(placeable, currentX, currentY))
            currentX += placeable.width + hGap
            maxRowHeight = maxOf(maxRowHeight, placeable.height)
        }

        layout(
            width = layoutWidth,
            height = maxOf(0, currentY + maxRowHeight)
        ) {
            placedData.forEach { (placeable, x, y) ->
                placeable.placeRelative(x, y)
            }
        }
    }
}

@Composable
fun SettingsGroup(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    viewModel: BlockerViewModel
) {
    val context = LocalContext.current
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
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
                        icon = Icons.Default.Settings
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
                        icon = Icons.Default.Notifications
                    ) {
                        Text(
                            text = "On newer system versions, the default system messaging client receives and stores message notifications. Our background Notification Guard intercepts in real-time, silences, and clears spam message alerts matching your rules.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        var isNotificationSettingAllowed by remember {
                            mutableStateOf(com.example.util.PhoneUtils.isNotificationServiceEnabled(context))
                        }
                        val isListenerCurrentlyActive = com.example.service.SmsNotificationListenerService.isServiceRunning

                        LaunchedEffect(show) {
                            isNotificationSettingAllowed = com.example.util.PhoneUtils.isNotificationServiceEnabled(context)
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
                                    } catch (e: Exception) {
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
