package com.calmed.calmedtics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.model.dto.request.SetIsOnboardedDto
import com.calmed.calmedtics.model.dto.request.SetConfirmOverEighteenDto
import com.calmed.calmedtics.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedtics.model.dto.response.UserDto
import com.calmed.calmedtics.model.joined.UserInfoTicsJoined
import com.calmed.calmedtics.model.joined.UserJoined
import com.calmed.calmedtics.model.toEntity
import com.calmed.calmedtics.model.toJoined
import com.calmed.calmedtics.repository.IUserDao
import com.calmed.calmedtics.repository.IUserInfoTicsDao
import com.calmed.calmedtics.service.specification.IAuthService
import com.calmed.calmedtics.store.ITokenDataStore
import com.calmed.calmedtics.util.currentUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SessionViewModel(
	private val api: IAppApi,
	private val tokenStore: ITokenDataStore,
	private val authService: IAuthService,
	private val userDao: IUserDao,
	private val userInfoDao: IUserInfoTicsDao,
) : ViewModel() {

	private val _loading = MutableStateFlow(false)
	val loading: StateFlow<Boolean> = _loading

	private val _error = MutableStateFlow<String?>(null)
	val error: StateFlow<String?> = _error

	val user: StateFlow<UserJoined?> =
		userDao.findFirst().map { it?.toJoined() }
			.stateIn(viewModelScope, SharingStarted.Eagerly, null)

	val userInfo: StateFlow<UserInfoTicsJoined?> = combine(
		userDao.findFirst(),
		userInfoDao.findFirst()
	) { uEntity, uiEntity ->
		if (uEntity == null || uiEntity == null) return@combine null
		if (uiEntity.userId != uEntity.id) return@combine null
		uiEntity.toJoined(uEntity.toJoined())
	}.stateIn(viewModelScope, SharingStarted.Eagerly, null)

	private suspend fun clearLocal() {
		userInfoDao.clearAll()
		userDao.clearAll()
	}

	private suspend fun cacheUserDto(u: UserDto) {
		userDao.upsert(u.toEntity())
	}

	private suspend fun cacheUserInfoDto(ui: com.calmed.calmedtics.model.dto.response.UserInfoTicsDto) {
		userInfoDao.upsert(ui.toEntity())
	}

	suspend fun loadSession(): UserDto? {
		_error.value = null
		_loading.value = true
		return try {
			val userId = tokenStore.currentUserId()
			if (userId == null) {
				clearLocal()
				_error.value = "Missing user id."
				return null
			}

			userDao.deleteAllExcept(userId)
			userInfoDao.deleteAllExcept(userId)

			val remoteUser = api.getUser(userId)
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
			remoteUser
		} catch (t: Throwable) {
			_error.value = t.message ?: "Failed to load session."
			null
		} finally {
			_loading.value = false
		}
	}

	suspend fun skipOnboarding(): Boolean {
		_error.value = null
		_loading.value = true
		return try {
			val userId = tokenStore.currentUserId()
			if (userId == null) {
				_error.value = "Missing user id."
				false
			} else {
				val updatedUser = api.setOnboarded(
					userId,
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

	suspend fun confirmOverEighteen(): Boolean {
		_error.value = null
		_loading.value = true
		return try {
			val userId = tokenStore.currentUserId()
			if (userId == null) {
				_error.value = "Missing user id."
				false
			} else {
				val updatedUser = api.confirmOverEighteen(
					userId,
					SetConfirmOverEighteenDto(confirmOverEighteen = true)
				)
				if (updatedUser == null) {
					_error.value = "Failed to confirm age."
					false
				} else {
					cacheUserDto(updatedUser)
					true
				}
			}
		} catch (t: Throwable) {
			_error.value = t.message ?: "Age confirmation failed."
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

	suspend fun uploadProfileImage(imageBytes: ByteArray): Boolean {
		_error.value = null
		_loading.value = true
		return try {
			val updatedUser = api.uploadProfileImage(
				imageBytes = imageBytes,
				fileName = "profile.jpg"
			)
			cacheUserDto(updatedUser)
			true
		} catch (t: Throwable) {
			_error.value = t.message ?: "Profile image upload failed."
			false
		} finally {
			_loading.value = false
		}
	}

	suspend fun completeOnboarding(update: UserInfoTicsUpdateDto): Boolean {
		_error.value = null
		_loading.value = true
		return try {
			val userId = tokenStore.currentUserId()
			val currentUserInfo = userInfo.value
			if (userId == null || currentUserInfo == null || currentUserInfo.user.id != userId) {
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
						userId,
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

	suspend fun deleteAccount(): Boolean {
		_error.value = null
		_loading.value = true
		return try {
			val userId = tokenStore.currentUserId()
			if (userId == null) {
				_error.value = "Missing user id."
				false
			} else {
				val deleted = api.deleteAccount(userId)
				if (deleted) {
					clearLocal()
					authService.logout()
					true
				} else {
					_error.value = "Failed to delete the account. Please try again."
					false
				}
			}
		} catch (t: Throwable) {
			_error.value = t.message ?: "Failed to delete the account."
			false
		} finally {
			_loading.value = false
		}
	}
}
