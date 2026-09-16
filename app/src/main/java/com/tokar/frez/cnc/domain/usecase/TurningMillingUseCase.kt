package com.tokar.frez.cnc.domain.usecase

import com.tokar.frez.cnc.domain.model.*
import kotlin.math.PI
import kotlin.math.atan

class TurningMillingUseCase {

    fun calculateTurning(
        diameter: Double,
        vc: Double? = null,
        n: Double? = null,
        feed: Double,
        ap: Double,
        toolRadius: Double,
        kc: Double = 2000.0,
        efficiency: Double = 0.85
    ): TurningResult {
        val d = if (diameter > 0) diameter else 1.0
        val computedN = if (n != null && n > 0) n else if (vc != null) (vc * 1000.0) / (PI * d) else 1000.0
        val computedVc = if (vc != null && vc > 0) vc else (PI * d * computedN) / 1000.0

        val mrr = computedVc * ap * feed
        val pc = (ap * feed * computedVc * kc) / (60000.0 * efficiency)

        val rz = if (toolRadius > 0) (feed * feed / (8.0 * toolRadius)) * 1000.0 else 0.0
        val ra = rz / 4.0

        return TurningResult(
            vc = computedVc,
            n = computedN,
            feed = feed,
            mrr = mrr,
            pc = pc,
            ra = ra,
            rz = rz
        )
    }

    fun calculateMilling(
        diameter: Double,
        teeth: Int,
        vc: Double? = null,
        n: Double? = null,
        fz: Double,
        ap: Double,
        ae: Double,
        kc: Double = 2000.0,
        efficiency: Double = 0.85
    ): MillingResult {
        val d = if (diameter > 0) diameter else 1.0
        val z = if (teeth > 0) teeth else 1
        val computedN = if (n != null && n > 0) n else if (vc != null) (vc * 1000.0) / (PI * d) else 1000.0
        val computedVc = if (vc != null && vc > 0) vc else (PI * d * computedN) / 1000.0

        val vf = fz * z * computedN
        val mrr = (ap * ae * vf) / 1000.0
        val pc = (mrr * kc) / (60.0 * 1000.0 * efficiency)

        return MillingResult(
            vc = computedVc,
            n = computedN,
            vf = vf,
            mrr = mrr,
            pc = pc
        )
    }

    fun calculateTaper(params: TaperParams): TaperResult {
        val dDiff = params.bigDiameter - params.smallDiameter
        val l = if (params.taperLength > 0) params.taperLength else 1.0
        val lTotal = if (params.totalLength > 0) params.totalLength else l

        val taperRatio = dDiff / l
        val halfAngleRad = atan(dDiff / (2.0 * l))
        val halfAngleDeg = Math.toDegrees(halfAngleRad)
        val tailstockOffset = (dDiff * lTotal) / (2.0 * l)

        return TaperResult(
            taperRatio = taperRatio,
            halfAngleDeg = halfAngleDeg,
            tailstockOffset = tailstockOffset
        )
    }
}
