package com.calmed.calmedtics.repository

import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.model.dto.request.SetConfirmOverEighteenDto
import com.calmed.calmedtics.model.dto.request.SetIsOnboardedDto
import com.calmed.calmedtics.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedtics.model.dto.response.UserDto
import com.calmed.calmedtics.model.joined.UserInfoTicsJoined
import com.calmed.calmedtics.model.joined.UserJoined
import com.calmed.calmedtics.model.toEntity
import com.calmed.calmedtics.model.toJoined
import com.calmed.calmedtics.service.specification.IAuthService
import com.calmed.calmedtics.store.ITokenDataStore
import com.calmed.calmedtics.util.currentUserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class SessionFailure {
	MissingUserId,
	MissingUser,
	MissingUserInfo,
	UserNotFound,
	OnboardFailed,
	ConfirmAgeFailed,
	UserInfoUpdateFailed,
	DeleteAccountFailed,
}

class SessionException(val failure: SessionFailure) : Exception(failure.name)

class SessionRepository(
	private val api: IAppApi,
	private val tokenStore: ITokenDataStore,
	private val authService: IAuthService,
	private val userDao: IUserDao,
	private val userInfoDao: IUserInfoTicsDao,
) {
	val user: Flow<UserJoined?> =
		userDao.findFirst().map { it?.toJoined() }

	val userInfo: Flow<UserInfoTicsJoined?> = combine(
		userDao.findFirst(),
		userInfoDao.findFirst()
	) { uEntity, uiEntity ->
		if (uEntity == null || uiEntity == null) return@combine null
		if (uiEntity.userId != uEntity.id) return@combine null
		uiEntity.toJoined(uEntity.toJoined())
	}

	suspend fun loadSession(): UserDto {
		val userId = tokenStore.currentUserId()
		if (userId == null) {
			clearLocal()
			throw SessionException(SessionFailure.MissingUserId)
		}

		userDao.deleteAllExcept(userId)
		userInfoDao.deleteAllExcept(userId)

		val remoteUser = api.getUser(userId)
		if (remoteUser == null) {
			clearLocal()
			throw SessionException(SessionFailure.UserNotFound)
		}
		cacheUser(remoteUser)

		val remoteInfo = api.getUserInfoTicsByUserId(userId)
		if (remoteInfo != null) {
			cacheUser(remoteInfo.user)
			cacheUserInfo(remoteInfo)
		} else {
			userInfoDao.clearAll()
		}
		return remoteUser
	}

	suspend fun skipOnboarding() {
		val userId = tokenStore.currentUserId()
			?: throw SessionException(SessionFailure.MissingUserId)
		val updatedUser = api.setOnboarded(userId, SetIsOnboardedDto(isOnboarded = true))
			?: throw SessionException(SessionFailure.OnboardFailed)
		cacheUser(updatedUser)
	}

	suspend fun confirmOverEighteen() {
		val userId = tokenStore.currentUserId()
			?: throw SessionException(SessionFailure.MissingUserId)
		val updatedUser = api.confirmOverEighteen(
			userId,
			SetConfirmOverEighteenDto(confirmOverEighteen = true)
		) ?: throw SessionException(SessionFailure.ConfirmAgeFailed)
		cacheUser(updatedUser)
	}

	suspend fun updateProfileUserInfoTics(update: UserInfoTicsUpdateDto) {
		val currentUser = user.first()
			?: throw SessionException(SessionFailure.MissingUser)

		var currentUserInfo = userInfo.first()
		if (currentUserInfo == null) {
			val fetched = api.getUserInfoTicsByUserId(currentUser.id)
			if (fetched != null) {
				cacheUser(fetched.user)
				cacheUserInfo(fetched)
				currentUserInfo =
					fetched.toEntity().toJoined(fetched.user.toEntity().toJoined())
			}
		}

		val resolved = currentUserInfo
			?: throw SessionException(SessionFailure.MissingUserInfo)

		val updatedInfo = api.updateUserInfoTics(resolved.id, update)
			?: throw SessionException(SessionFailure.UserInfoUpdateFailed)
		cacheUser(updatedInfo.user)
		cacheUserInfo(updatedInfo)
	}

	suspend fun uploadProfileImage(imageBytes: ByteArray) {
		val updatedUser = api.uploadProfileImage(
			imageBytes = imageBytes,
			fileName = "profile.jpg"
		)
		cacheUser(updatedUser)
	}

	suspend fun completeOnboarding(update: UserInfoTicsUpdateDto) {
		val userId = tokenStore.currentUserId()
		val currentUserInfo = userInfo.first()
		if (userId == null || currentUserInfo == null || currentUserInfo.user.id != userId) {
			throw SessionException(SessionFailure.MissingUserInfo)
		}

		val updatedInfo = api.updateUserInfoTics(currentUserInfo.id, update)
			?: throw SessionException(SessionFailure.UserInfoUpdateFailed)
		cacheUser(updatedInfo.user)
		cacheUserInfo(updatedInfo)

		val updatedUser = api.setOnboarded(userId, SetIsOnboardedDto(isOnboarded = true))
			?: throw SessionException(SessionFailure.OnboardFailed)
		cacheUser(updatedUser)
	}

	suspend fun logout() {
		try {
			authService.logout()
		} finally {
			clearLocal()
		}
	}

	suspend fun deleteAccount() {
		val userId = tokenStore.currentUserId()
			?: throw SessionException(SessionFailure.MissingUserId)
		val deleted = api.deleteAccount(userId)
		if (!deleted) throw SessionException(SessionFailure.DeleteAccountFailed)
		clearLocal()
		authService.logout()
	}

	private suspend fun clearLocal() {
		userInfoDao.clearAll()
		userDao.clearAll()
	}

	private suspend fun cacheUser(u: UserDto) {
		userDao.upsert(u.toEntity())
	}

	private suspend fun cacheUserInfo(ui: com.calmed.calmedtics.model.dto.response.UserInfoTicsDto) {
		userInfoDao.upsert(ui.toEntity())
	}
}
