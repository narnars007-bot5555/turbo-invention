package com.tokar.frez.cnc.ui.screens.turning

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
import com.tokar.frez.cnc.domain.usecase.ComplexSurfaceMillingUseCase
import com.tokar.frez.cnc.domain.usecase.CuttingForceUseCase
import com.tokar.frez.cnc.ui.MainViewModel
import com.tokar.frez.cnc.ui.theme.*

@Composable
fun TurningMillingScreen(viewModel: MainViewModel, state: Any? = null) {
    val forceUseCase = remember { CuttingForceUseCase() }
    val complexMillingUseCase = remember { ComplexSurfaceMillingUseCase() }

    var cutterDiaText by remember { mutableStateOf("10.0") }
    var apMillingText by remember { mutableStateOf("0.5") }
    var aeMillingText by remember { mutableStateOf("0.4") }
    var fzText by remember { mutableStateOf("0.08") }
    var angleText by remember { mutableStateOf("30.0") }

    val ballNoseRes = remember(cutterDiaText, apMillingText, aeMillingText, fzText, angleText) {
        complexMillingUseCase.calculateBallNoseMilling(
            cutterDiameterMm = cutterDiaText.toDoubleOrNull() ?: 10.0,
            apMm = apMillingText.toDoubleOrNull() ?: 0.5,
            aeMm = aeMillingText.toDoubleOrNull() ?: 0.4,
            fzMmTeeth = fzText.toDoubleOrNull() ?: 0.08,
            surfaceAngleDeg = angleText.toDoubleOrNull() ?: 30.0
        )
    }

    var apText by remember { mutableStateOf("2.5") }
    var fnText by remember { mutableStateOf("0.25") }
    var kcText by remember { mutableStateOf("1800") }
    var diaText by remember { mutableStateOf("50.0") }
    var vcText by remember { mutableStateOf("160.0") }
    var clampingText by remember { mutableStateOf("15000") }

    val res = remember(apText, fnText, kcText, diaText, vcText, clampingText) {
        forceUseCase.calculateCuttingForces(
            apMm = apText.toDoubleOrNull() ?: 2.5,
            fnMmRev = fnText.toDoubleOrNull() ?: 0.25,
            kcNmm2 = kcText.toDoubleOrNull() ?: 1800.0,
            diameterMm = diaText.toDoubleOrNull() ?: 50.0,
            vcMmin = vcText.toDoubleOrNull() ?: 160.0,
            clampingForceN = clampingText.toDoubleOrNull() ?: 15000.0
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "ТОКАРНО-ФРЕЗЕРНАЯ ОБРАБОТКА И СИЛЫ РЕЗАНИЯ (Fc, Fp, Ff)",
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
                    value = apText,
                    onValueChange = { apText = it },
                    label = { Text("Глубина резания ap (мм)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fnText,
                    onValueChange = { fnText = it },
                    label = { Text("Подача fn (мм/об)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = kcText,
                    onValueChange = { kcText = it },
                    label = { Text("Удельная сила резания Kc (Н/мм²), e.g. Сталь=1800") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = clampingText,
                    onValueChange = { clampingText = it },
                    label = { Text("Усилие зажима кулачков/тисков (Н)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialCyan),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Complex Surface 3D Milling Section
        Text(
            text = "ФРЕЗЕРОВАНИЕ СЛОЖНЫХ 3D ПОВЕРХНОСТЕЙ (СФЕРИЧЕСКАЯ ФРЕЗА)",
            color = IndustrialYellow,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IndustrialCardBg),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cutterDiaText,
                        onValueChange = { cutterDiaText = it },
                        label = { Text("Диаметр фрезы D (мм)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = angleText,
                        onValueChange = { angleText = it },
                        label = { Text("Угол наклона θ (°)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = apMillingText,
                        onValueChange = { apMillingText = it },
                        label = { Text("Глубина ap (мм)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = aeMillingText,
                        onValueChange = { aeMillingText = it },
                        label = { Text("Шаг ae (мм)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text("ЭФФЕКТИВНЫЕ РЕЖИМЫ 3D ФРЕЗЕРОВАНИЯ:", color = IndustrialCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Эффективный диаметр Deff: ${ballNoseRes.effectiveDiameterMm} мм", color = TextPrimary, fontSize = 14.sp)
                Text("Эффективная скорость Vc_eff: ${ballNoseRes.effectiveVcMmin} м/мин (Обороты n: ${ballNoseRes.rpm} об/мин)", color = IndustrialGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Высота гребешка Scallop (Rz): ${ballNoseRes.scallopHeightRzUm} мкм", color = IndustrialYellow, fontSize = 14.sp)
                Text("Съем металла MRR: ${ballNoseRes.mrrCm3Min} см³/мин", color = TextPrimary, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IndustrialCardBg),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("РАССЧИТАННЫЕ СИЛЫ И МОЩНОСТЬ ВСПИ:", color = IndustrialYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Тангенциальная сила Fc = ${res.fcTangentialN} Н", color = IndustrialCyan, fontSize = 15.sp)
                Text("Радиальная сила Fp = ${res.fpRadialN} Н", color = TextPrimary, fontSize = 14.sp)
                Text("Осевая сила Ff = ${res.ffAxialN} Н", color = TextPrimary, fontSize = 14.sp)
                Text("Крутящий момент Mкр = ${res.torqueNm} Н·м", color = TextPrimary, fontSize = 14.sp)
                Text("Мощность резания Pc = ${res.powerKw} кВт", color = IndustrialGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Text("БЕЗОПАСНОСТЬ ЗАЖИМА ДЕТАЛИ:", color = if (res.isClampingSafe) IndustrialGreen else IndustrialRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Мин. требуемый зажим: ${res.minClampingForceN} Н (Коэфф. запаса: ${res.safetyFactor})", color = TextPrimary, fontSize = 13.sp)
                Text(
                    text = if (res.isClampingSafe) "✓ ЗАЖИМ БЕЗОПАСЕН: Вырыв детали из патрона предотвращен." else "⚠️ ВНИМАНИЕ! Риск вырыва детали. Увеличьте давление зажима патрона!",
                    color = if (res.isClampingSafe) IndustrialGreen else IndustrialRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
