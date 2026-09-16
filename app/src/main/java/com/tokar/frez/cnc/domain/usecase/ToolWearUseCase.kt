package com.tokar.frez.cnc.domain.usecase

import kotlin.math.round

enum class CompensationMode {
    RADIUS, DIAMETER
}

enum class CncSystemType {
    FANUC, SINUMERIK, HAAS, HEIDENHAIN
}

data class ToolWearResult(
    val nominalSize: Double,
    val actualMeasuredSize: Double,
    val mode: CompensationMode,
    val cncSystem: CncSystemType,
    val deltaXUm: Double,
    val deltaZUm: Double,
    val deltaXmm: Double,
    val deltaZmm: Double,
    val inputHint: String
)

class ToolWearUseCase {

    fun calculateToolWear(
        nominalMm: Double,
        actualMm: Double,
        measuredAxisZActualMm: Double = 0.0,
        measuredAxisZNominalMm: Double = 0.0,
        mode: CompensationMode = CompensationMode.DIAMETER,
        cncSystem: CncSystemType = CncSystemType.FANUC
    ): ToolWearResult {
        val rawDeltaX = nominalMm - actualMm
        val deltaX = if (mode == CompensationMode.DIAMETER) rawDeltaX else rawDeltaX / 2.0
        val deltaZ = measuredAxisZNominalMm - measuredAxisZActualMm

        val deltaXUm = round(deltaX * 1000.0 * 10.0) / 10.0
        val deltaZUm = round(deltaZ * 1000.0 * 10.0) / 10.0
        val deltaXmm = round(deltaX * 10000.0) / 10000.0
        val deltaZmm = round(deltaZ * 10000.0) / 10000.0

        val hint = when (cncSystem) {
            CncSystemType.FANUC -> "Fanuc: Перейдите в OFFSET -> WEAR, введите X = $deltaXmm и нажмите [INPUT C] или [+INPUT]."
            CncSystemType.SINUMERIK -> "Sinumerik: В меню 'Корректировка инструмента' перейдите в столбец 'Износ X' и введите $deltaXmm или через приращение."
            CncSystemType.HAAS -> "Haas: Нажмите OFFSET, выберите номер инструмента, введите $deltaXmm в колонку X WEAR и нажмите [ENTER] / [TOOL OFFSET INCR+]."
            CncSystemType.HEIDENHAIN -> "Heidenhain: В таблице инструментов TOOL.T введите значение DR = $deltaXmm для радиуса или DX = $deltaXmm."
        }

        return ToolWearResult(
            nominalSize = nominalMm,
            actualMeasuredSize = actualMm,
            mode = mode,
            cncSystem = cncSystem,
            deltaXUm = deltaXUm,
            deltaZUm = deltaZUm,
            deltaXmm = deltaXmm,
            deltaZmm = deltaZmm,
            inputHint = hint
        )
    }
}
