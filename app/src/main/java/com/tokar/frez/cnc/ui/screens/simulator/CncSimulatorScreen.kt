package com.tokar.frez.cnc.ui.screens.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.data.models.CncModel
import com.tokar.frez.cnc.data.models.Hotspot
import com.tokar.frez.cnc.data.models.Scenario
import com.tokar.frez.cnc.ui.components.Cnc3DViewer
import com.tokar.frez.cnc.ui.components.CncPanelViewer
import com.tokar.frez.cnc.ui.theme.*

@Composable
fun CncSimulatorScreen(
    cncModel: CncModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(1) } // 0: 3D, 1: Panel, 2: Scenarios

    // Scenario execution state
    var activeScenario by remember { mutableStateOf<Scenario?>(null) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var scenarioCompleted by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
    ) {
        // Top Machine Model Title
        Surface(
            color = IndustrialCardBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "ИНТЕРАКТИВНЫЙ СИМУЛЯТОР СТОЙКИ",
                    color = IndustrialCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${cncModel.name} (${cncModel.type})",
                    color = IndustrialYellow,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3 Tabs Bar
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = IndustrialCardBg,
            contentColor = IndustrialCyan
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("1. 3D-УЗЛЫ", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("2. ПУЛЬТ ЧПУ", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("3. СЦЕНАРИИ", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            )
        }

        // Tab Contents
        when (selectedTab) {
            0 -> {
                Cnc3DViewer(modelPath = cncModel.model3d)
            }
            1 -> {
                CncPanelViewer(
                    model = cncModel,
                    onHotspotClick = { /* Hotspot clicked */ }
                )
            }
            2 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    if (activeScenario == null) {
                        // Scenario Selection List
                        Text("ОБУЧАЮЩИЕ ТЕХНОЛОГИЧЕСКИЕ СЦЕНАРИИ:", color = IndustrialYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        val scenarios = cncModel.scenarios.ifEmpty {
                            listOf(
                                Scenario(
                                    id = "ref_return",
                                    title = "Выход станка в ноль (REF / HOME)",
                                    steps = listOf(
                                        com.tokar.frez.cnc.data.models.ScenarioStep(1, "Нажмите и активируйте режим REF на пульте.", "btn_mode_ref"),
                                        com.tokar.frez.cnc.data.models.ScenarioStep(2, "Запустите зануление кнопкой CYCLE START.", "btn_cycle_start")
                                    )
                                ),
                                Scenario(
                                    id = "auto_run",
                                    title = "Запуск программы в режиме AUTO",
                                    steps = listOf(
                                        com.tokar.frez.cnc.data.models.ScenarioStep(1, "Выберите режим работы AUTO (MEM).", "btn_mode_auto"),
                                        com.tokar.frez.cnc.data.models.ScenarioStep(2, "Нажмите CYCLE START для выполнения.", "btn_cycle_start")
                                    )
                                )
                            )
                        }

                        scenarios.forEach { sc ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = IndustrialCardBg),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sc.title, color = IndustrialCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Шагов в сценарии: ${sc.steps.size}", color = TextSecondary, fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = {
                                            activeScenario = sc
                                            currentStepIndex = 0
                                            scenarioCompleted = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialCyan)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = IndustrialDarkBg)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("НАЧАТЬ", color = IndustrialDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        // Active Scenario Step Execution Screen
                        val currentStep = activeScenario?.steps?.getOrNull(currentStepIndex)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = IndustrialCardBg)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = activeScenario?.title ?: "",
                                        color = IndustrialYellow,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    OutlinedButton(
                                        onClick = { activeScenario = null },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IndustrialOrange)
                                    ) {
                                        Text("Сброс", fontSize = 11.sp)
                                    }
                                }

                                if (scenarioCompleted) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(color = IndustrialGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = IndustrialGreen)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("СЦЕНАРИЙ УСПЕШНО ВЫПОЛНЕН!", color = IndustrialGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                } else if (currentStep != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "ШАГ ${currentStep.step} из ${activeScenario?.steps?.size}:",
                                        color = IndustrialCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = currentStep.instruction,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Подсказка: Нажмите подсвеченную жёлтым кнопку на пульте ниже.",
                                        color = IndustrialOrange,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Hotspot Panel View in Training Mode
                        Box(modifier = Modifier.weight(1f)) {
                            CncPanelViewer(
                                model = cncModel,
                                targetHotspotId = if (!scenarioCompleted) currentStep?.targetHotspot else null,
                                onHotspotClick = { clickedHs ->
                                    if (currentStep != null && clickedHs.id == currentStep.targetHotspot) {
                                        if (currentStepIndex + 1 < (activeScenario?.steps?.size ?: 0)) {
                                            currentStepIndex++
                                        } else {
                                            scenarioCompleted = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
