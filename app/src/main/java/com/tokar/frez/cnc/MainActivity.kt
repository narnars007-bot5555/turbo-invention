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
import com.tokar.frez.cnc.ui.MainViewModel
import com.tokar.frez.cnc.ui.screens.*
import com.tokar.frez.cnc.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TokarFrezCncTheme {
                val state by viewModel.uiState.collectAsState()

                Scaffold(
                    topBar = {
                        ScrollableTabRow(
                            selectedTabIndex = state.selectedModuleIndex,
                            containerColor = IndustrialCardBg,
                            contentColor = IndustrialCyan,
                            edgePadding = 8.dp
                        ) {
                            val tabs = listOf("Обзор", "1. Токарка/Фреза", "2. Шлифовка", "3. Зубья", "4. Оснастка", "5. ISO Допуски", "6. СЧПУ", "7. Нормы/Брак")
                            tabs.forEachIndexed { idx, title ->
                                Tab(
                                    selected = state.selectedModuleIndex == idx,
                                    onClick = { viewModel.selectModule(idx) },
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
                            0 -> DashboardScreen(onSelectModule = { viewModel.selectModule(it) })
                            1 -> TurningMillingScreen(viewModel, state)
                            2 -> GrindingScreen(viewModel, state)
                            3 -> GearCuttingScreen(viewModel, state)
                            4 -> ToolFixtureScreen()
                            5 -> IsoStandardsScreen(viewModel, state)
                            6 -> CncSetupScreen(viewModel, state)
                            7 -> TimeAndDefectsScreen(viewModel, state)
                            else -> DashboardScreen(onSelectModule = { viewModel.selectModule(it) })
                        }
                    }
                }
            }
        }
    }
}
