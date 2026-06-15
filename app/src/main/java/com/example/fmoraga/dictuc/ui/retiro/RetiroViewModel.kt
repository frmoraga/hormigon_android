package com.example.fmoraga.dictuc.ui.retiro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fmoraga.dictuc.data.local.preferences.UserPreferences
import com.example.fmoraga.dictuc.domain.model.MuestraRetirada
import com.example.fmoraga.dictuc.domain.model.RetiroMuestra
import com.example.fmoraga.dictuc.domain.repository.DictucRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RetiroViewModel @Inject constructor(
    private val repository: DictucRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val retiros: StateFlow<List<RetiroMuestra>> = repository.getRetirosTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _retirado = MutableStateFlow(false)
    val retirado: StateFlow<Boolean> = _retirado

    private val _yaRetirada = MutableStateFlow(false)
    val yaRetirada: StateFlow<Boolean> = _yaRetirada

    fun checkYaRetirada(idMuestra: Long) {
        viewModelScope.launch {
            _yaRetirada.value = repository.getMuestraRetirada(idMuestra) != null
        }
    }

    fun retirarMuestra(idMuestra: Long, pathFoto: String, observacion: String) {
        viewModelScope.launch {
            val idMuestreador = userPreferences.idMuestreador.first()
            val fecha = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
            repository.saveMuestraRetirada(
                MuestraRetirada(
                    idMuestra = idMuestra,
                    runMuestreador = idMuestreador,
                    fechaRetiro = fecha,
                    pathFoto = pathFoto,
                    observacion = observacion
                )
            )
            _retirado.value = true
        }
    }

    fun resetRetiro() { _retirado.value = false }
}
