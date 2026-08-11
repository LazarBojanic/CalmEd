package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.UserJoined
import com.calmed.calmedbackend.model.raw.payment.Payment
import com.calmed.calmedbackend.model.raw.user.PaymentType
import com.calmed.calmedbackend.model.raw.user.User
import com.calmed.calmedbackend.repository.specification.IPaymentRepository
import com.calmed.calmedbackend.repository.specification.IUserRepository
import com.calmed.calmedbackend.service.specification.IUserService
import io.ktor.http.HttpStatusCode
import java.time.Instant
import java.util.UUID

class UserService(
	private val userRepository: IUserRepository,
	private val paymentRepository: IPaymentRepository
) : IUserService {

	override suspend fun getAll(): AppResult<List<UserJoined>> {
		val users = userRepository.findAll()
		return AppResult.Success(users.map { it.join() })
	}

	override suspend fun getById(id: UUID): AppResult<UserJoined> {
		val user = userRepository.findById(id)
		if (user != null) {
			return AppResult.Success(user.join())
		}
		else {
			return AppResult.Failure(HttpStatusCode.NotFound, "User not found.")
		}
	}

	override suspend fun getByEmail(email: String): AppResult<UserJoined> {
		val user = userRepository.findByEmail(email)
		if (user != null) {
			return AppResult.Success(user.join())
		}
		else {
			return AppResult.Failure(HttpStatusCode.NotFound, "User not found.")
		}
	}

	override suspend fun create(user: User): AppResult<UserJoined> {
		val created = userRepository.create(user)
		if (created != null) {
			return AppResult.Success(created.join())
		}
		else {
			return AppResult.Failure(HttpStatusCode.BadRequest, "Failed to create user.")
		}
	}

	override suspend fun update(user: User): AppResult<UserJoined> {
		val updated = userRepository.update(user)
		if (updated != null) {
			return AppResult.Success(updated.join())
		}
		else {
			return AppResult.Failure(HttpStatusCode.BadRequest, "Failed to update user.")
		}
	}

	override suspend fun delete(id: UUID): AppResult<Unit> {
		if (userRepository.delete(id)) {
			return AppResult.Success(Unit)
		}
		else {
			return AppResult.Failure(HttpStatusCode.BadRequest, "Failed to delete user.")
		}
	}

	override suspend fun setIsOnboarded(id: UUID, isOnboarded: Boolean): AppResult<UserJoined> {
		val updated = userRepository.setIsOnboarded(id, isOnboarded)
		return if (updated != null) {
			AppResult.Success(updated.join())
		} else {
			AppResult.Failure(HttpStatusCode.NotFound, "User not found.")
		}
	}

	override suspend fun setPaymentStatus(
		id: UUID,
		isPaid: Boolean,
		stripeCustomerId: String?
	): AppResult<UserJoined> {
		val user = userRepository.findById(id)
		if (user == null) {
			return AppResult.Failure(HttpStatusCode.NotFound, "User not found.")
		}

		// Update user's isPaid status and stripeCustomerId if provided
		val updatedUser = user.copy(
			isPaid = isPaid,
			stripeCustomerId = stripeCustomerId ?: user.stripeCustomerId,
			updatedAt = Instant.now()
		)
		val updated = userRepository.update(updatedUser)

		return if (updated != null) {
			AppResult.Success(updated.join())
		} else {
			AppResult.Failure(HttpStatusCode.NotFound, "User not found.")
		}
	}

	override suspend fun updateProfileImage(
		userId: UUID,
		profileImageUrl: String
	): AppResult<UserJoined> {

		val user = userRepository.findById(userId)

		if (user == null) {
			return AppResult.Failure(
				HttpStatusCode.NotFound,
				"User not found."
			)
		}

		val updatedUser = user.copy(
			profileImageUrl = profileImageUrl,
			updatedAt = Instant.now()
		)

		val updated = userRepository.update(updatedUser)

		return if (updated != null) {
			AppResult.Success(updated.join())
		} else {
			AppResult.Failure(
				HttpStatusCode.BadRequest,
				"Failed to update profile image."
			)
		}
	}
}
