package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class YogaPose(
    val title: String,
    val description: String,
    val steps: List<String>,
    val duration: String,
    val color: Color
)

data class HealthTip(
    val title: String,
    val content: String,
    val category: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YogaTipsTab() {
    val context = LocalContext.current
    var selectedPose by remember { mutableStateOf<YogaPose?>(null) }

    val poses = remember {
        listOf(
            YogaPose(
                title = "Child's Pose (Balasana)",
                description = "Gently stretches the lower back, relieving pelvic cramping and calming the nervous system.",
                steps = listOf(
                    "Kneel on the floor, touch your big toes together and sit on your heels.",
                    "Separate your knees about as wide as your hips.",
                    "Exhale and lay your torso down between your thighs, stretching your arms forward.",
                    "Rest your forehead on the floor and breathe deeply for 2-3 minutes."
                ),
                duration = "3-5 mins",
                color = Color(0xFFFF8A80)
            ),
            YogaPose(
                title = "Reclined Goddess (Supta Baddha Konasana)",
                description = "Opens the hips and pelvic region, promoting blood flow and reducing intense uterine spasms.",
                steps = listOf(
                    "Lie flat on your back on a comfortable mat or blanket.",
                    "Bend your knees and bring the soles of your feet together, letting your knees fall open to the sides.",
                    "Place one hand on your heart and the other on your lower belly.",
                    "Close your eyes and focus on slow, gentle diaphragmatic breaths."
                ),
                duration = "5-10 mins",
                color = Color(0xFFFFB74D)
            ),
            YogaPose(
                title = "Legs-Up-The-Wall (Viparita Karani)",
                description = "Implements passive inversion to relieve leg-swelling, lower back throb, and uterine congestion.",
                steps = listOf(
                    "Sit with your side against a wall, then gently turn and swing your vertical legs up the wall as you lie back.",
                    "Rest your shoulders and head flat on your mat.",
                    "Rest your arms out to the sides, palms facing upwards.",
                    "Allow gravity to filter circulation back to your pelvis. Hold for deep tranquility."
                ),
                duration = "10 mins",
                color = Color(0xFFBA68C8)
            ),
            YogaPose(
                title = "Cat-Cow Pose (Marjaryasana)",
                description = "Massages the abdominal organs and spine, relieving severe lumbar menstrual cramps.",
                steps = listOf(
                    "Start on your hands and knees in a tabletop position.",
                    "Inhale, arch your back down, lift your chin and chest to the sky (Cow path).",
                    "Exhale, round your spine up, tucking your chin to your collar (Cat path).",
                    "Repeat slowly in synch with your deep breathing cycles."
                ),
                duration = "3 mins",
                color = Color(0xFFF06292)
            )
        )
    }

    val healthTips = remember {
        listOf(
            HealthTip(
                title = "Therapeutic Heat Relief",
                content = "Applying a heating pad or warm patch to your lower abdomen relaxes uterine muscles, vastly reducing cramp pressure similarly to pain medication.",
                category = "Cramp Relief",
                icon = Icons.Default.Healing
            ),
            HealthTip(
                title = "Magnesium Rich Foods",
                content = "Eating foods rich in magnesium (like dark chocolate, bananas, spinach, and almonds) acts as a natural muscle relaxant and improves PMS moods.",
                category = "Nutrition",
                icon = Icons.Default.Spa
            ),
            HealthTip(
                title = "Gentle Hydration Boost",
                content = "Warm chamomile or peppermint teas can calm spasms, reduce digestive gas, and keep your body hydrated during blood loss days.",
                category = "Hydration",
                icon = Icons.Default.Spa
            ),
            HealthTip(
                title = "The Power of Rest",
                content = "Progesterone drops naturally during the luteal phase, inducing tiredness. Do not punish your body; aim for 8+ hours of uninterrupted sleep.",
                category = "Recovery",
                icon = Icons.Default.SelfImprovement
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Wellness & Care", 
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
            item {
                Text(
                    text = "Menstrual Pain Relief Yoga",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "Soft restorative poses to bring comfort, release cramped pelvic muscles, and soothe pain.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(poses) { pose ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPose = pose }
                        .testTag("yoga_card_${pose.title.replace(" ", "_")}"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(pose.color, pose.color.copy(alpha = 0.6f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = "Yoga Icon",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pose.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = pose.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 2
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable {
                                    shareText(
                                        context,
                                        "Yoga Pose: ${pose.title}\nBenefit: ${pose.description}\nTry it for ${pose.duration}! Track your health with Period Care."
                                    )
                                }
                                .testTag("share_yoga_${pose.title.substringBefore(" ")}")
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Menstrual Health Tips",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "Doctor-reviewed tips for daily body care, recovery and healthy cycles.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(healthTips) { tip ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = tip.icon,
                                contentDescription = tip.title,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tip.category.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    shareText(
                                        context,
                                        "Menstrual Health Tip - [${tip.title}]: ${tip.content} Applet: Period Care."
                                    )
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share tip",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = tip.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = tip.content,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet or Dialog for Yoga Details
    selectedPose?.let { pose ->
        AlertDialog(
            onDismissRequest = { selectedPose = null },
            confirmButton = {
                Button(
                    onClick = { selectedPose = null },
                    modifier = Modifier.testTag("dismiss_yoga_dialog")
                ) {
                    Text("Got It")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        shareText(
                            context,
                            "Learn how to do ${pose.title}:\n\n${pose.steps.joinToString("\n")}\n\nDownloaded via Period Care App!"
                        )
                    }
                ) {
                    Text("Share Steps")
                }
            },
            title = {
                Text(
                    text = pose.title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(pose.color.copy(alpha = 0.3f), pose.color.copy(alpha = 0.1f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = "Yoga icon",
                                tint = pose.color,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Recommended duration: ${pose.duration}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = pose.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "How to Practice:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    pose.steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = step,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        )
    }
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Wellness with Support Network"))
}

// Utility extension for vertical manual scrolling in Dialog
@Composable
fun rememberScrollState() = androidx.compose.foundation.rememberScrollState()
