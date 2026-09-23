package com.tokar.frez.cnc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokar.frez.cnc.data.ble.BleConnectionState
import com.tokar.frez.cnc.data.ble.BleGaugeDevice
import com.tokar.frez.cnc.data.ble.BleMeasurementManager
import com.tokar.frez.cnc.data.ble.BleMeasurementPacket
import com.tokar.frez.cnc.data.entity.ToolWearJournalEntity
import com.tokar.frez.cnc.data.repository.CncRepository
import com.tokar.frez.cnc.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ToolWearUiState(
    val nominalMm: Double = 50.0,
    val actualMm: Double = 49.92,
    val measuredZNominalMm: Double = 100.0,
    val measuredZActualMm: Double = 99.85,
    val mode: CompensationMode = CompensationMode.DIAMETER,
    val cncSystem: CncSystemType = CncSystemType.FANUC,
    val result: ToolWearResult? = null,
    val bleConnectionState: BleConnectionState = BleConnectionState.DISCONNECTED,
    val bleDevices: List<BleGaugeDevice> = emptyList(),
    val latestBlePacket: BleMeasurementPacket? = null
)

class ToolWearViewModel(
    private val toolWearUseCase: ToolWearUseCase = ToolWearUseCase(),
    private val bleManager: BleMeasurementManager = BleMeasurementManager(),
    private val repository: CncRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToolWearUiState())
    val uiState: StateFlow<ToolWearUiState> = _uiState.asStateFlow()

    init {
        calculate()
        observeBleManager()
    }

    private fun observeBleManager() {
        viewModelScope.launch {
            bleManager.connectionState.collect { connState ->
                _uiState.update { it.copy(bleConnectionState = connState) }
            }
        }
        viewModelScope.launch {
            bleManager.discoveredDevices.collect { devList ->
                _uiState.update { it.copy(bleDevices = devList) }
            }
        }
        viewModelScope.launch {
            bleManager.latestMeasurement.collect { packet ->
                _uiState.update { it.copy(latestBlePacket = packet) }
                if (packet != null) {
                    updateInputs(actual = packet.measuredValueMm)
                }
            }
        }
    }

    fun startBleDiscovery() {
        bleManager.startDiscovery()
    }

    fun connectBleDevice(device: BleGaugeDevice) {
        bleManager.connectToDevice(device)
    }

    fun disconnectBle() {
        bleManager.disconnect()
    }

    fun simulateBleMeasurement(valMm: Double, axis: String = "X") {
        bleManager.simulateIncomingMeasurement(valMm, axis)
    }

    fun saveWearLogToDb(toolId: String, partCount: Int, timeMin: Double, wearX: Double, wearZ: Double, cause: String) {
        val forecast = toolWearUseCase.forecastToolLife(toolId, wearX, wearZ, cause = cause)
        val entity = ToolWearJournalEntity(
            toolId = toolId,
            partCount = partCount,
            operatingTimeMin = timeMin,
            measuredWearXMm = wearX,
            measuredWearZMm = wearZ,
            measuredWearYMm = 0.0,
            cause = cause,
            remainingLifePercent = forecast.remainingLifePercent,
            comment = "Автоматический замер BLE"
        )
        viewModelScope.launch {
            repository?.saveToolWearLog(entity)
        }
    }

    fun updateInputs(
        nominal: Double = _uiState.value.nominalMm,
        actual: Double = _uiState.value.actualMm,
        zNominal: Double = _uiState.value.measuredZNominalMm,
        zActual: Double = _uiState.value.measuredZActualMm,
        mode: CompensationMode = _uiState.value.mode,
        cncSystem: CncSystemType = _uiState.value.cncSystem
    ) {
        _uiState.update {
            it.copy(
                nominalMm = nominal,
                actualMm = actual,
                measuredZNominalMm = zNominal,
                measuredZActualMm = zActual,
                mode = mode,
                cncSystem = cncSystem
            )
        }
        calculate()
    }

    private fun calculate() {
        val s = _uiState.value
        val res = toolWearUseCase.calculateToolWear(
            nominalMm = s.nominalMm,
            actualMm = s.actualMm,
            measuredAxisZActualMm = s.measuredZActualMm,
            measuredAxisZNominalMm = s.measuredZNominalMm,
            mode = s.mode,
            cncSystem = s.cncSystem
        )
        _uiState.update { it.copy(result = res) }
    }
}
