package com.tokar.frez.cnc.ui.screens.gcode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.domain.usecase.CncSystemType
import com.tokar.frez.cnc.ui.GCodeViewModel
import com.tokar.frez.cnc.ui.canvas.BackplotterCanvas
import com.tokar.frez.cnc.ui.theme.*

@Composable
fun GCodeGeneratorScreen(viewModel: GCodeViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "МИКРО-ГЕНЕРАТОР G-КОДА И BACKPLOTTER",
            color = IndustrialCyan,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Cycle Selector
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.cycleType == "G71_CYCLE95",
                onClick = { viewModel.updateCycleParams(cycleType = "G71_CYCLE95") },
                label = { Text("Черновое G71/CYCLE95") },
                modifier = Modifier.defaultMinSize(minHeight = 56.dp)
            )
            FilterChip(
                selected = state.cycleType == "G76_CYCLE97",
                onClick = { viewModel.updateCycleParams(cycleType = "G76_CYCLE97") },
                label = { Text("Резьба G76/CYCLE97") },
                modifier = Modifier.defaultMinSize(minHeight = 56.dp)
            )
            FilterChip(
                selected = state.cycleType == "G83_CYCLE83",
                onClick = { viewModel.updateCycleParams(cycleType = "G83_CYCLE83") },
                label = { Text("Сверление G83/83") },
                modifier = Modifier.defaultMinSize(minHeight = 56.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // CNC System selection
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CncSystemType.entries.forEach { sys ->
                FilterChip(
                    selected = state.cncSystem == sys,
                    onClick = { viewModel.updateCycleParams(system = sys) },
                    label = { Text(sys.name) },
                    modifier = Modifier.defaultMinSize(minHeight = 56.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Backplotter Canvas
        Text("Compose Canvas Backplotter (G0-пунктир/зеленый, G1-синий):", color = IndustrialYellow, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = IndustrialCardBg)
        ) {
            BackplotterCanvas(toolpathPoints = state.toolpathPoints)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Validation Issues
        if (state.validationIssues.isNotEmpty()) {
            Text("Результаты проверки G-кода для ${state.cncSystem.name}:", color = IndustrialYellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IndustrialCardBg),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    state.validationIssues.forEach { issue ->
                        val color = if (issue.severity == com.tokar.frez.cnc.domain.usecase.IssueSeverity.ERROR) IndustrialRed else IndustrialOrange
                        val prefix = if (issue.severity == com.tokar.frez.cnc.domain.usecase.IssueSeverity.ERROR) "❌ ОШИБКА" else "⚠️ ПРЕДУПРЕЖДЕНИЕ"
                        val lineStr = if (issue.lineNumber > 0) " (Кадр ${issue.lineNumber})" else ""
                        Text(
                            text = "$prefix$lineStr: ${issue.message}",
                            color = color,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Generated Code Output
        Text("Сгенерированная управляющая программа (УП):", color = IndustrialGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IndustrialCardBg),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = state.generatedGCode,
                color = IndustrialCyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
