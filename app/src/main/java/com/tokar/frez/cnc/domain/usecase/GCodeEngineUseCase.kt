package com.tokar.frez.cnc.domain.usecase

import kotlin.math.round

data class ToolpathPoint(
    val x: Float,
    val y: Float,
    val isRapid: Boolean // true for G0 (rapid/green), false for G1/G2/G3 (feed/blue)
)

data class GCodeGenerationParams(
    val cycleType: String, // G71/CYCLE95, G76/CYCLE97, G83/CYCLE83
    val startDiameterMm: Double,
    val endDiameterMm: Double,
    val lengthMm: Double,
    val depthOfCutMm: Double,
    val feedRate: Double,
    val pitchMm: Double = 1.5,
    val peckDepthMm: Double = 2.0
)

fun hasSafeTermination(program: String): Boolean {
    val upper = program.uppercase()
    val hasRetract = upper.contains("G00") || upper.contains("G0 ")
    val hasCoolantOff = upper.contains("M09") || upper.contains("M9")
    val hasSpindleStop = upper.contains("M05") || upper.contains("M5")
    val hasProgramEnd = upper.contains("M30") || upper.contains("M02") || upper.contains("M2") || upper.contains("END PGM")
    return hasRetract && hasCoolantOff && hasSpindleStop && hasProgramEnd
}

class GCodeEngineUseCase {

    fun hasSafeTermination(program: String): Boolean = com.tokar.frez.cnc.domain.usecase.hasSafeTermination(program)

    fun parseToolpath(gcodeText: String): List<ToolpathPoint> {
        val points = mutableListOf<ToolpathPoint>()
        var currentX = 0f
        var currentY = 0f
        var isRapid = true

        points.add(ToolpathPoint(currentX, currentY, isRapid))

        val lines = gcodeText.split("\n")
        for (line in lines) {
            val cleanLine = line.uppercase().takeWhile { it != '(' && it != ';' }.trim()
            if (cleanLine.isEmpty()) continue

            if (cleanLine.contains("G00") || cleanLine.contains("G0 ") || cleanLine.endsWith("G0")) {
                isRapid = true
            } else if (cleanLine.contains("G01") || cleanLine.contains("G1 ") || cleanLine.contains("G02") || cleanLine.contains("G2") || cleanLine.contains("G03") || cleanLine.contains("G3")) {
                isRapid = false
            }

            var newX = currentX
            var newY = currentY

            val tokens = cleanLine.split(" ")
            for (token in tokens) {
                if (token.startsWith("X")) {
                    token.drop(1).toFloatOrNull()?.let { newX = it }
                } else if (token.startsWith("Z") || token.startsWith("Y")) {
                    token.drop(1).toFloatOrNull()?.let { newY = it }
                }
            }

            if (newX != currentX || newY != currentY) {
                currentX = newX
                currentY = newY
                points.add(ToolpathPoint(currentX, currentY, isRapid))
            }
        }

        return points
    }

    fun generateCycleGCode(params: GCodeGenerationParams, system: CncSystemType): String {
        return when (params.cycleType) {
            "G71_CYCLE95" -> generateRoughTurning(params, system)
            "G76_CYCLE97" -> generateThreading(params, system)
            "G83_CYCLE83" -> generatePeckDrilling(params, system)
            else -> "; Выберите корректный тип цикла"
        }
    }

    private fun generateRoughTurning(p: GCodeGenerationParams, system: CncSystemType): String {
        return when (system) {
            CncSystemType.FANUC, CncSystemType.HAAS -> """
                ; Черновой токарный цикл снятия припуска
                G00 X${p.startDiameterMm + 5.0} Z2.0
                G71 U${p.depthOfCutMm} R1.0
                G71 P10 Q20 U0.5 W0.1 F${p.feedRate}
                N10 G00 X${p.startDiameterMm}
                G01 Z-${p.lengthMm} X${p.endDiameterMm}
                N20 G01 X${p.endDiameterMm + 5.0}
            """.trimIndent()

            CncSystemType.SINUMERIK -> """
                ; CYCLE95 Sinumerik Rough Turning
                G0 X${p.startDiameterMm + 5.0} Z2.0
                CYCLE95("CONTOUR", ${p.depthOfCutMm}, 0.5, 0.1, 0, ${p.feedRate}, 0.1, 0.1)
            """.trimIndent()

            CncSystemType.HEIDENHAIN -> """
                ; Heidenhain Roughing
                CYCL DEF 280 CONTOUR TURNING ~
                  Q496=${p.startDiameterMm} ; DIAMETER ~
                  Q497=-${p.lengthMm} ; LENGTH ~
                  Q498=${p.depthOfCutMm} ; DEPTH ~
                  Q505=${p.feedRate} ; FEED
            """.trimIndent()
        }
    }

    private fun generateThreading(p: GCodeGenerationParams, system: CncSystemType): String {
        val threadDepth = round(0.6134 * p.pitchMm * 1000.0) / 1000.0
        return when (system) {
            CncSystemType.FANUC, CncSystemType.HAAS -> """
                ; Нарезание резьбы G76
                G00 X${p.startDiameterMm + 3.0} Z5.0
                G76 P021060 Q100 R0.02
                G76 X${p.startDiameterMm - 2 * threadDepth} Z-${p.lengthMm} P${(threadDepth * 1000).toInt()} Q200 F${p.pitchMm}
            """.trimIndent()

            CncSystemType.SINUMERIK -> """
                ; CYCLE97 Sinumerik Threading
                G0 X${p.startDiameterMm + 3.0} Z5.0
                CYCLE97(${p.pitchMm}, , 0, -${p.lengthMm}, ${p.startDiameterMm}, ${p.startDiameterMm - 2 * threadDepth}, 5, 0.1, ${p.pitchMm}, 3, 4)
            """.trimIndent()

            CncSystemType.HEIDENHAIN -> """
                ; Heidenhain Threading
                CYCL DEF 260 THREAD CUTTING ~
                  Q471=0 ; THREAD TYPE ~
                  Q472=${p.pitchMm} ; PITCH ~
                  Q473=${threadDepth} ; DEPTH ~
                  Q474=-${p.lengthMm} ; LENGTH
            """.trimIndent()
        }
    }

    private fun generatePeckDrilling(p: GCodeGenerationParams, system: CncSystemType): String {
        return when (system) {
            CncSystemType.FANUC, CncSystemType.HAAS -> """
                ; Глубокое сверление G83
                G00 X0 Z5.0
                G83 Z-${p.lengthMm} Q${(p.peckDepthMm * 1000).toInt()} R2.0 F${p.feedRate}
                G80
            """.trimIndent()

            CncSystemType.SINUMERIK -> """
                ; CYCLE83 Deep Hole Drilling
                G0 X0 Z5.0
                CYCLE83(2.0, 0, 2.0, -${p.lengthMm}, , ${p.peckDepthMm}, , 1.0, 1, ${p.feedRate}, 0)
            """.trimIndent()

            CncSystemType.HEIDENHAIN -> """
                ; Heidenhain Deep Hole Drilling
                CYCL DEF 205 DEEP HOLE DRILLING ~
                  Q200=2.0 ; SET-UP CLEARANCE ~
                  Q201=-${p.lengthMm} ; DEPTH ~
                  Q202=${p.peckDepthMm} ; PLUNGING DEPTH ~
                  Q206=${p.feedRate} ; FEED RATE
            """.trimIndent()
        }
    }
}
