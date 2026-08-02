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
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Dashboard
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToUsers = { navController.navigate(Screen.Users.route) },
                onNavigateToVouchers = { navController.navigate(Screen.Vouchers.route) },
                onNavigateToMonitoring = { navController.navigate(Screen.Monitoring.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToRouters = { navController.navigate(Screen.Routers.route) }
            )
        }
        
        // Routers
        composable(route = Screen.Routers.route) {
            RouterListScreen(
                onBack = { navController.popBackStack() },
                onAddRouter = { navController.navigate(Screen.AddRouter.route) }
            )
        }
        
        // Users
        composable(route = Screen.Users.route) {
            UserListScreen(
                onBack = { navController.popBackStack() },
                onUserClick = { userId -> 
                    navController.navigate(Screen.UserDetail.createRoute(userId))
                },
                onAddUser = { navController.navigate(Screen.AddUser.route) }
            )
        }
        
        // Vouchers
        composable(route = Screen.Vouchers.route) {
            VoucherListScreen(
                onBack = { navController.popBackStack() },
                onGenerateVoucher = { navController.navigate(Screen.GenerateVoucher.route) }
            )
        }
        
        // Monitoring
        composable(route = Screen.Monitoring.route) {
            MonitoringScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        // Reports
        composable(route = Screen.Reports.route) {
            ReportScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        // Settings
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onViewLogs = { navController.navigate(Screen.Logs.route) }
            )
        }
    }
}
