package com.calmed.calmedtics

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import calmedtics.composeApp.BuildConfig
import com.calmed.calmedtics.database.AppDatabase
import com.calmed.calmedtics.database.getAppDatabase
import com.calmed.calmedtics.database.getDatabaseBuilder
import com.calmed.calmedtics.store.ITokenDataStore
import com.calmed.calmedtics.store.TokenDataStore
import com.calmed.calmedtics.store.provideTokenDataStore
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.module

actual fun platformEngine(): HttpClientEngineFactory<*> = Darwin

fun iosModule() = module {
    single<DataStore<Preferences>> { provideTokenDataStore() }
    single<ITokenDataStore> { TokenDataStore(get()) }

    single<AppDatabase> { getAppDatabase(getDatabaseBuilder()) }
    single { get<AppDatabase>().getUserDao() }
    single { get<AppDatabase>().getUserInfoTicsDao() }
}

fun initKoinIos() {
    var url = ""
    if (BuildConfig.development) {
        url = "http://192.168.0.35:8080"
    }
    else{
        url = "https://api.calm-ed.com"
    }
    initKoin(
        baseUrl = url,
        iosModule(),
    )
}