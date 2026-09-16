package com.tokar.frez.cnc.ui.screens.gear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.ui.MainUiState
import com.tokar.frez.cnc.ui.MainViewModel
import com.tokar.frez.cnc.ui.canvas.GearToothCanvas
import com.tokar.frez.cnc.ui.theme.*

@Composable
fun GearCuttingScreen(viewModel: MainViewModel, state: MainUiState) {
    var moduleText by remember { mutableStateOf("3.0") }
    var teethText by remember { mutableStateOf("30") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("МОДУЛЬ 3: ЗУБООБРАБОТКА", color = IndustrialCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = IndustrialCardBg), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Геометрия эвольвентного зацепления", color = IndustrialYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = moduleText, onValueChange = { moduleText = it }, label = { Text("Модуль m (мм)") })
                OutlinedTextField(value = teethText, onValueChange = { teethText = it }, label = { Text("Число зубьев z") })

                Button(
                    onClick = {
                        val m = moduleText.toDoubleOrNull() ?: 3.0
                        val z = teethText.toIntOrNull() ?: 30
                        viewModel.calculateGear(m, z, 20.0, 0)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialCyan),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("РАССЧИТАТЬ", color = IndustrialDarkBg, fontWeight = FontWeight.Bold)
                }

                state.gearResult?.let { g ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Делительный диаметр d: ${g.pitchDiameter} мм", color = TextPrimary)
                    Text("Диаметр вершин da: ${g.tipDiameter} мм", color = IndustrialGreen)
                    Text("Диаметр впадин df: ${g.rootDiameter} мм", color = TextPrimary)
                    Text("Длина общей нормали W: ${String.format("%.3f", g.baseTangentLength)} мм (на k=${g.recommendedSpanTeeth} зубьях)", color = IndustrialYellow)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        val m = moduleText.toDoubleOrNull() ?: 3.0
        val z = teethText.toIntOrNull() ?: 30
        GearToothCanvas(module = m, teeth = z)
    }
}
