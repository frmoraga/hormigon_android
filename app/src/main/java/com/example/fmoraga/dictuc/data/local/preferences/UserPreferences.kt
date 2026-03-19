package com.example.fmoraga.dictuc.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ID_MUESTREADOR = longPreferencesKey("id_muestreador")
        val RUN            = longPreferencesKey("run")
        val ID_PERFIL      = longPreferencesKey("id_perfil")
        val LOGGED_IN      = booleanPreferencesKey("logged_in")
    }

    val idMuestreador: Flow<Long> = context.dataStore.data.map { it[Keys.ID_MUESTREADOR] ?: -1L }
    val run: Flow<Long>           = context.dataStore.data.map { it[Keys.RUN] ?: -1L }
    val idPerfil: Flow<Long>      = context.dataStore.data.map { it[Keys.ID_PERFIL] ?: 1L }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[Keys.LOGGED_IN] ?: false }

    suspend fun saveSession(idMuestreador: Long, run: Long, idPerfil: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ID_MUESTREADOR] = idMuestreador
            prefs[Keys.RUN]            = run
            prefs[Keys.ID_PERFIL]      = idPerfil
            prefs[Keys.LOGGED_IN]      = true
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
