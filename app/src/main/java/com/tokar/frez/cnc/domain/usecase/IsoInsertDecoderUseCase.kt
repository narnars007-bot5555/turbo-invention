package com.tokar.frez.cnc.domain.usecase

import com.tokar.frez.cnc.domain.model.InsertCodeDecoded

class IsoInsertDecoderUseCase {

    fun decodeInsert(isoCode: String): InsertCodeDecoded {
        val clean = isoCode.trim().uppercase().replace(" ", "")

        val shapeChar = clean.getOrNull(0) ?: 'C'
        val clearanceChar = clean.getOrNull(1) ?: 'N'
        val tolChar = clean.getOrNull(2) ?: 'M'
        val featureChar = clean.getOrNull(3) ?: 'G'

        val numbersOnly = clean.drop(4).filter { it.isDigit() }
        val sizeDigits = numbersOnly.take(2)
        val thickDigits = numbersOnly.drop(2).take(2)
        val radiusDigits = numbersOnly.drop(4).take(2)

        val shape = when (shapeChar) {
            'C' -> "C - Ромб 80° (Diamond 80°)"
            'D' -> "D - Ромб 55° (Diamond 55°)"
            'S' -> "S - Квадрат 90° (Square 90°)"
            'T' -> "T - Треугольник 60° (Triangle 60°)"
            'V' -> "V - Ромб 35° (Diamond 35°)"
            'W' -> "W - Ломаный треугольник 80° (Trigon)"
            'R' -> "R - Круглая пластина (Round)"
            'H' -> "H - Шестигранник (Hexagon)"
            else -> "$shapeChar - Специальная форма"
        }

        val clearance = when (clearanceChar) {
            'N' -> "N - 0° (Задний угол отсутствует, двусторонняя)"
            'C' -> "C - 7° (Позитивная пластина)"
            'P' -> "P - 11° (Позитивная пластина)"
            'B' -> "B - 5°"
            'D' -> "D - 15°"
            else -> "$clearanceChar - Задний угол"
        }

        val tol = when (tolChar) {
            'M' -> "M - Средняя точность (Прессованная с стружколомом)"
            'G' -> "G - Высокая точность (Шлифованная периферия)"
            'E' -> "E - Прецизионная точность"
            'U' -> "U - Черновая точность"
            else -> "$tolChar - Класс допуск"
        }

        val feature = when (featureChar) {
            'G' -> "G - Отверстие и стружколомы с двух сторон"
            'M' -> "M - Отверстие и стружколом с одной стороны"
            'W' -> "W - Без отверстия, без стружколома"
            'T' -> "T - Отверстие с конусом 40-60°"
            else -> "$featureChar - Исполнение отверстия/стружколома"
        }

        val radiusVal = radiusDigits.toIntOrNull()?.let { it * 0.1 } ?: 0.8
        val thickVal = thickDigits.toIntOrNull()?.let { it * 0.1 } ?: 4.76

        return InsertCodeDecoded(
            code = clean,
            shape = shape,
            clearanceAngle = clearance,
            toleranceClass = tol,
            typeAndFeatures = feature,
            sizeMm = if (sizeDigits.isNotEmpty()) sizeDigits else "12",
            thicknessMm = "$thickVal мм",
            cornerRadiusMm = "$radiusVal мм (r_eps)"
        )
    }
}
