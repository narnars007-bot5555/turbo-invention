package com.tokar.frez.cnc.domain.usecase

import kotlin.math.pow
import kotlin.math.round

data class CostPerEdgeResult(
    val toolLifeMin: Double,
    val partsPerEdge: Int,
    val insertCostPerPart: Double,
    val machineHourlyCostPerPart: Double,
    val totalCostPerPart: Double
)

class CostPerEdgeUseCase {

    fun calculateCostPerEdge(
        cCoeff: Double = 300.0,      // Taylor C
        mExponent: Double = 0.25,    // Taylor m exponent
        vcMmin: Double = 180.0,      // Cutting speed
        machiningTimePerPartMin: Double = 2.5,
        insertCostRub: Double = 450.0,
        edgesPerInsert: Int = 4,
        machineHourlyRateRub: Double = 2500.0
    ): CostPerEdgeResult {
        // Taylor formula: T = (C / Vc)^(1/m)
        val toolLifeMin = (cCoeff / vcMmin).pow(1.0 / mExponent)
        val partsPerEdge = (toolLifeMin / machiningTimePerPartMin.coerceAtLeast(0.1)).toInt().coerceAtLeast(1)

        val costPerEdge = insertCostRub / edgesPerInsert.coerceAtLeast(1)
        val insertCostPerPart = costPerEdge / partsPerEdge

        val machineMinuteRate = machineHourlyRateRub / 60.0
        val machineHourlyCostPerPart = machiningTimePerPartMin * machineMinuteRate

        val totalCostPerPart = insertCostPerPart + machineHourlyCostPerPart

        return CostPerEdgeResult(
            toolLifeMin = round(toolLifeMin * 10.0) / 10.0,
            partsPerEdge = partsPerEdge,
            insertCostPerPart = round(insertCostPerPart * 100.0) / 100.0,
            machineHourlyCostPerPart = round(machineHourlyCostPerPart * 100.0) / 100.0,
            totalCostPerPart = round(totalCostPerPart * 100.0) / 100.0
        )
    }
}
