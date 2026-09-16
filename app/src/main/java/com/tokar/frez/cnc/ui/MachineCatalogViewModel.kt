package com.tokar.frez.cnc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokar.frez.cnc.data.entity.MachineEntity
import com.tokar.frez.cnc.data.repository.CncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MachineCatalogUiState(
    val machineClasses: List<String> = emptyList(),
    val selectedClass: String? = null,
    val cncSystems: List<String> = emptyList(),
    val selectedCncSystem: String? = null,
    val machines: List<MachineEntity> = emptyList(),
    val selectedMachine: MachineEntity? = null,
    val selectedTab: Int = 0 // 0: Setup Guide, 1: G-Code & CNC System Handbook
)

class MachineCatalogViewModel(private val repository: CncRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MachineCatalogUiState())
    val uiState: StateFlow<MachineCatalogUiState> = _uiState.asStateFlow()

    init {
        loadMachineClasses()
    }

    private fun loadMachineClasses() {
        viewModelScope.launch {
            repository.getMachineClasses().collect { classes ->
                _uiState.update { it.copy(machineClasses = classes) }
            }
        }
    }

    fun selectClass(cls: String) {
        _uiState.update { it.copy(selectedClass = cls, selectedCncSystem = null, selectedMachine = null) }
        viewModelScope.launch {
            repository.getCncSystemsForClass(cls).collect { systems ->
                _uiState.update { it.copy(cncSystems = systems) }
            }
        }
    }

    fun selectCncSystem(system: String) {
        val cls = _uiState.value.selectedClass ?: return
        _uiState.update { it.copy(selectedCncSystem = system, selectedMachine = null) }
        viewModelScope.launch {
            repository.getMachinesByClassAndCnc(cls, system).collect { machines ->
                _uiState.update { it.copy(machines = machines) }
            }
        }
    }

    fun selectMachine(machine: MachineEntity) {
        _uiState.update { it.copy(selectedMachine = machine) }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }
}
