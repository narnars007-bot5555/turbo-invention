package com.tokar.frez.cnc.ui

import androidx.lifecycle.ViewModel
import com.tokar.frez.cnc.domain.model.*
import com.tokar.frez.cnc.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MainUiState(
    val selectedModuleIndex: Int = 0,
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
    val selectedT: Int = 1
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

    fun selectModule(index: Int) {
        _uiState.value = _uiState.value.copy(selectedModuleIndex = index)
    }

    fun setSelectedT(t: Int) {
        _uiState.value = _uiState.value.copy(selectedT = t)
    }

    fun calculateTurning(d: Double, vc: Double?, n: Double?, f: Double, ap: Double, rEps: Double) {
        val res = turningMillingUseCase.calculateTurning(d, vc, n, f, ap, rEps)
        _uiState.value = _uiState.value.copy(turningResult = res)
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
        _uiState.value = _uiState.value.copy(toleranceResult = res)
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
