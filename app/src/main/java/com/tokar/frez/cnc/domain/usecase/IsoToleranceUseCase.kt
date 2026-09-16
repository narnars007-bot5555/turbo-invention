package com.tokar.frez.cnc.domain.usecase

import com.tokar.frez.cnc.domain.model.IsoToleranceParams
import com.tokar.frez.cnc.domain.model.IsoToleranceResult
import kotlin.math.cbrt
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

class IsoToleranceUseCase {

    fun calculateTolerance(params: IsoToleranceParams): IsoToleranceResult {
        val d = params.nominalSizeMm.coerceAtLeast(1.0).coerceAtMost(3150.0)
        val field = params.toleranceField.trim()

        if (field.isEmpty()) {
            return IsoToleranceResult(d, field, 0.0, 0.0, d, d)
        }

        val letter = field.takeWhile { it.isLetter() }
        val gradeNum = field.dropWhile { it.isLetter() }.toIntOrNull() ?: 7

        val dGeom = getGeometricMean(d)
        val iFactor = 0.45 * cbrt(dGeom) + 0.001 * dGeom
        val itValueUm = calculateItGrade(gradeNum, iFactor)

        val (es, ei) = calculateDeviations(letter, dGeom, itValueUm)

        val minLimit = d + (ei / 1000.0)
        val maxLimit = d + (es / 1000.0)

        return IsoToleranceResult(
            nominalSize = d,
            toleranceField = field,
            esUpperUm = round(es * 10.0) / 10.0,
            eiLowerUm = round(ei * 10.0) / 10.0,
            minLimitMm = round(minLimit * 10000.0) / 10000.0,
            maxLimitMm = round(maxLimit * 10000.0) / 10000.0
        )
    }

    private fun getGeometricMean(d: Double): Double {
        val (d1, d2) = when {
            d <= 3 -> 1.0 to 3.0
            d <= 6 -> 3.0 to 6.0
            d <= 10 -> 6.0 to 10.0
            d <= 18 -> 10.0 to 18.0
            d <= 30 -> 18.0 to 30.0
            d <= 50 -> 30.0 to 50.0
            d <= 80 -> 50.0 to 80.0
            d <= 120 -> 80.0 to 120.0
            d <= 180 -> 120.0 to 180.0
            d <= 250 -> 180.0 to 250.0
            d <= 315 -> 250.0 to 315.0
            d <= 400 -> 315.0 to 400.0
            d <= 500 -> 400.0 to 500.0
            else -> 500.0 to 3150.0
        }
        return sqrt(d1 * d2)
    }

    private fun calculateItGrade(grade: Int, i: Double): Double {
        return when (grade) {
            1 -> 0.8 + 0.015 * i
            2 -> 1.2 + 0.02 * i
            3 -> 2.0 + 0.03 * i
            4 -> 3.0 + 0.04 * i
            5 -> 7.0 * i
            6 -> 10.0 * i
            7 -> 16.0 * i
            8 -> 25.0 * i
            9 -> 40.0 * i
            10 -> 64.0 * i
            11 -> 100.0 * i
            12 -> 160.0 * i
            13 -> 250.0 * i
            14 -> 400.0 * i
            15 -> 640.0 * i
            16 -> 1000.0 * i
            else -> 16.0 * i
        }
    }

    private fun calculateDeviations(letter: String, dGeom: Double, itVal: Double): Pair<Double, Double> {
        val isHole = letter.firstOrNull()?.isUpperCase() == true
        val upperLetter = letter.uppercase()

        val fundDev = when (upperLetter) {
            "H" -> 0.0
            "JS" -> return (itVal / 2.0) to (-itVal / 2.0)
            "G" -> 2.5 * dGeom.pow(0.34)
            "F" -> 5.5 * dGeom.pow(0.41)
            "E" -> 11.0 * dGeom.pow(0.41)
            "D" -> 16.0 * dGeom.pow(0.44)
            "C" -> 52.0 * dGeom.pow(0.2)
            "B" -> 140.0 * dGeom.pow(0.09)
            "A" -> 265.0 * dGeom.pow(0.05)
            "K" -> 0.6 * cbrt(dGeom)
            "M" -> -(0.6 * cbrt(dGeom) + 0.002 * dGeom)
            "N" -> -5.0 * dGeom.pow(0.34)
            "P" -> -dGeom.pow(0.56)
            "R" -> -pDev(dGeom) * 1.5
            "S" -> -pDev(dGeom) * 2.0
            else -> 0.0
        }

        return if (isHole) {
            when (upperLetter) {
                "H" -> itVal to 0.0
                "JS" -> (itVal / 2.0) to (-itVal / 2.0)
                "F", "G", "E", "D", "C", "B", "A" -> (fundDev + itVal) to fundDev
                else -> fundDev to (fundDev - itVal)
            }
        } else {
            when (upperLetter) {
                "H" -> 0.0 to -itVal
                "JS" -> (itVal / 2.0) to (-itVal / 2.0)
                "F", "G", "E", "D", "C", "B", "A" -> (-fundDev) to (-fundDev - itVal)
                else -> (-fundDev + itVal) to (-fundDev)
            }
        }
    }

    private fun pDev(dGeom: Double): Double = dGeom.pow(0.56)
}
