package com.tokar.frez.cnc.domain.usecase

import kotlin.math.*

enum class MotionType {
    RAPID,          // G00
    LINEAR,         // G01
    ARC_CW,         // G02
    ARC_CCW,        // G03
    DRILL_CYCLE,    // G81 / G83
    TURNING_CYCLE,  // G71 / G72 / G73 / CYCLE95
    THREAD_CYCLE,   // G76 / CYCLE97
    DWELL           // G04
}

data class ParsedGCodeBlock(
    val lineNumber: Int,
    val rawText: String,
    val motionType: MotionType,
    val startX: Float,
    val startY: Float,
    val startZ: Float,
    val endX: Float,
    val endY: Float,
    val endZ: Float,
    val centerI: Float = 0f,
    val centerJ: Float = 0f,
    val radiusR: Float = 0f,
    val feedRate: Float = 0f,
    val spindleRpm: Float = 0f,
    val toolNumber: Int = 0,
    val arcPoints: List<Pair<Float, Float>> = emptyList(), // Interpolated points along G02/G03 arc
    val syntaxErrors: List<String> = emptyList()
)

data class GCodeParseResult(
    val blocks: List<ParsedGCodeBlock>,
    val toolpathPoints: List<ToolpathPoint>,
    val totalDistanceMm: Double,
    val estimatedTimeSec: Double,
    val errors: List<String>,
    val warnings: List<String>
)

class GCodeParserUseCase {

    fun parseProgram(gcodeText: String): GCodeParseResult {
        val blocks = mutableListOf<ParsedGCodeBlock>()
        val toolpathPoints = mutableListOf<ToolpathPoint>()
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        var currentX = 0f
        var currentY = 0f
        var currentZ = 0f
        var currentF = 100f
        var currentS = 1000f
        var currentT = 1
        var currentMotion = MotionType.RAPID

        toolpathPoints.add(ToolpathPoint(currentX, currentY, isRapid = true))

        var totalDist = 0.0
        var totalTimeSec = 0.0

        val lines = gcodeText.split("\n")
        lines.forEachIndexed { index, rawLine ->
            val lineNum = index + 1
            val clean = rawLine.uppercase().takeWhile { it != '(' && it != ';' }.trim()
            if (clean.isEmpty()) return@forEachIndexed

            val blockErrors = mutableListOf<String>()

            // Extract Modal commands
            if (clean.contains("G00") || clean.contains("G0 ") || clean.endsWith("G0")) currentMotion = MotionType.RAPID
            else if (clean.contains("G01") || clean.contains("G1 ") || clean.endsWith("G1")) currentMotion = MotionType.LINEAR
            else if (clean.contains("G02") || clean.contains("G2 ") || clean.endsWith("G2")) currentMotion = MotionType.ARC_CW
            else if (clean.contains("G03") || clean.contains("G3 ") || clean.endsWith("G3")) currentMotion = MotionType.ARC_CCW
            else if (clean.contains("G71") || clean.contains("G72") || clean.contains("G73") || clean.contains("CYCLE95")) currentMotion = MotionType.TURNING_CYCLE
            else if (clean.contains("G76") || clean.contains("CYCLE97")) currentMotion = MotionType.THREAD_CYCLE
            else if (clean.contains("G81") || clean.contains("G83")) currentMotion = MotionType.DRILL_CYCLE

            // Parse Feed, Speed, Tool
            val tokens = clean.split(" ")
            var newX = currentX
            var newY = currentY
            var newZ = currentZ
            var iVal = 0f
            var jVal = 0f
            var rVal = 0f
            var depthU = 2.0f
            var pitchF = 1.5f

            for (token in tokens) {
                if (token.startsWith("F")) token.drop(1).toFloatOrNull()?.let { currentF = it; pitchF = it }
                else if (token.startsWith("S")) token.drop(1).toFloatOrNull()?.let { currentS = it }
                else if (token.startsWith("T")) token.drop(1).toIntOrNull()?.let { currentT = it }
                else if (token.startsWith("X")) token.drop(1).toFloatOrNull()?.let { newX = it }
                else if (token.startsWith("Y")) token.drop(1).toFloatOrNull()?.let { newY = it }
                else if (token.startsWith("Z")) token.drop(1).toFloatOrNull()?.let { newZ = it }
                else if (token.startsWith("I")) token.drop(1).toFloatOrNull()?.let { iVal = it }
                else if (token.startsWith("J")) token.drop(1).toFloatOrNull()?.let { jVal = it }
                else if (token.startsWith("R")) token.drop(1).toFloatOrNull()?.let { rVal = it }
                else if (token.startsWith("U")) token.drop(1).toFloatOrNull()?.let { depthU = it }
            }

            val startX = currentX
            val startY = currentY
            val startZ = currentZ

            val arcPoints = mutableListOf<Pair<Float, Float>>()

            // Multi-pass G71 / CYCLE95 Turning Cycle expansion
            if (currentMotion == MotionType.TURNING_CYCLE) {
                val passes = 4
                val passDepth = depthU / passes
                for (pass in 1..passes) {
                    val passX = startX - pass * passDepth
                    toolpathPoints.add(ToolpathPoint(passX, startY, isRapid = false))
                    toolpathPoints.add(ToolpathPoint(passX, newZ, isRapid = false))
                    toolpathPoints.add(ToolpathPoint(startX, newZ, isRapid = true))
                    toolpathPoints.add(ToolpathPoint(startX, startY, isRapid = true))
                }
            } else if (currentMotion == MotionType.THREAD_CYCLE) {
                // Multi-pass G76 / CYCLE97 Threading Cycle expansion
                val threadDepth = 0.6134f * pitchF
                val passes = 5
                for (pass in 1..passes) {
                    val passX = startX - (pass.toFloat() / passes) * threadDepth * 2f
                    toolpathPoints.add(ToolpathPoint(passX, startY, isRapid = true))
                    toolpathPoints.add(ToolpathPoint(passX, newZ, isRapid = false))
                    toolpathPoints.add(ToolpathPoint(startX, newZ, isRapid = true))
                    toolpathPoints.add(ToolpathPoint(startX, startY, isRapid = true))
                }
            } else if (currentMotion == MotionType.ARC_CW || currentMotion == MotionType.ARC_CCW) {
                val cx = startX + iVal
                val cy = startY + jVal
                val radius = if (rVal != 0f) abs(rVal) else hypot(iVal.toDouble(), jVal.toDouble()).toFloat()

                val startAngle = atan2((startY - cy).toDouble(), (startX - cx).toDouble())
                var endAngle = atan2((newY - cy).toDouble(), (newX - cx).toDouble())

                if (currentMotion == MotionType.ARC_CW && endAngle >= startAngle) {
                    endAngle -= 2 * PI
                } else if (currentMotion == MotionType.ARC_CCW && endAngle <= startAngle) {
                    endAngle += 2 * PI
                }

                val steps = 16
                for (step in 0..steps) {
                    val t = step.toFloat() / steps
                    val angle = startAngle + t * (endAngle - startAngle)
                    val px = (cx + radius * cos(angle)).toFloat()
                    val py = (cy + radius * sin(angle)).toFloat()
                    arcPoints.add(Pair(px, py))
                    toolpathPoints.add(ToolpathPoint(px, py, isRapid = false))
                }
            } else {
                if (newX != currentX || newY != currentY || newZ != currentZ) {
                    toolpathPoints.add(ToolpathPoint(newX, newY, isRapid = (currentMotion == MotionType.RAPID)))
                }
            }

            val deltaX = (newX - startX).toDouble()
            val deltaY = (newY - startY).toDouble()
            val deltaZ = (newZ - startZ).toDouble()
            val segmentDist = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)

            totalDist += segmentDist
            if (currentF > 0) {
                val speed = if (currentMotion == MotionType.RAPID) 5000.0 else currentF.toDouble()
                totalTimeSec += (segmentDist / speed) * 60.0
            }

            currentX = newX
            currentY = newY
            currentZ = newZ

            val block = ParsedGCodeBlock(
                lineNumber = lineNum,
                rawText = rawLine,
                motionType = currentMotion,
                startX = startX,
                startY = startY,
                startZ = startZ,
                endX = newX,
                endY = newY,
                endZ = newZ,
                centerI = iVal,
                centerJ = jVal,
                radiusR = rVal,
                feedRate = currentF,
                spindleRpm = currentS,
                toolNumber = currentT,
                arcPoints = arcPoints,
                syntaxErrors = blockErrors
            )

            blocks.add(block)
        }

        return GCodeParseResult(
            blocks = blocks,
            toolpathPoints = toolpathPoints,
            totalDistanceMm = totalDist,
            estimatedTimeSec = totalTimeSec,
            errors = errors,
            warnings = warnings
        )
    }
}
