package com.tokar.frez.cnc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tokar.frez.cnc.data.db.AppDatabase
import com.tokar.frez.cnc.data.models.CncModel
import com.tokar.frez.cnc.data.models.Hotspot
import com.tokar.frez.cnc.data.models.Scenario
import com.tokar.frez.cnc.data.models.ScenarioStep
import com.tokar.frez.cnc.data.repository.CncRepository
import com.tokar.frez.cnc.ui.*
import com.tokar.frez.cnc.ui.screens.*
import com.tokar.frez.cnc.ui.screens.catalog.MachineCatalogScreen
import com.tokar.frez.cnc.ui.screens.gcode.GCodeGeneratorScreen
import com.tokar.frez.cnc.ui.screens.iso.IsoToleranceScreen
import com.tokar.frez.cnc.ui.screens.simulator.CncSimulatorScreen
import com.tokar.frez.cnc.ui.screens.turning.TurningMillingScreen
import com.tokar.frez.cnc.ui.screens.wear.ToolWearScreen
import com.tokar.frez.cnc.ui.theme.*

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = CncRepository(
            db.materialDao(),
            db.isoToleranceDao(),
            db.threadDao(),
            db.cncCycleDao(),
            db.toolFixtureDao(),
            db.machineDao(),
            db.calculationHistoryDao(),
            db.toolWearJournalDao()
        )

        setContent {
            val state by mainViewModel.uiState.collectAsState()
            var isSettingsOpen by remember { mutableStateOf(false) }
            val context = LocalContext.current

            val cncDbState by repository.cncDatabaseState.collectAsState()

            val toolWearViewModel: ToolWearViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return ToolWearViewModel(repository = repository) as T
                    }
                }
            )

            val catalogViewModel: MachineCatalogViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return MachineCatalogViewModel(repository) as T
                    }
                }
            )

            val gcodeViewModel: GCodeViewModel = viewModel()

            LaunchedEffect(Unit) {
                repository.loadCncDatabaseFromAssets(context)
            }

            val defaultFanucModel = CncModel(
                id = "fanuc_0i_tf",
                name = "Fanuc 0i-TF Plus",
                type = "Токарная обработка",
                imagePanel = "panels/fanuc_0i_tf.jpg",
                model3d = "models/lathe_fanuc.glb",
                hotspots = listOf(
                    Hotspot("btn_mode_auto", "Режим AUTO (MEM)", 65.4f, 82.1f, 22, "Автоматический режим исполнения программы.", "Режимы работы"),
                    Hotspot("btn_mode_ref", "Режим REF / HOME", 60.1f, 82.1f, 22, "Режим выхода станка в физический ноль.", "Режимы работы"),
                    Hotspot("btn_cycle_start", "CYCLE START", 88.5f, 88.0f, 28, "Запуск выполнения программы.", "Управление")
                ),
                scenarios = listOf(
                    Scenario(
                        id = "ref_return",
                        title = "Выход станка в ноль (REF)",
                        steps = listOf(
                            ScenarioStep(1, "Нажмите и активируйте режим REF на пульте.", "btn_mode_ref"),
                            ScenarioStep(2, "Запустите зануление кнопкой CYCLE START.", "btn_cycle_start")
                        )
                    )
                )
            )

            TokarFrezCncTheme(useDarkTheme = state.appTheme != AppThemeMode.LIGHT) {
                Scaffold(
                    topBar = {
                        Column {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = when (state.selectedCategory) {
                                            0 -> "РАСЧЁТЫ РЕЖИМОВ"
                                            1 -> "G-КОД И ЧПУ"
                                            2 -> "ИНСТРУМЕНТ И ИЗНОС"
                                            3 -> "ISO И СПРАВОЧНИКИ"
                                            4 -> "СТАНКИ И СИМУЛЯТОР СТОЙКИ"
                                            else -> "TOKAR-FREZ-CNC"
                                        },
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IndustrialCyan
                                    )
                                },
                                actions = {
                                    IconButton(onClick = { isSettingsOpen = !isSettingsOpen }) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Настройки",
                                            tint = IndustrialYellow
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = IndustrialCardBg
                                )
                            )

                            // Sub-navigation bar when in Category 0 (Calculations)
                            if (state.selectedCategory == 0 && !isSettingsOpen) {
                                ScrollableTabRow(
                                    selectedTabIndex = state.selectedModuleIndex,
                                    containerColor = IndustrialCardBg,
                                    contentColor = IndustrialCyan,
                                    edgePadding = 8.dp
                                ) {
                                    val calcTabs = listOf("Обзор", "Токарка/Фреза", "Шлифование", "Зубообработка")
                                    calcTabs.forEachIndexed { idx, title ->
                                        Tab(
                                            selected = state.selectedModuleIndex == idx,
                                            onClick = { mainViewModel.selectModule(idx) },
                                            modifier = Modifier.defaultMinSize(minHeight = 44.dp),
                                            text = {
                                                Text(
                                                    text = title,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (state.selectedModuleIndex == idx) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (state.selectedModuleIndex == idx) IndustrialCyan else TextPrimary
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = IndustrialCardBg,
                            contentColor = IndustrialCyan
                        ) {
                            val items = listOf(
                                Triple("Расчёты", Icons.Default.Calculate, 0),
                                Triple("G-код", Icons.Default.Code, 1),
                                Triple("Износ", Icons.Default.Build, 2),
                                Triple("ISO", Icons.Default.MenuBook, 3),
                                Triple("Симулятор", Icons.Default.PrecisionManufacturing, 4)
                            )
                            items.forEach { (label, icon, catIdx) ->
                                NavigationBarItem(
                                    selected = state.selectedCategory == catIdx && !isSettingsOpen,
                                    onClick = {
                                        isSettingsOpen = false
                                        mainViewModel.selectCategory(catIdx)
                                    },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = IndustrialDarkBg,
                                        selectedTextColor = IndustrialCyan,
                                        indicatorColor = IndustrialCyan,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .background(IndustrialDarkBg)
                    ) {
                        if (isSettingsOpen) {
                            SettingsScreen(mainViewModel, state)
                        } else {
                            when (state.selectedCategory) {
                                0 -> when (state.selectedModuleIndex) {
                                    0 -> VisualDashboardScreen(
                                        state = state,
                                        onNavigateToCategory = { cat, subMod ->
                                            mainViewModel.selectCategory(cat)
                                            mainViewModel.selectModule(subMod)
                                        }
                                    )
                                    1 -> TurningMillingScreen(mainViewModel, state)
                                    2 -> GrindingScreen(mainViewModel, state)
                                    3 -> GearCuttingScreen(mainViewModel, state)
                                    else -> DashboardScreen(
                                        viewModel = mainViewModel,
                                        state = state,
                                        onNavigateToCategory = { cat, subMod ->
                                            mainViewModel.selectCategory(cat)
                                            mainViewModel.selectModule(subMod)
                                        }
                                    )
                                }
                                1 -> GCodeGeneratorScreen(gcodeViewModel)
                                2 -> ToolWearScreen(toolWearViewModel)
                                3 -> IsoToleranceScreen(mainViewModel, state)
                                4 -> {
                                    val activeCncModel = cncDbState?.brands?.firstOrNull()?.models?.firstOrNull() ?: defaultFanucModel
                                    CncSimulatorScreen(cncModel = activeCncModel)
                                }
                                else -> DashboardScreen(
                                    viewModel = mainViewModel,
                                    state = state,
                                    onNavigateToCategory = { cat, subMod ->
                                        mainViewModel.selectCategory(cat)
                                        mainViewModel.selectModule(subMod)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
