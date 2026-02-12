package com.calmed.calmedbackend.di

import com.calmed.calmedbackend.repository.implementation.AuthCredentialRepository
import com.calmed.calmedbackend.repository.implementation.RefreshTokenRepository
import com.calmed.calmedbackend.repository.implementation.UserInfoTourettesRepository
import com.calmed.calmedbackend.repository.implementation.UserRepository
import com.calmed.calmedbackend.repository.implementation.ProgramExerciseRepository
import com.calmed.calmedbackend.repository.implementation.UserProgramRepository
import com.calmed.calmedbackend.repository.implementation.UserExerciseProgressRepository
import com.calmed.calmedbackend.repository.specification.IAuthCredentialRepository
import com.calmed.calmedbackend.repository.specification.IRefreshTokenRepository
import com.calmed.calmedbackend.repository.specification.IUserInfoTourettesRepository
import com.calmed.calmedbackend.repository.specification.IUserRepository
import com.calmed.calmedbackend.repository.specification.IProgramExerciseRepository
import com.calmed.calmedbackend.repository.specification.IUserProgramRepository
import com.calmed.calmedbackend.repository.specification.IUserExerciseProgressRepository
import org.koin.dsl.module

val repositoryModule = module {
	single<IUserRepository> { UserRepository() }
	single<IAuthCredentialRepository> { AuthCredentialRepository() }
	single<IRefreshTokenRepository> { RefreshTokenRepository() }
	single<IUserInfoTourettesRepository> { UserInfoTourettesRepository() }
	single<IProgramExerciseRepository> { ProgramExerciseRepository() }
	single<IUserProgramRepository> { UserProgramRepository() }
	single<IUserExerciseProgressRepository> { UserExerciseProgressRepository() }


}