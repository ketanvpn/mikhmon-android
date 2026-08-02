package com.mikhmon.android.di

import com.mikhmon.android.data.local.database.RouterDao
import com.mikhmon.android.data.repository.ProfileRepository
import com.mikhmon.android.data.repository.RouterRepository
import com.mikhmon.android.data.repository.UserRepository
import com.mikhmon.android.data.repository.VoucherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideRouterRepository(routerDao: RouterDao): RouterRepository {
        return RouterRepository(routerDao)
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(routerRepository: RouterRepository): UserRepository {
        return UserRepository(routerRepository)
    }
    
    @Provides
    @Singleton
    fun provideVoucherRepository(userRepository: UserRepository): VoucherRepository {
        return VoucherRepository(userRepository)
    }
    
    @Provides
    @Singleton
    fun provideProfileRepository(routerRepository: RouterRepository): ProfileRepository {
        return ProfileRepository(routerRepository)
    }
}
