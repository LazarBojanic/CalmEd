package com.calmed.calmedtics

import android.content.Context
import com.calmed.calmedtics.billing.AndroidBillingService
import com.calmed.calmedtics.billing.BillingService
import com.calmed.calmedtics.cast.AndroidCastController
import com.calmed.calmedtics.database.AppDatabase
import com.calmed.calmedtics.database.getAppDatabase
import com.calmed.calmedtics.database.getDatabaseBuilder
import com.calmed.calmedtics.di.AndroidActivityHolder
import com.calmed.calmedtics.di.provideSettings
import com.calmed.calmedtics.reminders.AndroidReminderManager
import com.calmed.calmedtics.reminders.ReminderManager
import com.calmed.calmedtics.service.specification.IVideoDownloadManager
import com.calmed.calmedtics.settings.AppSettings
import com.calmed.calmedtics.store.ITokenDataStore
import com.calmed.calmedtics.store.SettingsTokenDataStore
import com.calmed.calmedtics.store.provideTokenStoreSettings
import com.calmed.calmedtics.util.AndroidImagePicker
import com.calmed.calmedtics.util.ImagePickerProvider
import com.calmed.calmedtics.video.download.MuxVideoDownloadManager
import com.russhwolf.settings.Settings
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp
import androidx.media3.common.util.UnstableApi
import org.koin.dsl.module

actual fun platformEngine(): HttpClientEngineFactory<*> = OkHttp

@OptIn(UnstableApi::class)
fun androidModule(appContext: Context) = module {
    single<Context> { appContext }
    single<Settings> { provideSettings(appContext) }

    single { provideTokenStoreSettings(appContext) }
    single<ITokenDataStore> { SettingsTokenDataStore(get()) }

    single<AppDatabase> { getAppDatabase(getDatabaseBuilder(appContext)) }
    single { get<AppDatabase>().getUserDao() }
    single { get<AppDatabase>().getUserInfoTicsDao() }
    single { get<AppDatabase>().getExerciseCompletionDao() }
    single { get<AppDatabase>().getProgramExerciseDao() }
    single { get<AppDatabase>().getExerciseGroupDao() }

    single<BillingService> {
        AndroidBillingService(appContext, activityProvider = { AndroidActivityHolder.current() })
    }
    single {
        ImagePickerProvider {
            AndroidImagePicker(
                AndroidActivityHolder.current()
                    ?: error("No active Activity available for ImagePicker")
            )
        }
    }
    single<ReminderManager> { AndroidReminderManager(appContext, get()) }
    single { AndroidCastController(appContext) }
    single<IVideoDownloadManager> {
        MuxVideoDownloadManager(appContext) { get<AppSettings>().isDownloadWifiOnly() }
    }
}
