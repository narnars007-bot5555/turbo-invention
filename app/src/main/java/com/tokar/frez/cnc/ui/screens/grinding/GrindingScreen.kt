package com.tokar.frez.cnc.ui.screens.grinding

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
import com.tokar.frez.cnc.ui.theme.*

@Composable
fun GrindingScreen(viewModel: MainViewModel, state: MainUiState) {
    var codeText by remember { mutableStateOf("25A F60 K 5 V") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("МОДУЛЬ 2: ШЛИФОВАНИЕ И АБРАЗИВ", color = IndustrialCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = IndustrialCardBg), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Декодер маркировки шлифовальных кругов", color = IndustrialYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = codeText, onValueChange = { codeText = it }, label = { Text("Маркировка круга (ГОСТ / ISO)") })

                Button(
                    onClick = { viewModel.decodeWheelMarking(codeText) },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialCyan),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("РАСШИФРОВАТЬ", color = IndustrialDarkBg, fontWeight = FontWeight.Bold)
                }

                state.wheelDecoded?.let { dec ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Абразив: ${dec.abrasiveType}", color = TextPrimary)
                    Text("Зернистость: ${dec.grainSize}", color = TextPrimary)
                    Text("Твердость: ${dec.hardness}", color = TextPrimary)
                    Text("Связка: ${dec.bondType}", color = IndustrialGreen)
                }
            }
        }
    }
}
