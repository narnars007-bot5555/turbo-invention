package com.tokar.frez.cnc.domain.usecase

enum class FitType {
    CLEARANCE,    // Зазор (Зеленый)
    INTERFERENCE, // Натяг (Красный)
    TRANSITION    // Переходная (Желтый)
}

data class IsoMatingResult(
    val nominalSize: Double,
    val holeField: String,
    val shaftField: String,
    val holeMinMm: Double,
    val holeMaxMm: Double,
    val shaftMinMm: Double,
    val shaftMaxMm: Double,
    val fitType: FitType,
    val maxClearanceUm: Double,
    val minClearanceUm: Double,
    val maxInterferenceUm: Double,
    val minInterferenceUm: Double
)

class IsoMatingUseCase(private val toleranceUseCase: IsoToleranceUseCase = IsoToleranceUseCase()) {

    fun calculateMating(
        nominalMm: Double,
        holeField: String = "H7",
        shaftField: String = "g6"
    ): IsoMatingResult {
        val holeRes = toleranceUseCase.calculateTolerance(
            com.tokar.frez.cnc.domain.model.IsoToleranceParams(nominalMm, holeField)
        )
        val shaftRes = toleranceUseCase.calculateTolerance(
            com.tokar.frez.cnc.domain.model.IsoToleranceParams(nominalMm, shaftField)
        )

        val maxClearanceUm = holeRes.esUpperUm - shaftRes.eiLowerUm
        val minClearanceUm = holeRes.eiLowerUm - shaftRes.esUpperUm

        val fitType = when {
            minClearanceUm >= 0 -> FitType.CLEARANCE
            maxClearanceUm <= 0 -> FitType.INTERFERENCE
            else -> FitType.TRANSITION
        }

        return IsoMatingResult(
            nominalSize = nominalMm,
            holeField = holeField,
            shaftField = shaftField,
            holeMinMm = holeRes.minLimitMm,
            holeMaxMm = holeRes.maxLimitMm,
            shaftMinMm = shaftRes.minLimitMm,
            shaftMaxMm = shaftRes.maxLimitMm,
            fitType = fitType,
            maxClearanceUm = maxClearanceUm,
            minClearanceUm = minClearanceUm,
            maxInterferenceUm = -minClearanceUm,
            minInterferenceUm = -maxClearanceUm
        )
    }
}
