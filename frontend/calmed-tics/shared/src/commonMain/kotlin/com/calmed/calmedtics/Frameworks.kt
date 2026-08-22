package com.calmed.calmedtics

import com.calmed.calmedtics.di.settingsModule
import com.calmed.calmedtics.http.AppApi
import com.calmed.calmedtics.http.AppHttpClient
import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.repository.HomeRepository
import com.calmed.calmedtics.service.implementation.AuthService
import com.calmed.calmedtics.service.specification.IAuthService
import com.calmed.calmedtics.viewmodel.AuthViewModel
import com.calmed.calmedtics.viewmodel.SessionViewModel
import io.ktor.client.engine.HttpClientEngineFactory
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

expect fun platformEngine(): HttpClientEngineFactory<*>

fun commonModule(baseUrl: String) = module {
    single { AppHttpClient(baseUrl, platformEngine(), get()) }
    single { get<AppHttpClient>().client }
    single<IAppApi> { AppApi(get(), get()) }
    single { HomeRepository(api = get()) }
    single<IAuthService> { AuthService(get(), get()) }
    factory { AuthViewModel(get()) }
    factory { SessionViewModel(get(), get(), get(), get(), get(), get(), homeRepository = get()) }
}

fun initKoin(baseUrl: String, vararg platformModules: Module) {
    startKoin {
        modules(listOf(commonModule(baseUrl),settingsModule) + platformModules)
    }
}