package com.calmed.calmedbackend.di

import com.calmed.calmedbackend.service.implementation.AuthCredentialService
import com.calmed.calmedbackend.service.implementation.AuthService
import com.calmed.calmedbackend.service.implementation.MessageService
import com.calmed.calmedbackend.service.implementation.RefreshTokenService
import com.calmed.calmedbackend.service.implementation.UserService
import com.calmed.calmedbackend.service.specification.IAuthCredentialService
import com.calmed.calmedbackend.service.specification.IAuthService
import com.calmed.calmedbackend.service.specification.IMessageService
import com.calmed.calmedbackend.service.specification.IRefreshTokenService
import com.calmed.calmedbackend.service.specification.IUserService
import org.koin.dsl.module

val serviceModule = module {
	single<IUserService> { UserService(get()) }
	single<IAuthCredentialService> { AuthCredentialService(get(), get()) }
	single<IRefreshTokenService> { RefreshTokenService(get(), get()) }
	single<IMessageService>{ MessageService(get()) }
	single<IAuthService> { AuthService(get(), get(), get(), get()) }
}