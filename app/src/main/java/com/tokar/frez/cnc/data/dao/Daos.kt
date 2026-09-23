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

    @Query("SELECT * FROM materials WHERE name LIKE '%' || :query || '%' LIMIT 1")
    suspend fun findByName(query: String): MaterialEntity?

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

@Dao
interface CalculationHistoryDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationHistoryEntity>>

    @Query("SELECT * FROM calculation_history WHERE operationName LIKE '%' || :query || '%' OR materialName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchHistory(query: String): List<CalculationHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CalculationHistoryEntity): Long

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()
}

@Dao
interface ToolWearJournalDao {
    @Query("SELECT * FROM tool_wear_journal ORDER BY lastMeasuredTimestamp DESC")
    fun getAllWearLogs(): Flow<List<ToolWearJournalEntity>>

    @Query("SELECT * FROM tool_wear_journal WHERE toolId = :toolId ORDER BY lastMeasuredTimestamp DESC")
    suspend fun getLogsForTool(toolId: String): List<ToolWearJournalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ToolWearJournalEntity): Long

    @Query("DELETE FROM tool_wear_journal WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface MachineDao {
    @Query("SELECT DISTINCT machineClass FROM machines ORDER BY machineClass ASC")
    fun getMachineClasses(): Flow<List<String>>

    @Query("SELECT DISTINCT cncSystem FROM machines WHERE machineClass = :machineClass ORDER BY cncSystem ASC")
    fun getCncSystemsForClass(machineClass: String): Flow<List<String>>

    @Query("SELECT * FROM machines WHERE machineClass = :machineClass AND cncSystem = :cncSystem ORDER BY modelName ASC")
    fun getMachinesByClassAndCnc(machineClass: String, cncSystem: String): Flow<List<MachineEntity>>

    @Query("SELECT * FROM machines WHERE id = :id LIMIT 1")
    suspend fun getMachineById(id: Long): MachineEntity?

    @Query("SELECT * FROM machines ORDER BY id ASC")
    fun getAllMachines(): Flow<List<MachineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(machines: List<MachineEntity>)
}
