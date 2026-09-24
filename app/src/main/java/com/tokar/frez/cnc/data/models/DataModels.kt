package com.tokar.frez.cnc.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CncDatabase(
    val brands: List<Brand> = emptyList()
)

@Serializable
data class Brand(
    val id: String,
    val name: String,
    val country: String,
    val models: List<CncModel> = emptyList()
)

@Serializable
data class CncModel(
    val id: String,
    val name: String,
    val type: String,
    @SerialName("image_panel") val imagePanel: String,
    @SerialName("model_3d") val model3d: String? = null,
    val hotspots: List<Hotspot> = emptyList(),
    val scenarios: List<Scenario> = emptyList()
)

@Serializable
data class Hotspot(
    val id: String,
    val title: String,
    @SerialName("x_percent") val xPercent: Float,
    @SerialName("y_percent") val yPercent: Float,
    @SerialName("radius_dp") val radiusDp: Int = 22,
    val description: String,
    val category: String
)

@Serializable
data class Scenario(
    val id: String,
    val title: String,
    val steps: List<ScenarioStep> = emptyList()
)

@Serializable
data class ScenarioStep(
    val step: Int,
    val instruction: String,
    @SerialName("target_hotspot") val targetHotspot: String
)
