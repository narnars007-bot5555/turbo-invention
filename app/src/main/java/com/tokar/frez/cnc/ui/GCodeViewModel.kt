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
    val parsedBlocks: List<ParsedGCodeBlock> = emptyList(),
    val estimatedTimeSec: Double = 0.0,
    val totalDistanceMm: Double = 0.0,
    val selectedMachine: MachineProfile = MachineProfiles.default,
    val toolNumber: Int = 1,
    val spindleRpm: Int = 1000,
    val stockAllowancePerSideMm: Double = 3.0,
    val cuttingSpeedVc: Double = 200.0,
    val validationErrors: List<String> = emptyList(),
    val validationWarnings: List<String> = emptyList()
)

class GCodeViewModel(
    private val gcodeEngine: GCodeEngineUseCase = GCodeEngineUseCase(),
    private val gcodeParser: GCodeParserUseCase = GCodeParserUseCase(),
    private val safeGenerator: GCodeGeneratorUseCase = GCodeGeneratorUseCase()
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

    fun selectMachine(profile: MachineProfile) {
        _uiState.update { it.copy(selectedMachine = profile) }
        generateAndParse()
    }

    fun updateSafetyInputs(toolNumber: Int = _uiState.value.toolNumber, spindleRpm: Int = _uiState.value.spindleRpm, stockAllowance: Double = _uiState.value.stockAllowancePerSideMm, cuttingSpeedVc: Double = _uiState.value.cuttingSpeedVc) {
        _uiState.update { it.copy(toolNumber = toolNumber, spindleRpm = spindleRpm, stockAllowancePerSideMm = stockAllowance, cuttingSpeedVc = cuttingSpeedVc) }
        generateAndParse()
    }

    fun parseCustomGCode(rawCode: String) {
        val parseResult = gcodeParser.parseProgram(rawCode)
        _uiState.update {
            it.copy(
                generatedGCode = rawCode,
                toolpathPoints = parseResult.toolpathPoints,
                parsedBlocks = parseResult.blocks,
                estimatedTimeSec = parseResult.estimatedTimeSec,
                totalDistanceMm = parseResult.totalDistanceMm
            )
        }
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
        val safeResult = safeGenerator.generate(
            GCodeValidationInput(params, s.toolNumber, s.spindleRpm, s.stockAllowancePerSideMm, s.cuttingSpeedVc),
            s.selectedMachine
        )
        val code = safeResult.program.orEmpty()
        val parseResult = if (code.isBlank()) null else gcodeParser.parseProgram(code)

        _uiState.update {
            it.copy(
                generatedGCode = code,
                toolpathPoints = parseResult?.toolpathPoints.orEmpty(),
                parsedBlocks = parseResult?.blocks.orEmpty(),
                estimatedTimeSec = parseResult?.estimatedTimeSec ?: 0.0,
                totalDistanceMm = parseResult?.totalDistanceMm ?: 0.0,
                validationErrors = safeResult.errors,
                validationWarnings = safeResult.warnings
            )
        }
    }
}
