package com.tokar.frez.cnc.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
