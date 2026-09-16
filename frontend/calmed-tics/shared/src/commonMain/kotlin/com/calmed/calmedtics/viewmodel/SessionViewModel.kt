package com.calmed.calmedtics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedtics.model.dto.response.UserDto
import com.calmed.calmedtics.model.joined.UserInfoTicsJoined
import com.calmed.calmedtics.model.joined.UserJoined
import com.calmed.calmedtics.repository.SessionException
import com.calmed.calmedtics.repository.SessionFailure
import com.calmed.calmedtics.repository.SessionRepository
import com.calmed.calmedtics.settings.AppSettings
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SessionViewModel(
	private val repository: SessionRepository,
	private val api: IAppApi,
	private val appSettings: AppSettings,
) : ViewModel() {

	private val _loading = MutableStateFlow(false)
	val loading: StateFlow<Boolean> = _loading

	private val _error = MutableStateFlow<String?>(null)
	val error: StateFlow<String?> = _error

	val user: StateFlow<UserJoined?> =
		repository.user.stateIn(viewModelScope, SharingStarted.Eagerly, null)

	val userInfo: StateFlow<UserInfoTicsJoined?> =
		repository.userInfo.stateIn(viewModelScope, SharingStarted.Eagerly, null)

	private var welcomeHandledUserId: String? = null
	private var courseOverviewHandledUserId: String? = null

	suspend fun resolveRouting(): SessionRouting? {
		val remoteUser = loadSession() ?: return null
		return SessionRouting(
			confirmOverEighteen = remoteUser.confirmOverEighteen,
			isOnboarded = remoteUser.isOnboarded,
			isPaid = api.getPaymentStatus()?.hasAccess ?: false,
			shouldShowWelcomeVideo =
				appSettings.getShowWelcomeVideo(remoteUser.id) &&
					welcomeHandledUserId != remoteUser.id,
			shouldShowCourseOverview =
				appSettings.getShowCourseOverview(remoteUser.id) &&
					courseOverviewHandledUserId != remoteUser.id,
		)
	}

	fun onWelcomeHandled(userId: String?) {
		welcomeHandledUserId = userId
	}

	fun onCourseOverviewHandled(userId: String?) {
		courseOverviewHandledUserId = userId
	}

	fun resetRoutingFlags() {
		welcomeHandledUserId = null
		courseOverviewHandledUserId = null
	}

	suspend fun loadSession(): UserDto? = runSession(
		onFailure = { failure ->
			when (failure) {
				SessionFailure.MissingUserId -> getString(Res.string.session_missing_user_id)
				SessionFailure.UserNotFound -> getString(Res.string.session_load_user_failed)
				else -> getString(Res.string.session_load_failed)
			}
		},
		onThrowable = { it.message ?: getString(Res.string.session_load_failed) },
	) {
		repository.loadSession()
	}

	suspend fun skipOnboarding(): Boolean = runSession(
		onFailure = { failure ->
			when (failure) {
				SessionFailure.MissingUserId -> getString(Res.string.session_missing_user_id)
				else -> getString(Res.string.session_onboard_failed)
			}
		},
		onThrowable = { it.message ?: getString(Res.string.session_skip_onboarding_failed) },
	) {
		repository.skipOnboarding()
	} != null

	suspend fun confirmOverEighteen(): Boolean = runSession(
		onFailure = { failure ->
			when (failure) {
				SessionFailure.MissingUserId -> getString(Res.string.session_missing_user_id)
				else -> getString(Res.string.session_confirm_age_failed)
			}
		},
		onThrowable = { it.message ?: getString(Res.string.session_age_confirmation_failed) },
	) {
		repository.confirmOverEighteen()
	} != null

	suspend fun updateProfileUserInfoTics(update: UserInfoTicsUpdateDto): Boolean = runSession(
		onFailure = { failure ->
			when (failure) {
				SessionFailure.MissingUser -> getString(Res.string.session_missing_user)
				SessionFailure.MissingUserInfo -> getString(Res.string.session_missing_user_info)
				else -> getString(Res.string.session_update_user_info_failed)
			}
		},
		onThrowable = { it.message ?: getString(Res.string.session_update_profile_failed) },
	) {
		repository.updateProfileUserInfoTics(update)
	} != null

	suspend fun uploadProfileImage(imageBytes: ByteArray): Boolean = runSession(
		onFailure = { getString(Res.string.session_profile_image_upload_failed) },
		onThrowable = { it.message ?: getString(Res.string.session_profile_image_upload_failed) },
	) {
		repository.uploadProfileImage(imageBytes)
	} != null

	suspend fun completeOnboarding(update: UserInfoTicsUpdateDto): Boolean = runSession(
		onFailure = { failure ->
			when (failure) {
				SessionFailure.MissingUserInfo -> getString(Res.string.session_missing_user_info)
				SessionFailure.UserInfoUpdateFailed ->
					getString(Res.string.session_update_user_info_failed)
				SessionFailure.OnboardFailed -> getString(Res.string.session_onboard_failed)
				else -> getString(Res.string.session_onboarding_failed)
			}
		},
		onThrowable = { it.message ?: getString(Res.string.session_onboarding_failed) },
	) {
		repository.completeOnboarding(update)
	} != null

	suspend fun logout() {
		_error.value = null
		_loading.value = true
		try {
			repository.logout()
		} finally {
			_loading.value = false
		}
	}

	suspend fun deleteAccount(): Boolean = runSession(
		onFailure = { failure ->
			when (failure) {
				SessionFailure.MissingUserId -> getString(Res.string.session_missing_user_id)
				else -> getString(Res.string.session_delete_account_failed)
			}
		},
		onThrowable = { it.message ?: getString(Res.string.session_delete_account_failed) },
	) {
		repository.deleteAccount()
	} != null

	private suspend fun <T> runSession(
		onFailure: suspend (SessionFailure) -> String,
		onThrowable: suspend (Throwable) -> String,
		block: suspend () -> T,
	): T? {
		_error.value = null
		_loading.value = true
		return try {
			block()
		} catch (t: CancellationException) {
			throw t
		} catch (t: SessionException) {
			_error.value = onFailure(t.failure)
			null
		} catch (t: Throwable) {
			_error.value = onThrowable(t)
			null
		} finally {
			_loading.value = false
		}
	}
}
