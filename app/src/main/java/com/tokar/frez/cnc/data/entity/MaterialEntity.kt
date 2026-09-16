package com.tokar.frez.cnc.data.entity

import androidx.room.Entity
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
