package com.calmed.calmedfrontendtourettes.store

import com.calmed.calmedfrontendtourettes.model.dto.TokenDto
import kotlinx.coroutines.flow.StateFlow

interface ITokenDataStore {
	val tokenDto: StateFlow<TokenDto?>
	suspend fun getToken(): TokenDto?
	suspend fun setToken(tokenDto: TokenDto)
	suspend fun clear()
}
