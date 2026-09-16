package com.tokar.frez.cnc.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.ui.theme.*

@Composable
fun ToolOrientationCanvas(
    selectedT: Int,
    onSelectT: (Int) -> Unit,
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
            text = "ОРИЕНТАЦИЯ ВЕРШИНЫ РЕЗЦА (T1 - T9)",
            color = IndustrialYellow,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .size(240.dp)
                .background(IndustrialDarkBg)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f

                drawLine(
                    color = IndustrialCardBorder,
                    start = Offset(0f, cy),
                    end = Offset(w, cy),
                    strokeWidth = 2f
                )
                drawLine(
                    color = IndustrialCardBorder,
                    start = Offset(cx, 0f),
                    end = Offset(cx, h),
                    strokeWidth = 2f
                )

                drawCircle(
                    color = Color.Gray.copy(alpha = 0.3f),
                    radius = 40f,
                    center = Offset(cx, cy)
                )

                val points = mapOf(
                    1 to Offset(cx - 70f, cy - 70f),
                    2 to Offset(cx + 70f, cy - 70f),
                    3 to Offset(cx + 70f, cy + 70f),
                    4 to Offset(cx - 70f, cy + 70f),
                    5 to Offset(cx + 80f, cy),
                    6 to Offset(cx, cy - 80f),
                    7 to Offset(cx - 80f, cy),
                    8 to Offset(cx, cy + 80f),
                    9 to Offset(cx, cy)
                )

                points.forEach { (tNum, pos) ->
                    val isSelected = tNum == selectedT
                    val color = if (isSelected) IndustrialCyan else TextSecondary
                    val radius = if (isSelected) 18f else 12f

                    drawCircle(
                        color = color,
                        radius = radius,
                        center = pos
                    )

                    val path = Path().apply {
                        moveTo(pos.x, pos.y - radius * 1.5f)
                        lineTo(pos.x - radius, pos.y + radius)
                        lineTo(pos.x + radius, pos.y + radius)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = if (isSelected) IndustrialYellow else Color.Transparent,
                        style = Stroke(width = 3f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..9).chunked(3).forEach { row ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { t ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (t == selectedT) IndustrialCyan else IndustrialCardBorder
                                )
                                .clickable { onSelectT(t) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "T$t",
                                color = if (t == selectedT) IndustrialDarkBg else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
