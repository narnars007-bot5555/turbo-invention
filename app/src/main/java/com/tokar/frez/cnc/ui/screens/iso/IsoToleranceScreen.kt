package com.tokar.frez.cnc.ui.screens.iso

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
import com.tokar.frez.cnc.domain.usecase.IsoMatingUseCase
import com.tokar.frez.cnc.ui.MainViewModel
import com.tokar.frez.cnc.ui.canvas.ToleranceZoneCanvas
import com.tokar.frez.cnc.ui.theme.*

@Composable
fun IsoToleranceScreen(viewModel: MainViewModel, state: Any? = null) {
    val matingUseCase = remember { IsoMatingUseCase() }

    var sizeText by remember { mutableStateOf("50.0") }
    var holeField by remember { mutableStateOf("H7") }
    var shaftField by remember { mutableStateOf("g6") }

    val matingResult = remember(sizeText, holeField, shaftField) {
        val size = sizeText.toDoubleOrNull() ?: 50.0
        matingUseCase.calculateMating(size, holeField, shaftField)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "ДОПУСКИ И ПОСАДКИ ISO 286-1 (1 - 3150 мм)",
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
                OutlinedTextField(
                    value = sizeText,
                    onValueChange = { sizeText = it },
                    label = { Text("Номинальный размер (мм)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = holeField,
                        onValueChange = { holeField = it },
                        label = { Text("Поле отверстия (H7)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = shaftField,
                        onValueChange = { shaftField = it },
                        label = { Text("Поле вала (g6)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Canvas Chart with Fit Type Color
        ToleranceZoneCanvas(
            nominalSize = matingResult.nominalSize,
            field = holeField,
            upperUm = 25.0,
            lowerUm = 0.0,
            matingResult = matingResult
        )
    }
}
