package com.tokar.frez.cnc.domain.model

data class GrindingParams(
    val wheelDiameter: Double,    // Ds (mm)
    val wheelRpm: Double,         // ns (RPM)
    val workpieceDiameter: Double,// Dw (mm)
    val workpieceRpm: Double,     // nw (RPM)
    val depthOfCut: Double,       // ae (um)
    val longitudinalFeed: Double  // fs (mm/rev or mm/min)
)

data class GrindingResult(
    val vs: Double,               // Wheel speed Vs (m/s)
    val vw: Double,               // Workpiece speed Vw (m/min)
    val speedRatio: Double,       // q = (Vs * 60) / Vw
    val depthMm: Double           // ae in mm
)

data class DressingParams(
    val diamondTipWidth: Double,  // bd (mm)
    val dressingFeed: Double      // f_dres (mm/rev)
)

data class DressingResult(
    val ud: Double,               // Dressing lead Ud
    val ed: Double                // Overlap ratio Ed = bd / f_dres
)

data class WheelMarkingDecoded(
    val originalCode: String,
    val abrasiveType: String,
    val grainSize: String,
    val hardness: String,
    val structure: String,
    val bondType: String,
    val description: String
)
