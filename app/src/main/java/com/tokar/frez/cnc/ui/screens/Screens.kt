package com.tokar.frez.cnc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.ui.MainUiState
import com.tokar.frez.cnc.ui.MainViewModel
import com.tokar.frez.cnc.ui.canvas.*
import com.tokar.frez.cnc.ui.theme.*

@Composable
fun DashboardScreen(
    onSelectModule: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val modules = listOf(
        "1. Токарно-фрезерные расчеты" to "Vc, n, f, fz, MRR, Pc, Ra/Rz, Конусы",
        "2. Шлифование и абразив" to "Vs, Vw, ae, Декодер кругов, Правка Ud/Ed",
        "3. Зубообработка" to "Эвольвента m/z, Длина нормали W, Зубодолбление",
        "4. База инструмента и оснастки" to "Резцы, фрезы, оправки BT40/HSK, микрометры",
        "5. ISO Стандарты и Допуски" to "ISO 286-1 валы/отверстия, Резьбы ISO 965, Пластины ISO 1832",
        "6. Наладка СЧПУ и G-коды" to "Fanuc, Sinumerik, Haas, Heidenhain, Привязка G54, T1-T9",
        "7. Нормирование Tшт и Брак" to "Машинное время, Стойкость Тейлора, Матрица брака"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "TOKAR-FREZ-CNC",
            color = IndustrialCyan,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Инженерно-технологический комплекс машиностроителя",
            color = TextSecondary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        modules.forEachIndexed { index, (title, subtitle) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSelectModule(index + 1) },
                colors = CardDefaults.cardColors(containerColor = IndustrialCardBg),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        color = IndustrialYellow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TurningMillingScreen(viewModel: MainViewModel, state: MainUiState) {
    var diaText by remember { mutableStateOf("50") }
    var vcText by remember { mutableStateOf("200") }
    var feedText by remember { mutableStateOf("0.2") }
    var apText by remember { mutableStateOf("2.0") }
    var rEpsText by remember { mutableStateOf("0.8") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("МОДУЛЬ 1: ТОКАРНО-ФРЕЗЕРНЫЕ РАСЧЕТЫ", color = IndustrialCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = IndustrialCardBg), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Расчет токарных режимов", color = IndustrialYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = diaText, onValueChange = { diaText = it }, label = { Text("Диаметр D (мм)") })
                OutlinedTextField(value = vcText, onValueChange = { vcText = it }, label = { Text("Скорость Vc (м/мин)") })
                OutlinedTextField(value = feedText, onValueChange = { feedText = it }, label = { Text("Подача f (мм/об)") })
                OutlinedTextField(value = apText, onValueChange = { apText = it }, label = { Text("Глубина ap (мм)") })
                OutlinedTextField(value = rEpsText, onValueChange = { rEpsText = it }, label = { Text("Радиус резца r_eps (мм)") })

                Button(
                    onClick = {
                        val d = diaText.toDoubleOrNull() ?: 50.0
                        val vc = vcText.toDoubleOrNull() ?: 200.0
                        val f = feedText.toDoubleOrNull() ?: 0.2
                        val ap = apText.toDoubleOrNull() ?: 2.0
                        val r = rEpsText.toDoubleOrNull() ?: 0.8
                        viewModel.calculateTurning(d, vc, null, f, ap, r)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialCyan),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("РАССЧИТАТЬ", color = IndustrialDarkBg, fontWeight = FontWeight.Bold)
                }

                state.turningResult?.let { res ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Частота вращения n: ${res.n.toInt()} об/мин", color = TextPrimary)
                    Text("Съем металла MRR: ${String.format("%.1f", res.mrr)} см³/мин", color = IndustrialGreen)
                    Text("Мощность Pc: ${String.format("%.2f", res.pc)} кВт", color = IndustrialYellow)
                    Text("Шероховатость Ra: ${String.format("%.2f", res.ra)} мкм (Rz: ${String.format("%.2f", res.rz)} мкм)", color = TextPrimary)
                }
            }
        }
    }
}

@Composable
fun GrindingScreen(viewModel: MainViewModel, state: MainUiState) {
    var codeText by remember { mutableStateOf("25A F60 K 5 V") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("МОДУЛЬ 2: ШЛИФОВАНИЕ И АБРАЗИВ", color = IndustrialCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = IndustrialCardBg), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Декодер маркировки шлифовальных кругов", color = IndustrialYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = codeText, onValueChange = { codeText = it }, label = { Text("Маркировка круга (ГОСТ / ISO)") })

                Button(
                    onClick = { viewModel.decodeWheelMarking(codeText) },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialCyan),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("РАСШИФРОВАТЬ", color = IndustrialDarkBg, fontWeight = FontWeight.Bold)
                }

                state.wheelDecoded?.let { dec ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Абразив: ${dec.abrasiveType}", color = TextPrimary)
                    Text("Зернистость: ${dec.grainSize}", color = TextPrimary)
                    Text("Твердость: ${dec.hardness}", color = TextPrimary)
                    Text("Связка: ${dec.bondType}", color = IndustrialGreen)
                }
            }
        }
    }
}

@Composable
fun GearCuttingScreen(viewModel: MainViewModel, state: MainUiState) {
    var moduleText by remember { mutableStateOf("3.0") }
    var teethText by remember { mutableStateOf("30") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("МОДУЛЬ 3: ЗУБООБРАБОТКА", color = IndustrialCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = IndustrialCardBg), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Геометрия эвольвентного зацепления", color = IndustrialYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = moduleText, onValueChange = { moduleText = it }, label = { Text("Модуль m (мм)") })
                OutlinedTextField(value = teethText, onValueChange = { teethText = it }, label = { Text("Число зубьев z") })

                Button(
                    onClick = {
                        val m = moduleText.toDoubleOrNull() ?: 3.0
                        val z = teethText.toIntOrNull() ?: 30
                        viewModel.calculateGear(m, z, 20.0, 0)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialCyan),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("РАССЧИТАТЬ", color = IndustrialDarkBg, fontWeight = FontWeight.Bold)
                }

                state.gearResult?.let { g ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Делительный диаметр d: ${g.pitchDiameter} мм", color = TextPrimary)
                    Text("Диаметр вершин da: ${g.tipDiameter} мм", color = IndustrialGreen)
                    Text("Диаметр впадин df: ${g.rootDiameter} мм", color = TextPrimary)
                    Text("Длина общей нормали W: ${String.format("%.3f", g.baseTangentLength)} мм (на k=${g.recommendedSpanTeeth} зубьях)", color = IndustrialYellow)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        val m = moduleText.toDoubleOrNull() ?: 3.0
        val z = teethText.toIntOrNull() ?: 30
        GearToothCanvas(module = m, teeth = z)
    }
}

@Composable
fun ToolFixtureScreen() {
    val items = listOf(
        "Токарные резцы ISO 1832" to "PCLNR 2525M12, SVJCR 2020K16, SDJCR 2525M11",
        "Фрезерный инструмент" to "Фреза концевая Z4 D10, Насадная фреза D63 Z5",
        "Оснастка (BT40/HSK63)" to "BT40-ER32-100, HSK63A-C20-105, Гидропатрон BT40-HC20",
        "Измерительный инструмент" to "Микрометр МК 25-50 0.01, Штангенциркуль ШЦ-I-125",
        "Калибры ПР/НЕ" to "Пробка M12x1.75 6H, Кольцо M12x1.75 6g"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
    ) {
        Text("МОДУЛЬ 4: БАЗА ИНСТРУМЕНТА И ОСНАСТКИ", color = IndustrialCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(items) { (name, desc) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = IndustrialCardBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(name, color = IndustrialYellow, fontWeight = FontWeight.Bold)
                        Text(desc, color = TextPrimary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun IsoStandardsScreen(viewModel: MainViewModel, state: MainUiState) {
    var sizeText by remember { mutableStateOf("40") }
    var fieldText by remember { mutableStateOf("H7") }
    var threadCode by remember { mutableStateOf("M12x1.75") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("МОДУЛЬ 5: ISO СТАНДАРТЫ И ДОПУСКИ", color = IndustrialCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = IndustrialCardBg), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ISO 286-1 Калькулятор допусков", color = IndustrialYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = sizeText, onValueChange = { sizeText = it }, label = { Text("Номинал (мм)") })
                OutlinedTextField(value = fieldText, onValueChange = { fieldText = it }, label = { Text("Поле допуска (H7, h6, js16)") })

                Button(
                    onClick = {
                        val s = sizeText.toDoubleOrNull() ?: 40.0
                        viewModel.calculateTolerance(s, fieldText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialCyan),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("РАССЧИТАТЬ ДОПУСК", color = IndustrialDarkBg, fontWeight = FontWeight.Bold)
                }

                state.toleranceResult?.let { tol ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Верхнее отклонение ES/es: ${tol.esUpperUm} мкм", color = IndustrialGreen)
                    Text("Нижнее отклонение EI/ei: ${tol.eiLowerUm} мкм", color = IndustrialRed)
                    Text("Предельные размеры: ${tol.minLimitMm} .. ${tol.maxLimitMm} мм", color = TextPrimary)
                }
            }
        }

        state.toleranceResult?.let { tol ->
            Spacer(modifier = Modifier.height(16.dp))
            ToleranceZoneCanvas(
                nominalSize = tol.nominalSize,
                field = tol.toleranceField,
                upperUm = tol.esUpperUm,
                lowerUm = tol.eiLowerUm
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = IndustrialCardBg), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ISO 965 / ISO 68-1 Метрические резьбы", color = IndustrialYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = threadCode, onValueChange = { threadCode = it }, label = { Text("Обозначение резьбы (e.g. M12x1.75)") })

                Button(
                    onClick = { viewModel.calculateThread(threadCode) },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialCyan),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("РАССЧИТАТЬ РЕЗЬБУ", color = IndustrialDarkBg, fontWeight = FontWeight.Bold)
                }

                state.threadResult?.let { tr ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Средний диаметр d2: ${tr.pitchDiameter} мм", color = TextPrimary)
                    Text("Внутренний диаметр d1: ${tr.minorDiameter} мм", color = TextPrimary)
                    Text("Диаметр сверла под резьбу: ${tr.tapDrillDiameter} мм", color = IndustrialGreen, fontWeight = FontWeight.Bold)
                }
            }
        }

        state.threadResult?.let { tr ->
            Spacer(modifier = Modifier.height(16.dp))
            ThreadProfileCanvas(pitch = tr.pitch, threadHeight = tr.threadHeight)
        }
    }
}

@Composable
fun CncSetupScreen(viewModel: MainViewModel, state: MainUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("МОДУЛЬ 6: НАЛАДКА СТАНКОВ И СЧПУ", color = IndustrialCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        ToolOrientationCanvas(
            selectedT = state.selectedT,
            onSelectT = { viewModel.setSelectedT(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = IndustrialCardBg), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Выход в ноль и привязка WCS (G54-G59)", color = IndustrialYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("1. Выход в ноль станка (G28 X0 Z0 / REF MODE)", color = TextPrimary)
                Text("2. Метод пробного реза: точение D -> измерение микрометром -> ввод X_val в G54", color = TextPrimary)
                Text("3. Привязка инструмента: вылет L (Z) и радиус R с ориентацией T${state.selectedT}", color = IndustrialGreen)
            }
        }
    }
}

@Composable
fun TimeAndDefectsScreen(viewModel: MainViewModel, state: MainUiState) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
    ) {
        Text("МОДУЛЬ 7: НОРМИРОВАНИЕ Tшт И БРАК", color = IndustrialCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchDefects(it)
            },
            label = { Text("Поиск брака и дефектов (вибрации, прижоги, сколы)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(state.defectList) { defect ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = IndustrialCardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(defect.defectName, color = IndustrialYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(defect.category, color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Причины:", color = IndustrialOrange, fontWeight = FontWeight.Bold)
                        defect.probableCauses.forEach { cause ->
                            Text("• $cause", color = TextPrimary, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Устранение:", color = IndustrialGreen, fontWeight = FontWeight.Bold)
                        defect.correctiveActions.forEach { action ->
                            Text("• $action", color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
