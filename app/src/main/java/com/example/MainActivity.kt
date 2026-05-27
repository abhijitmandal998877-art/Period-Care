package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PeriodViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PeriodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val authError by viewModel.authError.collectAsState()
                val activeUser by viewModel.activeUser.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Splash Screen
                    composable("splash") {
                        SplashContent(onTimeout = {
                            if (activeUser != null) {
                                navController.navigate("main") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            } else {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        })
                    }

                    // 2. Login Screen
                    composable("login") {
                        // Clear previous error messages upon entry
                        LaunchedEffect(Unit) {
                            viewModel.clearAuthErrorMessage()
                        }

                        LoginScreen(
                            errorMessage = authError,
                            onLoginClick = { email, password ->
                                viewModel.login(email, password) { success ->
                                    if (success) {
                                        navController.navigate("main") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            },
                            onNavigateToSignup = { navController.navigate("signup") },
                            onNavigateToForgot = { navController.navigate("forgotPassword") }
                        )
                    }

                    // 3. Signup Screen
                    composable("signup") {
                        LaunchedEffect(Unit) {
                            viewModel.clearAuthErrorMessage()
                        }

                        SignupScreen(
                            errorMessage = authError,
                            onSignupClick = { name, email, password ->
                                viewModel.register(name, email, password) { success ->
                                    if (success) {
                                        navController.navigate("main") {
                                            popUpTo("signup") { inclusive = true }
                                        }
                                    }
                                }
                            },
                            onNavigateToLogin = { navController.navigate("login") }
                        )
                    }

                    // 4. Forgot Password Screen
                    composable("forgotPassword") {
                        LaunchedEffect(Unit) {
                            viewModel.clearAuthErrorMessage()
                        }

                        ForgotPasswordScreen(
                            errorMessage = authError,
                            onResetClick = { email, newPassword ->
                                viewModel.forgotPasswordReset(email, newPassword) { success ->
                                    // Feedback handled internally in screen
                                }
                            },
                            onNavigateToLogin = { 
                                navController.navigate("login") {
                                    popUpTo("forgotPassword") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 5. Main Dashboard layout (with bottom nav sheets swapping tabs)
                    composable("main") {
                        val activeUserSession = activeUser
                        if (activeUserSession == null) {
                            // Session expired or logged out outside
                            LaunchedEffect(Unit) {
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        } else {
                            MainContainerScreen(
                                viewModel = viewModel,
                                user = activeUserSession,
                                onNavigateToAdmin = { navController.navigate("admin") },
                                onLogoutClick = {
                                    viewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }

                    // 6. Passcode Protected Admin Console
                    composable("admin") {
                        val registeredUsers by viewModel.allUsers.collectAsState()

                        AdminScreen(
                            registeredUsers = registeredUsers,
                            onBack = { navController.navigateUp() },
                            onSendNotification = { email, title, msg, onDone ->
                                viewModel.sendCustomAdminNotification(email, title, msg, onDone)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainContainerScreen(
    viewModel: PeriodViewModel,
    user: com.example.data.database.User,
    onNavigateToAdmin: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val notifications by viewModel.notifications.collectAsState(initial = emptyList())
    val cycleHistory by viewModel.cycleHistory.collectAsState(initial = emptyList())
    val dailyLogsByDate by viewModel.dailyLogs.collectAsState(initial = emptyList())
    val activeLogForSelectedDate by viewModel.selectDateDailyLog.collectAsState(initial = null)
    val selectedDate by viewModel.selectedLogDate.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_bottom_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Home") },
                    label = { Text("Forecast") },
                    modifier = Modifier.testTag("bottom_tab_home")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
                    label = { Text("Health") },
                    modifier = Modifier.testTag("bottom_tab_analytics")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.SelfImprovement, contentDescription = "Yoga") },
                    label = { Text("Yoga & Tips") },
                    modifier = Modifier.testTag("bottom_tab_yoga")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("bottom_tab_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardTab(
                    user = user,
                    dailyLogs = dailyLogsByDate,
                    activeLog = activeLogForSelectedDate,
                    notifications = notifications,
                    selectedDate = selectedDate,
                    onDateSelected = { dateStr -> viewModel.selectLogDate(dateStr) },
                    onTrackPeriodToday = { viewModel.trackPeriodStartToday() },
                    onSaveDailyLog = { mood, symptom, severity, water, notes ->
                        viewModel.saveSelectedDateDailyLog(mood, symptom, severity, water, notes)
                    },
                    syncState = syncState,
                    onTriggerSync = { viewModel.syncData() }
                )
                1 -> AnalyticsTab(
                    logs = dailyLogsByDate,
                    cycleLength = user.cycleLength
                )
                2 -> YogaTipsTab()
                3 -> ProfileTab(
                    user = user,
                    onLogout = onLogoutClick,
                    onChangePassword = { old, new, callback ->
                        viewModel.changePassword(old, new, callback)
                    },
                    onUpdateCycleSettings = { cycleLen, periodLen ->
                        viewModel.updateCycleSettings(cycleLen, periodLen)
                    },
                    onUpdateWaterGoal = { targetMl ->
                        viewModel.updateWaterGoal(targetMl)
                    },
                    onToggleEncryption = { active ->
                        viewModel.toggleEncryption(active)
                    },
                    onToggleCloudSync = { active ->
                        viewModel.toggleCloudSync(active)
                    },
                    onNavigateToAdminPanel = onNavigateToAdmin
                )
            }
        }
    }
}
