package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredential
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialEntity
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialTable
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialType
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IAuthCredentialRepository
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import java.util.UUID

class AuthCredentialRepository : IAuthCredentialRepository {
	override suspend fun findAll(): List<AuthCredential> {
		return AuthCredentialEntity.all().map { it.toRaw() }
	}

	override suspend fun findById(id: UUID): AuthCredential? {
		return AuthCredentialEntity.findById(id)?.toRaw()
	}

	override suspend fun findByUserIdAndType(
		userId: UUID,
		type: AuthCredentialType
	): AuthCredential? {
		return AuthCredentialEntity
			.find {
				(AuthCredentialTable.userId eq userId) and (AuthCredentialTable.type eq type)
			}
			.firstOrNull()
			?.toRaw()
	}

	override suspend fun findAllByUserId(userId: UUID): Set<AuthCredential> {
		return AuthCredentialEntity
			.find { AuthCredentialTable.userId eq userId }
			.map { it.toRaw() }
			.toSet()
	}

	override suspend fun create(authCredential: AuthCredential): AuthCredential? {
		if (AuthCredentialEntity.findById(authCredential.id) != null) return null
		return AuthCredentialEntity.new(authCredential.id) {
			setFrom(authCredential, MapMode.CREATE)
		}.toRaw()
	}

	override suspend fun update(authCredential: AuthCredential): AuthCredential? {
		val e = AuthCredentialEntity.findById(authCredential.id) ?: return null
		e.setFrom(authCredential, MapMode.UPDATE)
		return e.toRaw()
	}

	override suspend fun findByProviderUserIdAndType(
		providerUserId: String,
		type: AuthCredentialType
	): AuthCredential? {
		return AuthCredentialEntity.find {
			(AuthCredentialTable.providerUserId eq providerUserId) and
					(AuthCredentialTable.type eq type)
		}.firstOrNull()?.toRaw()
	}

	override suspend fun delete(id: UUID): Boolean {
		val e = AuthCredentialEntity.findById(id) ?: return false
		e.delete()
		return true
	}

	override suspend fun deleteByUserId(userId: UUID): Boolean {
		val entities = AuthCredentialEntity.find {
			AuthCredentialTable.userId eq userId
		}
		var deleted = false
		for (e in entities) {
			e.delete()
			deleted = true
		}
		return deleted
	}
}
