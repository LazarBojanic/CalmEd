package com.calmed.calmedbackend.di

import com.calmed.calmedbackend.repository.implementation.AuthCredentialRepository
import com.calmed.calmedbackend.repository.implementation.RefreshTokenRepository
import com.calmed.calmedbackend.repository.implementation.UserInfoTourettesRepository
import com.calmed.calmedbackend.repository.implementation.UserRepository
import com.calmed.calmedbackend.repository.specification.IAuthCredentialRepository
import com.calmed.calmedbackend.repository.specification.IRefreshTokenRepository
import com.calmed.calmedbackend.repository.specification.IUserInfoTourettesRepository
import com.calmed.calmedbackend.repository.specification.IUserRepository
import org.koin.dsl.module

val repositoryModule = module {
	single<IUserRepository> { UserRepository() }
	single<IAuthCredentialRepository> { AuthCredentialRepository() }
	single<IRefreshTokenRepository> { RefreshTokenRepository() }
	single<IUserInfoTourettesRepository> { UserInfoTourettesRepository() }
}