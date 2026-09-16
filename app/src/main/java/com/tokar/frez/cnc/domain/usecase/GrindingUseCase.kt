package com.tokar.frez.cnc.domain.usecase

import com.tokar.frez.cnc.domain.model.*
import kotlin.math.PI

class GrindingUseCase {

    fun calculateGrinding(params: GrindingParams): GrindingResult {
        val vs = (PI * params.wheelDiameter * params.wheelRpm) / 60000.0
        val vw = (PI * params.workpieceDiameter * params.workpieceRpm) / 1000.0
        val q = if (vw > 0) (vs * 60.0) / vw else 0.0
        val depthMm = params.depthOfCut / 1000.0

        return GrindingResult(
            vs = vs,
            vw = vw,
            speedRatio = q,
            depthMm = depthMm
        )
    }

    fun calculateDressing(params: DressingParams): DressingResult {
        val ed = if (params.dressingFeed > 0) params.diamondTipWidth / params.dressingFeed else 0.0
        val ud = params.dressingFeed

        return DressingResult(
            ud = ud,
            ed = ed
        )
    }

    fun decodeWheelMarking(code: String): WheelMarkingDecoded {
        val cleanCode = code.trim().uppercase()

        var abrasive = "Электрокорунд / Карбид (Стандарт)"
        var grain = "F60 (Среднее зерно)"
        var hardness = "K/L (Средней мягкости)"
        var structure = "5-6 (Среднее движение пор)"
        var bond = "V (Керамическая связка)"

        when {
            cleanCode.contains("25A") -> abrasive = "25А - Электрокорунд белый (High purity alumina)"
            cleanCode.contains("14A") -> abrasive = "14А - Электрокорунд нормальный (Regular alumina)"
            cleanCode.contains("64C") -> abrasive = "64С - Карбид кремния зеленый (Green silicon carbide)"
            cleanCode.contains("CBN") || cleanCode.contains("ЭЛЬБОР") -> abrasive = "CBN / Эльбор (Cubic Boron Nitride)"
            cleanCode.contains("DIAMOND") || cleanCode.contains("АС") -> abrasive = "Diamond / Алмаз синтетический"
        }

        when {
            cleanCode.contains("F36") || cleanCode.contains("36") -> grain = "F36 / 36 (Черновое зерно ~500 мкм)"
            cleanCode.contains("F46") || cleanCode.contains("46") -> grain = "F46 / 46 (Получерновое ~370 мкм)"
            cleanCode.contains("F60") || cleanCode.contains("60") -> grain = "F60 / 60 (Чистовое ~250 мкм)"
            cleanCode.contains("F80") || cleanCode.contains("80") -> grain = "F80 / 80 (Мелкое чистовое ~180 мкм)"
            cleanCode.contains("F120") || cleanCode.contains("120") -> grain = "F120 / 120 (Особенно тонкое ~100 мкм)"
        }

        when {
            cleanCode.contains(" H ") || cleanCode.endsWith("H") -> hardness = "H - Мягкий (Soft)"
            cleanCode.contains(" I ") || cleanCode.endsWith("I") -> hardness = "I - Мягкий (Soft)"
            cleanCode.contains(" J ") || cleanCode.endsWith("J") -> hardness = "J - Средне-мягкий (Medium soft)"
            cleanCode.contains(" K ") || cleanCode.endsWith("K") -> hardness = "K - Средне-мягкий (Medium soft)"
            cleanCode.contains(" L ") || cleanCode.endsWith("L") -> hardness = "L - Средний (Medium)"
            cleanCode.contains(" M ") || cleanCode.endsWith("M") -> hardness = "M - Средне-твердый (Medium hard)"
        }

        when {
            cleanCode.contains("V") -> bond = "V / К - Керамическая связка (Vitrified)"
            cleanCode.contains("B") -> bond = "B / Б - Бакелитовая связка (Resinoid)"
            cleanCode.contains("R") -> bond = "R / В - Вулканитовая связка (Rubber)"
        }

        return WheelMarkingDecoded(
            originalCode = cleanCode,
            abrasiveType = abrasive,
            grainSize = grain,
            hardness = hardness,
            structure = structure,
            bondType = bond,
            description = "Абразив: $abrasive, Зерно: $grain, Твердость: $hardness, Связка: $bond"
        )
    }
}
