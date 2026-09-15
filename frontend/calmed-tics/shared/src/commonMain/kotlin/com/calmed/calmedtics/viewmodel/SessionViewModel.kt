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
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.session_missing_user_id
import calmedtics.shared.generated.resources.session_load_user_failed
import calmedtics.shared.generated.resources.session_load_failed
import calmedtics.shared.generated.resources.session_onboard_failed
import calmedtics.shared.generated.resources.session_skip_onboarding_failed
import calmedtics.shared.generated.resources.session_confirm_age_failed
import calmedtics.shared.generated.resources.session_age_confirmation_failed
import calmedtics.shared.generated.resources.session_missing_user
import calmedtics.shared.generated.resources.session_missing_user_info
import calmedtics.shared.generated.resources.session_update_user_info_failed
import calmedtics.shared.generated.resources.session_update_profile_failed
import calmedtics.shared.generated.resources.session_profile_image_upload_failed
import calmedtics.shared.generated.resources.session_onboarding_failed
import calmedtics.shared.generated.resources.session_delete_account_failed
import org.jetbrains.compose.resources.getString
import com.calmed.calmedtics.util.currentUserId
import kotlinx.coroutines.CancellationException
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
				_error.value = getString(Res.string.session_missing_user_id)
				return null
			}

			userDao.deleteAllExcept(userId)
			userInfoDao.deleteAllExcept(userId)

			val remoteUser = api.getUser(userId)
			if (remoteUser == null) {
				clearLocal()
				_error.value = getString(Res.string.session_load_user_failed)
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
		} catch (t: CancellationException) {
			throw t
		} catch (t: Throwable) {
			_error.value = t.message ?: getString(Res.string.session_load_failed)
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
				_error.value = getString(Res.string.session_missing_user_id)
				false
			} else {
				val updatedUser = api.setOnboarded(
					userId,
					SetIsOnboardedDto(isOnboarded = true)
				)
				if (updatedUser == null) {
					_error.value = getString(Res.string.session_onboard_failed)
					false
				} else {
					cacheUserDto(updatedUser)
					true
				}
			}
		} catch (t: CancellationException) {
			throw t
		} catch (t: Throwable) {
			_error.value = t.message ?: getString(Res.string.session_skip_onboarding_failed)
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
				_error.value = getString(Res.string.session_missing_user_id)
				false
			} else {
				val updatedUser = api.confirmOverEighteen(
					userId,
					SetConfirmOverEighteenDto(confirmOverEighteen = true)
				)
				if (updatedUser == null) {
					_error.value = getString(Res.string.session_confirm_age_failed)
					false
				} else {
					cacheUserDto(updatedUser)
					true
				}
			}
		} catch (t: CancellationException) {
			throw t
		} catch (t: Throwable) {
			_error.value = t.message ?: getString(Res.string.session_age_confirmation_failed)
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
				_error.value = getString(Res.string.session_missing_user)
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
					_error.value = getString(Res.string.session_missing_user_info)
					false
				} else {
					val updatedInfo = api.updateUserInfoTics(resolved.id, update)
					if (updatedInfo == null) {
						_error.value = getString(Res.string.session_update_user_info_failed)
						false
					} else {
						cacheUserDto(updatedInfo.user)
						cacheUserInfoDto(updatedInfo)
						true
					}
				}
			}
		} catch (t: CancellationException) {
			throw t
		} catch (t: Throwable) {
			_error.value = t.message ?: getString(Res.string.session_update_profile_failed)
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
		} catch (t: CancellationException) {
			throw t
		} catch (t: Throwable) {
			_error.value = t.message ?: getString(Res.string.session_profile_image_upload_failed)
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
				_error.value = getString(Res.string.session_missing_user_info)
				false
			} else {
				val updatedInfo = api.updateUserInfoTics(currentUserInfo.id, update)
				if (updatedInfo == null) {
					_error.value = getString(Res.string.session_update_user_info_failed)
					false
				} else {
					cacheUserDto(updatedInfo.user)
					cacheUserInfoDto(updatedInfo)

					val updatedUser = api.setOnboarded(
						userId,
						SetIsOnboardedDto(isOnboarded = true)
					)
					if (updatedUser == null) {
						_error.value = getString(Res.string.session_onboard_failed)
						false
					} else {
						cacheUserDto(updatedUser)
						true
					}
				}
			}
		} catch (t: CancellationException) {
			throw t
		} catch (t: Throwable) {
			_error.value = t.message ?: getString(Res.string.session_onboarding_failed)
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
				_error.value = getString(Res.string.session_missing_user_id)
				false
			} else {
				val deleted = api.deleteAccount(userId)
				if (deleted) {
					clearLocal()
					authService.logout()
					true
				} else {
					_error.value = getString(Res.string.session_delete_account_failed)
					false
				}
			}
		} catch (t: CancellationException) {
			throw t
		} catch (t: Throwable) {
			_error.value = t.message ?: getString(Res.string.session_delete_account_failed)
			false
		} finally {
			_loading.value = false
		}
	}
}
