package com.tokar.frez.cnc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.ui.theme.*

enum class MachineAssemblyNode {
    SPINDLE,        // Шпиндель
    TURRET,         // Суппорт / Револьверная головка
    TOOL_MAGAZINE,  // Магазин инструментов
    WORK_TABLE      // Рабочий стол / Патрон
}

@Composable
fun Cnc3DViewer(
    modelPath: String? = null,
    modifier: Modifier = Modifier
) {
    var selectedNode by remember { mutableStateOf(MachineAssemblyNode.SPINDLE) }
    var rotationAngleX by remember { mutableFloatStateOf(20f) }
    var rotationAngleY by remember { mutableFloatStateOf(35f) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(12.dp)
    ) {
        Text(
            text = "3D-ИНСПЕКТОР УЗЛОВ СТАНКА (3D-Model Inspector)",
            color = IndustrialCyan,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Assembly Nodes Toggle Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val nodes = listOf(
                MachineAssemblyNode.SPINDLE to "Шпиндель",
                MachineAssemblyNode.TURRET to "Револьвер",
                MachineAssemblyNode.TOOL_MAGAZINE to "Магазин",
                MachineAssemblyNode.WORK_TABLE to "Патрон/Стол"
            )
            nodes.forEach { (node, label) ->
                FilterChip(
                    selected = selectedNode == node,
                    onClick = { selectedNode = node },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive 3D Canvas Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = IndustrialCardBg),
            shape = RoundedCornerShape(10.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.0f)
                                rotationAngleY += pan.x * 0.5f
                                rotationAngleX = (rotationAngleX - pan.y * 0.5f).coerceIn(-60f, 60f)
                            }
                        }
                ) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f

                    // Render simulated 3D Machine Base
                    drawRect(
                        color = Color(0xFF263238),
                        topLeft = Offset(cx - 140f * zoomScale, cy + 40f * zoomScale),
                        size = Size(280f * zoomScale, 100f * zoomScale)
                    )

                    // Spindle node (Highlited if selected)
                    val spindleColor = if (selectedNode == MachineAssemblyNode.SPINDLE) IndustrialYellow else Color(0xFF78909C)
                    drawRect(
                        color = spindleColor,
                        topLeft = Offset(cx - 100f * zoomScale, cy - 80f * zoomScale),
                        size = Size(80f * zoomScale, 120f * zoomScale)
                    )

                    // Turret node (Highlighted if selected)
                    val turretColor = if (selectedNode == MachineAssemblyNode.TURRET) IndustrialCyan else Color(0xFF546E7A)
                    drawCircle(
                        color = turretColor,
                        radius = 45f * zoomScale,
                        center = Offset(cx + 60f * zoomScale, cy - 20f * zoomScale)
                    )

                    // Tool Magazine node (Highlighted if selected)
                    val magColor = if (selectedNode == MachineAssemblyNode.TOOL_MAGAZINE) IndustrialGreen else Color(0xFF37474F)
                    drawRect(
                        color = magColor,
                        topLeft = Offset(cx - 160f * zoomScale, cy - 140f * zoomScale),
                        size = Size(50f * zoomScale, 140f * zoomScale)
                    )

                    // Work table / Chuck node (Highlighted if selected)
                    val tableColor = if (selectedNode == MachineAssemblyNode.WORK_TABLE) IndustrialOrange else Color(0xFF455A64)
                    drawCircle(
                        color = tableColor,
                        radius = 35f * zoomScale,
                        center = Offset(cx - 20f * zoomScale, cy - 20f * zoomScale)
                    )
                }

                // Information Overlay Card
                Surface(
                    color = IndustrialDarkBg.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = when (selectedNode) {
                                MachineAssemblyNode.SPINDLE -> "УЗЕЛ: Высокоскоростной шпиндель (12000 об/мин, SK40)"
                                MachineAssemblyNode.TURRET -> "УЗЕЛ: 12-позиционная револьверная головка BMT55"
                                MachineAssemblyNode.TOOL_MAGAZINE -> "УЗЕЛ: Автоматический магазин на 30 инструментов"
                                MachineAssemblyNode.WORK_TABLE -> "УЗЕЛ: 3-кулачковый гидравлический патрон D210 мм"
                            },
                            color = IndustrialYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Управление 3D: Вращение — провести пальцем | Зум — двухпальцевый жест",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
