package com.tokar.frez.cnc.domain.usecase

import com.tokar.frez.cnc.domain.model.ThreadParams
import com.tokar.frez.cnc.domain.model.ThreadResult
import kotlin.math.round
import kotlin.math.sqrt

class ThreadUseCase {

    fun calculateThread(params: ThreadParams): ThreadResult {
        val d = if (params.nominalDiameter > 0) params.nominalDiameter else 10.0
        val p = if (params.pitch > 0) params.pitch else 1.5

        val h = (sqrt(3.0) / 2.0) * p
        val h1 = (5.0 / 8.0) * h
        val d2 = d - 0.649519 * p
        val d1 = d - 1.082532 * p
        val tapDrill = d - p

        return ThreadResult(
            nominalDiameter = d,
            pitch = p,
            pitchDiameter = round(d2 * 1000.0) / 1000.0,
            minorDiameter = round(d1 * 1000.0) / 1000.0,
            threadHeight = round(h1 * 1000.0) / 1000.0,
            tapDrillDiameter = round(tapDrill * 100.0) / 100.0
        )
    }

    fun parseStandardThread(threadCode: String): ThreadParams {
        val clean = threadCode.trim().uppercase()
        return when {
            clean.startsWith("M12X1.75") || clean == "M12" -> ThreadParams("M12", 12.0, 1.75)
            clean.startsWith("M10X1.5") || clean == "M10" -> ThreadParams("M10", 10.0, 1.5)
            clean.startsWith("M8X1.25") || clean == "M8" -> ThreadParams("M8", 8.0, 1.25)
            clean.startsWith("M16X2") || clean == "M16" -> ThreadParams("M16", 16.0, 2.0)
            clean.startsWith("M20X2.5") || clean == "M20" -> ThreadParams("M20", 20.0, 2.5)
            clean.startsWith("G1/2") -> ThreadParams("G 1/2", 20.955, 1.814)
            clean.startsWith("G1/4") -> ThreadParams("G 1/4", 13.157, 1.337)
            else -> {
                val mRegex = Regex("""M(\d+\.?\d*)\s*X?\s*(\d+\.?\d*)?""")
                val match = mRegex.find(clean)
                if (match != null) {
                    val dia = match.groupValues[1].toDoubleOrNull() ?: 10.0
                    val pitch = match.groupValues[2].toDoubleOrNull() ?: 1.5
                    ThreadParams("M$dia x $pitch", dia, pitch)
                } else {
                    ThreadParams(clean, 10.0, 1.5)
                }
            }
        }
    }
}
