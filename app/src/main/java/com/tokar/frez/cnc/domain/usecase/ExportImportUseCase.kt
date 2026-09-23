package com.tokar.frez.cnc.domain.usecase

import com.tokar.frez.cnc.data.entity.CalculationHistoryEntity
import com.tokar.frez.cnc.data.entity.ToolWearJournalEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TechnologicalRouteSheet(
    val partName: String,
    val operationName: String,
    val materialName: String,
    val machineName: String,
    val toolName: String,
    val vcMmin: Double,
    val rpm: Int,
    val feedMmRev: Double,
    val apMm: Double,
    val cuttingForceN: Double,
    val powerKw: Double,
    val warnings: List<String>,
    val technologistComment: String,
    val formattedTimestamp: String = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
)

class ExportImportUseCase {

    fun exportHistoryToCsv(historyList: List<CalculationHistoryEntity>): String {
        val sb = StringBuilder()
        sb.append("ID;Дата;Операция;Категория;Материал;Станок;Инструмент;Параметры;Результат;Комментарий\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        for (item in historyList) {
            val dateStr = dateFormat.format(Date(item.timestamp))
            val cleanParams = item.inputParamsJson.replace("\n", " ").replace(";", ",")
            val cleanRes = item.resultSummaryJson.replace("\n", " ").replace(";", ",")
            val cleanComment = item.technologistComment.replace("\n", " ").replace(";", ",")

            sb.append("${item.id};$dateStr;${item.operationName};${item.category};${item.materialName};${item.machineName};${item.toolName};$cleanParams;$cleanRes;$cleanComment\n")
        }
        return sb.toString()
    }

    fun exportToolWearJournalToCsv(wearList: List<ToolWearJournalEntity>): String {
        val sb = StringBuilder()
        sb.append("ID;Инструмент;Количество_деталей;Время_работы_мин;Износ_X_мм;Износ_Z_мм;Износ_Y_мм;Остаточный_ресурс_%;Причина;Дата\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        for (item in wearList) {
            val dateStr = dateFormat.format(Date(item.lastMeasuredTimestamp))
            sb.append("${item.id};${item.toolId};${item.partCount};${item.operatingTimeMin};${item.measuredWearXMm};${item.measuredWearZMm};${item.measuredWearYMm};${item.remainingLifePercent};${item.cause};$dateStr\n")
        }
        return sb.toString()
    }

    fun generateTechnologicalRouteSheetText(sheet: TechnologicalRouteSheet): String {
        val line = "=========================================================================="
        return """
            $line
                           ТЕХНОЛОГИЧЕСКАЯ КАРТА ОБРАБОТКИ
            $line
            Дата/Время:           ${sheet.formattedTimestamp}
            Деталь:               ${sheet.partName}
            Операция:             ${sheet.operationName}
            Обрабатываемый материал: ${sheet.materialName}
            Оборудование (Станок): ${sheet.machineName}
            Инструмент / Оснастка: ${sheet.toolName}
            $line
                               РЕЖИМЫ РЕЗАНИЯ
            $line
            Скорость резания Vc:   ${sheet.vcMmin} м/мин
            Частота вращения n:   ${sheet.rpm} об/мин
            Подача f:              ${sheet.feedMmRev} мм/об
            Глубина резания ap:    ${sheet.apMm} мм
            $line
                          ИНЖЕНЕРНЫЕ РАСЧЕТЫ И СИЛЫ
            $line
            Главная сила резания Fc: ${sheet.cuttingForceN} Н
            Потребляемая мощность Pc: ${sheet.powerKw} кВт
            $line
                          ТРЕБОВАНИЯ ТЕХНИКИ БЕЗОПАСНОСТИ
            $line
            ${if (sheet.warnings.isNotEmpty()) sheet.warnings.joinToString("\n• ") { it } else "• Параметры находятся в пределах допуска."}

            Комментарий технолога:
            ${sheet.technologistComment.ifBlank { "Без комментариев." }}
            $line
            Подпись главного технолога: ____________________ / ( Ф.И.О. )
            $line
        """.trimIndent()
    }
}
