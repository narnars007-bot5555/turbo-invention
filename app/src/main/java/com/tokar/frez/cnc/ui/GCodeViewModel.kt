package com.tokar.frez.cnc.ui

import androidx.lifecycle.ViewModel
import com.tokar.frez.cnc.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GCodeUiState(
    val cycleType: String = "G71_CYCLE95",
    val startDiameterMm: Double = 60.0,
    val endDiameterMm: Double = 40.0,
    val lengthMm: Double = 50.0,
    val depthOfCutMm: Double = 2.0,
    val feedRate: Double = 0.25,
    val cncSystem: CncSystemType = CncSystemType.FANUC,
    val generatedGCode: String = "",
    val toolpathPoints: List<ToolpathPoint> = emptyList(),
    val validationIssues: List<GCodeValidationIssue> = emptyList()
)

class GCodeViewModel(
    private val gcodeEngine: GCodeEngineUseCase = GCodeEngineUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GCodeUiState())
    val uiState: StateFlow<GCodeUiState> = _uiState.asStateFlow()

    init {
        generateAndParse()
    }

    fun updateCycleParams(
        cycleType: String = _uiState.value.cycleType,
        startDia: Double = _uiState.value.startDiameterMm,
        endDia: Double = _uiState.value.endDiameterMm,
        length: Double = _uiState.value.lengthMm,
        depth: Double = _uiState.value.depthOfCutMm,
        feed: Double = _uiState.value.feedRate,
        system: CncSystemType = _uiState.value.cncSystem
    ) {
        _uiState.update {
            it.copy(
                cycleType = cycleType,
                startDiameterMm = startDia,
                endDiameterMm = endDia,
                lengthMm = length,
                depthOfCutMm = depth,
                feedRate = feed,
                cncSystem = system
            )
        }
        generateAndParse()
    }

    private fun generateAndParse() {
        val s = _uiState.value
        val params = GCodeGenerationParams(
            cycleType = s.cycleType,
            startDiameterMm = s.startDiameterMm,
            endDiameterMm = s.endDiameterMm,
            lengthMm = s.lengthMm,
            depthOfCutMm = s.depthOfCutMm,
            feedRate = s.feedRate
        )
        val code = gcodeEngine.generateCycleGCode(params, s.cncSystem)
        val points = gcodeEngine.parseToolpath(code)
        val issues = gcodeEngine.validateGCode(code, s.cncSystem)
        _uiState.update {
            it.copy(generatedGCode = code, toolpathPoints = points, validationIssues = issues)
        }
    }
}
