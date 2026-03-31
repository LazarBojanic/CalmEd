package com.calmed.calmedtics.viewmodel

import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.model.dto.request.SetIsOnboardedDto
import com.calmed.calmedtics.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedtics.model.dto.response.HomeDto
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.model.joined.UserInfoTicsJoined
import com.calmed.calmedtics.model.joined.UserJoined
import com.calmed.calmedtics.model.toEntity
import com.calmed.calmedtics.model.toJoined
import com.calmed.calmedtics.repository.HomeRepository
import com.calmed.calmedtics.repository.IUserDao
import com.calmed.calmedtics.repository.IUserInfoTicsDao
import com.calmed.calmedtics.service.specification.IAuthService
import com.calmed.calmedtics.store.ITokenDataStore
import com.calmed.calmedtics.util.jwtDecode
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
	private val userInfoDao: IUserInfoTicsDao,
	private val homeRepository: HomeRepository,

) {
	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	private val _loading = MutableStateFlow(false)
	val loading: StateFlow<Boolean> = _loading

	private val _error = MutableStateFlow<String?>(null)
	val error: StateFlow<String?> = _error

	val user: StateFlow<UserJoined?> =
		userDao.findFirst().map { it?.toJoined() }.stateIn(scope, SharingStarted.Eagerly, null)

	private val _home = MutableStateFlow<HomeDto?>(null)
	val home: StateFlow<HomeDto?> = _home

	private val _allExercises = MutableStateFlow<List<ProgramExerciseDto>>(emptyList())
	val allExercises: StateFlow<List<ProgramExerciseDto>> = _allExercises

	val userInfo: StateFlow<UserInfoTicsJoined?> = combine(
		userDao.findFirst(),
		userInfoDao.findFirst()
	) { uEntity, uiEntity ->
		if (uEntity == null || uiEntity == null) return@combine null
		if (uiEntity.userId != uEntity.id) return@combine null
		uiEntity.toJoined(uEntity.toJoined())
	}.stateIn(scope, SharingStarted.Eagerly, null)

	private suspend fun currentUserId(): String? {
		val access = tokenStore.getToken()?.access ?: return null
		val payload = jwtDecode(access)
		return payload["sub"]?.jsonPrimitive?.content
	}

	private suspend fun clearLocal() {
		userInfoDao.clearAll()
		userDao.clearAll()
	}

	private suspend fun cacheUserDto(u: com.calmed.calmedtics.model.dto.response.UserDto) {
		userDao.clearAll()
		userDao.upsert(u.toEntity())
	}

	private suspend fun cacheUserInfoDto(ui: com.calmed.calmedtics.model.dto.response.UserInfoTicsDto) {
		userInfoDao.clearAll()
		userInfoDao.upsert(ui.toEntity())
	}

	suspend fun loadSession(): com.calmed.calmedtics.model.dto.response.UserDto? {
		println("LOAD SESSION CALLED");
		_error.value = null
		_loading.value = true
		try {
			val userId = currentUserId()
			println("USER ID = $userId")
			if (userId == null) {
				clearLocal()
				_error.value = "Missing user id."
				return null
			}


			val remoteUser = api.getUser(userId)
			println("REMOTE USER = $remoteUser")
			if (remoteUser == null) {
				clearLocal()
				_error.value = "Failed to load user."
				return null
			}
			cacheUserDto(remoteUser)

			val remoteInfo = api.getUserInfoTicsByUserId(userId)
			if (remoteInfo != null) {
				cacheUserDto(remoteInfo.user)
				cacheUserInfoDto(remoteInfo)
			} else {
				userInfoDao.clearAll()
			}
			return remoteUser
		} catch (t: Throwable) {
			_error.value = t.message ?: "Failed to load session."
			return null
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

	suspend fun updateProfileUserInfoTics(update: UserInfoTicsUpdateDto): Boolean {
		_error.value = null
		_loading.value = true
		return try {
			val currentUser = user.value
			if (currentUser == null) {
				_error.value = "Missing user."
				false
			} else {
				var currentUserInfo = userInfo.value
				if (currentUserInfo == null) {
					val fetched = api.getUserInfoTicsByUserId(currentUser.id)
					if (fetched != null) {
						cacheUserDto(fetched.user)
						cacheUserInfoDto(fetched)
						currentUserInfo = fetched.toEntity().toJoined(fetched.user.toEntity().toJoined())
					}
				}
				val resolved = currentUserInfo
				if (resolved == null) {
					_error.value = "Missing user info."
					false
				} else {
					val updatedInfo = api.updateUserInfoTics(resolved.id, update)
					if (updatedInfo == null) {
						_error.value = "Failed to update user info."
						false
					} else {
						cacheUserDto(updatedInfo.user)
						cacheUserInfoDto(updatedInfo)
						true
					}
				}
			}
		} catch (t: Throwable) {
			_error.value = t.message ?: "Update profile failed."
			false
		} finally {
			_loading.value = false
		}
	}

	suspend fun completeOnboarding(update: UserInfoTicsUpdateDto): Boolean {
		_error.value = null
		_loading.value = true
		return try {
			val currentUserInfo = userInfo.value
			val currentUser = user.value
			if (currentUserInfo == null || currentUser == null) {
				_error.value = "Missing user info."
				false
			} else {
				val updatedInfo = api.updateUserInfoTics(currentUserInfo.id, update)
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
	suspend fun loadHome(year: Int, month: Int) {
		try {
			val result = homeRepository.getHome(year, month)
			_home.value = result
		} catch (t: Throwable) {
			println("HOME ERROR ${t.message}")
			_error.value = t.message ?: "Home failed."
		}
	}
	suspend fun loadAllExercises() {
		_allExercises.value = api.getAllProgramExercises()
	}


}
