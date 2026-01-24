package com.calmed.calmedfrontendtourettes.viewmodel

import com.calmed.calmedfrontendtourettes.http.IAppApi
import com.calmed.calmedfrontendtourettes.model.dto.request.SetIsOnboardedDto
import com.calmed.calmedfrontendtourettes.model.dto.request.UserInfoTourettesUpdateDto
import com.calmed.calmedfrontendtourettes.model.joined.UserInfoTourettesJoined
import com.calmed.calmedfrontendtourettes.model.joined.UserJoined
import com.calmed.calmedfrontendtourettes.model.toEntity
import com.calmed.calmedfrontendtourettes.model.toJoined
import com.calmed.calmedfrontendtourettes.repository.IUserDao
import com.calmed.calmedfrontendtourettes.repository.IUserInfoTourettesDao
import com.calmed.calmedfrontendtourettes.service.specification.IAuthService
import com.calmed.calmedfrontendtourettes.store.ITokenDataStore
import com.calmed.calmedfrontendtourettes.util.jwtDecode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.jsonPrimitive

class SessionViewModel(
	private val api: IAppApi,
	private val tokenStore: ITokenDataStore,
	private val authService: IAuthService,
	private val userDao: IUserDao,
	private val userInfoDao: IUserInfoTourettesDao
) {
	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	private val _loading = MutableStateFlow(false)
	val loading: StateFlow<Boolean> = _loading

	private val _error = MutableStateFlow<String?>(null)
	val error: StateFlow<String?> = _error

	val user: StateFlow<UserJoined?> =
		userDao.findFirst().map { it?.toJoined() }.stateIn(scope, SharingStarted.Eagerly, null)

	val userInfo: StateFlow<UserInfoTourettesJoined?> = combine(
		userDao.findFirst(),
		userInfoDao.findFirst()
	) { uEntity, uiEntity ->
		if (uEntity == null || uiEntity == null) return@combine null
		if (uiEntity.userId != uEntity.id) return@combine null
		uiEntity.toJoined(uEntity.toJoined())
	}.stateIn(scope, SharingStarted.Eagerly, null)

	fun currentUserId(): String? {
		val access = tokenStore.tokenDto.value?.access ?: return null
		val payload = jwtDecode(access)
		return payload["sub"]?.jsonPrimitive?.content
	}

	private suspend fun clearLocal() {
		userInfoDao.clearAll()
		userDao.clearAll()
	}

	private suspend fun cacheUserDto(u: com.calmed.calmedfrontendtourettes.model.dto.response.UserDto) {
		userDao.clearAll()
		userDao.upsert(u.toEntity())
	}

	private suspend fun cacheUserInfoDto(ui: com.calmed.calmedfrontendtourettes.model.dto.response.UserInfoTourettesDto) {
		userInfoDao.clearAll()
		userInfoDao.upsert(ui.toEntity())
	}

	suspend fun loadSession() {
		println("LOAD SESSION CALLED");
		_error.value = null
		_loading.value = true
		try {
			val userId = currentUserId()
			println("USER ID = $userId")
			if (userId == null) {
				clearLocal()
				_error.value = "Missing user id."
				return
			}


			val remoteUser = api.getUser(userId)
			println("REMOTE USER = $remoteUser")
			if (remoteUser == null) {
				clearLocal()
				_error.value = "Failed to load user."
				return
			}
			cacheUserDto(remoteUser)

			val remoteInfo = api.getUserInfoTourettesByUserId(userId)
			if (remoteInfo != null) {
				cacheUserDto(remoteInfo.user)
				cacheUserInfoDto(remoteInfo)
			} else {
				userInfoDao.clearAll()
			}
		} catch (t: Throwable) {
			_error.value = t.message ?: "Failed to load session."
		} finally {
			_loading.value = false
			println("LOAD SESSION DONE, error=${_error.value}")
		}

	}

	suspend fun skipOnboarding(): Boolean {
		_error.value = null
		_loading.value = true
		return try {
			val currentUser = user.value
			if (currentUser == null) {
				_error.value = "Missing user."
				false
			} else {
				val updatedUser = api.setOnboarded(
					currentUser.id,
					SetIsOnboardedDto(isOnboarded = true)
				)
				if (updatedUser == null) {
					_error.value = "Failed to mark user as onboarded."
					false
				} else {
					cacheUserDto(updatedUser)
					true
				}
			}
		} catch (t: Throwable) {
			_error.value = t.message ?: "Skip onboarding failed."
			false
		} finally {
			_loading.value = false
		}
	}

	suspend fun completeOnboarding(update: UserInfoTourettesUpdateDto): Boolean {
		_error.value = null
		_loading.value = true
		return try {
			val currentUserInfo = userInfo.value
			val currentUser = user.value
			if (currentUserInfo == null || currentUser == null) {
				_error.value = "Missing user info."
				false
			} else {
				val updatedInfo = api.updateUserInfoTourettes(currentUserInfo.id, update)
				if (updatedInfo == null) {
					_error.value = "Failed to update user info."
					false
				} else {
					cacheUserDto(updatedInfo.user)
					cacheUserInfoDto(updatedInfo)

					val updatedUser = api.setOnboarded(
						currentUser.id,
						SetIsOnboardedDto(isOnboarded = true)
					)
					if (updatedUser == null) {
						_error.value = "Failed to mark user as onboarded."
						false
					} else {
						cacheUserDto(updatedUser)
						true
					}
				}
			}
		} catch (t: Throwable) {
			_error.value = t.message ?: "Onboarding failed."
			false
		} finally {
			_loading.value = false
		}
	}

	suspend fun logout() {
		_error.value = null
		_loading.value = true
		try {
			authService.logout()
		} finally {
			clearLocal()
			_loading.value = false
		}
	}
}