package com.tokar.frez.cnc

import com.tokar.frez.cnc.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test

class EngineeringUseCasesTest {

    @Test
    fun testToolWearUseCase() {
        val useCase = ToolWearUseCase()
        val result = useCase.calculateToolWear(
            nominalMm = 50.0,
            actualMm = 49.92,
            measuredAxisZNominalMm = 100.0,
            measuredAxisZActualMm = 99.85,
            mode = CompensationMode.DIAMETER,
            cncSystem = CncSystemType.FANUC
        )

        assertEquals(0.08, result.deltaXmm, 0.001)
        assertEquals(80.0, result.deltaXUm, 0.1)
        assertEquals(0.15, result.deltaZmm, 0.001)
        assertTrue(result.inputHint.contains("Fanuc"))
    }

    @Test
    fun testGCodeEngineUseCase() {
        val useCase = GCodeEngineUseCase()
        val params = GCodeGenerationParams(
            cycleType = "G71_CYCLE95",
            startDiameterMm = 60.0,
            endDiameterMm = 40.0,
            lengthMm = 50.0,
            depthOfCutMm = 2.0,
            feedRate = 0.25
        )

        val gcode = useCase.generateCycleGCode(params, CncSystemType.FANUC)
        assertTrue(gcode.contains("G71"))
        assertTrue(gcode.contains("U2.0"))

        val points = useCase.parseToolpath(gcode)
        assertTrue(points.isNotEmpty())
    }

    @Test
    fun testCuttingForceUseCase() {
        val useCase = CuttingForceUseCase()
        val res = useCase.calculateCuttingForces(
            apMm = 2.5,
            fnMmRev = 0.25,
            kcNmm2 = 1800.0,
            diameterMm = 50.0,
            vcMmin = 160.0,
            clampingForceN = 15000.0
        )

        assertEquals(1125.0, res.fcTangentialN, 0.1)
        assertEquals(450.0, res.fpRadialN, 0.1)
        assertEquals(337.5, res.ffAxialN, 0.1)
        assertTrue(res.isClampingSafe)
    }

    @Test
    fun testCostPerEdgeUseCase() {
        val useCase = CostPerEdgeUseCase()
        val res = useCase.calculateCostPerEdge(
            cCoeff = 300.0,
            mExponent = 0.25,
            vcMmin = 180.0,
            machiningTimePerPartMin = 2.5,
            insertCostRub = 450.0,
            edgesPerInsert = 4,
            machineHourlyRateRub = 2500.0
        )

        assertTrue(res.toolLifeMin > 0)
        assertTrue(res.partsPerEdge >= 1)
        assertTrue(res.totalCostPerPart > 0)
    }

    @Test
    fun testIsoMatingUseCase() {
        val useCase = IsoMatingUseCase()
        val mating = useCase.calculateMating(
            nominalMm = 50.0,
            holeField = "H7",
            shaftField = "g6"
        )

        assertEquals(50.0, mating.nominalSize, 0.001)
        assertEquals(FitType.CLEARANCE, mating.fitType)
        assertTrue(mating.maxClearanceUm > 0)
    }
}
