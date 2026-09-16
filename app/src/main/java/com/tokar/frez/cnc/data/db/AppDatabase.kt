package com.tokar.frez.cnc.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tokar.frez.cnc.data.dao.*
import com.tokar.frez.cnc.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MaterialEntity::class,
        IsoToleranceEntity::class,
        ThreadEntity::class,
        CncCycleEntity::class,
        ToolFixtureEntity::class,
        MachineEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun materialDao(): MaterialDao
    abstract fun isoToleranceDao(): IsoToleranceDao
    abstract fun threadDao(): ThreadDao
    abstract fun cncCycleDao(): CncCycleDao
    abstract fun toolFixtureDao(): ToolFixtureDao
    abstract fun machineDao(): MachineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tokar_frez_cnc_db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }

            private suspend fun populateDatabase(db: AppDatabase) {
                db.materialDao().insertAll(
                    listOf(
                        MaterialEntity(name = "Сталь 20 / 45 (Unalloyed Steel)", isoGroup = "P", tensileStrength = 600.0, hardnessHb = 180.0, vcCoeff = 1.0, fzCoeff = 1.0),
                        MaterialEntity(name = "40Х / 30ХГСА (Alloy Steel)", isoGroup = "P", tensileStrength = 850.0, hardnessHb = 250.0, vcCoeff = 0.8, fzCoeff = 0.9),
                        MaterialEntity(name = "12Х18Н10Т / 08Х18Н10 (Stainless Steel)", isoGroup = "M", tensileStrength = 650.0, hardnessHb = 200.0, vcCoeff = 0.6, fzCoeff = 0.8),
                        MaterialEntity(name = "СЧ20 / ВЧ50 (Cast Iron)", isoGroup = "K", tensileStrength = 300.0, hardnessHb = 220.0, vcCoeff = 1.1, fzCoeff = 1.1),
                        MaterialEntity(name = "Д16Т / АМг6 (Aluminum Alloy)", isoGroup = "N", tensileStrength = 450.0, hardnessHb = 120.0, vcCoeff = 3.0, fzCoeff = 1.5),
                        MaterialEntity(name = "ВТ6 / ВТ20 (Titanium Alloy)", isoGroup = "S", tensileStrength = 950.0, hardnessHb = 320.0, vcCoeff = 0.35, fzCoeff = 0.7),
                        MaterialEntity(name = "Сталь ШХ15 (Hardened Steel 60 HRC)", isoGroup = "H", tensileStrength = 2000.0, hardnessHb = 600.0, vcCoeff = 0.4, fzCoeff = 0.6)
                    )
                )

                db.threadDao().insertAll(
                    listOf(
                        ThreadEntity(standard = "ISO 965", designation = "M6x1", nominalDiameter = 6.0, pitch = 1.0, tapDrillDia = 5.0),
                        ThreadEntity(standard = "ISO 965", designation = "M8x1.25", nominalDiameter = 8.0, pitch = 1.25, tapDrillDia = 6.8),
                        ThreadEntity(standard = "ISO 965", designation = "M10x1.5", nominalDiameter = 10.0, pitch = 1.5, tapDrillDia = 8.5),
                        ThreadEntity(standard = "ISO 965", designation = "M12x1.75", nominalDiameter = 12.0, pitch = 1.75, tapDrillDia = 10.25),
                        ThreadEntity(standard = "ISO 965", designation = "M16x2.0", nominalDiameter = 16.0, pitch = 2.0, tapDrillDia = 14.0),
                        ThreadEntity(standard = "ISO 965", designation = "M20x2.5", nominalDiameter = 20.0, pitch = 2.5, tapDrillDia = 17.5),
                        ThreadEntity(standard = "G Pipe", designation = "G1/4", nominalDiameter = 13.157, pitch = 1.337, tapDrillDia = 11.8),
                        ThreadEntity(standard = "G Pipe", designation = "G1/2", nominalDiameter = 20.955, pitch = 1.814, tapDrillDia = 19.0)
                    )
                )

                db.cncCycleDao().insertAll(
                    listOf(
                        CncCycleEntity(
                            functionType = "Черновое точение (Rough Turning)",
                            fanucCode = "G71 U.. R.. / G71 P.. Q.. U.. W.. F..",
                            sinumerikCode = "CYCLE95(NPP, MID, FALZ, FALX, FAL, VARI, DT, DAM, _VRT)",
                            haasCode = "G71 P.. Q.. U.. W.. D.. F..",
                            heidenhainCode = "CYCL DEF 280 / CONTOUR TURNING",
                            description = "Продольный черновытачивающий цикл снятия припуска по контуру"
                        ),
                        CncCycleEntity(
                            functionType = "Нарезание резьбы (Threading)",
                            fanucCode = "G76 P.. Q.. R.. / G76 X.. Z.. P.. Q.. F..",
                            sinumerikCode = "CYCLE97(PIT, MPIT, SPL, FPL, DM1, DM2, APP, ROP, TPI, VARI, NUMT)",
                            haasCode = "G76 P.. Q.. R.. / G76 X.. Z.. I.. K.. D.. F..",
                            heidenhainCode = "CYCL DEF 260 / THREAD CUTTING",
                            description = "Многопроходный цикл нарезания наружной и внутренней резьбы"
                        ),
                        CncCycleEntity(
                            functionType = "Сверление глубоких отверстий (Peck Drilling)",
                            fanucCode = "G83 X.. Y.. Z.. Q.. R.. F..",
                            sinumerikCode = "CYCLE83(RTP, RFP, SDIS, DP, DPR, FDEP, FDPR, DAM, DT, FFR, VARI)",
                            haasCode = "G83 X.. Y.. Z.. Q.. R.. F..",
                            heidenhainCode = "CYCL DEF 200 DRILLING / CYCL DEF 205 DEEP HOLE DRILLING",
                            description = "Цикл сверления с периодическим выводом сверла для стружколомания"
                        ),
                        CncCycleEntity(
                            functionType = "Фрезерование прямоугольного кармана (Pocket Milling)",
                            fanucCode = "G12 / G13 или макрос G68/G65",
                            sinumerikCode = "POCKET3(RTP, RFP, SDIS, DP, DPR, LENG, WIDT, CRAD, CDIR, FAL, VARI)",
                            haasCode = "G12 / G13 Circular Pocket / G150 General Pocket",
                            heidenhainCode = "CYCL DEF 251 RECTANGULAR POCKET",
                            description = "Выборка прямоугольного или круглого кармана с автоматической зачисткой дна и стенок"
                        )
                    )
                )

                db.toolFixtureDao().insertAll(
                    listOf(
                        ToolFixtureEntity(
                            category = "Токарный инструмент",
                            name = "Резец PCLNR 2525M12",
                            standardCode = "ISO 1832",
                            specifications = "Державка 25х25 мм для пластин CNMG 120408, угол в плане 95°"
                        ),
                        ToolFixtureEntity(
                            category = "Фрезерная оснастка",
                            name = "Патрон цанговый BT40-ER32-100",
                            standardCode = "MAS 403 BT40",
                            specifications = "Оправка BT40, биение < 0.005 мм, диаметр цанг ER32 (2-20 мм)"
                        ),
                        ToolFixtureEntity(
                            category = "Измерительный прибор",
                            name = "Микрометр гладкий МК 25-50 0.01",
                            standardCode = "ГОСТ 6507-90",
                            specifications = "Диапазон измерений 25-50 мм, цена деления 0.01 мм, трещотка"
                        ),
                        ToolFixtureEntity(
                            category = "Калибры",
                            name = "Резьбовой калибр-пробка М12х1.75 6H ПР/НЕ",
                            standardCode = "ГОСТ 17756-72",
                            specifications = "Контроль среднего и внутреннего диаметра метрической резьбы 6H"
                        )
                    )
                )
            }
        }
    }
}
