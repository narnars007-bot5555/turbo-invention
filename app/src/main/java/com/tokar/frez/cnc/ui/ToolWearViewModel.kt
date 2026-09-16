package com.tokar.frez.cnc.ui

import androidx.lifecycle.ViewModel
import com.tokar.frez.cnc.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ToolWearUiState(
    val nominalMm: Double = 50.0,
    val actualMm: Double = 49.92,
    val measuredZNominalMm: Double = 100.0,
    val measuredZActualMm: Double = 99.85,
    val mode: CompensationMode = CompensationMode.DIAMETER,
    val cncSystem: CncSystemType = CncSystemType.FANUC,
    val result: ToolWearResult? = null
)

class ToolWearViewModel(
    private val toolWearUseCase: ToolWearUseCase = ToolWearUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToolWearUiState())
    val uiState: StateFlow<ToolWearUiState> = _uiState.asStateFlow()

    init {
        calculate()
    }

    fun updateInputs(
        nominal: Double = _uiState.value.nominalMm,
        actual: Double = _uiState.value.actualMm,
        zNominal: Double = _uiState.value.measuredZNominalMm,
        zActual: Double = _uiState.value.measuredZActualMm,
        mode: CompensationMode = _uiState.value.mode,
        cncSystem: CncSystemType = _uiState.value.cncSystem
    ) {
        _uiState.update {
            it.copy(
                nominalMm = nominal,
                actualMm = actual,
                measuredZNominalMm = zNominal,
                measuredZActualMm = zActual,
                mode = mode,
                cncSystem = cncSystem
            )
        }
        calculate()
    }

    private fun calculate() {
        val s = _uiState.value
        val res = toolWearUseCase.calculateToolWear(
            nominalMm = s.nominalMm,
            actualMm = s.actualMm,
            measuredAxisZActualMm = s.measuredZActualMm,
            measuredAxisZNominalMm = s.measuredZNominalMm,
            mode = s.mode,
            cncSystem = s.cncSystem
        )
        _uiState.update { it.copy(result = res) }
    }
}
