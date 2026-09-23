package com.tokar.frez.cnc.domain.usecase

data class GCodeValidationInput(
    val params: GCodeGenerationParams,
    val toolNumber: Int = 1,
    val spindleRpm: Int = 1000,
    val stockAllowancePerSideMm: Double = 0.5,
    val cuttingSpeedVc: Double = 200.0
)

data class GCodeValidationResult(
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    val isValid: Boolean get() = errors.isEmpty()
}

class GCodeValidator {
    fun validate(input: GCodeValidationInput, profile: MachineProfile): GCodeValidationResult {
        val p = input.params
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        if (!p.startDiameterMm.isFinite() || !p.endDiameterMm.isFinite() || p.startDiameterMm <= p.endDiameterMm) {
            errors += "Начальный диаметр Dнач должен быть больше конечного Dкон."
        }
        if (p.lengthMm <= 0.0) errors += "Длина обработки должна быть больше нуля."
        if (p.depthOfCutMm <= 0.0) errors += "Глубина ap должна быть больше нуля."
        if (p.depthOfCutMm > input.stockAllowancePerSideMm) {
            errors += "Глубина ap превышает припуск на сторону (${input.stockAllowancePerSideMm} мм)."
        }
        if (p.feedRate !in 0.001..2.0) errors += "Подача f должна быть в диапазоне 0.001..2.0 мм/об."
        if (input.cuttingSpeedVc <= 0.0 || input.cuttingSpeedVc > 2000.0) errors += "Скорость Vc должна быть в диапазоне 0..2000 м/мин."
        if (input.toolNumber !in 1..99) errors += "Номер инструмента должен быть в диапазоне 1..99."
        if (input.spindleRpm !in 1..profile.maxSpindleRpm) errors += "Обороты шпинделя превышают лимит профиля: ${profile.maxSpindleRpm} об/мин."
        if (p.pitchMm <= 0.0 && p.cycleType.contains("G76")) errors += "Шаг резьбы должен быть положительным."
        if (input.cuttingSpeedVc > 300.0) warnings += "Критически высокая скорость резания Vc > 300 м/мин. Проверьте материал и пластину."
        if (p.feedRate > 0.8) warnings += "Подача близка к верхней границе для стандартной токарной операции."
        return GCodeValidationResult(errors, warnings)
    }
}

data class GCodeResult(
    val program: String? = null,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    val isSuccess: Boolean get() = program != null && errors.isEmpty()
}
