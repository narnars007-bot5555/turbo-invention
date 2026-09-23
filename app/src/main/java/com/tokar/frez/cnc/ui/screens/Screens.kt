package com.tokar.frez.cnc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.ui.AppThemeMode
import com.tokar.frez.cnc.ui.MainUiState
import com.tokar.frez.cnc.ui.MainViewModel
import com.tokar.frez.cnc.ui.canvas.*
import com.tokar.frez.cnc.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    state: MainUiState,
    onNavigateToCategory: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
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
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(14.dp))

        // Global Search Bar
        OutlinedTextField(
            value = state.globalSearchQuery,
            onValueChange = { viewModel.setGlobalSearchQuery(it) },
            placeholder = { Text("Поиск по модулям, материалам, G-кодам...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IndustrialCyan) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IndustrialCyan,
                unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Search Results display if searching
        if (state.searchResults.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = IndustrialCardBg),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("РЕЗУЛЬТАТЫ ПОИСКА:", color = IndustrialYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    state.searchResults.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToCategory(item.categoryIndex, item.subModuleIndex) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, color = IndustrialCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(item.description, color = TextPrimary, fontSize = 12.sp)
                            }
                        }
                        HorizontalDivider(color = IndustrialDarkBg)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Favorites Section (Избранное)
        if (state.favorites.isNotEmpty()) {
            Text("ИЗБРАННЫЕ МОДУЛИ", color = IndustrialYellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.favorites.take(3).forEach { fav ->
                    AssistChip(
                        onClick = {
                            when (fav) {
                                "Токарно-фрезерные расчеты" -> onNavigateToCategory(0, 1)
                                "ISO 286-1 Допуски" -> onNavigateToCategory(3, 4)
                                "Черновое G71/CYCLE95" -> onNavigateToCategory(1, 7)
                                else -> onNavigateToCategory(0, 1)
                            }
                        },
                        label = { Text(fav, fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = IndustrialYellow, modifier = Modifier.size(14.dp)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = IndustrialCardBg, labelColor = TextPrimary)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Recent Calculations Section (Последние использованные расчёты)
        if (state.recentCalculations.isNotEmpty()) {
            Text("ПОСЛЕДНИЕ РАСЧЕТЫ", color = IndustrialGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = IndustrialCardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    state.recentCalculations.take(2).forEach { rec ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = IndustrialCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("${rec.title} (${rec.category})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(rec.summary, color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 5 Primary Categories Cards
        Text("КАТЕГОРИИ ФУНКЦИЙ", color = IndustrialCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        val categories = listOf(
            Triple("1. РАСЧЁТЫ (Точение, Фрезерование, Шлифовка, Зубья)", "Vc, n, f, fz, MRR, Pc, Ra/Rz, Конусы, Модуль эвольвенты", 0),
            Triple("2. G-КОД И ЧПУ (Безопасный генератор и Backplotter)", "G71/G76/G83, Проверка безопасности, Пошаговый симулятор, .nc экспорт", 1),
            Triple("3. ИНСТРУМЕНТ И ИЗНОС (Журнал T01-T99 и ISO 1832)", "Учет износа X/Z, Прогноз ресурса %, Замена пластин, Расшифровка ISO", 2),
            Triple("4. ISO И СПРАВОЧНИКИ (Допуски ISO 286-1 и Резьбы)", "Предельные отклонения ES/EI, Метрические резьбы ISO 965, Матрица брака", 3),
            Triple("5. СТАНКИ И ОСНАСТКА (Каталог и Наладка WCS)", "HAAS, Fanuc, Sinumerik, Выход в ноль, Привязка G54, Оправки BT40/HSK", 4)
        )

        categories.forEach { (title, desc, catIdx) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onNavigateToCategory(catIdx, 1) },
                colors = CardDefaults.cardColors(containerColor = IndustrialCardBg),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(title, color = IndustrialYellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(desc, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: MainViewModel, state: MainUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("НАСТРОЙКИ ПРИЛОЖЕНИЯ И ИНТЕРФЕЙСА", color = IndustrialCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IndustrialCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Тема оформления:", color = IndustrialYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.appTheme == AppThemeMode.DARK,
                        onClick = { viewModel.setAppTheme(AppThemeMode.DARK) },
                        label = { Text("Промышленная Тёмная") }
                    )
                    FilterChip(
                        selected = state.appTheme == AppThemeMode.LIGHT,
                        onClick = { viewModel.setAppTheme(AppThemeMode.LIGHT) },
                        label = { Text("Светлая") }
                    )
                    FilterChip(
                        selected = state.appTheme == AppThemeMode.SYSTEM,
                        onClick = { viewModel.setAppTheme(AppThemeMode.SYSTEM) },
                        label = { Text("Системная") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Формат десятичных чисел:", color = IndustrialYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.decimalPrecision == 2,
                        onClick = { viewModel.setDecimalPrecision(2) },
                        label = { Text("2 знака (0.01)") }
                    )
                    FilterChip(
                        selected = state.decimalPrecision == 3,
                        onClick = { viewModel.setDecimalPrecision(3) },
                        label = { Text("3 знака (0.001)") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Система единиц измерения:", color = IndustrialYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.isMetric,
                        onClick = { viewModel.setMetricUnits(true) },
                        label = { Text("Метрическая (мм, м/мин)") }
                    )
                    FilterChip(
                        selected = !state.isMetric,
                        onClick = { viewModel.setMetricUnits(false) },
                        label = { Text("Дюймовая (inch, SFM)") }
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
