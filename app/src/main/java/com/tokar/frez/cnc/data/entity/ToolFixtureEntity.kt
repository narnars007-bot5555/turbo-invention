package com.tokar.frez.cnc.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tool_fixtures")
data class ToolFixtureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,         // Cutting Tool, Tool Holder, Gauge, Measuring Instrument
    val name: String,             // PCLNR 2525M12, BT40-ER32-100, Штангенциркуль
    val standardCode: String,     // ISO 1832, DIN 69871, ГОСТ 166-89
    val specifications: String
)
