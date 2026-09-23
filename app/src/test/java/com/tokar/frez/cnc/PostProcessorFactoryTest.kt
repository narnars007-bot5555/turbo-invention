package com.tokar.frez.cnc

import com.tokar.frez.cnc.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test

class PostProcessorFactoryTest {

    @Test
    fun testPostProcessorDialectsAndProhibitions() {
        val fanuc = PostProcessorFactory.forProfile(MachineProfiles.enterpriseFleet[0])
        val siemens = PostProcessorFactory.forProfile(MachineProfiles.enterpriseFleet[1])
        val haas = PostProcessorFactory.forProfile(MachineProfiles.enterpriseFleet[2])
        val okuma = PostProcessorFactory.forProfile(MachineProfiles.enterpriseFleet[3])

        val params = GCodeGenerationParams(
            cycleType = "G71",
            startDiameterMm = 60.0,
            endDiameterMm = 40.0,
            lengthMm = 50.0,
            depthOfCutMm = 2.0,
            feedRate = 0.25,
            pitchMm = 1.5,
            peckDepthMm = 2.0
        )

        // Fanuc checks
        val fanucCode = fanuc.roughTurning(params)
        assertTrue(fanucCode.contains("G71"))

        // Siemens checks (must NOT contain Fanuc G71)
        val siemensCode = siemens.roughTurning(params)
        assertTrue(siemensCode.contains("CYCLE95"))
        assertFalse(siemensCode.contains("G71"))

        // Okuma checks
        val okumaCode = okuma.roughTurning(params)
        assertTrue(okumaCode.contains("G85"))

        // Haas checks
        val haasHeader = haas.header(MachineProfiles.enterpriseFleet[2], 1)
        assertTrue(haasHeader.contains("G21"))
    }
}
