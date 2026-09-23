package com.tokar.frez.cnc.ui

import androidx.lifecycle.ViewModel
import com.tokar.frez.cnc.data.entity.MaterialEntity
import com.tokar.frez.cnc.domain.model.*
import com.tokar.frez.cnc.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppThemeMode {
    DARK, LIGHT, SYSTEM
}

data class SearchResultItem(
    val title: String,
    val description: String,
    val categoryIndex: Int,
    val subModuleIndex: Int
)

data class RecentCalculationItem(
    val title: String,
    val category: String,
    val timestamp: String,
    val summary: String
)

data class MainUiState(
    val selectedCategory: Int = 0, // 0: Расчёты, 1: G-код и ЧПУ, 2: Инструмент и износ, 3: ISO, 4: Станки
    val selectedModuleIndex: Int = 0,
    val globalSearchQuery: String = "",
    val searchResults: List<SearchResultItem> = emptyList(),
    val favorites: Set<String> = setOf("Токарно-фрезерные расчеты", "ISO 286-1 Допуски", "Черновое G71/CYCLE95"),
    val recentCalculations: List<RecentCalculationItem> = listOf(
        RecentCalculationItem("Точение вала D50", "Расчёты", "Только что", "Vc=200 м/мин, n=1273 об/мин, Ra=1.6 мкм"),
        RecentCalculationItem("Допуск 40 H7", "ISO Допуски", "5 мин назад", "ES=+25 мкм, EI=0 мкм, 40.000..40.025 мм")
    ),
    val appTheme: AppThemeMode = AppThemeMode.DARK,
    val decimalPrecision: Int = 2,
    val isMetric: Boolean = true,
    val turningResult: TurningResult? = null,
    val millingResult: MillingResult? = null,
    val taperResult: TaperResult? = null,
    val grindingResult: GrindingResult? = null,
    val dressingResult: DressingResult? = null,
    val wheelDecoded: WheelMarkingDecoded? = null,
    val gearResult: GearResult? = null,
    val shapingResult: GearShapingResult? = null,
    val toleranceResult: IsoToleranceResult? = null,
    val threadResult: ThreadResult? = null,
    val insertDecoded: InsertCodeDecoded? = null,
    val timeNormativeResult: TimeNormativeResult? = null,
    val taylorResult: TaylorToolLifeResult? = null,
    val defectList: List<DefectInfo> = emptyList(),
    val selectedT: Int = 1,
    val recommendedRegime: CuttingRegimeRecommendation? = null,
    val selectedMaterial: MaterialEntity? = null
)

class MainViewModel : ViewModel() {

    private val turningMillingUseCase = TurningMillingUseCase()
    private val grindingUseCase = GrindingUseCase()
    private val gearCuttingUseCase = GearCuttingUseCase()
    private val isoToleranceUseCase = IsoToleranceUseCase()
    private val threadUseCase = ThreadUseCase()
    private val insertDecoderUseCase = IsoInsertDecoderUseCase()
    private val timeNormativeUseCase = TimeNormativeUseCase()
    private val diagnosticMatrixUseCase = DiagnosticMatrixUseCase()
    private val materialRecommendationUseCase = MaterialRecommendationUseCase()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        calculateTurning(50.0, 200.0, null, 0.2, 2.0, 0.8)
        calculateMilling(63.0, 5, 180.0, null, 0.1, 3.0, 40.0)
        calculateTaper(50.0, 40.0, 100.0, 200.0)
        calculateGrinding(400.0, 1500.0, 80.0, 100.0, 20.0, 5.0)
        calculateDressing(0.8, 0.2)
        decodeWheelMarking("25A F60 K 5 V")
        calculateGear(3.0, 30, 20.0, 0)
        calculateShaping(40.0, 20.0, 0.04, 0.2)
        calculateTolerance(40.0, "H7")
        calculateThread("M12x1.75")
        decodeInsert("CNMG120408")
        calculateTimeNormative(10.0, 2.0, 10, 30.0)
        calculateTaylor(150.0, 300.0, 0.25)
        searchDefects("")
    }

    fun selectCategory(catIndex: Int) {
        _uiState.update { it.copy(selectedCategory = catIndex) }
    }

    fun selectModule(index: Int) {
        _uiState.update { it.copy(selectedModuleIndex = index) }
    }

    fun toggleFavorite(itemTitle: String) {
        val current = _uiState.value.favorites.toMutableSet()
        if (current.contains(itemTitle)) {
            current.remove(itemTitle)
        } else {
            current.add(itemTitle)
        }
        _uiState.update { it.copy(favorites = current) }
    }

    fun setGlobalSearchQuery(query: String) {
        val searchItems = listOf(
            SearchResultItem("Токарно-фрезерные расчеты (Vc, n, f, MRR, Pc)", "Расчет режимов резания точения и фрезерования", 0, 1),
            SearchResultItem("Шлифование и абразивные круги", "Скорость круга Vs, Vw, расшифровка маркировки", 0, 2),
            SearchResultItem("Зубообработка и Эвольвента", "Расчет модуля m, числа зубьев z, нормали W", 0, 3),
            SearchResultItem("G-код и Backplotter Симулятор", "Генерация безопасного G-кода Fanuc, Haas, Sinumerik", 1, 7),
            SearchResultItem("Журнал износа инструмента T01-T99", "Учет износа X, Z, прогноз ресурса, замена пластин", 2, 6),
            SearchResultItem("Расшифровка пластин ISO 1832", "Расшифровка маркировки CNMG, WNMG, DNMG", 2, 4),
            SearchResultItem("ISO 286-1 Допуски и посадки", "Расчет квадрантов ES/EI, минимальных/максимальных зазоров", 3, 4),
            SearchResultItem("ISO 965 Метрические резьбы", "Параметры резьб M, G, диаметр сверла под резьбу", 3, 4),
            SearchResultItem("Матрица брака и дефектов", "Причины и устранение вибраций, сколов, шероховатости", 3, 7),
            SearchResultItem("Каталог станков и ЧПУ", "Инструкции по наладке, выходу в ноль, привязке WCS", 4, 5)
        )

        val filtered = if (query.isBlank()) emptyList() else {
            searchItems.filter {
                it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
            }
        }
        _uiState.update { it.copy(globalSearchQuery = query, searchResults = filtered) }
    }

    fun setAppTheme(theme: AppThemeMode) {
        _uiState.update { it.copy(appTheme = theme) }
    }

    fun setDecimalPrecision(precision: Int) {
        _uiState.update { it.copy(decimalPrecision = precision) }
    }

    fun setMetricUnits(isMetric: Boolean) {
        _uiState.update { it.copy(isMetric = isMetric) }
    }

    fun setSelectedT(t: Int) {
        _uiState.update { it.copy(selectedT = t) }
    }

    fun autoRecommendRegime(material: MaterialEntity?, diameter: Double, operation: String) {
        val rec = materialRecommendationUseCase.recommendCuttingRegime(
            material = material,
            diameterMm = diameter,
            operationType = operation
        )
        _uiState.update { it.copy(recommendedRegime = rec, selectedMaterial = material) }
    }

    fun calculateTurning(d: Double, vc: Double?, n: Double?, f: Double, ap: Double, rEps: Double) {
        val res = turningMillingUseCase.calculateTurning(d, vc, n, f, ap, rEps)
        val newRecent = RecentCalculationItem(
            title = "Точение D${d.toInt()}",
            category = "Расчёты",
            timestamp = "Только что",
            summary = "n=${res.n.toInt()} об/мин, MRR=${String.format("%.1f", res.mrr)} см³/мин, Ra=${String.format("%.2f", res.ra)} мкм"
        )
        val list = listOf(newRecent) + _uiState.value.recentCalculations.take(9)
        _uiState.update { it.copy(turningResult = res, recentCalculations = list) }
    }

    fun calculateMilling(d: Double, z: Int, vc: Double?, n: Double?, fz: Double, ap: Double, ae: Double) {
        val res = turningMillingUseCase.calculateMilling(d, z, vc, n, fz, ap, ae)
        _uiState.value = _uiState.value.copy(millingResult = res)
    }

    fun calculateTaper(D: Double, d: Double, L: Double, Ltot: Double) {
        val res = turningMillingUseCase.calculateTaper(TaperParams(D, d, L, Ltot))
        _uiState.value = _uiState.value.copy(taperResult = res)
    }

    fun calculateGrinding(Ds: Double, ns: Double, Dw: Double, nw: Double, aeUm: Double, fs: Double) {
        val res = grindingUseCase.calculateGrinding(GrindingParams(Ds, ns, Dw, nw, aeUm, fs))
        _uiState.value = _uiState.value.copy(grindingResult = res)
    }

    fun calculateDressing(bd: Double, fDres: Double) {
        val res = grindingUseCase.calculateDressing(DressingParams(bd, fDres))
        _uiState.value = _uiState.value.copy(dressingResult = res)
    }

    fun decodeWheelMarking(code: String) {
        val res = grindingUseCase.decodeWheelMarking(code)
        _uiState.value = _uiState.value.copy(wheelDecoded = res)
    }

    fun calculateGear(m: Double, z: Int, alpha: Double, k: Int) {
        val res = gearCuttingUseCase.calculateGearGeometry(GearParams(m, z, alpha, k))
        _uiState.value = _uiState.value.copy(gearResult = res)
    }

    fun calculateShaping(l: Double, vc: Double, feedRad: Double, feedCirc: Double) {
        val res = gearCuttingUseCase.calculateGearShaping(GearShapingParams(l, vc, feedRad, feedCirc))
        _uiState.value = _uiState.value.copy(shapingResult = res)
    }

    fun calculateTolerance(size: Double, field: String) {
        val res = isoToleranceUseCase.calculateTolerance(IsoToleranceParams(size, field))
        val newRecent = RecentCalculationItem(
            title = "Допуск $size $field",
            category = "ISO Допуски",
            timestamp = "Только что",
            summary = "ES=${res.esUpperUm} мкм, EI=${res.eiLowerUm} мкм, [${res.minLimitMm}..${res.maxLimitMm}] мм"
        )
        val list = listOf(newRecent) + _uiState.value.recentCalculations.take(9)
        _uiState.update { it.copy(toleranceResult = res, recentCalculations = list) }
    }

    fun calculateThread(code: String) {
        val parsed = threadUseCase.parseStandardThread(code)
        val res = threadUseCase.calculateThread(parsed)
        _uiState.value = _uiState.value.copy(threadResult = res)
    }

    fun decodeInsert(code: String) {
        val res = insertDecoderUseCase.decodeInsert(code)
        _uiState.value = _uiState.value.copy(insertDecoded = res)
    }

    fun calculateTimeNormative(To: Double, Tv: Double, N: Int, Tpz: Double) {
        val res = timeNormativeUseCase.calculateNormativeTime(TimeNormativeParams(To, Tv, N, Tpz))
        _uiState.value = _uiState.value.copy(timeNormativeResult = res)
    }

    fun calculateTaylor(Vc: Double, C: Double, nExp: Double) {
        val res = timeNormativeUseCase.calculateTaylorToolLife(TaylorToolLifeParams(Vc, C, nExp))
        _uiState.value = _uiState.value.copy(taylorResult = res)
    }

    fun searchDefects(q: String) {
        val res = diagnosticMatrixUseCase.searchDefects(q)
        _uiState.value = _uiState.value.copy(defectList = res)
    }
}
