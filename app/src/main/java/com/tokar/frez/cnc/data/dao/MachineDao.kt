package com.tokar.frez.cnc.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tokar.frez.cnc.data.entity.MachineEntity
import kotlinx.coroutines.flow.Flow

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
