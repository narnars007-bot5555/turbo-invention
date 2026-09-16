package com.tokar.frez.cnc.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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
