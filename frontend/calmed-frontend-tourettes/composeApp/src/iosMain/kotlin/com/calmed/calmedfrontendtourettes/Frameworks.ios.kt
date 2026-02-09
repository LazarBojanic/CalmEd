package com.calmed.calmedfrontendtourettes

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import calmedfrontendtourettes.composeApp.BuildConfig
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
    single<DataStore<Preferences>> { provideTokenDataStore() }
    single<ITokenDataStore> { TokenDataStore(get()) }

    single<AppDatabase> { getAppDatabase(getDatabaseBuilder()) }
    single { get<AppDatabase>().getUserDao() }
    single { get<AppDatabase>().getUserInfoTourettesDao() }
}

fun initKoinIos() {
    var url = ""
    if (BuildConfig.development) {
        url = "http://127.0.0.1:8080"
    }
    else{
        url = "https://api.calm-ed.com"
    }
    initKoin(
        baseUrl = url,
        iosModule(),
    )
}