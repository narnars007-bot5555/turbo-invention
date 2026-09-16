package com.tokar.frez.cnc

import com.tokar.frez.cnc.domain.model.GearParams
import com.tokar.frez.cnc.domain.model.GearShapingParams
import com.tokar.frez.cnc.domain.model.IsoToleranceParams
import com.tokar.frez.cnc.domain.model.TaylorToolLifeParams
import com.tokar.frez.cnc.domain.model.TimeNormativeParams
import com.tokar.frez.cnc.domain.usecase.DiagnosticMatrixUseCase
import com.tokar.frez.cnc.domain.usecase.GearCuttingUseCase
import com.tokar.frez.cnc.domain.usecase.IsoInsertDecoderUseCase
import com.tokar.frez.cnc.domain.usecase.IsoToleranceUseCase
import com.tokar.frez.cnc.domain.usecase.ThreadUseCase
import com.tokar.frez.cnc.domain.usecase.TimeNormativeUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GearCuttingIsoStandardsTest {

    private val gearCuttingUseCase = GearCuttingUseCase()
    private val isoToleranceUseCase = IsoToleranceUseCase()
    private val threadUseCase = ThreadUseCase()
    private val insertDecoderUseCase = IsoInsertDecoderUseCase()
    private val timeNormativeUseCase = TimeNormativeUseCase()
    private val diagnosticMatrixUseCase = DiagnosticMatrixUseCase()

    @Test
    fun testGearGeometryAndShaping() {
        val gearRes = gearCuttingUseCase.calculateGearGeometry(
            GearParams(module = 3.0, teeth = 30, pressureAngleDeg = 20.0)
        )
        assertEquals(90.0, gearRes.pitchDiameter, 0.001)
        assertEquals(96.0, gearRes.tipDiameter, 0.001)
        assertEquals(82.5, gearRes.rootDiameter, 0.001)
        assertTrue(gearRes.baseTangentLength > 20.0)

        val shapingRes = gearCuttingUseCase.calculateGearShaping(
            GearShapingParams(strokeLength = 40.0, cuttingSpeed = 20.0, radialFeed = 0.04, circularFeed = 0.2)
        )
        assertEquals(250, shapingRes.doubleStrokesPerMin)
    }

    @Test
    fun testIsoToleranceCalculation() {
        val tolH7 = isoToleranceUseCase.calculateTolerance(
            IsoToleranceParams(nominalSizeMm = 40.0, toleranceField = "H7")
        )
        assertEquals(0.0, tolH7.eiLowerUm, 0.1)
        assertTrue(tolH7.esUpperUm > 20.0 && tolH7.esUpperUm < 30.0)

        val tolH6 = isoToleranceUseCase.calculateTolerance(
            IsoToleranceParams(nominalSizeMm = 40.0, toleranceField = "h6")
        )
        assertEquals(0.0, tolH6.esUpperUm, 0.1)
        assertTrue(tolH6.eiLowerUm < 0.0)
    }

    @Test
    fun testThreadCalculation() {
        val threadRes = threadUseCase.calculateThread(
            threadUseCase.parseStandardThread("M12x1.75")
        )
        assertEquals(12.0, threadRes.nominalDiameter, 0.001)
        assertEquals(1.75, threadRes.pitch, 0.001)
        assertEquals(10.25, threadRes.tapDrillDiameter, 0.01)
    }

    @Test
    fun testIsoInsertDecoder() {
        val decoded = insertDecoderUseCase.decodeInsert("CNMG120408")
        assertTrue(decoded.shape.contains("Ромб 80°"))
        assertTrue(decoded.clearanceAngle.contains("0°"))
        assertTrue(decoded.cornerRadiusMm.contains("0.8"))
    }

    @Test
    fun testTimeNormativeAndTaylor() {
        val normRes = timeNormativeUseCase.calculateNormativeTime(
            TimeNormativeParams(cuttingTimeMin = 10.0, auxiliaryTimeMin = 2.0, batchSize = 10, setupTimeMin = 30.0)
        )
        assertEquals(12.0, normRes.operatingTimeMin, 0.1)
        assertEquals(13.2, normRes.pieceTimeMin, 0.1)
        assertEquals(16.2, normRes.calcBatchTimeMin, 0.1)

        val taylor = timeNormativeUseCase.calculateTaylorToolLife(
            TaylorToolLifeParams(vc = 150.0, taylorConstantC = 300.0, exponentN = 0.25)
        )
        assertEquals(16.0, taylor.toolLifeMin, 0.1)
    }

    @Test
    fun testDiagnosticMatrix() {
        val defects = diagnosticMatrixUseCase.getAllDefects()
        assertTrue(defects.size >= 4)
        val searchRes = diagnosticMatrixUseCase.searchDefects("прижог")
        assertEquals(1, searchRes.size)
        assertNotNull(searchRes.first())
    }
}
