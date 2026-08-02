package com.mikhmon.android.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Navigation screens/routes for Mikhmon Android
 */
sealed interface Screen {
    
    @Serializable
    object Login : Screen
    
    @Serializable
    object Dashboard : Screen
    
    @Serializable
    object Routers : Screen
    
    @Serializable
    object AddRouter : Screen
    
    @Serializable
    object Users : Screen
    
    @Serializable
    data class UserDetail(val userId: String) : Screen
    
    @Serializable
    object AddUser : Screen
    
    @Serializable
    object Profiles : Screen
    
    @Serializable
    object AddProfile : Screen
    
    @Serializable
    object Vouchers : Screen
    
    @Serializable
    object GenerateVoucher : Screen
    
    @Serializable
    object Monitoring : Screen
    
    @Serializable
    object Reports : Screen
    
    @Serializable
    object Settings : Screen
    
    @Serializable
    object Logs : Screen
}
