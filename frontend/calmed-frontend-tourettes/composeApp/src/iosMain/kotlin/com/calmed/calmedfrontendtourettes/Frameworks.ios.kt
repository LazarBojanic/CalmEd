package com.calmed.calmedfrontendtourettes

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.calmed.calmedfrontendtourettes.database.AppDatabase
import com.calmed.calmedfrontendtourettes.database.getAppDatabase
import com.calmed.calmedfrontendtourettes.database.getDatabaseBuilder
import com.calmed.calmedfrontendtourettes.store.ITokenDataStore
import com.calmed.calmedfrontendtourettes.store.TokenDataStore
import com.calmed.calmedfrontendtourettes.store.provideTokenDataStore
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.module

actual fun platformEngine(): HttpClientEngineFactory<*> = Darwin

fun iosModule() = module {
	single { getAppDatabase(getDatabaseBuilder()) }
	single<DataStore<Preferences>> {
		provideTokenDataStore()
	}
	single<ITokenDataStore> { TokenDataStore(get()) }
	single { get<AppDatabase>().getMessageDao() }

}

fun initKoinIos() {
	val dev = true
	var url = ""
	if(dev){
		url = "http://127.0.0.1:8080"
	}
	else{
		url = "https://srv1092316.hstgr.cloud"
	}
	initKoin(
		baseUrl = url,
		iosModule(),
	)
}