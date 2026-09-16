package com.tokar.frez.cnc.domain.model

data class TurningParams(
    val vc: Double,         // m/min
    val n: Double,          // RPM
    val feed: Double,       // mm/rev (f)
    val diameter: Double,   // mm (D)
    val ap: Double,         // mm (depth of cut)
    val toolRadius: Double  // mm (r_epsilon)
)

data class TurningResult(
    val vc: Double,
    val n: Double,
    val feed: Double,
    val mrr: Double,        // cm3/min
    val pc: Double,         // kW
    val ra: Double,         // um
    val rz: Double          // um
)

data class MillingParams(
    val vc: Double,         // m/min
    val n: Double,          // RPM
    val fz: Double,         // mm/tooth
    val teeth: Int,         // z
    val diameter: Double,   // mm (D)
    val ap: Double,         // mm (axial depth)
    val ae: Double          // mm (radial width)
)

data class MillingResult(
    val vc: Double,
    val n: Double,
    val vf: Double,         // mm/min (feed rate)
    val mrr: Double,        // cm3/min
    val pc: Double          // kW
)

data class TaperParams(
    val bigDiameter: Double,   // D (mm)
    val smallDiameter: Double, // d (mm)
    val taperLength: Double,   // L (mm)
    val totalLength: Double    // L_total (mm)
)

data class TaperResult(
    val taperRatio: Double,    // C = (D-d)/L
    val halfAngleDeg: Double,  // alpha/2 in degrees
    val tailstockOffset: Double// ZB in mm
)
