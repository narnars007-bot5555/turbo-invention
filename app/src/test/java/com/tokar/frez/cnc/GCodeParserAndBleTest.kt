package com.tokar.frez.cnc

import com.tokar.frez.cnc.data.ble.BleMeasurementManager
import com.tokar.frez.cnc.data.entity.ToolWearJournalEntity
import com.tokar.frez.cnc.domain.usecase.*
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
    fun testMultiPassTurningAndThreadCycles() {
        val parser = GCodeParserUseCase()
        val gcode = """
            G00 X60 Z5
            G71 U2.0 R1.0
            G76 P010060 Q100 R0.05
        """.trimIndent()

        val result = parser.parseProgram(gcode)
        assertNotNull(result)
        assertTrue(result.toolpathPoints.size > 5)
    }

    @Test
    fun testBleMeasurementPacketAndMedianFilter() {
        val bleManager = BleMeasurementManager()

        // Feed raw measurements
        bleManager.simulateIncomingMeasurement(50.0)
        bleManager.simulateIncomingMeasurement(50.1)
        bleManager.simulateIncomingMeasurement(49.9)
        bleManager.simulateIncomingMeasurement(50.2)
        bleManager.simulateIncomingMeasurement(50.0)

        val latest = bleManager.latestMeasurement.value
        assertNotNull(latest)
        assertEquals(50.0, latest!!.measuredValueMm, 0.05)
        assertTrue(latest.valueInInches > 0)
    }

    @Test
    fun testExportFanucOffsetsAndRouteSheet() {
        val exportUseCase = ExportImportUseCase()
        val wearList = listOf(
            ToolWearJournalEntity(
                id = 1,
                toolId = "T0101",
                partCount = 50,
                operatingTimeMin = 120.0,
                measuredWearXMm = 0.08,
                measuredWearZMm = 0.12,
                measuredWearYMm = 0.0,
                lastMeasuredTimestamp = System.currentTimeMillis(),
                cause = "FLANK_WEAR",
                remainingLifePercent = 84.0,
                comment = "Пластина норма"
            )
        )

        val offsetContent = exportUseCase.exportFanucToolOffsetFile(wearList)
        assertTrue(offsetContent.contains("O9001"))
        assertTrue(offsetContent.contains("G10 L3 P01 U0.0800 W0.1200"))

        val sheet = TechnologicalRouteSheet(
            partName = "Втулка шлицевая",
            operationName = "Токарная обдирка",
            materialName = "40Х",
            machineName = "16К20Ф3 (Fanuc)",
            toolName = "T0101 CNMG 120408",
            vcMmin = 180.0,
            rpm = 1200,
            feedMmRev = 0.25,
            apMm = 2.0,
            cuttingForceN = 1125.0,
            powerKw = 4.0,
            warnings = emptyList(),
            technologistComment = "Проверить зажим"
        )

        val textSheet = exportUseCase.generateTechnologicalRouteSheetText(sheet)
        assertTrue(textSheet.contains("ТЕХНОЛОГИЧЕСКАЯ КАРТА"))
        assertTrue(textSheet.contains("40Х"))
    }

    @Test
    fun testSpindlePowerAutoLowering() {
        val forceUseCase = CuttingForceUseCase()
        val res = forceUseCase.calculateCuttingForces(
            apMm = 5.0,
            fnMmRev = 0.4,
            kcNmm2 = 2200.0,
            mcExponent = 0.25,
            diameterMm = 80.0,
            vcMmin = 200.0,
            maxSpindlePowerKw = 5.0 // Force power limit override
        )

        assertTrue(res.powerKw > 5.0)
        assertTrue(res.recommendedLoweredApMm < 5.0)
    }
}
