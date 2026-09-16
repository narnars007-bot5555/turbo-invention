package com.tokar.frez.cnc.domain.model

data class GearParams(
    val module: Double,            // m (mm)
    val teeth: Int,                // z
    val pressureAngleDeg: Double = 20.0, // alpha
    val spanTeeth: Int = 0         // k (for base tangent W)
)

data class GearResult(
    val pitchDiameter: Double,     // d = m * z
    val tipDiameter: Double,       // da = m * (z + 2)
    val rootDiameter: Double,      // df = m * (z - 2.5)
    val baseDiameter: Double,      // db = d * cos(alpha)
    val baseTangentLength: Double, // W (length of base tangent)
    val recommendedSpanTeeth: Int  // k
)

data class GearShapingParams(
    val strokeLength: Double,      // L_str (mm)
    val cuttingSpeed: Double,      // Vc (m/min)
    val radialFeed: Double,        // mm/stroke
    val circularFeed: Double,      // mm/stroke
    val passes: Int = 2
)

data class GearShapingResult(
    val doubleStrokesPerMin: Int,  // n_st
    val estimatedTimeMinutes: Double
)

data class IsoToleranceParams(
    val nominalSizeMm: Double,    // e.g. 50.0
    val toleranceField: String    // e.g. "H7", "h6", "js16"
)

data class IsoToleranceResult(
    val nominalSize: Double,
    val toleranceField: String,
    val esUpperUm: Double,        // Upper deviation in um
    val eiLowerUm: Double,        // Lower deviation in um
    val minLimitMm: Double,
    val maxLimitMm: Double
)

data class ThreadParams(
    val threadName: String,       // e.g. "M12x1.75", "G1/2"
    val nominalDiameter: Double,  // mm
    val pitch: Double             // mm
)

data class ThreadResult(
    val nominalDiameter: Double,
    val pitch: Double,
    val pitchDiameter: Double,    // d2
    val minorDiameter: Double,    // d1
    val threadHeight: Double,     // H1
    val tapDrillDiameter: Double  // D_drill
)

data class InsertCodeDecoded(
    val code: String,
    val shape: String,
    val clearanceAngle: String,
    val toleranceClass: String,
    val typeAndFeatures: String,
    val sizeMm: String,
    val thicknessMm: String,
    val cornerRadiusMm: String
)

data class TimeNormativeParams(
    val cuttingTimeMin: Double,   // To
    val auxiliaryTimeMin: Double, // Tv
    val batchSize: Int = 1,       // N
    val setupTimeMin: Double = 0.0// Tpz
)

data class TimeNormativeResult(
    val operatingTimeMin: Double, // Top = To + Tv
    val pieceTimeMin: Double,     // Tst = Top * 1.1
    val calcBatchTimeMin: Double  // Tcalc = Tst + Tpz/N
)

data class TaylorToolLifeParams(
    val vc: Double,               // m/min
    val taylorConstantC: Double = 300.0,
    val exponentN: Double = 0.25  // n exponent
)

data class TaylorToolLifeResult(
    val toolLifeMin: Double        // T (min)
)

data class DefectInfo(
    val defectName: String,
    val category: String,
    val probableCauses: List<String>,
    val correctiveActions: List<String>
)
