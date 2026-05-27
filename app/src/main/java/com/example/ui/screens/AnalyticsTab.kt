package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.DailyLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsTab(logs: List<DailyLog>, cycleLength: Int) {
    // Process logs
    val logsLast7Days = remember(logs) {
        logs.sortedBy { it.date }.takeLast(7)
    }

    val moodCounts = remember(logs) {
        val counts = mutableMapOf(
            "Happy" to 0,
            "Crampy" to 0,
            "Sad" to 0,
            "Anxious" to 0,
            "Calm" to 0,
            "Energetic" to 0,
            "Tired" to 0
        )
        logs.forEach { log ->
            if (counts.containsKey(log.mood)) {
                counts[log.mood] = counts[log.mood]!! + 1
            }
        }
        counts.filterValues { it > 0 }
    }

    // Colors mapping for symptoms
    val symptomSeverityMap = remember(logs) {
        val res = mutableMapOf("None" to 0, "Mild" to 0, "Moderate" to 0, "Severe" to 0)
        logs.forEach { log ->
            if (res.containsKey(log.symptomSeverity)) {
                res[log.symptomSeverity] = res[log.symptomSeverity]!! + 1
            }
        }
        res
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Cycles & Health Analytics", 
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // General Cycle Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "cycle stats",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Cycle Consistency Stats",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Cycle Length", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                Text("$cycleLength Days", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column {
                                Text("Tracked Logs", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                Text("${logs.size} Logs Entered", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Column {
                                Text("Cycle Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                Text(
                                    if (logs.any { it.symptomSeverity == "Severe" }) "High Symptoms" else "Consistent & Healthy",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (logs.any { it.symptomSeverity == "Severe" }) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            // Water Intake Progression (Past 7 Logs)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .testTag("hydration_analytics_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = "water drop",
                                tint = Color(0xFF29B6F6),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Water Intake (Last 7 Logs)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (logsLast7Days.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No water intake logged yet.\nSelect dates and log water targets in Dashboard!",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            val activePrimary = MaterialTheme.colorScheme.primary
                            val barColor = Color(0xFF29B6F6)
                            val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            val textLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(top = 16.dp)
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val maxVal = 3000f // 3 Liters max benchmark
                                val spacing = canvasWidth / 8f

                                // Draw baseline and 2000ml goal line
                                val goalY = canvasHeight - ((2000f / maxVal) * (canvasHeight - 40f)) - 20f
                                drawLine(
                                    color = activePrimary.copy(alpha = 0.4f),
                                    start = Offset(40f, goalY),
                                    end = Offset(canvasWidth, goalY),
                                    strokeWidth = 2f
                                )

                                logsLast7Days.forEachIndexed { idx, log ->
                                    val logDateClean = log.date.substringAfterLast("-") // Just day code e.g. "25"
                                    val x = (idx + 1) * spacing
                                    val barHeight = (log.waterIntakeMl.toFloat() / maxVal) * (canvasHeight - 40f)
                                    val y = canvasHeight - barHeight - 20f

                                    // Bar Drawing
                                    drawRoundRect(
                                        color = if (log.waterIntakeMl >= 2000) barColor else barColor.copy(alpha = 0.6f),
                                        topLeft = Offset(x - 12.dp.toPx(), y),
                                        size = Size(24.dp.toPx(), barHeight),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                logsLast7Days.forEach { log ->
                                    Text(
                                        text = log.date.substringAfterLast("-") + "d",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textLabelColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(barColor))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Logged Water", fontSize = 11.sp, color = textLabelColor)
                                }
                                Text("*Target Goal is 2,000ml", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Mood Ratios (Insights Pie/Arc Chart Representation)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .testTag("mood_distribution_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "moods scale",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Logged Mood Frequencies",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (moodCounts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No moods logged yet.\nCheck-in and record moods inside home tab!",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            val totalMoodSessions = moodCounts.values.sum().toFloat()
                            val moodColors = mapOf(
                                "Happy" to Color(0xFFFFD54F),
                                "Crampy" to Color(0xFFE57373),
                                "Sad" to Color(0xFF64B5F6),
                                "Anxious" to Color(0xFFFFB74D),
                                "Calm" to Color(0xFF81C784),
                                "Energetic" to Color(0xFF4DB6AC),
                                "Tired" to Color(0xFF90A4AE)
                            )

                            // Linear Stacked Distribution Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                moodCounts.forEach { (mood, count) ->
                                    val percent = count / totalMoodSessions
                                    val col = moodColors[mood] ?: Color.Gray
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(if (percent > 0f) percent else 0.01f)
                                            .background(col)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Legend and Percentages
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                moodCounts.forEach { (mood, count) ->
                                    val pct = (count / totalMoodSessions * 100).toInt()
                                    val col = moodColors[mood] ?: Color.Gray
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(col)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(mood, fontSize = 13.sp, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                                        Text("$pct% ($count days)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Symptom Progression Analysis Logs
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "symptom stats",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Symptom Severity Breakdown",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val totalSymptomLogs = logs.size
                        if (totalSymptomLogs == 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No physical symptoms logged yet.\nTrack daily cramps or discomfort milestones.",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            listOf("None", "Mild", "Moderate", "Severe").forEach { severity ->
                                val count = symptomSeverityMap[severity] ?: 0
                                val progressPercent = count.toFloat() / totalSymptomLogs.toFloat()
                                val progressColor = when (severity) {
                                    "Severe" -> MaterialTheme.colorScheme.error
                                    "Moderate" -> Color(0xFFFFB74D)
                                    "Mild" -> Color(0xFF64B5F6)
                                    else -> Color(0xFF81C784)
                                }

                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(severity, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("$count days", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { progressPercent },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = progressColor,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
