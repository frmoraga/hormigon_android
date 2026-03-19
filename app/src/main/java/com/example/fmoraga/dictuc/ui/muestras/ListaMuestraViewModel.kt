package com.example.fmoraga.dictuc.ui.muestras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fmoraga.dictuc.data.local.preferences.UserPreferences
import com.example.fmoraga.dictuc.domain.model.MuestraPlanificacion
import com.example.fmoraga.dictuc.domain.repository.DictucRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListaMuestraViewModel @Inject constructor(
    private val repository: DictucRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError

    val muestras: StateFlow<List<MuestraPlanificacion>> = userPreferences.idMuestreador
        .flatMapLatest { id ->
            if (id >= 0) repository.getMuestrasPlanificadas(id)
            else flowOf(emptyList())
        }
        .map { list -> list.sortedBy { estadoOrden(it.estado) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Pendientes primero, luego suspendidas, tomadas y retiradas al final. */
    private fun estadoOrden(estado: Int) = when (estado) {
        1 -> 0   // pendiente (servidor: 1)
        3 -> 1   // suspendida (servidor: 3, requiere atención)
        2 -> 2   // tomada (servidor: 2)
        4 -> 3   // retirada (servidor: 4)
        else -> 4
    }

    private val _jornadaTerminada = MutableStateFlow(false)
    val jornadaTerminada: StateFlow<Boolean> = _jornadaTerminada

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncError.value = null
            val idMuestreador = userPreferences.idMuestreador.first()
            repository.syncMuestrasFromServer()
                .onFailure { _syncError.value = "Sin conexión. Mostrando datos locales." }
            repository.syncPlanificacionFromServer(idMuestreador)
                .onFailure { _syncError.value = "Sin conexión. Mostrando datos locales." }
            _isLoading.value = false
        }
    }

    fun terminarJornada() {
        viewModelScope.launch {
            val run   = userPreferences.run.first()
            val fecha = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            repository.terminarJornada(run, fecha)
            _jornadaTerminada.value = true
        }
    }

    fun clearSyncError() { _syncError.value = null }
}
