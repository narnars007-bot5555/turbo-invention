package com.tokar.frez.cnc.ui.screens.gcode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.domain.usecase.*
import com.tokar.frez.cnc.ui.GCodeViewModel
import com.tokar.frez.cnc.ui.canvas.BackplotterCanvas
import com.tokar.frez.cnc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GCodeGeneratorScreen(viewModel: GCodeViewModel) {
    val state by viewModel.uiState.collectAsState()
    val clipboard = LocalClipboardManager.current
    var machineMenuOpen by remember { mutableStateOf(false) }
    val operations = listOf(
        "G71_CYCLE95" to "Точение G71",
        "G76_CYCLE97" to "Резьба G76",
        "G75" to "Канавка G75",
        "G83_CYCLE83" to "Сверление G83",
        "FACING" to "Торцевание"
    )

    Column(Modifier.fillMaxSize().background(IndustrialDarkBg).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("БЕЗОПАСНЫЙ ГЕНЕРАТОР G-КОДА", color = IndustrialCyan, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text("Валидация → постпроцессор → безопасный отвод", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))

        Text("СТАНОК И СТОЙКА", color = IndustrialYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Box {
            OutlinedButton(onClick = { machineMenuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                Text(state.selectedMachine.displayName, modifier = Modifier.weight(1f))
                Text("▼")
            }
            DropdownMenu(expanded = machineMenuOpen, onDismissRequest = { machineMenuOpen = false }) {
                MachineProfiles.enterpriseFleet.forEach { profile ->
                    DropdownMenuItem(text = { Text(profile.displayName) }, onClick = { machineMenuOpen = false; viewModel.selectMachine(profile) })
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        Text("ОПЕРАЦИЯ", color = IndustrialYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            operations.take(3).forEach { (code, label) ->
                FilterChip(selected = state.cycleType == code, onClick = { viewModel.updateCycleParams(cycleType = code) }, label = { Text(label, fontSize = 10.sp) })
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            operations.drop(3).forEach { (code, label) ->
                FilterChip(selected = state.cycleType == code, onClick = { viewModel.updateCycleParams(cycleType = code) }, label = { Text(label, fontSize = 10.sp) })
            }
        }
        Spacer(Modifier.height(10.dp))

        Text("КОНТРОЛЬ РЕЖИМОВ", color = IndustrialYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("Инструмент T${state.toolNumber} · ${state.spindleRpm} об/мин · Vc ${state.cuttingSpeedVc} м/мин · припуск ${state.stockAllowancePerSideMm} мм", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))

        state.validationErrors.forEach { message ->
            Card(Modifier.fillMaxWidth().padding(vertical = 3.dp), colors = CardDefaults.cardColors(containerColor = IndustrialRed.copy(alpha = .18f)), shape = RoundedCornerShape(8.dp)) {
                Text("ОШИБКА: $message", color = IndustrialRed, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
            }
        }
        state.validationWarnings.forEach { message ->
            Card(Modifier.fillMaxWidth().padding(vertical = 3.dp), colors = CardDefaults.cardColors(containerColor = IndustrialYellow.copy(alpha = .15f)), shape = RoundedCornerShape(8.dp)) {
                Text("ПРЕДУПРЕЖДЕНИЕ: $message", color = IndustrialYellow, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
            }
        }

        Text("BACKPLOTTER", color = IndustrialYellow, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
        Card(Modifier.fillMaxWidth().height(190.dp), colors = CardDefaults.cardColors(containerColor = IndustrialCardBg)) {
            BackplotterCanvas(toolpathPoints = state.toolpathPoints)
        }
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("ВЫВОД УП", color = IndustrialGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            TextButton(enabled = state.generatedGCode.isNotBlank() && state.validationErrors.isEmpty(), onClick = { clipboard.setText(AnnotatedString(state.generatedGCode)) }) { Text("КОПИРОВАТЬ") }
        }
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = IndustrialCardBg), shape = RoundedCornerShape(8.dp)) {
            Text(
                text = if (state.generatedGCode.isBlank()) "Генерация заблокирована до исправления ошибок." else state.generatedGCode,
                color = if (state.generatedGCode.isBlank()) IndustrialRed else IndustrialCyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}
