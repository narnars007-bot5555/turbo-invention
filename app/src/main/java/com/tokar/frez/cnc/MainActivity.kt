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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.data.db.AppDatabase
import com.tokar.frez.cnc.data.repository.CncRepository
import com.tokar.frez.cnc.ui.*
import com.tokar.frez.cnc.ui.screens.*
import com.tokar.frez.cnc.ui.screens.catalog.MachineCatalogScreen
import com.tokar.frez.cnc.ui.screens.gcode.GCodeGeneratorScreen
import com.tokar.frez.cnc.ui.screens.iso.IsoToleranceScreen
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

        val catalogViewModel = MachineCatalogViewModel(repository)
        val toolWearViewModel = ToolWearViewModel()
        val gcodeViewModel = GCodeViewModel()

        setContent {
            val state by mainViewModel.uiState.collectAsState()
            var isSettingsOpen by remember { mutableStateOf(false) }

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
                                            4 -> "СТАНКИ И ОСНАСТКА"
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
                                Triple("Станки", Icons.Default.PrecisionManufacturing, 4)
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
                                    0 -> DashboardScreen(
                                        viewModel = mainViewModel,
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
                                4 -> MachineCatalogScreen(catalogViewModel)
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
