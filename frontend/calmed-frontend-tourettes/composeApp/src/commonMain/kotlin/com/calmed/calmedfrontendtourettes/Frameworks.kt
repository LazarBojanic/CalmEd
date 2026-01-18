package com.calmed.calmedfrontendtourettes

import com.calmed.calmedfrontendtourettes.http.AppApi
import com.calmed.calmedfrontendtourettes.http.AppHttpClient
import com.calmed.calmedfrontendtourettes.http.IAppApi
import com.calmed.calmedfrontendtourettes.service.implementation.AuthService
import com.calmed.calmedfrontendtourettes.service.specification.IAuthService
import com.calmed.calmedfrontendtourettes.viewmodel.AuthViewModel
import com.calmed.calmedfrontendtourettes.viewmodel.SessionViewModel
import io.ktor.client.engine.HttpClientEngineFactory
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

expect fun platformEngine(): HttpClientEngineFactory<*>

fun commonModule(baseUrl: String) = module {
    single { AppHttpClient(baseUrl, platformEngine(), get()) }
    single<IAppApi> { AppApi(get(), get()) }

    single<IAuthService> { AuthService(get(), get()) }
    factory { AuthViewModel(get()) }
    factory { SessionViewModel(get(), get(), get(), get(), get()) }
}

fun initKoin(baseUrl: String, vararg platformModules: Module) {
    startKoin {
        modules(listOf(commonModule(baseUrl)) + platformModules)
    }
}