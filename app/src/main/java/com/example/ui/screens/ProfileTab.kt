package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(
    user: User,
    onLogout: () -> Unit,
    onChangePassword: (String, String, (Boolean, String) -> Unit) -> Unit,
    onUpdateCycleSettings: (Int, Int) -> Unit,
    onUpdateWaterGoal: (Int) -> Unit,
    onToggleEncryption: (Boolean) -> Unit,
    onToggleCloudSync: (Boolean) -> Unit,
    onNavigateToAdminPanel: () -> Unit
) {
    var showPwdDialog by remember { mutableStateOf(false) }
    var showAdminPassDialog by remember { mutableStateOf(false) }

    // local update states for settings slider/dialogs
    var showCycleDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Manage Account Settings", 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User short details card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User avatar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = user.email,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Period Settings subcategory
            Text(
                "Cycle Configuration",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Average Cycle Interval") },
                        supportingContent = { Text("Currently set to ${user.cycleLength} days") },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, "arrow")
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .clickable { showCycleDialog = true }
                            .testTag("cycle_length_listItem")
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Average Period Duration") },
                        supportingContent = { Text("Currently set to ${user.periodLength} days") },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            // Privacy & Options category
            Text(
                "Privacy, Security & Options",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("AES Room Storage Encryption") },
                        supportingContent = { Text("Encrypts symptoms, diaries & profile offline in SQL") },
                        trailingContent = {
                            Switch(
                                checked = user.isLocalEncryptionEnabled,
                                onCheckedChange = { onToggleEncryption(it) },
                                modifier = Modifier.testTag("encryption_toggle_switch")
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Secure Cloud Backup Sync") },
                        supportingContent = { Text("Automatically backs up log info when server is online") },
                        trailingContent = {
                            Switch(
                                checked = user.isCloudSyncEnabled,
                                onCheckedChange = { onToggleCloudSync(it) },
                                modifier = Modifier.testTag("cloud_sync_toggle_switch")
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            // Security actions
            Text(
                "Account Security",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Update Secret Password") },
                        supportingContent = { Text("Changes account login security key") },
                        trailingContent = { Icon(Icons.Default.LockOpen, "Lock icon") },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .clickable { showPwdDialog = true }
                            .testTag("change_password_listItem")
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Administrative Gate") },
                        supportingContent = { Text("Owner-only system metrics access") },
                        trailingContent = { Icon(Icons.Default.AdminPanelSettings, "Admin icon") },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .clickable { showAdminPassDialog = true }
                            .testTag("admin_gate_listItem")
                    )
                }
            }

            // Logout execution
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical = 4.dp)
                    .testTag("logout_button")
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = "exit")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out Session", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Interactive Update Cycle settings Dialog
    if (showCycleDialog) {
        var localCycleLen by remember { mutableFloatStateOf(user.cycleLength.toFloat()) }
        var localPeriodLen by remember { mutableFloatStateOf(user.periodLength.toFloat()) }

        AlertDialog(
            onDismissRequest = { showCycleDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateCycleSettings(localCycleLen.toInt(), localPeriodLen.toInt())
                        showCycleDialog = false
                    },
                    modifier = Modifier.testTag("cycle_dialog_save")
                ) {
                    Text("Save Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCycleDialog = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text("Update Cycle Standards", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Average cycle length: ${localCycleLen.toInt()} days", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = localCycleLen,
                        onValueChange = { localCycleLen = it },
                        valueRange = 21f..35f,
                        steps = 14,
                        modifier = Modifier.testTag("cycle_length_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Average menstrual duration: ${localPeriodLen.toInt()} days", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = localPeriodLen,
                        onValueChange = { localPeriodLen = it },
                        valueRange = 3f..10f,
                        steps = 7,
                        modifier = Modifier.testTag("period_length_slider")
                    )
                }
            }
        )
    }

    // Password Alteration Dialog
    if (showPwdDialog) {
        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var messageResult by remember { mutableStateOf<String?>(null) }
        var isSuccess by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPwdDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        onChangePassword(oldPassword, newPassword) { success, msg ->
                            isSuccess = success
                            messageResult = msg
                            if (success) {
                                oldPassword = ""
                                newPassword = ""
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_change_password")
                ) {
                    Text("Update Key")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPwdDialog = false
                        messageResult = null
                    },
                    modifier = Modifier.testTag("dismiss_password_dialog")
                ) {
                    Text("Dismiss")
                }
            },
            title = {
                Text("Update Password", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("old_password_field")
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Secure Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("new_password_field")
                    )

                    messageResult?.let { msg ->
                        Text(
                            text = msg,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp).testTag("password_change_result_text")
                        )
                    }
                }
            }
        )
    }

    // Owner Access Admin Gate Passcode Verification Dialog
    if (showAdminPassDialog) {
        var adminPasswordInput by remember { mutableStateOf("") }
        var passError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAdminPassDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminPasswordInput == "924250") {
                            showAdminPassDialog = false
                            onNavigateToAdminPanel()
                        } else {
                            passError = true
                        }
                    },
                    modifier = Modifier.testTag("confirm_admin_passcode")
                ) {
                    Text("Unlock Panel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminPassDialog = false }) {
                    Text("Close")
                }
            },
            title = {
                Text("Administrator Secure Entrance", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Please enter the 6-digit administrative passcode below to audit statistics and transmit global tips.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    OutlinedTextField(
                        value = adminPasswordInput,
                        onValueChange = { 
                            if (it.length <= 6) {
                                adminPasswordInput = it 
                                passError = false
                            }
                        },
                        label = { Text("Administrative Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("admin_passcode_input_field")
                    )

                    if (passError) {
                        Text(
                            "ACCESS DENIED: Incorrect Admin Passcode!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("admin_passcode_error")
                        )
                    }
                }
            }
        )
    }
}
