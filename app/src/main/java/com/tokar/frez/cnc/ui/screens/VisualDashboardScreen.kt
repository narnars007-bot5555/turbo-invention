package com.tokar.frez.cnc.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.ui.MainUiState
import com.tokar.frez.cnc.ui.theme.*

@Composable
fun VisualDashboardScreen(
    state: MainUiState,
    onNavigateToCategory: (Int, Int) -> Unit
) {
    val turning = state.turningResult
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(IndustrialDarkBg).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("TOKAR-FREZ-CNC", color = IndustrialYellow, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                    Text("Инженерная панель технолога", color = TextSecondary, fontSize = 12.sp)
                }
                BleStatusChip(isConnected = false)
            }
        }
        item { ActiveMachineCard("FANUC 0i-TF", "Сталь 40Х (ISO P)", "Точение наружное") }
        item {
            Text("ПАРАМЕТРЫ РЕЗАНИЯ", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardMetricCard("Мощность Pc", turning?.pc?.let { "%.2f".format(it) } ?: "—", "кВт", IndustrialCyan, Modifier.weight(1f))
                DashboardMetricCard("Износ VB", "0.18", "мм", IndustrialGreen, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardMetricCard("Скорость Vc", "200", "м/мин", IndustrialYellow, Modifier.weight(1f))
                DashboardMetricCard("Подача f", "0.20", "мм/об", TextPrimary, Modifier.weight(1f))
            }
        }
        item {
            Text("БЫСТРЫЙ ДОСТУП", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                QuickActionButton(Icons.Default.Calculate, "Расчёт сил", Modifier.weight(1f)) { onNavigateToCategory(0, 1) }
                QuickActionButton(Icons.Default.Code, "G-код", Modifier.weight(1f)) { onNavigateToCategory(1, 0) }
                QuickActionButton(Icons.Default.Monitor, "Стойка ЧПУ", Modifier.weight(1f)) { onNavigateToCategory(4, 0) }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth().border(1.dp, IndustrialYellow.copy(alpha = .55f), RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = IndustrialCardBg), shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = IndustrialYellow, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("КОРРЕКЦИЯ ИЗНОСА · T0101", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Рекомендация по сдвигу: Z −0.04 мм", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BleStatusChip(isConnected: Boolean) {
    Surface(color = IndustrialCardBg, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, TextSecondary.copy(alpha = .45f))) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(if (isConnected) IndustrialGreen else TextSecondary))
            Spacer(Modifier.width(6.dp))
            Text(if (isConnected) "BLE" else "OFFLINE", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActiveMachineCard(machineName: String, material: String, operation: String) {
    Card(Modifier.fillMaxWidth().border(1.dp, TextSecondary.copy(alpha = .35f), RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = IndustrialCardBg), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(machineName, color = IndustrialYellow, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.PrecisionManufacturing, null, tint = TextSecondary)
            }
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = IndustrialDarkBg)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Материал: $material", color = TextPrimary, fontSize = 12.sp)
                Text(operation, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(title: String, value: String, unit: String, statusColor: Color, modifier: Modifier) {
    Card(modifier.border(1.dp, TextSecondary.copy(alpha = .3f), RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = IndustrialCardBg), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(13.dp)) {
            Text(title.uppercase(), color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text(value, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(unit, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuickActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(72.dp), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, TextSecondary.copy(alpha = .4f)), colors = ButtonDefaults.outlinedButtonColors(containerColor = IndustrialCardBg), contentPadding = PaddingValues(4.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = IndustrialYellow)
            Spacer(Modifier.height(4.dp))
            Text(label, color = TextPrimary, fontSize = 10.sp)
        }
    }
}
