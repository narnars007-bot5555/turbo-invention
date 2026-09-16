package com.tokar.frez.cnc.domain.usecase

import com.tokar.frez.cnc.domain.model.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.tan

class GearCuttingUseCase {

    fun calculateGearGeometry(params: GearParams): GearResult {
        val m = if (params.module > 0) params.module else 1.0
        val z = if (params.teeth > 0) params.teeth else 12
        val alphaRad = Math.toRadians(if (params.pressureAngleDeg > 0) params.pressureAngleDeg else 20.0)

        val d = m * z
        val da = m * (z + 2.0)
        val df = m * (z - 2.5)
        val db = d * cos(alphaRad)

        val recommendedK = (z.toDouble() * (if (params.pressureAngleDeg > 0) params.pressureAngleDeg else 20.0) / 180.0 + 0.5).roundToInt().coerceAtLeast(2)
        val k = if (params.spanTeeth > 0) params.spanTeeth else recommendedK

        val invAlpha = tan(alphaRad) - alphaRad
        val w = m * cos(alphaRad) * (PI * (k - 0.5) + z * invAlpha)

        return GearResult(
            pitchDiameter = d,
            tipDiameter = da,
            rootDiameter = df,
            baseDiameter = db,
            baseTangentLength = w,
            recommendedSpanTeeth = recommendedK
        )
    }

    fun calculateGearShaping(params: GearShapingParams): GearShapingResult {
        val l = if (params.strokeLength > 0) params.strokeLength else 50.0
        val vc = if (params.cuttingSpeed > 0) params.cuttingSpeed else 25.0

        val nSt = ((1000.0 * vc) / (2.0 * l)).roundToInt().coerceAtLeast(10)
        val feed = if (params.radialFeed > 0) params.radialFeed else 0.05
        val passes = params.passes.coerceAtLeast(1)
        val estTime = (passes * 5.0) / (feed * (nSt / 100.0))

        return GearShapingResult(
            doubleStrokesPerMin = nSt,
            estimatedTimeMinutes = estTime
        )
    }
}
