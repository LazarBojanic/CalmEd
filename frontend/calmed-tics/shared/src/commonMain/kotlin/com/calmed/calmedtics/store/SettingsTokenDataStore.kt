package com.calmed.calmedtics.store

import com.calmed.calmedtics.model.dto.TokenDto
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TokenStoreSettings(val settings: Settings)

class SettingsTokenDataStore(
    private val tokenSettings: TokenStoreSettings
) : ITokenDataStore {

    private val settings: Settings get() = tokenSettings.settings

    private val _tokenDto = MutableStateFlow(read())
    override val tokenDto: StateFlow<TokenDto?> = _tokenDto.asStateFlow()

    override suspend fun getToken(): TokenDto? = _tokenDto.value

    override suspend fun setToken(tokenDto: TokenDto) {
        val access = tokenDto.access
        val refresh = tokenDto.refresh
        if (!access.isNullOrBlank()) {
            settings.putString(KEY_ACCESS, access)
        } else {
            settings.remove(KEY_ACCESS)
        }
        if (!refresh.isNullOrBlank()) {
            settings.putString(KEY_REFRESH, refresh)
        } else {
            settings.remove(KEY_REFRESH)
        }
        _tokenDto.value = read()
    }

    override suspend fun clear() {
        settings.remove(KEY_ACCESS)
        settings.remove(KEY_REFRESH)
        _tokenDto.value = null
    }

    private fun read(): TokenDto? {
        val access = settings.getStringOrNull(KEY_ACCESS)
        val refresh = settings.getStringOrNull(KEY_REFRESH)
        return if (access != null && refresh != null) {
            TokenDto(access = access, refresh = refresh)
        } else {
            null
        }
    }

    private companion object {
        const val KEY_ACCESS = "token_access"
        const val KEY_REFRESH = "token_refresh"
    }
}
