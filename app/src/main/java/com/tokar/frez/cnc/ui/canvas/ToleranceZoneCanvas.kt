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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.ui.theme.*

@Composable
fun ToleranceZoneCanvas(
    nominalSize: Double,
    field: String,
    upperUm: Double,
    lowerUm: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(IndustrialCardBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ДИАГРАММА ПОЛЯ ДОПУСКА $nominalSize $field (ES/es: ${if (upperUm >= 0) "+$upperUm" else upperUm} мкм, EI/ei: ${if (lowerUm >= 0) "+$lowerUm" else lowerUm} мкм)",
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

                drawLine(
                    color = IndustrialGreen,
                    start = Offset(w * 0.25f, rectTop),
                    end = Offset(w * 0.7f, rectTop),
                    strokeWidth = 2f
                )

                drawLine(
                    color = IndustrialRed,
                    start = Offset(w * 0.25f, rectTop + rectHeight),
                    end = Offset(w * 0.7f, rectTop + rectHeight),
                    strokeWidth = 2f
                )
            }
        }
    }
}
