package com.tokar.frez.cnc.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,             // Марка стали (e.g. 09Г2С, 40Х, 12Х18Н10Т, Д16Т, ВТ6, СЧ20)
    val isoGroup: String,         // ISO 513: P, M, K, N, S, H
    val tensileStrength: Double,  // Rm (MPa)
    val hardnessHb: Double,       // HB / HRC
    val hardnessRange: String = "180-220 HB", // Диапазон твердости
    val vcCoeff: Double = 1.0,    // Vc multiplier
    val fzCoeff: Double = 1.0,    // fz multiplier
    val recVcMin: Double = 100.0, // Рекомендуемая Vc min (м/мин)
    val recVcMax: Double = 250.0, // Рекомендуемая Vc max (м/мин)
    val recFeedMin: Double = 0.1, // Рекомендуемая подача min (мм/об)
    val recFeedMax: Double = 0.35 // Рекомендуемая подача max (мм/об)
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

@Entity(tableName = "calculation_history")
data class CalculationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val operationName: String,
    val category: String,
    val materialName: String,
    val machineName: String,
    val toolName: String,
    val inputParamsJson: String,
    val resultSummaryJson: String,
    val technologistComment: String
)

@Entity(tableName = "tool_wear_journal")
data class ToolWearJournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val toolId: String,               // T01 .. T99
    val partCount: Int,
    val operatingTimeMin: Double,
    val measuredWearXMm: Double,
    val measuredWearZMm: Double,
    val measuredWearYMm: Double,
    val lastMeasuredTimestamp: Long = System.currentTimeMillis(),
    val cause: String,                // Абразивный, Адгезионный, Термический, Выкрашивание, Поломка
    val remainingLifePercent: Double, // 0 - 100%
    val comment: String
)

@Entity(
    tableName = "machines",
    indices = [
        Index(value = ["machineClass"]),
        Index(value = ["cncSystem"]),
        Index(value = ["machineClass", "cncSystem"])
    ]
)
data class MachineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val machineClass: String, // Vertical Milling, Horizontal Milling, 5-Axis MC, Turning, Swiss-Type, Gear Cutting, Grinding
    val cncSystem: String,    // HAAS, FANUC, SINUMERIK, HEIDENHAIN
    val modelName: String,    // e.g. "VF-2SS", "Puma 2100SY", "DMC 635 V"
    val description: String,  // Specifications & Features
    val setupGuideStepsJson: String, // JSON string list of setup guide steps
    val gcodeHandbookJson: String    // JSON string map of G/M codes and descriptions
)
