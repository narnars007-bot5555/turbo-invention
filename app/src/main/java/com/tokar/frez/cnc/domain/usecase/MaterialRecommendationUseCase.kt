package com.tokar.frez.cnc.domain.usecase

import com.tokar.frez.cnc.data.entity.MaterialEntity
import kotlin.math.PI
import kotlin.math.round

data class CuttingRegimeRecommendation(
    val recommendedVcMmin: Double,
    val recommendedFeedMmRev: Double,
    val recommendedApMm: Double,
    val calculatedRpm: Int,
    val coatingNote: String,
    val warningText: String = ""
)

class MaterialRecommendationUseCase {

    fun recommendCuttingRegime(
        material: MaterialEntity?,
        diameterMm: Double,
        operationType: String = "Черновое", // Черновое, Чистовое, Нарезание резьбы
        cornerRadiusMm: Double = 0.8,       // r_eps
        coatingType: String = "CVD TiAlN"    // CVD, PVD, Uncoated
    ): CuttingRegimeRecommendation {
        val baseVc = when {
            material != null -> (material.recVcMin + material.recVcMax) / 2.0
            else -> 180.0
        }

        val baseFeed = when {
            material != null -> (material.recFeedMin + material.recFeedMax) / 2.0
            else -> 0.20
        }

        // Coating multiplier
        val coatingMult = when (coatingType.uppercase()) {
            "CVD TIALN", "CVD" -> 1.20
            "PVD TIALN", "PVD" -> 1.00
            "БЕЗ ПОКРЫТИЯ (UNCOATED)", "UNCOATED" -> 0.70
            else -> 1.00
        }

        // Operation type modifier
        var vc = baseVc * coatingMult
        var feed = baseFeed
        var ap = 2.0

        when (operationType) {
            "Чистовое" -> {
                vc *= 1.25
                feed = cornerRadiusMm * 0.35 // f <= 0.35 * r_eps for good Ra
                ap = cornerRadiusMm * 0.8
            }
            "Черновое" -> {
                vc *= 0.90
                feed = baseFeed
                ap = cornerRadiusMm * 2.5
            }
            "Нарезание резьбы" -> {
                vc *= 0.60
                feed = 1.5 // Pitch
                ap = 0.20 // Peck depth
            }
        }

        // Bound checks
        if (material != null) {
            vc = vc.coerceIn(material.recVcMin * 0.5, material.recVcMax * 1.5)
        }

        val dia = if (diameterMm <= 0) 50.0 else diameterMm
        val rpm = (vc * 1000.0) / (PI * dia)

        val note = "Покрытие $coatingType: Vc x${String.format("%.2f", coatingMult)}. Для r_eps=$cornerRadiusMm мм рекомендуемая чистовая подача f <= ${String.format("%.2f", cornerRadiusMm * 0.35)} мм/об."

        val warn = if (material?.isoGroup == "S" || material?.isoGroup == "H") {
            "ВНИМАНИЕ: Сложнообрабатываемый материал (${material.name}). Обязательно использование СОЖ под высоким давлением."
        } else ""

        return CuttingRegimeRecommendation(
            recommendedVcMmin = round(vc * 10.0) / 10.0,
            recommendedFeedMmRev = round(feed * 1000.0) / 1000.0,
            recommendedApMm = round(ap * 10.0) / 10.0,
            calculatedRpm = rpm.toInt(),
            coatingNote = note,
            warningText = warn
        )
    }
}
