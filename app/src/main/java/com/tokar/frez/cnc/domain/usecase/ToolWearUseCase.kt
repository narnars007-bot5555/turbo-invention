package com.tokar.frez.cnc.domain.usecase

import kotlin.math.max
import kotlin.math.min
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

data class ToolWearForecastResult(
    val toolId: String,
    val maxMeasuredWearMm: Double, // VB (мм)
    val maxAllowedWearMm: Double = 0.30, // Критический износ по фаске VB_max = 0.3 мм
    val remainingLifePercent: Double,
    val statusText: String,
    val requiresImmediateReplacement: Boolean,
    val causeDescription: String
)

class ToolWearUseCase {

    fun forecastToolLife(
        toolId: String,
        wearX: Double,
        wearZ: Double,
        wearY: Double = 0.0,
        operatingTimeMin: Double = 0.0,
        cause: String = "Абразивный"
    ): ToolWearForecastResult {
        val maxWear = max(wearX, max(wearZ, wearY))
        val maxAllowed = 0.30 // 0.3 мм по ISO 3685
        val ratio = maxWear / maxAllowed
        val remainingPercent = max(0.0, min(100.0, (1.0 - ratio) * 100.0))

        val needsReplace = remainingPercent <= 10.0 || maxWear >= maxAllowed
        val status = when {
            needsReplace -> "ВНИМАНИЕ: Пластина изношена! Требуется поворот или замена пластины."
            remainingPercent < 30.0 -> "ПРЕДУПРЕЖДЕНИЕ: Остаточный ресурс $remainingPercent%. Подготовьте новую пластину."
            else -> "НОРМА: Остаточный ресурс ${round(remainingPercent)}%."
        }

        val causeDesc = when (cause.lowercase()) {
            "абразивный" -> "Абразивный износ по задней поверхности VB. Причина: высокая твердость заготовки или недостаток СОЖ."
            "адгезионный" -> "Адгезионное наростообразование BUE. Причина: слишком низкая скорость резания Vc."
            "термический" -> "Термическое трещинообразование. Причина: циклический нагрев, пульсирующая подача СОЖ."
            "выкрашивание" -> "Выкрашивание режущей кромки (Chipping). Причина: прерывистое резание, жесткий затор."
            "поломка" -> "Катастрофическая поломка пластины. Причина: перегрузка по глубине ap или подаче fz."
            else -> "Классический износ по задней поверхности VB."
        }

        return ToolWearForecastResult(
            toolId = toolId,
            maxMeasuredWearMm = round(maxWear * 1000.0) / 1000.0,
            maxAllowedWearMm = maxAllowed,
            remainingLifePercent = round(remainingPercent * 10.0) / 10.0,
            statusText = status,
            requiresImmediateReplacement = needsReplace,
            causeDescription = causeDesc
        )
    }

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
