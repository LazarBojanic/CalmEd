package com.calmed.calmedfrontendtourettes

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.calmed.calmedfrontendtourettes.database.AppDatabase
import com.calmed.calmedfrontendtourettes.database.getAppDatabase
import com.calmed.calmedfrontendtourettes.database.getDatabaseBuilder
import com.calmed.calmedfrontendtourettes.store.ITokenDataStore
import com.calmed.calmedfrontendtourettes.store.TokenDataStore
import com.calmed.calmedfrontendtourettes.store.provideTokenDataStore
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

actual fun platformEngine(): HttpClientEngineFactory<*> = OkHttp

fun androidModule(appContext: Context) = module {
	single { getAppDatabase(getDatabaseBuilder(appContext)) }
	single<DataStore<Preferences>> {
		provideTokenDataStore(appContext)
	}
	single<ITokenDataStore> { TokenDataStore(get()) }
	single { get<AppDatabase>().getMessageDao() }

}