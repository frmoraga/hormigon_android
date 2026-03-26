package com.example.fmoraga.dictuc.ui.muestras

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fmoraga.dictuc.data.local.preferences.UserPreferences
import com.example.fmoraga.dictuc.data.remote.sync.SyncWorker
import com.example.fmoraga.dictuc.domain.model.Muestra
import com.example.fmoraga.dictuc.domain.model.MuestraTomada
import com.example.fmoraga.dictuc.domain.model.ProbetaExtra
import com.example.fmoraga.dictuc.domain.repository.DictucRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TomaMuestraState {
    object Idle : TomaMuestraState()
    object Saving : TomaMuestraState()
    object Saved : TomaMuestraState()
    data class Error(val message: String) : TomaMuestraState()
}

@HiltViewModel
class TomaMuestraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DictucRepository,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val idMuestra: Long = checkNotNull(savedStateHandle["idMuestra"])

    val muestra: StateFlow<Muestra?> = flow {
        emit(repository.getMuestra(idMuestra))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Fotos y firmas (URIs temporales gestionados por el Fragment)
    var photoUri1: Uri? = null
    var photoUri2: Uri? = null
    var photoUri3: Uri? = null
    var photoUri4: Uri? = null
    var photoPath1: String = ""
    var photoPath2: String = ""
    var photoPath3: String = ""
    var photoPath4: String = ""
    var signaturePath1: String = ""
    var signaturePath2: String = ""

    private val _state = MutableStateFlow<TomaMuestraState>(TomaMuestraState.Idle)
    val state: StateFlow<TomaMuestraState> = _state

    fun saveMuestra(mt: MuestraTomada) {
        viewModelScope.launch {
            _state.value = TomaMuestraState.Saving
            val idMuestreador = userPreferences.idMuestreador.first()
            runCatching { repository.saveMuestraTomada(mt.copy(runMuestreador = idMuestreador)) }
                .onSuccess {
                    triggerImmediateSync()
                    _state.value = TomaMuestraState.Saved
                }
                .onFailure { _state.value = TomaMuestraState.Error(it.message ?: "Error al guardar") }
        }
    }

    private fun triggerImmediateSync() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            "${SyncWorker.WORK_NAME}_immediate",
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<SyncWorker>().build()
        )
    }

    fun agregarProbetaExtra(cantidad: Int, observacion: String) {
        viewModelScope.launch {
            val idMuestreador = userPreferences.idMuestreador.first()
            repository.saveProbetaExtra(
                ProbetaExtra(
                    idMuestra = idMuestra,
                    runMuestreador = idMuestreador,
                    cantidad = cantidad,
                    observacion = observacion
                )
            )
        }
    }

    fun suspenderMuestra(
        motivo: String,
        esViajePerdido: Boolean = false,
        numBoleta: String = "",
        horaLlegada: String = "",
        horaSalidaObra: String = "",
        numPatente: String = ""
    ) {
        viewModelScope.launch {
            val idMuestreador = userPreferences.idMuestreador.first()
            val fecha = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
            repository.saveMuestraSuspendida(
                com.example.fmoraga.dictuc.domain.model.MuestraSuspendida(
                    idMuestra = idMuestra,
                    runMuestreador = idMuestreador,
                    fechaSuspension = fecha,
                    motivo = motivo,
                    idEstado = if (esViajePerdido) 3 else 2,
                    numBoleta = numBoleta,
                    horaLlegada = horaLlegada,
                    horaSalidaObra = horaSalidaObra,
                    numPatente = numPatente
                )
            )
            _state.value = TomaMuestraState.Saved
        }
    }

    fun resetState() { _state.value = TomaMuestraState.Idle }
}
