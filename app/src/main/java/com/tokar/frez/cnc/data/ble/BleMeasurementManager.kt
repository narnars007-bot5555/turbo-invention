package com.tokar.frez.cnc.data.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

enum class BleConnectionState {
    DISCONNECTED, DISCOVERING, CONNECTING, CONNECTED, RECONNECTING, ERROR
}

enum class BleGaugeType {
    DIGITAL_CALIPER,    // Цифровой штангенциркуль
    MICROMETER,         // Микрометр
    BORE_GAUGE          // Нутромер
}

data class BleGaugeDevice(
    val name: String,
    val address: String,
    val rssi: Int = -60,
    val gaugeType: BleGaugeType = BleGaugeType.MICROMETER
)

data class BleMeasurementPacket(
    val measuredValueMm: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val targetAxis: String = "X", // X, Z, Y
    val batteryPercent: Int = 95,
    val rawHex: String = ""
) {
    val valueInInches: Double
        get() = measuredValueMm / 25.4
}

class BleMeasurementManager(
    private val context: Context? = null
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _connectionState = MutableStateFlow(BleConnectionState.DISCONNECTED)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BleGaugeDevice>>(
        listOf(
            BleGaugeDevice("Dasqua Digital Micrometer", "00:11:22:33:44:55", -55, BleGaugeType.MICROMETER),
            BleGaugeDevice("Mitutoyo Caliper BLE", "AA:BB:CC:DD:EE:FF", -62, BleGaugeType.DIGITAL_CALIPER),
            BleGaugeDevice("Mahr Bore Gauge", "11:22:33:44:55:66", -70, BleGaugeType.BORE_GAUGE)
        )
    )
    val discoveredDevices: StateFlow<List<BleGaugeDevice>> = _discoveredDevices.asStateFlow()

    private val _latestMeasurement = MutableStateFlow<BleMeasurementPacket?>(null)
    val latestMeasurement: StateFlow<BleMeasurementPacket?> = _latestMeasurement.asStateFlow()

    private var activeDevice: BleGaugeDevice? = null

    // Buffer for median sliding filter (window size = 5)
    private val filterWindow = LinkedList<Double>()

    fun startDiscovery() {
        _connectionState.value = BleConnectionState.DISCOVERING
    }

    fun connectToDevice(device: BleGaugeDevice) {
        activeDevice = device
        _connectionState.value = BleConnectionState.CONNECTING

        scope.launch {
            kotlinx.coroutines.delay(800)
            _connectionState.value = BleConnectionState.CONNECTED
        }
    }

    fun disconnect() {
        _connectionState.value = BleConnectionState.DISCONNECTED
        activeDevice = null
        filterWindow.clear()
    }

    fun applyMedianFilter(rawValMm: Double): Double {
        synchronized(filterWindow) {
            filterWindow.add(rawValMm)
            if (filterWindow.size > 5) {
                filterWindow.removeFirst()
            }
            val sorted = filterWindow.sorted()
            return sorted[sorted.size / 2]
        }
    }

    fun simulateIncomingMeasurement(measuredMm: Double, axis: String = "X") {
        val filteredValue = applyMedianFilter(measuredMm)
        val packet = BleMeasurementPacket(
            measuredValueMm = filteredValue,
            timestamp = System.currentTimeMillis(),
            targetAxis = axis,
            batteryPercent = 92
        )
        _latestMeasurement.value = packet
    }

    fun parseBlePacketBytes(data: ByteArray, targetAxis: String = "X"): BleMeasurementPacket? {
        if (data.isEmpty()) return null
        return try {
            val str = String(data).trim()
            val valMm = str.toDoubleOrNull() ?: (data[0].toInt() and 0xFF) / 100.0
            val filteredVal = applyMedianFilter(valMm)
            BleMeasurementPacket(
                measuredValueMm = filteredVal,
                timestamp = System.currentTimeMillis(),
                targetAxis = targetAxis,
                rawHex = data.joinToString("") { String.format("%02X", it) }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
