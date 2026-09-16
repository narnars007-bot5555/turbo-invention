package com.tokar.frez.cnc.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
