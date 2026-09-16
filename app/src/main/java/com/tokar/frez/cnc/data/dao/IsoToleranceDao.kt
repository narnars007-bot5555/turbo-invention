package com.tokar.frez.cnc.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tokar.frez.cnc.data.entity.IsoToleranceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IsoToleranceDao {
    @Query("SELECT * FROM iso_tolerances WHERE fieldName = :field AND :size >= nominalRangeMin AND :size <= nominalRangeMax LIMIT 1")
    suspend fun getTolerance(field: String, size: Double): IsoToleranceEntity?

    @Query("SELECT * FROM iso_tolerances ORDER BY fieldName ASC")
    fun getAllTolerances(): Flow<List<IsoToleranceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tolerances: List<IsoToleranceEntity>)
}
