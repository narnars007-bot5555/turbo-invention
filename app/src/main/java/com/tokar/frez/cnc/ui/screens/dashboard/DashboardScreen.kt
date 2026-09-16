package com.tokar.frez.cnc.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokar.frez.cnc.ui.theme.*

@Composable
fun DashboardScreen(
    onSelectModule: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val modules = listOf(
        "1. Токарно-фрезерные расчеты" to "Vc, n, f, fz, MRR, Pc, Ra/Rz, Конусы",
        "2. Шлифование и абразив" to "Vs, Vw, ae, Декодер кругов, Правка Ud/Ed",
        "3. Зубообработка" to "Эвольвента m/z, Длина нормали W, Зубодолбление",
        "4. ISO Стандарты и Допуски" to "ISO 286-1 валы/отверстия, Резьбы ISO 965, Пластины ISO 1832",
        "5. Каталог Станков и СТОЕК ЧПУ" to "VMC/HMC, 5 осей, Haas, Fanuc, Sinumerik, Heidenhain",
        "6. Компенсация износа резца" to "Расчет дельты корректоров X, Z, Y и подсказки ввода",
        "7. G-код генератор & Backplot" to "G71, G76, G83, CYCLE95/97/83, отрисовка Compose Canvas"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "TOKAR-FREZ-CNC",
            color = IndustrialCyan,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Инженерно-технологический комплекс машиностроителя",
            color = TextSecondary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        modules.forEachIndexed { index, (title, subtitle) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSelectModule(index + 1) },
                colors = CardDefaults.cardColors(containerColor = IndustrialCardBg),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        color = IndustrialYellow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
