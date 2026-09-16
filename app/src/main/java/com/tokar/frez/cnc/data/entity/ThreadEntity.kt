package com.tokar.frez.cnc.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "threads")
data class ThreadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val standard: String,         // ISO 965, ISO 68-1, UNC, UNF, G
    val designation: String,      // M12x1.75, M10, G1/2
    val nominalDiameter: Double,  // mm
    val pitch: Double,            // mm
    val tapDrillDia: Double       // mm
)
