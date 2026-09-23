package com.tokar.frez.cnc.data.repository

import android.content.Context
import com.tokar.frez.cnc.data.dao.*
import com.tokar.frez.cnc.data.entity.*
import com.tokar.frez.cnc.data.models.CncDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class CncRepository(
    private val materialDao: MaterialDao,
    private val isoToleranceDao: IsoToleranceDao,
    private val threadDao: ThreadDao,
    private val cncCycleDao: CncCycleDao,
    private val toolFixtureDao: ToolFixtureDao,
    private val machineDao: MachineDao,
    private val calculationHistoryDao: CalculationHistoryDao? = null,
    private val toolWearJournalDao: ToolWearJournalDao? = null
) {

    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _cncDatabaseState = MutableStateFlow<CncDatabase?>(null)
    val cncDatabaseState: StateFlow<CncDatabase?> = _cncDatabaseState.asStateFlow()

    private var cachedMaterials: List<MaterialEntity>? = null
    private var cachedThreads: List<ThreadEntity>? = null
    private var cachedCycles: List<CncCycleEntity>? = null
    private var cachedTools: List<ToolFixtureEntity>? = null

    suspend fun loadCncDatabaseFromAssets(context: Context): CncDatabase? {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = context.assets.open("database/cnc_database.json")
                    .bufferedReader()
                    .use { it.readText() }
                val parsed = jsonParser.decodeFromString<CncDatabase>(jsonString)
                _cncDatabaseState.value = parsed
                parsed
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun parseCncDatabaseJsonString(jsonString: String): CncDatabase {
        return jsonParser.decodeFromString(jsonString)
    }

    fun getMaterialsFlow(): Flow<List<MaterialEntity>> = flow {
        cachedMaterials?.let { emit(it) }
        materialDao.getAllMaterials().collect { list ->
            cachedMaterials = list
            emit(list)
        }
    }

    suspend fun findMaterialByName(query: String): MaterialEntity? {
        return materialDao.findByName(query)
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

    // Calculation History
    fun getCalculationHistoryFlow(): Flow<List<CalculationHistoryEntity>>? = calculationHistoryDao?.getAllHistory()
    suspend fun saveCalculationHistory(entry: CalculationHistoryEntity): Long = calculationHistoryDao?.insert(entry) ?: 0L
    suspend fun searchHistory(query: String): List<CalculationHistoryEntity> = calculationHistoryDao?.searchHistory(query) ?: emptyList()
    suspend fun deleteHistoryById(id: Long) { calculationHistoryDao?.deleteById(id) }
    suspend fun clearHistory() { calculationHistoryDao?.clearAll() }

    // Tool Wear Journal
    fun getToolWearJournalFlow(): Flow<List<ToolWearJournalEntity>>? = toolWearJournalDao?.getAllWearLogs()
    suspend fun getToolWearLogsForTool(toolId: String): List<ToolWearJournalEntity> = toolWearJournalDao?.getLogsForTool(toolId) ?: emptyList()
    suspend fun saveToolWearLog(entry: ToolWearJournalEntity): Long = toolWearJournalDao?.insert(entry) ?: 0L
    suspend fun deleteToolWearLogById(id: Long) { toolWearJournalDao?.deleteById(id) }
}
