package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.database.withTransaction
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.repository.specification.IAuthCredentialRepository
import com.calmed.calmedbackend.repository.specification.IPaymentRepository
import com.calmed.calmedbackend.repository.specification.IRefreshTokenRepository
import com.calmed.calmedbackend.repository.specification.IUserExerciseProgressRepository
import com.calmed.calmedbackend.repository.specification.IUserInfoTicsRepository
import com.calmed.calmedbackend.repository.specification.IUserProgramRepository
import com.calmed.calmedbackend.repository.specification.IUserRepository
import com.calmed.calmedbackend.service.specification.IAccountDeletionService
import io.ktor.http.HttpStatusCode
import org.slf4j.LoggerFactory
import java.io.File
import java.util.UUID

/**
 * Orchestrates the permanent deletion of a user's account.
 *
 * Depends only on repositories (not services) to avoid a circular dependency
 * with [IUserService] and to keep the entire operation atomic within a single
 * transaction.
 */
class AccountDeletionService(
    private val userRepository: IUserRepository,
    private val refreshTokenRepository: IRefreshTokenRepository,
    private val authCredentialRepository: IAuthCredentialRepository,
    private val userInfoTicsRepository: IUserInfoTicsRepository,
    private val userProgramRepository: IUserProgramRepository,
    private val userExerciseProgressRepository: IUserExerciseProgressRepository,
    private val paymentRepository: IPaymentRepository
) : IAccountDeletionService {

    private val logger = LoggerFactory.getLogger(AccountDeletionService::class.java)

    override suspend fun deleteAccount(userId: UUID): AppResult<Unit> {
        return try {
            withTransaction {
                val user = userRepository.findById(userId)
                    ?: return@withTransaction AppResult.Failure(
                        HttpStatusCode.NotFound,
                        "User not found."
                    )

                refreshTokenRepository.deleteByUserId(userId)

                authCredentialRepository.deleteByUserId(userId)
                userInfoTicsRepository.deleteByUserId(userId)
                userProgramRepository.deleteByUserId(userId)
                userExerciseProgressRepository.deleteByUserId(userId)


                paymentRepository.anonymizeByUserId(userId)

                user.profileImageUrl?.let { profileImageUrl ->
                    deleteProfileImageFile(profileImageUrl)
                }

                userRepository.delete(userId)

                AppResult.Success(Unit)
            }
        } catch (e: Exception) {
            logger.error("Failed to delete account for user $userId: ${e.message}", e)
            AppResult.Failure(
                HttpStatusCode.InternalServerError,
                "Failed to delete account: ${e.message}"
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
            logger.warn("Could not delete profile image '$profileImageUrl': ${e.message}")
        }
    }
}

