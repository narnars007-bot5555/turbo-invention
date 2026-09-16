package com.tokar.frez.cnc

import com.tokar.frez.cnc.domain.model.DressingParams
import com.tokar.frez.cnc.domain.model.GrindingParams
import com.tokar.frez.cnc.domain.model.TaperParams
import com.tokar.frez.cnc.domain.usecase.GrindingUseCase
import com.tokar.frez.cnc.domain.usecase.TurningMillingUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TurningMillingGrindingTest {

    private val turningMillingUseCase = TurningMillingUseCase()
    private val grindingUseCase = GrindingUseCase()

    @Test
    fun testTurningCalculation() {
        val res = turningMillingUseCase.calculateTurning(
            diameter = 50.0,
            vc = 200.0,
            feed = 0.2,
            ap = 2.0,
            toolRadius = 0.8
        )

        assertEquals(1273.23, res.n, 1.0)
        assertEquals(80.0, res.mrr, 0.1)
        assertEquals(6.25, res.rz, 0.01)
        assertEquals(1.5625, res.ra, 0.01)
    }

    @Test
    fun testMillingCalculation() {
        val res = turningMillingUseCase.calculateMilling(
            diameter = 63.0,
            teeth = 5,
            vc = 180.0,
            fz = 0.1,
            ap = 3.0,
            ae = 40.0
        )

        assertTrue(res.n > 900.0)
        assertTrue(res.vf > 400.0)
        assertTrue(res.mrr > 50.0)
    }

    @Test
    fun testTaperCalculation() {
        val res = turningMillingUseCase.calculateTaper(
            TaperParams(
                bigDiameter = 50.0,
                smallDiameter = 40.0,
                taperLength = 100.0,
                totalLength = 200.0
            )
        )

        assertEquals(0.1, res.taperRatio, 0.001)
        assertEquals(10.0, res.tailstockOffset, 0.001)
    }

    @Test
    fun testGrindingAndDressing() {
        val gRes = grindingUseCase.calculateGrinding(
            GrindingParams(
                wheelDiameter = 400.0,
                wheelRpm = 1500.0,
                workpieceDiameter = 80.0,
                workpieceRpm = 100.0,
                depthOfCut = 20.0,
                longitudinalFeed = 5.0
            )
        )

        assertEquals(31.415, gRes.vs, 0.1)
        assertEquals(25.132, gRes.vw, 0.1)

        val dRes = grindingUseCase.calculateDressing(
            DressingParams(diamondTipWidth = 0.8, dressingFeed = 0.2)
        )
        assertEquals(4.0, dRes.ed, 0.01)

        val decode = grindingUseCase.decodeWheelMarking("25A F60 K 5 V")
        assertTrue(decode.abrasiveType.contains("25А"))
        assertTrue(decode.grainSize.contains("F60"))
    }
}
