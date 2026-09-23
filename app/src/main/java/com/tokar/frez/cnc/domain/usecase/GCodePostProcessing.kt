package com.tokar.frez.cnc.domain.usecase

enum class CncControllerType(val displayName: String) {
    FANUC_0I_TF("Fanuc 0i-TF"),
    GSK("GSK"),
    SIEMENS_828D("Siemens Sinumerik 828D/840D"),
    OKUMA_OSP("Okuma OSP-P300/P500"),
    HAAS_NGC("Haas NGC"),
    HEIDENHAIN("Heidenhain")
}

data class MachineProfile(
    val id: String,
    val brand: String,
    val model: String,
    val controller: CncControllerType,
    val maxSpindleRpm: Int,
    val highPressureCoolantMCode: String = "M88",
    val coolantOffMCode: String = "M09",
    val spindleStopMCode: String = "M05"
) {
    val displayName: String get() = "$brand $model · ${controller.displayName}"
}

object MachineProfiles {
    val enterpriseFleet = listOf(
        MachineProfile("dn_dnm_5700", "DN Solutions", "NHP 5000", CncControllerType.FANUC_0I_TF, 8000),
        MachineProfile("dmg_ctx", "DMG MORI", "CTX beta 800", CncControllerType.SIEMENS_828D, 12000, "M88"),
        MachineProfile("haas_st", "HAAS", "ST-20", CncControllerType.HAAS_NGC, 6000, "M88"),
        MachineProfile("okuma_genos", "Okuma", "GENOS L3000", CncControllerType.OKUMA_OSP, 5000, "M13")
    )
    val default: MachineProfile get() = enterpriseFleet.first()
}

interface GCodePostProcessor {
    val controller: CncControllerType
    fun header(profile: MachineProfile, toolNumber: Int): String
    fun facing(params: GCodeGenerationParams): String
    fun roughTurning(params: GCodeGenerationParams): String
    fun threading(params: GCodeGenerationParams): String
    fun grooving(params: GCodeGenerationParams): String
    fun drilling(params: GCodeGenerationParams): String
    fun footer(profile: MachineProfile): String
}

abstract class BasePostProcessor(override val controller: CncControllerType) : GCodePostProcessor {
    protected fun safeTool(tool: Int) = tool.coerceIn(1, 99)
    protected fun commonFooter(profile: MachineProfile, endCode: String = "M30") =
        "G00 X${"%.3f".format(100.0)} Z${"%.3f".format(100.0)}\n${profile.coolantOffMCode}\n${profile.spindleStopMCode}\n$endCode"

    override fun facing(p: GCodeGenerationParams) = "G00 X${p.startDiameterMm + 3} Z2\nG01 Z0 F${p.feedRate}\nG01 X0"
    override fun grooving(p: GCodeGenerationParams) = "G00 X${p.startDiameterMm} Z-${p.lengthMm / 2}\nG75 R1\nG75 X${p.endDiameterMm} P${(p.depthOfCutMm * 1000).toInt()} Q500 F${p.feedRate}"
    override fun drilling(p: GCodeGenerationParams) = "G00 X0 Z2\nG83 Z-${p.lengthMm} Q${(p.peckDepthMm * 1000).toInt()} R1 F${p.feedRate}\nG80"
}

open class FanucPostProcessor : BasePostProcessor(CncControllerType.FANUC_0I_TF) {
    open override val controller: CncControllerType = CncControllerType.FANUC_0I_TF
    override fun header(profile: MachineProfile, toolNumber: Int) = "G21 G40 G80 G99\nT${safeTool(toolNumber).toString().padStart(2, '0')}${safeTool(toolNumber).toString().padStart(2, '0')} M06\n${profile.highPressureCoolantMCode}"
    override fun roughTurning(p: GCodeGenerationParams) = "G00 X${p.startDiameterMm + 3} Z2\nG71 U${p.depthOfCutMm} R1.0\nG71 P10 Q20 U0.5 W0.1 F${p.feedRate}\nN10 G00 X${p.startDiameterMm}\nG01 Z-${p.lengthMm} X${p.endDiameterMm}\nN20 G01 X${p.endDiameterMm + 3}"
    override fun threading(p: GCodeGenerationParams) = "G00 X${p.startDiameterMm + 3} Z2\nG76 P021060 Q100 R0.02\nG76 X${p.endDiameterMm} Z-${p.lengthMm} P${(0.6134 * p.pitchMm * 1000).toInt()} Q200 F${p.pitchMm}"
    override fun footer(profile: MachineProfile) = commonFooter(profile, "M30")
}

class SiemensPostProcessor : BasePostProcessor(CncControllerType.SIEMENS_828D) {
    override fun header(profile: MachineProfile, toolNumber: Int) = "G710 G95 LIMS=${profile.maxSpindleRpm}\nT=\"TOOL_${safeTool(toolNumber)}\" D1 M6\n${profile.highPressureCoolantMCode}"
    override fun roughTurning(p: GCodeGenerationParams) = "CYCLE95(\"CONTOUR\", ${p.depthOfCutMm}, 0.5, 0.1, 0, ${p.feedRate}, 0.1, 0.1)"
    override fun threading(p: GCodeGenerationParams) = "CYCLE97(${p.pitchMm}, , 0, -${p.lengthMm}, ${p.startDiameterMm}, ${p.endDiameterMm}, 5, 0.1, ${p.pitchMm}, 3, 4)"
    override fun footer(profile: MachineProfile) = commonFooter(profile, "M30")
}

class OkumaPostProcessor : BasePostProcessor(CncControllerType.OKUMA_OSP) {
    override fun header(profile: MachineProfile, toolNumber: Int) = "G15 H1\nT${safeTool(toolNumber)} M06\n${profile.highPressureCoolantMCode}"
    override fun roughTurning(p: GCodeGenerationParams) = "G00 X${p.startDiameterMm + 3} Z2\nG85 NBAR=10 D${p.depthOfCutMm} F${p.feedRate}"
    override fun threading(p: GCodeGenerationParams) = "G71 X${p.endDiameterMm} Z-${p.lengthMm} F${p.pitchMm}"
    override fun grooving(p: GCodeGenerationParams) = "G73 X${p.endDiameterMm} Z-${p.lengthMm / 2} D${p.depthOfCutMm} F${p.feedRate}"
    override fun drilling(p: GCodeGenerationParams) = "G81 Z-${p.lengthMm} Q${p.peckDepthMm} F${p.feedRate}\nG80"
    override fun footer(profile: MachineProfile) = commonFooter(profile, "M02")
}

class HaasPostProcessor : FanucPostProcessor() {
    override val controller = CncControllerType.HAAS_NGC
}

class GskPostProcessor : FanucPostProcessor() {
    override val controller = CncControllerType.GSK
}

class HeidenhainPostProcessor : BasePostProcessor(CncControllerType.HEIDENHAIN) {
    override fun header(profile: MachineProfile, toolNumber: Int) = "BEGIN PGM CNC MM\nTOOL CALL ${safeTool(toolNumber)} Z S${profile.maxSpindleRpm}"
    override fun roughTurning(p: GCodeGenerationParams) = "CYCL DEF 280 CONTOUR TURNING\nQ496=${p.startDiameterMm} Q497=-${p.lengthMm} Q498=${p.depthOfCutMm} Q505=${p.feedRate}"
    override fun threading(p: GCodeGenerationParams) = "CYCL DEF 260 THREAD CUTTING\nQ472=${p.pitchMm} Q474=-${p.lengthMm}"
    override fun footer(profile: MachineProfile) = "M09\nM05\nEND PGM CNC MM"
}

object PostProcessorFactory {
    fun forProfile(profile: MachineProfile): GCodePostProcessor = when (profile.controller) {
        CncControllerType.FANUC_0I_TF -> FanucPostProcessor()
        CncControllerType.GSK -> GskPostProcessor()
        CncControllerType.SIEMENS_828D -> SiemensPostProcessor()
        CncControllerType.OKUMA_OSP -> OkumaPostProcessor()
        CncControllerType.HAAS_NGC -> HaasPostProcessor()
        CncControllerType.HEIDENHAIN -> HeidenhainPostProcessor()
    }
}
