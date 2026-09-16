package com.tokar.frez.cnc.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tokar.frez.cnc.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials ORDER BY name ASC")
    fun getAllMaterials(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE isoGroup = :isoGroup")
    suspend fun getMaterialsByGroup(isoGroup: String): List<MaterialEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(materials: List<MaterialEntity>)
}

@Dao
interface IsoToleranceDao {
    @Query("SELECT * FROM iso_tolerances WHERE fieldName = :field AND :size >= nominalRangeMin AND :size <= nominalRangeMax LIMIT 1")
    suspend fun getTolerance(field: String, size: Double): IsoToleranceEntity?

    @Query("SELECT * FROM iso_tolerances ORDER BY fieldName ASC")
    fun getAllTolerances(): Flow<List<IsoToleranceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tolerances: List<IsoToleranceEntity>)
}

@Dao
interface ThreadDao {
    @Query("SELECT * FROM threads WHERE designation LIKE '%' || :query || '%'")
    suspend fun searchThreads(query: String): List<ThreadEntity>

    @Query("SELECT * FROM threads ORDER BY nominalDiameter ASC")
    fun getAllThreads(): Flow<List<ThreadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(threads: List<ThreadEntity>)
}

@Dao
interface CncCycleDao {
    @Query("SELECT * FROM cnc_cycles ORDER BY functionType ASC")
    fun getAllCncCycles(): Flow<List<CncCycleEntity>>

    @Query("SELECT * FROM cnc_cycles WHERE functionType LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    suspend fun searchCycles(query: String): List<CncCycleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cycles: List<CncCycleEntity>)
}

@Dao
interface ToolFixtureDao {
    @Query("SELECT * FROM tool_fixtures ORDER BY category ASC, name ASC")
    fun getAllToolFixtures(): Flow<List<ToolFixtureEntity>>

    @Query("SELECT * FROM tool_fixtures WHERE category = :category")
    suspend fun getByCategory(category: String): List<ToolFixtureEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tools: List<ToolFixtureEntity>)
}
