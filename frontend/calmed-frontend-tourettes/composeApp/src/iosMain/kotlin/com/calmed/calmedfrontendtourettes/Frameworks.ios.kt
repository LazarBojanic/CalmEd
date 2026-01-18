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
    single<DataStore<Preferences>> { provideTokenDataStore() }
    single<ITokenDataStore> { TokenDataStore(get()) }

    single<AppDatabase> { getAppDatabase(getDatabaseBuilder()) }
    single { get<AppDatabase>().getUserDao() }
    single { get<AppDatabase>().getUserInfoTourettesDao() }
}

fun initKoinIos() {
    val dev = true
    val url = if (dev) "http://127.0.0.1:8080" else "https://srv1092316.hstgr.cloud"
    initKoin(
        baseUrl = url,
        iosModule(),
    )
}