package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.joined.UserJoined
import com.calmed.calmedbackend.model.raw.user.PaymentType
import com.calmed.calmedbackend.model.raw.user.User
import java.util.UUID

interface IUserService {
	suspend fun getAll(): AppResult<List<UserJoined>>
	suspend fun getById(id: UUID): AppResult<UserJoined>
	suspend fun getByEmail(email: String): AppResult<UserJoined>
	suspend fun create(user: User): AppResult<UserJoined>
	suspend fun update(user: User): AppResult<UserJoined>
	suspend fun delete(id: UUID): AppResult<Unit>
	suspend fun setIsOnboarded(id: UUID, isOnboarded: Boolean): AppResult<UserJoined>
	suspend fun setPaymentStatus(
		id: UUID,
		isPaid: Boolean,
		paymentType: PaymentType?,
		stripeCustomerId: String?
	): AppResult<UserJoined>
}
