package com.cardioconnect.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.cardioconnect.presentation.MainViewModel
import com.cardioconnect.presentation.dashboard.DashboardScreen
import com.cardioconnect.presentation.device.DeviceScreen
import com.cardioconnect.presentation.history.HistoryScreen
import com.cardioconnect.presentation.history.SessionDetailScreen
import com.cardioconnect.presentation.live_ecg.LiveEcgScreen
import com.cardioconnect.presentation.onboarding.OnboardingScreen
import com.cardioconnect.presentation.settings.SettingsScreen
import com.cardioconnect.ui.theme.CardDark
import com.cardioconnect.ui.theme.PhosphorGreen
import com.cardioconnect.ui.theme.TextSecondary
import com.cardioconnect.ui.theme.TextTertiary

import androidx.compose.material.icons.filled.People
import com.cardioconnect.presentation.patients.PatientsScreen
import com.cardioconnect.presentation.patients.PatientProfileScreen

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    val settings by viewModel.settings.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem("Dashboard", Screen.Dashboard.route, Icons.Default.Dashboard),
        BottomNavItem("Patients", Screen.Patients.route, Icons.Default.People),
        BottomNavItem("Live ECG", Screen.LiveEcg.route, Icons.Default.MonitorHeart),
        BottomNavItem("History", Screen.History.route, Icons.Default.History),
        BottomNavItem("Device", Screen.Device.route, Icons.Default.Bluetooth),
        BottomNavItem("Settings", Screen.Settings.route, Icons.Default.Settings)
    )

    val showBottomBar = currentRoute != Screen.Onboarding.route &&
            currentRoute?.startsWith("session_detail") == false &&
            currentRoute?.startsWith("patient_profile") == false

    val startDestination = if (settings.hasCompletedOnboarding) Screen.Dashboard.route else Screen.Onboarding.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = CardDark,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) PhosphorGreen else TextTertiary
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) PhosphorGreen else TextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinished = {
                        viewModel.completeOnboarding()
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToLive = { navController.navigate(Screen.LiveEcg.route) },
                    onNavigateToDevice = { navController.navigate(Screen.Device.route) },
                    onNavigateToPatients = { navController.navigate(Screen.Patients.route) }
                )
            }

            composable(Screen.Patients.route) {
                PatientsScreen(
                    viewModel = viewModel,
                    onNavigateToProfile = { patientId ->
                        navController.navigate(Screen.PatientProfile.createRoute(patientId))
                    },
                    onNavigateToMonitor = {
                        navController.navigate(Screen.LiveEcg.route)
                    }
                )
            }

            composable(
                route = Screen.PatientProfile.route,
                arguments = listOf(navArgument("patientId") { type = NavType.StringType })
            ) { backStackEntry ->
                val patientId = backStackEntry.arguments?.getString("patientId") ?: ""
                PatientProfileScreen(
                    patientId = patientId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToMonitor = { navController.navigate(Screen.LiveEcg.route) }
                )
            }

            composable(Screen.LiveEcg.route) {
                LiveEcgScreen(viewModel = viewModel)
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = viewModel,
                    onSessionSelected = { sessionId ->
                        navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                    }
                )
            }

            composable(
                route = Screen.SessionDetail.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                SessionDetailScreen(
                    sessionId = sessionId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Device.route) {
                DeviceScreen(viewModel = viewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
