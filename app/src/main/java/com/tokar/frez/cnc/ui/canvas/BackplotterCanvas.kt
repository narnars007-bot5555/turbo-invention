package com.tokar.frez.cnc.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.tokar.frez.cnc.domain.usecase.ToolpathPoint
import com.tokar.frez.cnc.ui.theme.IndustrialDarkBg

@Composable
fun BackplotterCanvas(
    toolpathPoints: List<ToolpathPoint>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
    ) {
        if (toolpathPoints.size < 2) return@Canvas

        val minX = toolpathPoints.minOf { it.x }.coerceAtMost(-10f)
        val maxX = toolpathPoints.maxOf { it.x }.coerceAtLeast(80f)
        val minY = toolpathPoints.minOf { it.y }.coerceAtMost(-60f)
        val maxY = toolpathPoints.maxOf { it.y }.coerceAtLeast(10f)

        val padding = 40f
        val scaleX = (size.width - 2 * padding) / (maxX - minX).coerceAtLeast(1f)
        val scaleY = (size.height - 2 * padding) / (maxY - minY).coerceAtLeast(1f)
        val scale = minOf(scaleX, scaleY)

        fun toScreenX(x: Float): Float = padding + (x - minX) * scale
        fun toScreenY(y: Float): Float = size.height - (padding + (y - minY) * scale)

        // Draw grid coordinate axes
        val axisX = toScreenX(0f)
        val axisY = toScreenY(0f)
        drawLine(
            color = Color.Gray.copy(alpha = 0.4f),
            start = Offset(0f, axisY),
            end = Offset(size.width, axisY),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Gray.copy(alpha = 0.4f),
            start = Offset(axisX, 0f),
            end = Offset(axisX, size.height),
            strokeWidth = 2f
        )

        // Draw backplot toolpath G0 (green dashed) & G1/G2/G3 (blue solid)
        for (i in 0 until toolpathPoints.size - 1) {
            val p1 = toolpathPoints[i]
            val p2 = toolpathPoints[i + 1]

            val start = Offset(toScreenX(p1.x), toScreenY(p1.y))
            val end = Offset(toScreenX(p2.x), toScreenY(p2.y))

            if (p2.isRapid) {
                drawLine(
                    color = Color(0xFF00FF66), // Green for G0 rapid
                    start = start,
                    end = end,
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )
            } else {
                drawLine(
                    color = Color(0xFF00E5FF), // Cyan/Blue for G1 feed
                    start = start,
                    end = end,
                    strokeWidth = 5f
                )
            }
        }
    }
}
