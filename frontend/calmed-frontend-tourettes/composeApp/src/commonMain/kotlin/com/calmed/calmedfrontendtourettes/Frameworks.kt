package com.calmed.calmedfrontendtourettes

import com.calmed.calmedfrontendtourettes.http.AppApi
import com.calmed.calmedfrontendtourettes.http.AppHttpClient
import com.calmed.calmedfrontendtourettes.http.IAppApi
import com.calmed.calmedfrontendtourettes.service.implementation.MessageService
import com.calmed.calmedfrontendtourettes.service.specification.IMessageService
import com.calmed.calmedfrontendtourettes.viewmodel.WelcomeViewModel
import io.ktor.client.engine.HttpClientEngineFactory
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.core.module.Module

expect fun platformEngine(): HttpClientEngineFactory<*>

fun commonModule(baseUrl: String) = module {
	single { AppHttpClient(baseUrl, platformEngine(), get()) }
	single<IAppApi> { AppApi(get(), get()) }

	single<IMessageService> { MessageService(get(), get()) }
	factory{ WelcomeViewModel(get()) }

}
fun initKoin(baseUrl: String, vararg platformModules: Module) {
	startKoin {
		modules(listOf(commonModule(baseUrl)) + platformModules)
	}
}