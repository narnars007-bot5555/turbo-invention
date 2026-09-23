package com.tokar.frez.cnc.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.tokar.frez.cnc.domain.usecase.ToolpathPoint
import com.tokar.frez.cnc.ui.theme.IndustrialDarkBg

@Composable
fun BackplotterCanvas(
    toolpathPoints: List<ToolpathPoint>,
    stockLengthMm: Float = 60f,
    stockDiameterMm: Float = 60f,
    animProgress: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0.0f) }
    var panOffsetY by remember { mutableFloatStateOf(0.0f) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.5f, 5.0f)
                    panOffsetX += pan.x
                    panOffsetY += pan.y
                }
            }
    ) {
        if (toolpathPoints.size < 2) return@Canvas

        val minX = toolpathPoints.minOf { it.x }.coerceAtMost(-10f)
        val maxX = toolpathPoints.maxOf { it.x }.coerceAtLeast(80f)
        val minY = toolpathPoints.minOf { it.y }.coerceAtMost(-70f)
        val maxY = toolpathPoints.maxOf { it.y }.coerceAtLeast(10f)

        val padding = 40f
        val baseScaleX = (size.width - 2 * padding) / (maxX - minX).coerceAtLeast(1f)
        val baseScaleY = (size.height - 2 * padding) / (maxY - minY).coerceAtLeast(1f)
        val scale = minOf(baseScaleX, baseScaleY) * zoomScale

        fun toScreenX(x: Float): Float = (padding + (x - minX) * scale) + panOffsetX
        fun toScreenY(y: Float): Float = (size.height - (padding + (y - minY) * scale)) + panOffsetY

        // 1. Draw Stock Workpiece Box (Заготовка)
        val stockStartX = toScreenX(0f)
        val stockStartY = toScreenY(stockDiameterMm)
        val stockWidth = stockLengthMm * scale
        val stockHeight = stockDiameterMm * scale

        drawRect(
            color = Color(0xFF4A6572).copy(alpha = 0.35f),
            topLeft = Offset(stockStartX, stockStartY),
            size = Size(stockWidth, stockHeight)
        )
        drawRect(
            color = Color(0xFF00E5FF).copy(alpha = 0.5f),
            topLeft = Offset(stockStartX, stockStartY),
            size = Size(stockWidth, stockHeight),
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
        )

        // 2. Draw Grid coordinate axes X/Z
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

        // 3. Draw Toolpath segments
        val limitCount = (toolpathPoints.size * animProgress).toInt().coerceIn(1, toolpathPoints.size)

        for (i in 0 until limitCount - 1) {
            val p1 = toolpathPoints[i]
            val p2 = toolpathPoints[i + 1]

            val start = Offset(toScreenX(p1.x), toScreenY(p1.y))
            val end = Offset(toScreenX(p2.x), toScreenY(p2.y))

            when {
                p2.isRapid -> {
                    // G0 Rapid movement (Green dashed)
                    drawLine(
                        color = Color(0xFF00FF66),
                        start = start,
                        end = end,
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )
                }
                else -> {
                    // G1/G2/G3 Working Feed (Cyan solid)
                    drawLine(
                        color = Color(0xFF00E5FF),
                        start = start,
                        end = end,
                        strokeWidth = 4f
                    )
                }
            }
        }

        // 4. Draw Tool marker (Инструмент / T-Point)
        val currentP = toolpathPoints[limitCount - 1]
        val toolPos = Offset(toScreenX(currentP.x), toScreenY(currentP.y))

        drawCircle(
            color = Color(0xFFFF3D00),
            radius = 12f,
            center = toolPos
        )
        drawCircle(
            color = Color.White,
            radius = 6f,
            center = toolPos
        )
    }
}
