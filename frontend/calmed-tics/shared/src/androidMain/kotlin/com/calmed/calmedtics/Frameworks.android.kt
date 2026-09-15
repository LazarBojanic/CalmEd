package com.calmed.calmedtics

import android.content.Context
import com.calmed.calmedtics.database.AppDatabase
import com.calmed.calmedtics.database.getAppDatabase
import com.calmed.calmedtics.database.getDatabaseBuilder
import com.calmed.calmedtics.store.ITokenDataStore
import com.calmed.calmedtics.store.SettingsTokenDataStore
import com.calmed.calmedtics.store.provideTokenStoreSettings
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

actual fun platformEngine(): HttpClientEngineFactory<*> = OkHttp

fun androidModule(appContext: Context) = module {
    single { provideTokenStoreSettings(appContext) }
    single<ITokenDataStore> { SettingsTokenDataStore(get()) }

    single<AppDatabase> { getAppDatabase(getDatabaseBuilder(appContext)) }
    single { get<AppDatabase>().getUserDao() }
    single { get<AppDatabase>().getUserInfoTicsDao() }
    single { get<AppDatabase>().getExerciseCompletionDao() }
    single { get<AppDatabase>().getProgramExerciseDao() }
    single { get<AppDatabase>().getExerciseGroupDao() }
}