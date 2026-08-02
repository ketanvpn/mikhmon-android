package com.mikhmon.android.data.local.database

import androidx.room.*
import com.mikhmon.android.data.model.Router
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for Router entity
 */
@Dao
interface RouterDao {
    
    @Query("SELECT * FROM routers ORDER BY name ASC")
    fun getAllRouters(): Flow<List<Router>>
    
    @Query("SELECT * FROM routers WHERE id = :id")
    suspend fun getRouterById(id: String): Router?
    
    @Query("SELECT * FROM routers WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultRouter(): Router?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouter(router: Router)
    
    @Update
    suspend fun updateRouter(router: Router)
    
    @Delete
    suspend fun deleteRouter(router: Router)
    
    @Query("DELETE FROM routers WHERE id = :id")
    suspend fun deleteRouterById(id: String)
    
    @Query("UPDATE routers SET isDefault = 0")
    suspend fun clearDefaultFlag()
    
    @Query("UPDATE routers SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultRouter(id: String)
    
    @Query("SELECT COUNT(*) FROM routers")
    suspend fun getRouterCount(): Int
}
