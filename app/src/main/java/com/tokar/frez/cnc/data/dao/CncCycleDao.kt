package com.tokar.frez.cnc.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tokar.frez.cnc.data.entity.CncCycleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CncCycleDao {
    @Query("SELECT * FROM cnc_cycles ORDER BY functionType ASC")
    fun getAllCncCycles(): Flow<List<CncCycleEntity>>

    @Query("SELECT * FROM cnc_cycles WHERE functionType LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    suspend fun searchCycles(query: String): List<CncCycleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cycles: List<CncCycleEntity>)
}
