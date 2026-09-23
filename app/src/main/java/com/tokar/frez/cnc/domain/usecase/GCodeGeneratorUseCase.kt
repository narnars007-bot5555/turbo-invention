package com.tokar.frez.cnc.domain.usecase

class GCodeGeneratorUseCase(
    private val validator: GCodeValidator = GCodeValidator()
) {
    fun generate(input: GCodeValidationInput, profile: MachineProfile): GCodeResult {
        val validation = validator.validate(input, profile)
        if (!validation.isValid) return GCodeResult(errors = validation.errors, warnings = validation.warnings)

        val post = PostProcessorFactory.forProfile(profile)
        val p = input.params
        val operation = when {
            p.cycleType.contains("FACING", true) -> post.facing(p)
            p.cycleType.contains("G71", true) || p.cycleType.contains("CYCLE95", true) -> post.roughTurning(p)
            p.cycleType.contains("G76", true) || p.cycleType.contains("CYCLE97", true) -> post.threading(p)
            p.cycleType.contains("G75", true) -> post.grooving(p)
            p.cycleType.contains("G83", true) || p.cycleType.contains("CYCLE83", true) -> post.drilling(p)
            else -> return GCodeResult(errors = listOf("Неизвестная операция: ${p.cycleType}"), warnings = validation.warnings)
        }
        val program = buildString {
            appendLine("%")
            appendLine("; SAFE PROGRAM · ${profile.displayName}")
            appendLine(post.header(profile, input.toolNumber))
            appendLine(operation)
            appendLine("G00 X100.000 Z100.000 ; БЕЗОПАСНЫЙ ОТВОД")
            appendLine(post.footer(profile))
            appendLine("%")
        }
        return GCodeResult(program = program, warnings = validation.warnings)
    }
}
