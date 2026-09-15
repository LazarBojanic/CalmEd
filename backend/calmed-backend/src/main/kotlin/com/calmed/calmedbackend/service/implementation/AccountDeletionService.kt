package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.database.withResultTransaction
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.repository.specification.IAuthCredentialRepository
import com.calmed.calmedbackend.repository.specification.IRefreshTokenRepository
import com.calmed.calmedbackend.repository.specification.IUserExerciseProgressRepository
import com.calmed.calmedbackend.repository.specification.IUserInfoTicsRepository
import com.calmed.calmedbackend.repository.specification.IUserProgramRepository
import com.calmed.calmedbackend.repository.specification.IUserRepository
import com.calmed.calmedbackend.repository.specification.IStoreEntitlementRepository
import com.calmed.calmedbackend.service.specification.IAccountDeletionService
import io.ktor.http.HttpStatusCode
import org.slf4j.LoggerFactory
import java.io.File
import java.util.UUID


class AccountDeletionService(
    private val userRepository: IUserRepository,
    private val refreshTokenRepository: IRefreshTokenRepository,
    private val authCredentialRepository: IAuthCredentialRepository,
    private val userInfoTicsRepository: IUserInfoTicsRepository,
    private val userProgramRepository: IUserProgramRepository,
    private val userExerciseProgressRepository: IUserExerciseProgressRepository,
    private val storeEntitlementRepository: IStoreEntitlementRepository
) : IAccountDeletionService {

    private val logger = LoggerFactory.getLogger(AccountDeletionService::class.java)

    override suspend fun deleteAccount(userId: UUID): AppResult<Unit> {
        return try {
            val deleted = withResultTransaction {
                val user = userRepository.findById(userId)
                    ?: return@withResultTransaction AppResult.Failure(
                        HttpStatusCode.NotFound,
                        "User not found."
                    )

                refreshTokenRepository.deleteByUserId(userId)

                authCredentialRepository.deleteByUserId(userId)
                userInfoTicsRepository.deleteByUserId(userId)
                userProgramRepository.deleteByUserId(userId)
                userExerciseProgressRepository.deleteByUserId(userId)

                storeEntitlementRepository.detachByUserId(userId)

                userRepository.delete(userId)

                AppResult.Success(user.profileImageUrl)
            }

            if (deleted is AppResult.Failure) return deleted

            (deleted as AppResult.Success).data?.let { profileImageUrl ->
                deleteProfileImageFile(profileImageUrl)
            }

            AppResult.Success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to delete account for user {}", userId, e)
            AppResult.Failure(
                HttpStatusCode.InternalServerError,
                "Failed to delete account."
            )
        }
    }
    private fun deleteProfileImageFile(profileImageUrl: String) {
        if (!profileImageUrl.startsWith("/uploads/profile/")) {
            return
        }
        val fileName = profileImageUrl.removePrefix("/uploads/profile/")
        if (fileName.isBlank() || fileName.contains('/') || fileName.contains('\\')) {
            return
        }
        try {
            val file = File("uploads/profile", fileName)
            if (file.exists() && file.isFile) {
                file.delete()
            }
        } catch (e: Exception) {
            logger.warn("Could not delete profile image '{}'", profileImageUrl, e)
        }
    }
}
