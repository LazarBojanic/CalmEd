package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.tx
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
		return tx { AuthCredentialEntity.all().map { it.toRaw() } }
	}

	override suspend fun findById(id: UUID): AuthCredential? {
		return tx {
			val e = AuthCredentialEntity.findById(id)
			if (e != null) {
				return@tx e.toRaw()
			}
			else {
				return@tx null
			}
		}
	}

	override suspend fun findByUserIdAndType(
		userId: UUID,
		type: AuthCredentialType
	): AuthCredential? {
		return tx {
			val e = AuthCredentialEntity
				.find {
					(AuthCredentialTable.userId eq userId) and (AuthCredentialTable.type eq type)
				}
				.firstOrNull()
			if(e != null){
				return@tx e.toRaw()
			}
			else{
				return@tx null
			}
		}
	}

	override suspend fun findAllByUserId(userId: UUID): Set<AuthCredential> {
		return tx {
			AuthCredentialEntity
				.find { AuthCredentialTable.userId eq userId }
				.map { it.toRaw() }
				.toSet()
		}
	}

	override suspend fun create(authCredential: AuthCredential): AuthCredential? {
		return tx {
			val e = AuthCredentialEntity.findById(authCredential.id)
			if(e != null){
				return@tx AuthCredentialEntity.new(authCredential.id) {
					setFrom(authCredential, MapMode.CREATE)
				}.toRaw()
			}
			else{
				return@tx null
			}
		}
	}

	override suspend fun update(authCredential: AuthCredential): AuthCredential? {
		return tx {
			val e = AuthCredentialEntity.findById(authCredential.id)
			if (e != null) {
				e.setFrom(authCredential, MapMode.UPDATE)
				return@tx e.toRaw()
			}
			else {
				return@tx null
			}
		}
	}

	override suspend fun delete(id: UUID): Boolean {
		return tx {
			val e = AuthCredentialEntity.findById(id)
			if (e != null) {
				e.delete()
				return@tx true
			}
			else {
				return@tx false
			}
		}
	}
}