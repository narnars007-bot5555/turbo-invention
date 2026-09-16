package com.tokar.frez.cnc.ui.screens.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.domain.usecase.CncSystemType
import com.tokar.frez.cnc.domain.usecase.CompensationMode
import com.tokar.frez.cnc.ui.ToolWearViewModel
import com.tokar.frez.cnc.ui.theme.*

@Composable
fun ToolWearScreen(viewModel: ToolWearViewModel) {
    val state by viewModel.uiState.collectAsState()

    var nominalText by remember { mutableStateOf(state.nominalMm.toString()) }
    var actualText by remember { mutableStateOf(state.actualMm.toString()) }

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
                        modifier = Modifier.defaultMinSize(minHeight = 56.dp)
                    )
                    FilterChip(
                        selected = state.mode == CompensationMode.RADIUS,
                        onClick = { viewModel.updateInputs(mode = CompensationMode.RADIUS) },
                        label = { Text("RADIUS (Радиус)") },
                        modifier = Modifier.defaultMinSize(minHeight = 56.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nominalText,
                    onValueChange = {
                        nominalText = it
                        it.toDoubleOrNull()?.let { v -> viewModel.updateInputs(nominal = v) }
                    },
                    label = { Text("Номинал по чертежу (мм), e.g. 50.00") },
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
                    label = { Text("Фактический замер микрометром (мм), e.g. 49.92") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Стойка ЧПУ для подсказа ввода:", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CncSystemType.entries.forEach { sys ->
                        FilterChip(
                            selected = state.cncSystem == sys,
                            onClick = { viewModel.updateInputs(cncSystem = sys) },
                            label = { Text(sys.name) },
                            modifier = Modifier.defaultMinSize(minHeight = 56.dp)
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
