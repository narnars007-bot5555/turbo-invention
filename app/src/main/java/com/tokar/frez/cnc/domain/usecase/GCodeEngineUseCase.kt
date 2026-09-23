package com.tokar.frez.cnc.domain.usecase

import kotlin.math.round

data class ToolpathPoint(
    val x: Float,
    val y: Float,
    val isRapid: Boolean // true for G0 (rapid/green), false for G1/G2/G3 (feed/blue)
)

data class GCodeValidationIssue(
    val lineNumber: Int,
    val severity: IssueSeverity, // ERROR, WARNING
    val message: String
)

enum class IssueSeverity {
    ERROR, WARNING
}

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

class GCodeEngineUseCase {

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

    fun validateGCode(gcodeText: String, system: CncSystemType): List<GCodeValidationIssue> {
        val issues = mutableListOf<GCodeValidationIssue>()
        val lines = gcodeText.split("\n")

        var feedrateSet = false
        var spindleSet = false

        for ((index, rawLine) in lines.withIndex()) {
            val lineNum = index + 1
            val line = rawLine.uppercase().takeWhile { it != '(' && it != ';' }.trim()
            if (line.isEmpty()) continue

            if (line.contains("F")) feedrateSet = true
            if (line.contains("S")) spindleSet = true

            // Check feedrate requirement on cutting moves
            if ((line.contains("G01") || line.contains("G1 ") || line.contains("G02") || line.contains("G03")) && !line.contains("F") && !feedrateSet) {
                issues.add(GCodeValidationIssue(lineNum, IssueSeverity.ERROR, "Кадр резания без указания подачи F."))
            }

            // Check arc center parameters
            if (line.contains("G02") || line.contains("G2") || line.contains("G03") || line.contains("G3")) {
                if (!line.contains("R") && !line.contains("I") && !line.contains("J") && !line.contains("K")) {
                    issues.add(GCodeValidationIssue(lineNum, IssueSeverity.ERROR, "Круговая интерполяция требует задания радиуса R или векторов I, J, K."))
                }
            }

            // Check system-specific syntax issues
            when (system) {
                CncSystemType.FANUC -> {
                    if (line.contains("CYCLE95") || line.contains("CYCLE97")) {
                        issues.add(GCodeValidationIssue(lineNum, IssueSeverity.ERROR, "Цикл CYCLE... не поддерживается на стойках Fanuc. Используйте G71/G76."))
                    }
                }
                CncSystemType.SINUMERIK -> {
                    if (line.contains("G71") || line.contains("G76")) {
                        issues.add(GCodeValidationIssue(lineNum, IssueSeverity.WARNING, "На Sinumerik рекомендуется использовать CYCLE95/CYCLE97 вместо G71/G76."))
                    }
                }
                CncSystemType.HAAS -> {
                    if (line.contains("G43.4") && !line.contains("G143")) {
                        issues.add(GCodeValidationIssue(lineNum, IssueSeverity.WARNING, "На Haas для TCPC коррекции 5 осей используется команда G143 вместо G43.4."))
                    }
                }
                CncSystemType.HEIDENHAIN -> {
                    if (line.contains("G00") || line.contains("G01")) {
                        issues.add(GCodeValidationIssue(lineNum, IssueSeverity.WARNING, "Для Heidenhain ISO синтаксис G-кода требует спец. формата или CYCL DEF."))
                    }
                }
            }
        }

        if (!spindleSet) {
            issues.add(GCodeValidationIssue(0, IssueSeverity.WARNING, "В программе не задана частота вращения шпинделя (S...)."))
        }

        return issues
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
