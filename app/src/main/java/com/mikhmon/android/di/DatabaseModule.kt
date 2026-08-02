package com.mikhmon.android.di

import android.content.Context
import androidx.room.Room
import com.mikhmon.android.data.local.database.MikhmonDatabase
import com.mikhmon.android.data.local.database.RouterDao
import com.mikhmon.android.data.local.database.LogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MikhmonDatabase {
        return Room.databaseBuilder(
            context,
            MikhmonDatabase::class.java,
            "mikhmon_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideRouterDao(database: MikhmonDatabase): RouterDao {
        return database.routerDao()
    }
    
    @Provides
    fun provideLogDao(database: MikhmonDatabase): LogDao {
        return database.logDao()
    }
}
