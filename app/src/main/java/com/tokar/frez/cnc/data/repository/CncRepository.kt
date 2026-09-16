package com.tokar.frez.cnc.data.repository

import com.tokar.frez.cnc.data.dao.*
import com.tokar.frez.cnc.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CncRepository(
    private val materialDao: MaterialDao,
    private val isoToleranceDao: IsoToleranceDao,
    private val threadDao: ThreadDao,
    private val cncCycleDao: CncCycleDao,
    private val toolFixtureDao: ToolFixtureDao,
    private val machineDao: MachineDao
) {

    private var cachedMaterials: List<MaterialEntity>? = null
    private var cachedThreads: List<ThreadEntity>? = null
    private var cachedCycles: List<CncCycleEntity>? = null
    private var cachedTools: List<ToolFixtureEntity>? = null

    fun getMaterialsFlow(): Flow<List<MaterialEntity>> = flow {
        cachedMaterials?.let { emit(it) }
        materialDao.getAllMaterials().collect { list ->
            cachedMaterials = list
            emit(list)
        }
    }

    fun getThreadsFlow(): Flow<List<ThreadEntity>> = flow {
        cachedThreads?.let { emit(it) }
        threadDao.getAllThreads().collect { list ->
            cachedThreads = list
            emit(list)
        }
    }

    fun getCncCyclesFlow(): Flow<List<CncCycleEntity>> = flow {
        cachedCycles?.let { emit(it) }
        cncCycleDao.getAllCncCycles().collect { list ->
            cachedCycles = list
            emit(list)
        }
    }

    fun getToolFixturesFlow(): Flow<List<ToolFixtureEntity>> = flow {
        cachedTools?.let { emit(it) }
        toolFixtureDao.getAllToolFixtures().collect { list ->
            cachedTools = list
            emit(list)
        }
    }

    fun getMachineClasses(): Flow<List<String>> = machineDao.getMachineClasses()
    fun getCncSystemsForClass(machineClass: String): Flow<List<String>> = machineDao.getCncSystemsForClass(machineClass)
    fun getMachinesByClassAndCnc(machineClass: String, cncSystem: String): Flow<List<MachineEntity>> = machineDao.getMachinesByClassAndCnc(machineClass, cncSystem)
    suspend fun getMachineById(id: Long): MachineEntity? = machineDao.getMachineById(id)
}
