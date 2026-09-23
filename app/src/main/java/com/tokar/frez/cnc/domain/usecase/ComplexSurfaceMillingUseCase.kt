package com.tokar.frez.cnc.domain.usecase

import kotlin.math.PI
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

data class ComplexSurfaceMillingResult(
    val nominalDiameterMm: Double,
    val effectiveDiameterMm: Double,
    val nominalVcMmin: Double,
    val effectiveVcMmin: Double,
    val rpm: Int,
    val scallopHeightRzUm: Double,
    val mrrCm3Min: Double,
    val chipThinningFactor: Double
)

class ComplexSurfaceMillingUseCase {

    fun calculateBallNoseMilling(
        cutterDiameterMm: Double = 10.0,
        apMm: Double = 0.5,
        aeMm: Double = 0.4,
        fzMmTeeth: Double = 0.08,
        teeth: Int = 2,
        vcMmin: Double = 150.0,
        surfaceAngleDeg: Double = 30.0
    ): ComplexSurfaceMillingResult {
        val radiusMm = cutterDiameterMm / 2.0
        val apClamped = apMm.coerceAtMost(radiusMm).coerceAtLeast(0.01)

        // Effective diameter Deff = 2 * sqrt(R^2 - (R - ap)^2)
        val Deff = 2.0 * sqrt(radiusMm * radiusMm - (radiusMm - apClamped) * (radiusMm - apClamped))

        // Adjusted Deff for inclined surface angle theta
        val angleRad = Math.toRadians(surfaceAngleDeg)
        val DeffInclined = (Deff * sin(angleRad)).coerceAtLeast(1.0)

        val rpm = ((vcMmin * 1000.0) / (PI * DeffInclined.coerceAtLeast(1.0))).toInt()
        val effectiveVc = (PI * Deff * rpm) / 1000.0

        // Scallop height Rz = R - sqrt(R^2 - (ae/2)^2) in um
        val aeHalf = (aeMm / 2.0).coerceAtMost(radiusMm)
        val scallopMm = radiusMm - sqrt((radiusMm * radiusMm - aeHalf * aeHalf).coerceAtLeast(0.0))
        val scallopUm = scallopMm * 1000.0

        val vf = fzMmTeeth * teeth * rpm
        val mrr = (apMm * aeMm * vf) / 1000.0

        val chipThinningFactor = Deff / cutterDiameterMm

        return ComplexSurfaceMillingResult(
            nominalDiameterMm = cutterDiameterMm,
            effectiveDiameterMm = round(Deff * 100.0) / 100.0,
            nominalVcMmin = vcMmin,
            effectiveVcMmin = round(effectiveVc * 10.0) / 10.0,
            rpm = rpm,
            scallopHeightRzUm = round(scallopUm * 10.0) / 10.0,
            mrrCm3Min = round(mrr * 100.0) / 100.0,
            chipThinningFactor = round(chipThinningFactor * 100.0) / 100.0
        )
    }
}
