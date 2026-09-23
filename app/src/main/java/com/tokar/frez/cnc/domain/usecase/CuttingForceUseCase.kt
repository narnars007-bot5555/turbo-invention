package com.tokar.frez.cnc.domain.usecase

import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.round

data class CuttingForceResult(
    val fcTangentialN: Double,
    val fpRadialN: Double,
    val ffAxialN: Double,
    val torqueNm: Double,
    val powerKw: Double,
    val minClampingForceN: Double,
    val safetyFactor: Double,
    val isClampingSafe: Boolean,
    val recommendedLoweredApMm: Double = 0.0
)

class CuttingForceUseCase {

    fun calculateCuttingForces(
        apMm: Double,
        fnMmRev: Double,
        kcNmm2: Double = 1800.0, // Specific cutting force e.g. for steel Kc1.1 = 1800 N/mm²
        mcExponent: Double = 0.0, // Kienzle exponent mc (defaults to 0.0 if not specified)
        diameterMm: Double = 50.0,
        vcMmin: Double = 160.0,
        clampingForceN: Double = 15000.0,
        frictionCoeff: Double = 0.25,
        efficiencyEta: Double = 0.85,
        maxSpindlePowerKw: Double = 11.0
    ): CuttingForceResult {
        // Specific cutting force adjusted by feed: Kc = Kc1.1 * f^(-mc)
        val correctedKc = if (fnMmRev > 0 && mcExponent != 0.0) kcNmm2 * fnMmRev.pow(-mcExponent) else kcNmm2

        val fc = apMm * fnMmRev * correctedKc
        val fp = 0.4 * fc
        val ff = 0.3 * fc

        val radiusM = (diameterMm / 2.0) / 1000.0
        val torque = fc * radiusM

        // Power calculation with machine efficiency eta = 0.85
        val powerKw = (fc * vcMmin) / (60000.0 * efficiencyEta)

        val minClampingForceN = (fc * 1.5) / frictionCoeff
        val currentSafetyFactor = (clampingForceN * frictionCoeff) / (fc.coerceAtLeast(1.0))

        // Calculate auto-lowered depth of cut if power exceeds spindle limits
        val recommendedAp = if (powerKw > maxSpindlePowerKw && powerKw > 0) {
            apMm * (maxSpindlePowerKw / powerKw)
        } else {
            apMm
        }

        return CuttingForceResult(
            fcTangentialN = round(fc * 10.0) / 10.0,
            fpRadialN = round(fp * 10.0) / 10.0,
            ffAxialN = round(ff * 10.0) / 10.0,
            torqueNm = round(torque * 10.0) / 10.0,
            powerKw = round(powerKw * 100.0) / 100.0,
            minClampingForceN = round(minClampingForceN * 10.0) / 10.0,
            safetyFactor = round(currentSafetyFactor * 100.0) / 100.0,
            isClampingSafe = currentSafetyFactor >= 1.5,
            recommendedLoweredApMm = round(recommendedAp * 100.0) / 100.0
        )
    }
}
