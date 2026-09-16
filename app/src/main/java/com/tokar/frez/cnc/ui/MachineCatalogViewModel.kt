package com.tokar.frez.cnc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokar.frez.cnc.data.entity.MachineEntity
import com.tokar.frez.cnc.data.repository.CncRepository
import com.tokar.frez.cnc.domain.usecase.DetailedMachineInfo
import com.tokar.frez.cnc.domain.usecase.GetMachineDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MachineCatalogUiState(
    val machineClasses: List<String> = emptyList(),
    val selectedClass: String? = null,
    val cncSystems: List<String> = emptyList(),
    val selectedCncSystem: String? = null,
    val machines: List<MachineEntity> = emptyList(),
    val selectedMachine: MachineEntity? = null,
    val selectedTab: Int = 0
)

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class ClassSelection(val machineClasses: List<String>) : CatalogUiState
    data class SystemSelection(val selectedClass: String, val cncSystems: List<String>) : CatalogUiState
    data class ModelSelection(val selectedClass: String, val selectedCncSystem: String, val machines: List<MachineEntity>) : CatalogUiState
    data class MachineDetail(val details: DetailedMachineInfo, val selectedTab: Int = 0) : CatalogUiState
}

class MachineCatalogViewModel(
    private val repository: CncRepository,
    private val detailsUseCase: GetMachineDetailsUseCase = GetMachineDetailsUseCase()
) : ViewModel() {

    private val _state = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow(MachineCatalogUiState())
    val uiState: StateFlow<MachineCatalogUiState> = _uiState.asStateFlow()

    init {
        loadMachineClasses()
    }

    fun loadMachineClasses() {
        viewModelScope.launch {
            repository.getMachineClasses().collect { classes ->
                val list = classes.ifEmpty {
                    listOf(
                        "Вертикально-фрезерные (VMC)",
                        "Горизонтально-фрезерные (HMC)",
                        "5-осевые ОЦ (5-Axis MC)",
                        "Токарные и токарно-фрезерные",
                        "Автоматы продольного точения (Swiss)",
                        "Зубообрабатывающие (Gear)",
                        "Шлифовальная группа (Grinding)"
                    )
                }
                _state.value = CatalogUiState.ClassSelection(list)
                _uiState.value = _uiState.value.copy(machineClasses = list)
            }
        }
    }

    fun selectClass(cls: String) {
        _uiState.value = _uiState.value.copy(selectedClass = cls, selectedCncSystem = null, selectedMachine = null)
        viewModelScope.launch {
            repository.getCncSystemsForClass(cls).collect { systems ->
                val list = systems.ifEmpty { listOf("FANUC", "SINUMERIK", "HAAS", "HEIDENHAIN") }
                _state.value = CatalogUiState.SystemSelection(cls, list)
                _uiState.value = _uiState.value.copy(cncSystems = list)
            }
        }
    }

    fun selectCncSystem(system: String) {
        val currentCls = _uiState.value.selectedClass ?: "5-Axis MC"
        _uiState.value = _uiState.value.copy(selectedCncSystem = system, selectedMachine = null)
        viewModelScope.launch {
            repository.getMachinesByClassAndCnc(currentCls, system).collect { machines ->
                _state.value = CatalogUiState.ModelSelection(currentCls, system, machines)
                _uiState.value = _uiState.value.copy(machines = machines)
            }
        }
    }

    fun selectMachine(machine: MachineEntity) {
        val details = detailsUseCase.parseMachineDetails(machine)
        _state.value = CatalogUiState.MachineDetail(details)
        _uiState.value = _uiState.value.copy(selectedMachine = machine)
    }

    fun selectTab(index: Int) {
        val currentState = _state.value
        if (currentState is CatalogUiState.MachineDetail) {
            _state.value = currentState.copy(selectedTab = index)
        }
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }
}
