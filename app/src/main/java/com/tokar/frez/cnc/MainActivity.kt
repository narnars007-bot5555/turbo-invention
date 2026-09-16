package com.tokar.frez.cnc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.data.db.AppDatabase
import com.tokar.frez.cnc.data.repository.CncRepository
import com.tokar.frez.cnc.ui.*
import com.tokar.frez.cnc.ui.screens.dashboard.DashboardScreen
import com.tokar.frez.cnc.ui.screens.gear.GearCuttingScreen
import com.tokar.frez.cnc.ui.screens.grinding.GrindingScreen
import com.tokar.frez.cnc.ui.screens.catalog.MachineCatalogScreen
import com.tokar.frez.cnc.ui.screens.gcode.GCodeGeneratorScreen
import com.tokar.frez.cnc.ui.screens.iso.IsoToleranceScreen
import com.tokar.frez.cnc.ui.screens.turning.TurningMillingScreen
import com.tokar.frez.cnc.ui.screens.wear.ToolWearScreen
import com.tokar.frez.cnc.ui.theme.*

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = CncRepository(
            db.materialDao(),
            db.isoToleranceDao(),
            db.threadDao(),
            db.cncCycleDao(),
            db.toolFixtureDao(),
            db.machineDao()
        )

        val catalogViewModel = MachineCatalogViewModel(repository)
        val toolWearViewModel = ToolWearViewModel()
        val gcodeViewModel = GCodeViewModel()

        setContent {
            TokarFrezCncTheme {
                val state by mainViewModel.uiState.collectAsState()

                Scaffold(
                    topBar = {
                        ScrollableTabRow(
                            selectedTabIndex = state.selectedModuleIndex,
                            containerColor = IndustrialCardBg,
                            contentColor = IndustrialCyan,
                            edgePadding = 8.dp
                        ) {
                            val tabs = listOf(
                                "Обзор",
                                "1. Токарка/Фреза",
                                "2. Шлифовка",
                                "3. Зубья",
                                "4. ISO Допуски",
                                "5. Каталог Станков",
                                "6. Износ инструмента",
                                "7. G-код & Backplot"
                            )
                            tabs.forEachIndexed { idx, title ->
                                Tab(
                                    selected = state.selectedModuleIndex == idx,
                                    onClick = { mainViewModel.selectModule(idx) },
                                    modifier = Modifier.defaultMinSize(minHeight = 56.dp),
                                    text = {
                                        Text(
                                            text = title,
                                            fontSize = 13.sp,
                                            fontWeight = if (state.selectedModuleIndex == idx) FontWeight.Bold else FontWeight.Normal,
                                            color = if (state.selectedModuleIndex == idx) IndustrialCyan else TextPrimary
                                        )
                                    }
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
                        when (state.selectedModuleIndex) {
                            0 -> DashboardScreen(onSelectModule = { mainViewModel.selectModule(it) })
                            1 -> TurningMillingScreen(mainViewModel, state)
                            2 -> GrindingScreen(mainViewModel, state)
                            3 -> GearCuttingScreen(mainViewModel, state)
                            4 -> IsoToleranceScreen(mainViewModel, state)
                            5 -> MachineCatalogScreen(catalogViewModel)
                            6 -> ToolWearScreen(toolWearViewModel)
                            7 -> GCodeGeneratorScreen(gcodeViewModel)
                            else -> DashboardScreen(onSelectModule = { mainViewModel.selectModule(it) })
                        }
                    }
                }
            }
        }
    }
}
