package com.calmed.calmedtics.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.calmed.calmedtics.model.dto.TokenDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TokenDataStore(private val dataStore: DataStore<Preferences>) : ITokenDataStore {
    private val keyAccess = stringPreferencesKey("token_access")
    private val keyRefresh = stringPreferencesKey("token_refresh")
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val tokenDtoFlow: Flow<TokenDto?> = dataStore.data.map { prefs ->
        val access = prefs[keyAccess]
        val refresh = prefs[keyRefresh]
        if (access != null && refresh != null) {
            TokenDto(access = access, refresh = refresh)
        } else {
            null
        }
    }
    
    override val tokenDto = tokenDtoFlow.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    override suspend fun getToken(): TokenDto? {
        return tokenDtoFlow.first()
    }

    override suspend fun setToken(tokenDto: TokenDto) {
        dataStore.edit { prefs ->
            val access = tokenDto.access
            if (access != null && access.isNotBlank()) {
                prefs[keyAccess] = access
            } else {
                prefs.remove(keyAccess)
            }
            val refresh = tokenDto.refresh
            if (refresh != null && refresh.isNotBlank()) {
                prefs[keyRefresh] = refresh
            } else {
                prefs.remove(keyRefresh)
            }
        }
    }

    override suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(keyAccess)
            prefs.remove(keyRefresh)
        }
    }
}
