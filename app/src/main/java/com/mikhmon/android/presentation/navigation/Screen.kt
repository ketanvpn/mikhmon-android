package com.mikhmon.android.presentation.navigation

/**
 * Navigation screens/routes for Mikhmon Android
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Routers : Screen("routers")
    object AddRouter : Screen("add_router")
    object Users : Screen("users")
    object UserDetail : Screen("user_detail/{userId}") {
        fun createRoute(userId: String) = "user_detail/$userId"
    }
    object AddUser : Screen("add_user")
    object Profiles : Screen("profiles")
    object AddProfile : Screen("add_profile")
    object Vouchers : Screen("vouchers")
    object GenerateVoucher : Screen("generate_voucher")
    object Monitoring : Screen("monitoring")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    object Logs : Screen("logs")
}
