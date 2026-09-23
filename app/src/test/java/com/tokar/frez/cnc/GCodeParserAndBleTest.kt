package com.tokar.frez.cnc

import com.tokar.frez.cnc.data.ble.BleMeasurementManager
import com.tokar.frez.cnc.domain.usecase.GCodeParserUseCase
import com.tokar.frez.cnc.domain.usecase.MotionType
import org.junit.Assert.*
import org.junit.Test

class GCodeParserAndBleTest {

    @Test
    fun testGCodeParserLinearAndArcInterpolation() {
        val parser = GCodeParserUseCase()
        val gcode = """
            G00 X0 Y0 S1200 M3
            G01 X50 Y0 F200
            G02 X50 Y50 I0 J25
            G03 X0 Y50 I-25 J0
            G81 X10 Y10 Z-15 R2 F100
        """.trimIndent()

        val result = parser.parseProgram(gcode)
        assertNotNull(result)
        assertTrue(result.blocks.isNotEmpty())

        val arcCwBlock = result.blocks.find { it.motionType == MotionType.ARC_CW }
        assertNotNull(arcCwBlock)
        assertTrue(arcCwBlock!!.arcPoints.isNotEmpty())

        val arcCcwBlock = result.blocks.find { it.motionType == MotionType.ARC_CCW }
        assertNotNull(arcCcwBlock)
        assertTrue(arcCcwBlock!!.arcPoints.isNotEmpty())

        assertTrue(result.totalDistanceMm > 0)
        assertTrue(result.estimatedTimeSec > 0)
    }

    @Test
    fun testBleMeasurementPacketParsing() {
        val bleManager = BleMeasurementManager()
        val rawData = "49.92".toByteArray()

        val packet = bleManager.parseBlePacketBytes(rawData, targetAxis = "X")
        assertNotNull(packet)
        assertEquals(49.92, packet!!.measuredValueMm, 0.001)
        assertEquals("X", packet.targetAxis)
    }
}
