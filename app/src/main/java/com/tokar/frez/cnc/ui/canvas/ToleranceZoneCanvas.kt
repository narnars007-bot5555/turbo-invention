package com.tokar.frez.cnc.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.domain.usecase.FitType
import com.tokar.frez.cnc.domain.usecase.IsoMatingResult
import com.tokar.frez.cnc.ui.theme.*

@Composable
fun ToleranceZoneCanvas(
    nominalSize: Double,
    field: String,
    upperUm: Double,
    lowerUm: Double,
    matingResult: IsoMatingResult? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(IndustrialCardBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val titleText = if (matingResult != null) {
            "ПОСАДКА ISO: Ø$nominalSize ${matingResult.holeField}/${matingResult.shaftField} " +
                    "(${when (matingResult.fitType) {
                        FitType.CLEARANCE -> "ЗАЗОР Smax=${matingResult.maxClearanceUm}мкм"
                        FitType.INTERFERENCE -> "НАТЯГ Nmax=${matingResult.maxInterferenceUm}мкм"
                        FitType.TRANSITION -> "ПЕРЕХОДНАЯ"
                    }})"
        } else {
            "ДИАГРАММА ПОЛЯ ДОПУСКА $nominalSize $field (ES/es: ${if (upperUm >= 0) "+$upperUm" else upperUm} мкм, EI/ei: ${if (lowerUm >= 0) "+$lowerUm" else lowerUm} мкм)"
        }

        Text(
            text = titleText,
            color = IndustrialYellow,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(IndustrialDarkBg)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val zeroY = h / 2f

                drawLine(
                    color = TextPrimary,
                    start = Offset(20f, zeroY),
                    end = Offset(w - 20f, zeroY),
                    strokeWidth = 3f
                )

                if (matingResult != null) {
                    val holeUpper = (matingResult.holeMaxMm - nominalSize) * 1000f
                    val holeLower = (matingResult.holeMinMm - nominalSize) * 1000f
                    val shaftUpper = (matingResult.shaftMaxMm - nominalSize) * 1000f
                    val shaftLower = (matingResult.shaftMinMm - nominalSize) * 1000f

                    val scale = 1.2f

                    // Draw Hole (left blue box)
                    val hTop = zeroY - (holeUpper * scale).toFloat()
                    val hBot = zeroY - (holeLower * scale).toFloat()
                    drawRect(
                        color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                        topLeft = Offset(w * 0.15f, Math.min(hTop, hBot)),
                        size = Size(w * 0.3f, Math.abs(hBot - hTop).coerceAtLeast(8f))
                    )

                    // Draw Shaft (right box color coded)
                    val fitColor = when (matingResult.fitType) {
                        FitType.CLEARANCE -> Color(0xFF00E676)   // Green = Clearance
                        FitType.INTERFERENCE -> Color(0xFFFF1744) // Red = Interference
                        FitType.TRANSITION -> Color(0xFFFFEA00)   // Yellow = Transition
                    }

                    val sTop = zeroY - (shaftUpper * scale).toFloat()
                    val sBot = zeroY - (shaftLower * scale).toFloat()
                    drawRect(
                        color = fitColor.copy(alpha = 0.5f),
                        topLeft = Offset(w * 0.55f, Math.min(sTop, sBot)),
                        size = Size(w * 0.3f, Math.abs(sBot - sTop).coerceAtLeast(8f))
                    )

                } else {
                    val scaleFactor = 1.5f
                    val topY = zeroY - (upperUm * scaleFactor).toFloat().coerceIn(-zeroY + 20f, zeroY - 20f)
                    val bottomY = zeroY - (lowerUm * scaleFactor).toFloat().coerceIn(-zeroY + 20f, zeroY - 20f)

                    val rectTop = Math.min(topY, bottomY)
                    val rectHeight = Math.abs(topY - bottomY).coerceAtLeast(10f)

                    val isHole = field.firstOrNull()?.isUpperCase() == true
                    val zoneColor = if (isHole) IndustrialCyan else IndustrialOrange

                    drawRect(
                        color = zoneColor.copy(alpha = 0.4f),
                        topLeft = Offset(w * 0.35f, rectTop),
                        size = Size(w * 0.3f, rectHeight)
                    )

                    drawRect(
                        color = zoneColor,
                        topLeft = Offset(w * 0.35f, rectTop),
                        size = Size(w * 0.3f, rectHeight),
                        style = Stroke(width = 3f)
                    )
                }
            }
        }
    }
}
