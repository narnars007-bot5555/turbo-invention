package com.tokar.frez.cnc.domain.usecase

import kotlin.math.PI
import kotlin.math.round

data class CuttingForceResult(
    val fcTangentialN: Double,
    val fpRadialN: Double,
    val ffAxialN: Double,
    val torqueNm: Double,
    val powerKw: Double,
    val minClampingForceN: Double,
    val safetyFactor: Double,
    val isClampingSafe: Boolean
)

class CuttingForceUseCase {

    fun calculateCuttingForces(
        apMm: Double,
        fnMmRev: Double,
        kcNmm2: Double = 1800.0, // Specific cutting force e.g. for steel 1800 N/mm²
        diameterMm: Double = 50.0,
        vcMmin: Double = 150.0,
        clampingForceN: Double = 15000.0,
        frictionCoeff: Double = 0.25
    ): CuttingForceResult {
        val fc = apMm * fnMmRev * kcNmm2
        val fp = 0.4 * fc
        val ff = 0.3 * fc

        val radiusM = (diameterMm / 2.0) / 1000.0
        val torque = fc * radiusM

        val powerKw = (fc * vcMmin) / 60000.0

        val minClampingForceN = (fc * 1.5) / frictionCoeff
        val currentSafetyFactor = (clampingForceN * frictionCoeff) / (fc.coerceAtLeast(1.0))

        return CuttingForceResult(
            fcTangentialN = round(fc * 10.0) / 10.0,
            fpRadialN = round(fp * 10.0) / 10.0,
            ffAxialN = round(ff * 10.0) / 10.0,
            torqueNm = round(torque * 10.0) / 10.0,
            powerKw = round(powerKw * 100.0) / 100.0,
            minClampingForceN = round(minClampingForceN * 10.0) / 10.0,
            safetyFactor = round(currentSafetyFactor * 100.0) / 100.0,
            isClampingSafe = currentSafetyFactor >= 1.5
        )
    }
}
