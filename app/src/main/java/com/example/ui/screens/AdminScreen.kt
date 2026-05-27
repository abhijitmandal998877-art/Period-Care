package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    registeredUsers: List<User>,
    onBack: () -> Unit,
    onSendNotification: (String?, String, String, (Boolean) -> Unit) -> Unit
) {
    var notifTitle by remember { mutableStateOf("") }
    var notifMessage by remember { mutableStateOf("") }
    var targetEmailInput by remember { mutableStateOf("") }
    var sendToAll by remember { mutableStateOf(true) }

    var feedbackMsg by remember { mutableStateOf<String?>(null) }
    var isErrorResult by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Power Admin Console", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Stats
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "people",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Total Platform Registrations",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                "${registeredUsers.size} Users Registered",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("admin_total_users_count")
                            )
                        }
                    }
                }
            }

            // Create custom notifications form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "broadcast",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Broadcast Custom Notification / Tip-alert",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Send targets toggles
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = sendToAll,
                                onClick = { sendToAll = true },
                                modifier = Modifier.testTag("radio_send_to_all")
                            )
                            Text(
                                "All Users (Global announcement)",
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { sendToAll = true },
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = !sendToAll,
                                onClick = { sendToAll = false },
                                modifier = Modifier.testTag("radio_send_to_specific")
                            )
                            Text(
                                "Specific User via Registered Email",
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { sendToAll = false },
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Inputs
                        if (!sendToAll) {
                            OutlinedTextField(
                                value = targetEmailInput,
                                onValueChange = { targetEmailInput = it },
                                label = { Text("Target User Email Address") },
                                modifier = Modifier.fillMaxWidth().testTag("target_email_input_field"),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = notifTitle,
                            onValueChange = { notifTitle = it },
                            label = { Text("Alert Title / Header") },
                            modifier = Modifier.fillMaxWidth().testTag("notif_title_input_field"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = notifMessage,
                            onValueChange = { notifMessage = it },
                            label = { Text("Advice message or alert description details...") },
                            modifier = Modifier.fillMaxWidth().testTag("notif_message_input_field")
                        )

                        // Transmit trigger button
                        Button(
                            onClick = {
                                if (notifTitle.isBlank() || notifMessage.isBlank()) {
                                    feedbackMsg = "Validation Failure: Title and message cannot be blank!"
                                    isErrorResult = true
                                    return@Button
                                }
                                val target = if (sendToAll) null else targetEmailInput
                                onSendNotification(target, notifTitle, notifMessage) { success ->
                                    if (success) {
                                        feedbackMsg = "SUCCESS: Notification transmitted perfectly!"
                                        isErrorResult = false
                                        notifTitle = ""
                                        notifMessage = ""
                                        targetEmailInput = ""
                                    } else {
                                        feedbackMsg = "FAILURE: Target user with details not found on local SQL database records."
                                        isErrorResult = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("admin_send_notif_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Transmit Notification", fontWeight = FontWeight.Bold)
                        }

                        feedbackMsg?.let { feedback ->
                            Text(
                                text = feedback,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isErrorResult) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                                modifier = Modifier.padding(top = 4.dp).testTag("admin_notif_feedback_text")
                            )
                        }
                    }
                }
            }

            // Database inspection list
            item {
                Text(
                    text = "Platform Audit User Records List",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (registeredUsers.isEmpty()) {
                item {
                    Text(
                        "No users have registered yet on this device's SQL database.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                items(registeredUsers) { userRec ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = userRec.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "ID: ${userRec.id}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Email: ${userRec.email}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Cycle: ${userRec.cycleLength}d", fontSize = 10.sp) }
                                )
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Period: ${userRec.periodLength}d", fontSize = 10.sp) }
                                )
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("AES Secure: " + if(userRec.isLocalEncryptionEnabled) "Yes" else "No", fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
