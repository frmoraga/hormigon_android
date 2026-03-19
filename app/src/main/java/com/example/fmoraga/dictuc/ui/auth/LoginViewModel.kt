package com.example.fmoraga.dictuc.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fmoraga.dictuc.data.local.preferences.UserPreferences
import com.example.fmoraga.dictuc.domain.repository.DictucRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val idPerfil: Long) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: DictucRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun login(runStr: String, password: String) {
        if (runStr.isBlank()) {
            _state.value = LoginState.Error("Ingrese su RUN")
            return
        }
        if (password.isBlank()) {
            _state.value = LoginState.Error("Ingrese su contraseña")
            return
        }
        val run = runStr.toLongOrNull() ?: run {
            _state.value = LoginState.Error("RUN inválido")
            return
        }

        viewModelScope.launch {
            _state.value = LoginState.Loading
            repository.syncMuestreadoresFromServer()
            val muestreador = repository.getMuestreador(run, password)
            if (muestreador != null) {
                userPreferences.saveSession(
                    idMuestreador = muestreador.id,
                    run = muestreador.run,
                    idPerfil = muestreador.idPerfil
                )
                _state.value = LoginState.Success(muestreador.idPerfil)
            } else {
                _state.value = LoginState.Error("RUN o contraseña incorrectos")
            }
        }
    }

    fun resetState() { _state.value = LoginState.Idle }
}
