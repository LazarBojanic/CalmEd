package com.calmed.calmedbackend.di

import com.calmed.calmedbackend.auth.apple.AppleTokenApi
import com.calmed.calmedbackend.config.AppleConfig
import com.calmed.calmedbackend.service.implementation.AuthCredentialService
import com.calmed.calmedbackend.service.implementation.AuthService
import com.calmed.calmedbackend.service.implementation.RefreshTokenService
import com.calmed.calmedbackend.service.implementation.UserInfoTourettesService
import com.calmed.calmedbackend.service.implementation.UserService
import com.calmed.calmedbackend.service.implementation.ProgramExerciseService
import com.calmed.calmedbackend.service.implementation.UserProgramService
import com.calmed.calmedbackend.service.implementation.UserExerciseProgressService
import com.calmed.calmedbackend.service.specification.IAuthCredentialService
import com.calmed.calmedbackend.service.specification.IAuthService
import com.calmed.calmedbackend.service.specification.IRefreshTokenService
import com.calmed.calmedbackend.service.specification.IUserInfoTourettesService
import com.calmed.calmedbackend.service.specification.IUserService
import com.calmed.calmedbackend.service.specification.IProgramExerciseService
import com.calmed.calmedbackend.service.specification.IUserProgramService
import com.calmed.calmedbackend.service.specification.IUserExerciseProgressService
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val serviceModule = module {
	single {
		AppleTokenApi(
			http = HttpClient {
				install(ContentNegotiation) {
					json(Json { ignoreUnknownKeys = true })
				}
			},
			appleConfig = get<AppleConfig>()
		)
	}
	single<IUserService> { UserService(get()) }
	single<IAuthCredentialService> { AuthCredentialService(get(), get()) }
	single<IRefreshTokenService> { RefreshTokenService(get(), get()) }
	single<IUserInfoTourettesService> { UserInfoTourettesService(get(), get()) }
	single<IProgramExerciseService> { ProgramExerciseService(get()) }
	single<IUserProgramService> { UserProgramService(get(), get()) }
	single<IUserExerciseProgressService> { UserExerciseProgressService(get(), get(), get()) }
	single<IAuthService> { AuthService(get(), get(), get(), get(), get(), get(), get()) }
}