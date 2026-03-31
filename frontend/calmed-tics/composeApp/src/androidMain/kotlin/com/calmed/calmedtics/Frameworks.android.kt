package com.calmed.calmedtics

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.calmed.calmedtics.database.AppDatabase
import com.calmed.calmedtics.database.getAppDatabase
import com.calmed.calmedtics.database.getDatabaseBuilder
import com.calmed.calmedtics.store.ITokenDataStore
import com.calmed.calmedtics.store.TokenDataStore
import com.calmed.calmedtics.store.provideTokenDataStore
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

actual fun platformEngine(): HttpClientEngineFactory<*> = OkHttp

fun androidModule(appContext: Context) = module {
    single<DataStore<Preferences>> { provideTokenDataStore(appContext) }
    single<ITokenDataStore> { TokenDataStore(get()) }

    single<AppDatabase> { getAppDatabase(getDatabaseBuilder(appContext)) }
    single { get<AppDatabase>().getUserDao() }
    single { get<AppDatabase>().getUserInfoTicsDao() }
}