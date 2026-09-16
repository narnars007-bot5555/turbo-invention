package com.tokar.frez.cnc.domain.usecase

import com.tokar.frez.cnc.domain.model.*
import kotlin.math.pow
import kotlin.math.round

class TimeNormativeUseCase {

    fun calculateNormativeTime(params: TimeNormativeParams): TimeNormativeResult {
        val to = params.cuttingTimeMin.coerceAtLeast(0.0)
        val tv = params.auxiliaryTimeMin.coerceAtLeast(0.0)
        val n = params.batchSize.coerceAtLeast(1)
        val tpz = params.setupTimeMin.coerceAtLeast(0.0)

        val top = to + tv
        val tst = top * 1.10
        val tcalc = tst + (tpz / n.toDouble())

        return TimeNormativeResult(
            operatingTimeMin = round(top * 100.0) / 100.0,
            pieceTimeMin = round(tst * 100.0) / 100.0,
            calcBatchTimeMin = round(tcalc * 100.0) / 100.0
        )
    }

    fun calculateTaylorToolLife(params: TaylorToolLifeParams): TaylorToolLifeResult {
        val vc = params.vc.coerceAtLeast(1.0)
        val c = params.taylorConstantC.coerceAtLeast(10.0)
        val exponent = params.exponentN.coerceIn(0.1, 0.6)

        val tLife = (c / vc).pow(1.0 / exponent)

        return TaylorToolLifeResult(
            toolLifeMin = round(tLife * 10.0) / 10.0
        )
    }
}
