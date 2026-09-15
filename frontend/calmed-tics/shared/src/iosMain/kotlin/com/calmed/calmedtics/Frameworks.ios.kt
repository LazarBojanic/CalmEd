package com.calmed.calmedtics

import calmedtics.shared.BuildConfig
import com.calmed.calmedtics.billing.BillingService
import com.calmed.calmedtics.billing.IosBillingService
import com.calmed.calmedtics.database.AppDatabase
import com.calmed.calmedtics.database.getAppDatabase
import com.calmed.calmedtics.database.getDatabaseBuilder
import com.calmed.calmedtics.di.provideSettings
import com.calmed.calmedtics.reminders.IosReminderManager
import com.calmed.calmedtics.reminders.ReminderManager
import com.calmed.calmedtics.service.specification.IVideoDownloadManager
import com.calmed.calmedtics.service.specification.IosVideoDownloadManager
import com.calmed.calmedtics.settings.AppSettings
import com.calmed.calmedtics.store.ITokenDataStore
import com.calmed.calmedtics.store.SettingsTokenDataStore
import com.calmed.calmedtics.store.provideTokenStoreSettings
import com.calmed.calmedtics.util.ImagePickerProvider
import com.calmed.calmedtics.util.IosImagePicker
import com.russhwolf.settings.Settings
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.module

actual fun platformEngine(): HttpClientEngineFactory<*> = Darwin

fun iosModule() = module {
    single<Settings> { provideSettings() }

    single { provideTokenStoreSettings() }
    single<ITokenDataStore> { SettingsTokenDataStore(get()) }

    single<AppDatabase> { getAppDatabase(getDatabaseBuilder()) }
    single { get<AppDatabase>().getUserDao() }
    single { get<AppDatabase>().getUserInfoTicsDao() }
    single { get<AppDatabase>().getExerciseCompletionDao() }
    single { get<AppDatabase>().getProgramExerciseDao() }
    single { get<AppDatabase>().getExerciseGroupDao() }

    single<BillingService> { IosBillingService() }
    single { ImagePickerProvider { IosImagePicker() } }
    single<ReminderManager> { IosReminderManager(get()) }
    single<IVideoDownloadManager> { IosVideoDownloadManager() }
    single { get<IVideoDownloadManager>() as IosVideoDownloadManager }
}

fun initKoinIos() {
    initKoin(
        baseUrl = appBaseUrl,
        development = BuildConfig.development,
        iosModule(),
    )
}
