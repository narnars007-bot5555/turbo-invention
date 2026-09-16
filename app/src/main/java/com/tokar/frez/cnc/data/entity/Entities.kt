package com.tokar.frez.cnc.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,             // Марка стали
    val isoGroup: String,         // ISO 513: P, M, K, N, S, H
    val tensileStrength: Double,  // Rm (MPa)
    val hardnessHb: Double,       // HB / HRC
    val vcCoeff: Double,          // Vc multiplier
    val fzCoeff: Double           // fz multiplier
)

@Entity(
    tableName = "iso_tolerances",
    indices = [
        Index(value = ["fieldName", "nominalRangeMin", "nominalRangeMax"])
    ]
)
data class IsoToleranceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nominalRangeMin: Double,  // mm
    val nominalRangeMax: Double,  // mm
    val fieldName: String,        // e.g., "H7", "h6", "js16"
    val upperDevUm: Double,       // um
    val lowerDevUm: Double        // um
)

@Entity(tableName = "threads")
data class ThreadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val standard: String,         // ISO 965, ISO 68-1, UNC, UNF, G
    val designation: String,      // M12x1.75, M10, G1/2
    val nominalDiameter: Double,  // mm
    val pitch: Double,            // mm
    val tapDrillDia: Double       // mm
)

@Entity(tableName = "cnc_cycles")
data class CncCycleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val functionType: String,     // Turning Roughing, Threading, Drilling, Pocketing
    val fanucCode: String,        // G71, G76, G81
    val sinumerikCode: String,    // CYCLE95, CYCLE97, CYCLE81
    val haasCode: String,         // G71, G76, G81
    val heidenhainCode: String,   // CYCL DEF 200, CYCL DEF 260
    val description: String
)

@Entity(tableName = "tool_fixtures")
data class ToolFixtureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,         // Cutting Tool, Tool Holder, Gauge, Measuring Instrument
    val name: String,             // PCLNR 2525M12, BT40-ER32-100, Штангенциркуль
    val standardCode: String,     // ISO 1832, DIN 69871, ГОСТ 166-89
    val specifications: String
)
