package com.tokar.frez.cnc.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GearToothCanvas(
    module: Double,
    teeth: Int,
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
            text = "ГЕОМЕТРИЯ ЭВОЛЬВЕНТНОГО ЗУБА (m=$module, z=$teeth)",
            color = IndustrialYellow,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(IndustrialDarkBg)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h + 80f

                val scale = 2.5f
                val rPitch = (module * teeth / 2.0 * scale).toFloat()
                val rTip = (module * (teeth + 2) / 2.0 * scale).toFloat()
                val rRoot = (module * (teeth - 2.5) / 2.0 * scale).toFloat()

                drawCircle(
                    color = IndustrialCyan,
                    radius = rPitch,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2f)
                )

                drawCircle(
                    color = IndustrialGreen,
                    radius = rTip,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2f)
                )

                drawCircle(
                    color = IndustrialOrange,
                    radius = rRoot,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2f)
                )

                val toothAngle = (2.0 * Math.PI / teeth.toDouble()).toFloat()
                val path = Path()

                for (i in -2..2) {
                    val aCenter = -Math.PI.toFloat() / 2f + i * toothAngle
                    val aLeft = aCenter - toothAngle * 0.25f
                    val aRight = aCenter + toothAngle * 0.25f

                    val xRootL = cx + rRoot * cos(aLeft)
                    val yRootL = cy + rRoot * sin(aLeft)
                    val xTipL = cx + rTip * cos(aLeft + 0.05f)
                    val yTipL = cy + rTip * sin(aLeft + 0.05f)

                    val xTipR = cx + rTip * cos(aRight - 0.05f)
                    val yTipR = cy + rTip * sin(aRight - 0.05f)
                    val xRootR = cx + rRoot * cos(aRight)
                    val yRootR = cy + rRoot * sin(aRight)

                    if (i == -2) path.moveTo(xRootL, yRootL)
                    path.lineTo(xTipL, yTipL)
                    path.lineTo(xTipR, yTipR)
                    path.lineTo(xRootR, yRootR)
                }

                drawPath(
                    path = path,
                    color = IndustrialYellow,
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}

@Composable
fun ThreadProfileCanvas(
    pitch: Double,
    threadHeight: Double,
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
            text = "ПРОФИЛЬ МЕТРИЧЕСКОЙ РЕЗЬБЫ ISO 68-1 (P=$pitch мм, H1=$threadHeight мм)",
            color = IndustrialYellow,
            fontSize = 14.sp,
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

                val pPx = (pitch * 50.0).toFloat().coerceIn(60f, w / 4f)
                val hPx = (threadHeight * 50.0).toFloat().coerceIn(40f, h * 0.6f)

                val startY = h * 0.2f
                val endY = startY + hPx

                val path = Path()
                var currentX = 20f
                path.moveTo(currentX, startY)

                while (currentX < w - pPx) {
                    val crestX1 = currentX + pPx * 0.125f
                    val rootX = currentX + pPx * 0.5f
                    val crestX2 = currentX + pPx * 0.875f
                    val nextCrest = currentX + pPx

                    path.lineTo(crestX1, startY)
                    path.lineTo(rootX, endY)
                    path.lineTo(crestX2, startY)
                    path.lineTo(nextCrest, startY)

                    currentX = nextCrest
                }

                drawPath(
                    path = path,
                    color = IndustrialCyan,
                    style = Stroke(width = 4f)
                )

                drawLine(
                    color = IndustrialGreen,
                    start = Offset(0f, startY + hPx * 0.5f),
                    end = Offset(w, startY + hPx * 0.5f),
                    strokeWidth = 2f
                )
            }
        }
    }
}
