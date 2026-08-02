package com.mikhmon.android.di

import com.mikhmon.android.data.repository.RouterRepository
import com.mikhmon.android.data.repository.UserRepository
import com.mikhmon.android.domain.usecase.router.*
import com.mikhmon.android.domain.usecase.user.*
import com.mikhmon.android.domain.usecase.voucher.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    // Router Use Cases
    @Provides
    @Singleton
    fun provideConnectRouterUseCase(routerRepository: RouterRepository): ConnectRouterUseCase {
        return ConnectRouterUseCase(routerRepository)
    }
    
    @Provides
    @Singleton
    fun provideDisconnectRouterUseCase(routerRepository: RouterRepository): DisconnectRouterUseCase {
        return DisconnectRouterUseCase(routerRepository)
    }
    
    @Provides
    @Singleton
    fun provideGetRouterStatusUseCase(routerRepository: RouterRepository): GetRouterStatusUseCase {
        return GetRouterStatusUseCase(routerRepository)
    }
    
    @Provides
    @Singleton
    fun provideAddRouterUseCase(routerRepository: RouterRepository): AddRouterUseCase {
        return AddRouterUseCase(routerRepository)
    }
    
    // User Use Cases
    @Provides
    @Singleton
    fun provideGetUsersUseCase(userRepository: UserRepository): GetUsersUseCase {
        return GetUsersUseCase(userRepository)
    }
    
    @Provides
    @Singleton
    fun provideGetUsersByProfileUseCase(userRepository: UserRepository): GetUsersByProfileUseCase {
        return GetUsersByProfileUseCase(userRepository)
    }
    
    @Provides
    @Singleton
    fun provideAddUserUseCase(userRepository: UserRepository): AddUserUseCase {
        return AddUserUseCase(userRepository)
    }
    
    @Provides
    @Singleton
    fun provideUpdateUserUseCase(userRepository: UserRepository): UpdateUserUseCase {
        return UpdateUserUseCase(userRepository)
    }
    
    @Provides
    @Singleton
    fun provideDeleteUserUseCase(userRepository: UserRepository): DeleteUserUseCase {
        return DeleteUserUseCase(userRepository)
    }
    
    @Provides
    @Singleton
    fun provideSetUserEnabledUseCase(userRepository: UserRepository): SetUserEnabledUseCase {
        return SetUserEnabledUseCase(userRepository)
    }
    
    @Provides
    @Singleton
    fun provideGetActiveUsersUseCase(userRepository: UserRepository): GetActiveUsersUseCase {
        return GetActiveUsersUseCase(userRepository)
    }
    
    @Provides
    @Singleton
    fun provideKickUserUseCase(userRepository: UserRepository): KickUserUseCase {
        return KickUserUseCase(userRepository)
    }
    
    @Provides
    @Singleton
    fun provideGetUserProfilesUseCase(userRepository: UserRepository): GetUserProfilesUseCase {
        return GetUserProfilesUseCase(userRepository)
    }
    
    // Voucher Use Cases
    @Provides
    @Singleton
    fun provideGenerateVouchersUseCase(userRepository: UserRepository): GenerateVouchersUseCase {
        return GenerateVouchersUseCase(userRepository)
    }
    
    @Provides
    @Singleton
    fun provideDeleteVouchersUseCase(userRepository: UserRepository): DeleteVouchersUseCase {
        return DeleteVouchersUseCase(userRepository)
    }
}
