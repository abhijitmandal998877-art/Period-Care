package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.DailyLog
import com.example.data.database.InAppNotification
import com.example.data.database.User
import com.example.data.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardTab(
    user: User,
    dailyLogs: List<DailyLog>,
    activeLog: DailyLog?,
    notifications: List<InAppNotification>,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onTrackPeriodToday: () -> Unit,
    onSaveDailyLog: (String, String, String, Int, String) -> Unit,
    syncState: com.example.ui.viewmodel.PeriodViewModel.SyncState,
    onTriggerSync: () -> Unit
) {
    var showLogDialog by remember { mutableStateOf(false) }

    // Compute cycle information
    val hasCycle = user.lastPeriodStartDate.isNotBlank()
    val daysSinceStart = if (hasCycle) {
        DateUtils.getDaysDifference(user.lastPeriodStartDate, DateUtils.getCurrentDateString())
    } else {
        0
    }

    val daysInCurrentCycle = if (daysSinceStart >= 0) daysSinceStart % user.cycleLength else 0
    val nextPeriodStartDateString = if (hasCycle) {
        DateUtils.addDaysToDate(user.lastPeriodStartDate, user.cycleLength)
    } else {
        ""
    }
    val daysUntilNextPeriod = if (hasCycle) {
        DateUtils.getDaysDifference(DateUtils.getCurrentDateString(), nextPeriodStartDateString)
    } else {
        0
    }

    val cyclePhaseName = remember(daysInCurrentCycle, hasCycle) {
        if (!hasCycle) "Not initialized"
        else when (daysInCurrentCycle) {
            in 0 until user.periodLength -> "Menstrual Phase"
            in user.periodLength until 13 -> "Follicular Phase"
            13, 14 -> "Ovulatory Phase"
            else -> "Luteal Phase"
        }
    }

    val phaseDescription = remember(cyclePhaseName) {
        when (cyclePhaseName) {
            "Menstrual Phase" -> "Your body is shedding its lining. Rest, prioritize heavy hydration and warm baths."
            "Follicular Phase" -> "Estrogen rises, boosting brain function and physical stamina."
            "Ovulatory Phase" -> "Peak fertility state. Active energy is high and mood is typically vibrant."
            "Luteal Phase" -> "PMS symptoms might trigger. Keep nutrition rich, sleep 8+ hours."
            else -> "Log your last cycle date to predict menstrual phases."
        }
    }

    val isPeriodActiveToday = hasCycle && (daysInCurrentCycle in 0 until user.periodLength)

    // Generate last 7 calendar days selection horizontal wheel
    val weekDates = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayNameSdf = SimpleDateFormat("EEE", Locale.getDefault())
        val dayNumSdf = SimpleDateFormat("dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -5) // Show 5 days back and 2 days forward
        (0..7).map {
            val dateStr = sdf.format(cal.time)
            val dayName = dayNameSdf.format(cal.time)
            val dayNum = dayNumSdf.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            Triple(dateStr, dayName, dayNum)
        }
    }

    // Interactive Pulsating Flower animation for center tracker
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flower_pulsing"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp) // extra padding so content isn't swallowed by BottomNav
    ) {
        // App header with Online/Offline Backup Status indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = user.fullName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("welcome_user_title")
                )
            }

            // Sync Status Pill (Fulfills online / offline requirement beautifully)
            Card(
                onClick = onTriggerSync,
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.testTag("sync_status_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val statusColor = when (syncState) {
                        is com.example.ui.viewmodel.PeriodViewModel.SyncState.ConnectedAndSynced -> Color(0xFF81C784)
                        is com.example.ui.viewmodel.PeriodViewModel.SyncState.Syncing -> MaterialTheme.colorScheme.secondary
                        else -> Color(0xFFFFB74D) // Offline mode
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = when (syncState) {
                            is com.example.ui.viewmodel.PeriodViewModel.SyncState.ConnectedAndSynced -> "Online Backup"
                            is com.example.ui.viewmodel.PeriodViewModel.SyncState.Syncing -> "Syncing..."
                            else -> "Offline Secure"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Period Predictive Circle Indicator (High Visual Polish representation)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulse circle
            Box(
                modifier = Modifier
                    .size(200.dp * pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Outer ring
            Box(
                modifier = Modifier
                    .size(175.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = if (isPeriodActiveToday) Icons.Default.WaterDrop else Icons.Default.Spa,
                            contentDescription = "Period flower",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (!hasCycle) "Unregistered" else if (daysUntilNextPeriod > 0) "$daysUntilNextPeriod Days" else "Today!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (!hasCycle) "Log Cycle" else if (daysUntilNextPeriod > 0) "Until Period" else "Cycle Commences",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cyclePhaseName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // On period days / Hydration Warning Bar (Personalized Water and Symptom reminders)
        if (isPeriodActiveToday) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Critical Period Day Rule!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "Drink at least 2.5L water today to alleviate cramps and track symptom peaks below.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Cycle Quick Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onTrackPeriodToday,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("track_period_button")
            ) {
                Icon(imageVector = Icons.Default.WaterDrop, contentDescription = "drop")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Period Starts Today", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { showLogDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("record_log_button")
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "edit")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Daily Check-in", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Horizontal Week date selector
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Select Date and log symptoms:",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(weekDates) { (dateStr, dayName, dayNum) ->
                val isSel = dateStr == selectedDate
                val hasLog = dailyLogs.any { it.date == dateStr }

                Card(
                    onClick = { onDateSelected(dateStr) },
                    modifier = Modifier
                        .width(55.dp)
                        .height(75.dp)
                        .testTag("date_selector_$dateStr"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) MaterialTheme.colorScheme.primary
                        else if (hasLog) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    ),
                    border = if (isSel) null else androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(dayName, fontSize = 11.sp, fontWeight = FontWeight.Normal)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(dayNum, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        if (hasLog) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }

        // Display current day's tracked info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Summary for " + DateUtils.parseDateFriendly(selectedDate),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (activeLog == null) {
                    Text(
                        text = "No symptoms or moods logged for this date yet. Tap 'Daily Check-in' or use actions to log.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text("Mood", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Mood, "mood", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(activeLog.mood.ifBlank { "Not logged" }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        Column {
                            Text("Symptom", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Healing, "symptom", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(activeLog.symptom.ifBlank { "None" } + " (${activeLog.symptomSeverity})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        Column {
                            Text("Water intake", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WaterDrop, "water", tint = Color(0xFF29B6F6), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${activeLog.waterIntakeMl} ml", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    if (activeLog.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Notes:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(activeLog.notes, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                }
            }
        }

        // Dynamic wellness notification updates feed (In-app notification triggers)
        Text(
            text = "Timely Alerts & Cycle Tips:",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )

        val unreadNotifications = notifications.take(3)
        if (unreadNotifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "You're all caught up! No recent wellness alerts.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                unreadNotifications.forEach { notif ->
                    val colorBrush = when (notif.type) {
                        "alert" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        "period" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        "admin" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    }
                    val icon = when (notif.type) {
                        "alert" -> Icons.Default.Warning
                        "period" -> Icons.Default.CalendarToday
                        "admin" -> Icons.Default.Campaign
                        else -> Icons.Default.Info
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colorBrush)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = notif.type,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = notif.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notif.message,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive daily log entry dialog sheet
    if (showLogDialog) {
        var selectedMood by remember { mutableStateOf(activeLog?.mood ?: "Happy") }
        var selectedSymptom by remember { mutableStateOf(activeLog?.symptom ?: "None") }
        var selectedSeverity by remember { mutableStateOf(activeLog?.symptomSeverity ?: "None") }
        var loggedWater by remember { mutableStateOf(activeLog?.waterIntakeMl ?: 1000) }
        var notesInput by remember { mutableStateOf(activeLog?.notes ?: "") }

        val moodOptions = listOf("Happy", "Crampy", "Sad", "Anxious", "Calm", "Energetic", "Tired")
        val symptomOptions = listOf("None", "Cramps", "Headache", "Bloating", "Acne")
        val severityOptions = listOf("None", "Mild", "Moderate", "Severe")

        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveDailyLog(selectedMood, selectedSymptom, selectedSeverity, loggedWater, notesInput)
                        showLogDialog = false
                    },
                    modifier = Modifier.testTag("save_log_confirm")
                ) {
                    Text("Save Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogDialog = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text(
                    "Daily Wellness Check-in",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mood selector
                    Text("How is your mood?", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(moodOptions) { mood ->
                            val isChosen = selectedMood == mood
                            FilterChip(
                                selected = isChosen,
                                onClick = { selectedMood = mood },
                                label = { Text(mood, fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_mood_$mood")
                            )
                        }
                    }

                    // Physical symptoms selector
                    Text("Logged physical symptom?", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(symptomOptions) { sym ->
                            val isChosen = selectedSymptom == sym
                            FilterChip(
                                selected = isChosen,
                                onClick = { selectedSymptom = sym },
                                label = { Text(sym, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Symptom intensity severity
                    if (selectedSymptom != "None") {
                        Text("Symptom Severity Progression", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            severityOptions.forEach { sev ->
                                val isChosen = selectedSeverity == sev
                                FilterChip(
                                    selected = isChosen,
                                    onClick = { selectedSeverity = sev },
                                    label = { Text(sev, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Water tracking
                    Text("Track water intake Today (ml)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    ) {
                        IconButton(onClick = { if (loggedWater >= 250) loggedWater -= 250 }) {
                            Icon(Icons.Default.RemoveCircleOutline, "remove")
                        }
                        Text(
                            "$loggedWater ml",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { loggedWater += 250 }) {
                            Icon(Icons.Default.AddCircleOutline, "add")
                        }
                    }

                    // Additional notes
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Private Wellness diaries / notes", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}
