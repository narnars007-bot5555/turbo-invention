package com.tokar.frez.cnc.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.data.entity.MachineEntity
import com.tokar.frez.cnc.ui.MachineCatalogViewModel
import com.tokar.frez.cnc.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun MachineCatalogScreen(viewModel: MachineCatalogViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "КАТАЛОГ СТАНКОВ И СТОЕК ЧПУ",
            color = IndustrialCyan,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Select Machine Class
        Text("1. Выберите класс станка:", color = TextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
        val machineClasses = state.machineClasses.ifEmpty {
            listOf(
                "Вертикально-фрезерные (VMC)",
                "Горизонтально-фрезерные (HMC)",
                "5-осевые ОЦ (5-Axis MC)",
                "Токарные и токарно-фрезерные",
                "Автоматы продольного точения (Swiss)",
                "Зубообрабатывающие (Gear)",
                "Шлифовальная группа (Grinding)"
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            machineClasses.take(3).forEach { cls ->
                Button(
                    onClick = { viewModel.selectClass(cls) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.selectedClass == cls) IndustrialCyan else IndustrialCardBg,
                        contentColor = if (state.selectedClass == cls) IndustrialDarkBg else TextPrimary
                    ),
                    modifier = Modifier.defaultMinSize(minHeight = 56.dp)
                ) {
                    Text(cls, fontSize = 11.sp, maxLines = 2)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // 2. Select CNC System
        if (state.selectedClass != null) {
            Text("2. Выберите стойку ЧПУ:", color = TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            val systems = listOf("FANUC", "SINUMERIK", "HAAS", "HEIDENHAIN")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                systems.forEach { sys ->
                    FilterChip(
                        selected = state.selectedCncSystem == sys,
                        onClick = { viewModel.selectCncSystem(sys) },
                        label = { Text(sys, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.defaultMinSize(minHeight = 56.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. Machine Card (2-Tab Layout)
        val dummyMachine = MachineEntity(
            id = 1,
            machineClass = state.selectedClass ?: "5-Axis MC",
            cncSystem = state.selectedCncSystem ?: "HAAS",
            modelName = "HAAS UMC-750 (DWO/TCPC)",
            description = "5-осевой обработочный центр с наклонно-поворотным столом. Обороты шпинделя: 12000 об/мин, SK40.",
            setupGuideStepsJson = """[
                "1. Включение питания, снятие E-Stop, выход в ноль (Power Up / Restart).",
                "2. Прогрев шпинделя (Spindle Warm-up): 1000 -> 5000 -> 12000 об/мин (15 мин).",
                "3. Установка заготовки, выверка тисков/патрона индикатором (Pupitaster, 0.001 мм).",
                "4. Привязка инструмента: автоматическая датчиком Renishaw TS27R / Blum Laser.",
                "5. Привязка детали (WCS G54-G59) датчиком OMP40 / 3D-Tester.",
                "6. Для 5 осей: вычисление центра вращения стола (COR) и настройка DWO/TCPC (G143).",
                "7. Проход на высотной Z-плоскости (+100 мм) в режиме Single Block / Dry Run."
            ]""",
            gcodeHandbookJson = """{
                "G00": "Быстрое позиционирование холостого хода",
                "G01": "Линейная интерполяция рабочей подачи",
                "G02/G03": "Круговая интерполяция по/против часовой стрелки",
                "G43.4 / G143": "Инструментальная коррекция TCPC в 5 осях",
                "G254 / CYCLE800": "Динамический разворот плоскости обработки (DWO / Вращение плоскости)",
                "M03 S...": "Включение шпинделя по часовой стрелке",
                "M08 / M09": "СОЖ Включить / Выключить"
            }"""
        )

        val activeMachine = state.selectedMachine ?: dummyMachine

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IndustrialCardBg),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = activeMachine.modelName,
                    color = IndustrialYellow,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = activeMachine.description,
                    color = TextPrimary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = state.selectedTab,
                    containerColor = IndustrialDarkBg,
                    contentColor = IndustrialCyan
                ) {
                    Tab(
                        selected = state.selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        text = { Text("1. НАЛАДКА И ПОДГОТОВКА", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.defaultMinSize(minHeight = 56.dp)
                    )
                    Tab(
                        selected = state.selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        text = { Text("2. G-КОДЫ И ПУЛЬТ", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.defaultMinSize(minHeight = 56.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (state.selectedTab == 0) {
                    Text("Инструкция по наладке станка к обработке:", color = IndustrialGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    val stepsList = parseSetupSteps(activeMachine.setupGuideStepsJson)
                    stepsList.forEach { step ->
                        Text(text = step, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                    }
                } else {
                    Text("Справочник G-кодов и пульта ЧПУ (${activeMachine.cncSystem}):", color = IndustrialCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    val gcodeMap = parseGCodeMap(activeMachine.gcodeHandbookJson)
                    gcodeMap.forEach { (code, desc) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(text = code, color = IndustrialYellow, fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
                            Text(text = desc, color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun parseSetupSteps(json: String): List<String> {
    return try {
        val list = mutableListOf<String>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        list
    } catch (e: Exception) {
        listOf(json)
    }
}

private fun parseGCodeMap(json: String): Map<String, String> {
    return try {
        val map = mutableMapOf<String, String>()
        val obj = JSONObject(json)
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = obj.getString(key)
        }
        map
    } catch (e: Exception) {
        mapOf("G-Code" to json)
    }
}
