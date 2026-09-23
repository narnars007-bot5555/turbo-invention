package com.tokar.frez.cnc

import com.tokar.frez.cnc.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test

class GCodeSafetyTest {

    private val validator = GCodeValidator()

    @Test
    fun testGCodeValidatorGeometryAndLimits() {
        val profile = MachineProfiles.default

        // Invalid geometry: startDiameter <= endDiameter
        val inputInvalidDiameter = GCodeValidationInput(
            params = GCodeGenerationParams(
                cycleType = "G71",
                startDiameterMm = 40.0,
                endDiameterMm = 50.0,
                lengthMm = 30.0,
                depthOfCutMm = 2.0,
                feedRate = 0.2
            ),
            stockAllowancePerSideMm = 5.0,
            toolNumber = 1,
            spindleRpm = 1000,
            cuttingSpeedVc = 150.0
        )
        val resultInvalidDia = validator.validate(inputInvalidDiameter, profile)
        assertFalse(resultInvalidDia.isValid)
        assertTrue(resultInvalidDia.errors.any { it.contains("Начальный диаметр Dнач должен быть больше конечного") })

        // Valid input with warnings for high Vc
        val inputHighVc = GCodeValidationInput(
            params = GCodeGenerationParams(
                cycleType = "G71",
                startDiameterMm = 60.0,
                endDiameterMm = 40.0,
                lengthMm = 30.0,
                depthOfCutMm = 2.0,
                feedRate = 0.2
            ),
            stockAllowancePerSideMm = 10.0,
            toolNumber = 1,
            spindleRpm = 1200,
            cuttingSpeedVc = 320.0
        )
        val resultHighVc = validator.validate(inputHighVc, profile)
        assertTrue(resultHighVc.isValid)
        assertTrue(resultHighVc.warnings.any { it.contains("300 м/мин") })
    }

    @Test
    fun testPostProcessorFactoryAndSafetyTermination() {
        val fanucProfile = MachineProfiles.enterpriseFleet.first { it.controller == CncControllerType.FANUC_0I_TF }
        val siemensProfile = MachineProfiles.enterpriseFleet.first { it.controller == CncControllerType.SIEMENS_828D }
        val okumaProfile = MachineProfiles.enterpriseFleet.first { it.controller == CncControllerType.OKUMA_OSP }

        val fanucPP = PostProcessorFactory.forProfile(fanucProfile)
        val siemensPP = PostProcessorFactory.forProfile(siemensProfile)
        val okumaPP = PostProcessorFactory.forProfile(okumaProfile)

        assertTrue(fanucPP is FanucPostProcessor)
        assertTrue(siemensPP is SiemensPostProcessor)
        assertTrue(okumaPP is OkumaPostProcessor)

        val engine = GCodeEngineUseCase()
        val safeProgram = """
            G21 G40 G80
            T0101
            G00 X60 Z5
            G71 U2.0 R1.0
            G00 X100 Z100
            M09
            M05
            M30
        """.trimIndent()

        assertTrue(engine.hasSafeTermination(safeProgram))

        val unsafeProgram = """
            G21 G40 G80
            T0101
            G01 X50 Z-20 F0.2
        """.trimIndent()

        assertFalse(engine.hasSafeTermination(unsafeProgram))
    }
}
