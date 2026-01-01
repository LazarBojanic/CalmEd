package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.dbQuery
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
		return dbQuery { AuthCredentialEntity.all().map { it.toRaw() } }
	}

	override suspend fun findById(id: UUID): AuthCredential? {
		return dbQuery { AuthCredentialEntity.findById(id)?.toRaw() }
	}

	override suspend fun findByUserIdAndType(
		userId: UUID,
		type: AuthCredentialType
	): AuthCredential? {
		return dbQuery {
			AuthCredentialEntity
				.find {
					(AuthCredentialTable.userId eq userId) and
						(AuthCredentialTable.type eq type)
				}
				.firstOrNull()
				?.toRaw()
		}
	}

	override suspend fun findAllByUserId(userId: UUID): Set<AuthCredential> {
		return dbQuery {
			AuthCredentialEntity
				.find { AuthCredentialTable.userId eq userId }
				.map { it.toRaw() }
				.toSet()
		}
	}

	override suspend fun create(authCredential: AuthCredential): AuthCredential? {
		return dbQuery {
			AuthCredentialEntity.new(authCredential.id) {
				setFrom(authCredential, MapMode.CREATE)
			}.toRaw()
		}
	}

	override suspend fun update(authCredential: AuthCredential): AuthCredential? {
		return dbQuery {
			val e = AuthCredentialEntity.findById(authCredential.id) ?: return@dbQuery null
			e.setFrom(authCredential, MapMode.UPDATE)
			e.toRaw()
		}
	}

	override suspend fun delete(id: UUID): Boolean {
		return dbQuery {
			val e = AuthCredentialEntity.findById(id) ?: return@dbQuery false
			e.delete(); true
		}
	}
}