package com.tokar.frez.cnc.ui.screens.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.data.ble.BleConnectionState
import com.tokar.frez.cnc.domain.usecase.CncSystemType
import com.tokar.frez.cnc.domain.usecase.CompensationMode
import com.tokar.frez.cnc.ui.ToolWearViewModel
import com.tokar.frez.cnc.ui.theme.*

@Composable
fun ToolWearScreen(viewModel: ToolWearViewModel) {
    val state by viewModel.uiState.collectAsState()

    var nominalText by remember { mutableStateOf(state.nominalMm.toString()) }
    var actualText by remember { mutableStateOf(state.actualMm.toString()) }

    LaunchedEffect(state.latestBlePacket) {
        state.latestBlePacket?.let {
            actualText = String.format("%.2f", it.measuredValueMm)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "КОМПЕНСАЦИЯ ИЗНОСА И ВВОД КОРРЕКТОРОВ",
            color = IndustrialCyan,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Bluetooth LE Gauge Integration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IndustrialCardBg),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (state.bleConnectionState) {
                                BleConnectionState.CONNECTED -> Icons.Default.BluetoothConnected
                                BleConnectionState.DISCOVERING -> Icons.Default.BluetoothSearching
                                else -> Icons.Default.Bluetooth
                            },
                            contentDescription = null,
                            tint = if (state.bleConnectionState == BleConnectionState.CONNECTED) IndustrialGreen else IndustrialCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BLE Измерительный прибор: ${state.bleConnectionState.name}",
                            color = IndustrialYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    if (state.bleConnectionState == BleConnectionState.DISCONNECTED) {
                        OutlinedButton(
                            onClick = { viewModel.startBleDiscovery() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IndustrialCyan)
                        ) {
                            Text("Поиск приборов", fontSize = 11.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.disconnectBle() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IndustrialOrange)
                        ) {
                            Text("Отключить", fontSize = 11.sp)
                        }
                    }
                }

                // Discovered Devices list
                if (state.bleDevices.isNotEmpty() && state.bleConnectionState != BleConnectionState.CONNECTED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Доступные цифровые микрометры / штангенциркули:", color = TextSecondary, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.bleDevices.take(2).forEach { dev ->
                            FilterChip(
                                selected = false,
                                onClick = { viewModel.connectBleDevice(dev) },
                                label = { Text("${dev.name} (${dev.rssi}dBm)", fontSize = 10.sp) }
                            )
                        }
                    }
                }

                // Auto-fill trigger button
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.simulateBleMeasurement(49.91, "X") },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialCyan),
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 40.dp)
                ) {
                    Text("АВТО-ВВОД ЗАМЕРА С BLE МИКРОМЕТРА (49.91 мм)", color = IndustrialDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Offsets Inputs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IndustrialCardBg),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Режим коррекции по диаметру/радиусу:", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.mode == CompensationMode.DIAMETER,
                        onClick = { viewModel.updateInputs(mode = CompensationMode.DIAMETER) },
                        label = { Text("DIAMETER (Диаметр)") },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    )
                    FilterChip(
                        selected = state.mode == CompensationMode.RADIUS,
                        onClick = { viewModel.updateInputs(mode = CompensationMode.RADIUS) },
                        label = { Text("RADIUS (Радиус)") },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nominalText,
                    onValueChange = {
                        nominalText = it
                        it.toDoubleOrNull()?.let { v -> viewModel.updateInputs(nominal = v) }
                    },
                    label = { Text("Номинал по чертежу (мм)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = actualText,
                    onValueChange = {
                        actualText = it
                        it.toDoubleOrNull()?.let { v -> viewModel.updateInputs(actual = v) }
                    },
                    label = { Text("Фактический замер (мм)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Стойка ЧПУ для подсказки ввода:", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CncSystemType.entries.forEach { sys ->
                        FilterChip(
                            selected = state.cncSystem == sys,
                            onClick = { viewModel.updateInputs(cncSystem = sys) },
                            label = { Text(sys.name) },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        state.result?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IndustrialCardBg),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("РАССЧИТАННАЯ ДЕЛЬТА КОРРЕКЦИИ:", color = IndustrialYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ΔX (мм): ${res.deltaXmm} мм  (${res.deltaXUm} мкм)", color = IndustrialCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("ΔZ (мм): ${res.deltaZmm} мм  (${res.deltaZUm} мкм)", color = IndustrialCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("ПОДСКАЗКА ДЛЯ ОПЕРАТОРА:", color = IndustrialGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(res.inputHint, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}
