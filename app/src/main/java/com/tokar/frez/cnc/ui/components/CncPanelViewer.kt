package com.tokar.frez.cnc.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.data.models.CncModel
import com.tokar.frez.cnc.data.models.Hotspot
import com.tokar.frez.cnc.ui.theme.*
import kotlin.math.hypot

@Composable
fun CncPanelViewer(
    model: CncModel,
    targetHotspotId: String? = null,
    onHotspotClick: (Hotspot) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeHotspotForSheet by remember { mutableStateOf<Hotspot?>(null) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0.0f) }
    var panOffsetY by remember { mutableFloatStateOf(0.0f) }

    val density = LocalDensity.current

    // Pulsing ring animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadiusScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.8f, 4.0f)
                        panOffsetX += pan.x
                        panOffsetY += pan.y
                    }
                }
                .pointerInput(model.hotspots, targetHotspotId) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height

                        // Find clicked hotspot
                        val clicked = model.hotspots.find { hs ->
                            val hx = (hs.x_percent / 100f) * w * zoomScale + panOffsetX
                            val hy = (hs.y_percent / 100f) * h * zoomScale + panOffsetY
                            val radiusPx = with(density) { hs.radius_dp.dp.toPx() } * zoomScale
                            hypot(tapOffset.x - hx, tapOffset.y - hy) <= radiusPx * 1.5f
                        }

                        clicked?.let { hs ->
                            activeHotspotForSheet = hs
                            onHotspotClick(hs)
                        }
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // Background Panel Simulation Box
            drawRect(
                color = Color(0xFF1B2228),
                topLeft = Offset(panOffsetX, panOffsetY),
                size = androidx.compose.ui.geometry.Size(canvasW * zoomScale, canvasH * zoomScale)
            )

            // Draw hotspots vector layer
            model.hotspots.forEach { hs ->
                val hx = (hs.x_percent / 100f) * canvasW * zoomScale + panOffsetX
                val hy = (hs.y_percent / 100f) * canvasH * zoomScale + panOffsetY
                val baseRadiusPx = density.run { hs.radius_dp.dp.toPx() } * zoomScale

                val isTarget = targetHotspotId != null && hs.id == targetHotspotId
                val isDimmed = targetHotspotId != null && !isTarget

                val ringColor = when {
                    isTarget -> Color(0xFFFFD600) // Glowing yellow for scenario target
                    isDimmed -> Color.Gray.copy(alpha = 0.2f)
                    else -> Color(0xFF00E5FF) // Cyan for active
                }

                // Pulsing outer ring
                drawCircle(
                    color = ringColor.copy(alpha = if (isDimmed) 0.1f else 0.35f),
                    radius = baseRadiusPx * pulseRadiusScale,
                    center = Offset(hx, hy)
                )

                // Hotspot core button
                drawCircle(
                    color = ringColor.copy(alpha = if (isDimmed) 0.3f else 0.85f),
                    radius = baseRadiusPx,
                    center = Offset(hx, hy)
                )

                // Button border
                drawCircle(
                    color = Color.White.copy(alpha = if (isDimmed) 0.2f else 0.9f),
                    radius = baseRadiusPx,
                    center = Offset(hx, hy),
                    style = Stroke(width = 3f)
                )
            }
        }

        // Hotspot BottomSheet Detail Dialog
        activeHotspotForSheet?.let { hs ->
            AlertDialog(
                onDismissRequest = { activeHotspotForSheet = null },
                containerColor = IndustrialCardBg,
                shape = RoundedCornerShape(12.dp),
                title = {
                    Column {
                        Text(
                            text = hs.title,
                            color = IndustrialYellow,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Категория: ${hs.category}",
                            color = IndustrialCyan,
                            fontSize = 12.sp
                        )
                    }
                },
                text = {
                    Text(
                        text = hs.description,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { activeHotspotForSheet = null }) {
                        Text("ЗАКРЫТЬ", color = IndustrialCyan, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
