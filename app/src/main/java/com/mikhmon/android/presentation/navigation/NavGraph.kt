package com.mikhmon.android.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mikhmon.android.presentation.features.dashboard.DashboardScreen
import com.mikhmon.android.presentation.features.login.LoginScreen
import com.mikhmon.android.presentation.features.routers.RouterListScreen
import com.mikhmon.android.presentation.features.users.UserListScreen
import com.mikhmon.android.presentation.features.vouchers.VoucherListScreen
import com.mikhmon.android.presentation.features.monitoring.MonitoringScreen
import com.mikhmon.android.presentation.features.reports.ReportScreen
import com.mikhmon.android.presentation.features.settings.SettingsScreen

@Composable
fun MikhmonNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Screen = Screen.Login
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login
        composable<Screen.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                }
            )
        }
        
        // Dashboard
        composable<Screen.Dashboard> {
            DashboardScreen(
                onNavigateToUsers = { navController.navigate(Screen.Users) },
                onNavigateToVouchers = { navController.navigate(Screen.Vouchers) },
                onNavigateToMonitoring = { navController.navigate(Screen.Monitoring) },
                onNavigateToReports = { navController.navigate(Screen.Reports) },
                onNavigateToSettings = { navController.navigate(Screen.Settings) },
                onNavigateToRouters = { navController.navigate(Screen.Routers) }
            )
        }
        
        // Routers
        composable<Screen.Routers> {
            RouterListScreen(
                onBack = { navController.popBackStack() },
                onAddRouter = { navController.navigate(Screen.AddRouter) }
            )
        }
        
        // Users
        composable<Screen.Users> {
            UserListScreen(
                onBack = { navController.popBackStack() },
                onUserClick = { userId -> 
                    navController.navigate(Screen.UserDetail(userId))
                },
                onAddUser = { navController.navigate(Screen.AddUser) }
            )
        }
        
        // Vouchers
        composable<Screen.Vouchers> {
            VoucherListScreen(
                onBack = { navController.popBackStack() },
                onGenerateVoucher = { navController.navigate(Screen.GenerateVoucher) }
            )
        }
        
        // Monitoring
        composable<Screen.Monitoring> {
            MonitoringScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        // Reports
        composable<Screen.Reports> {
            ReportScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        // Settings
        composable<Screen.Settings> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onViewLogs = { navController.navigate(Screen.Logs) }
            )
        }
    }
}
