package com.calmed.calmedtics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.download_completed
import calmedtics.shared.generated.resources.download_completed_title
import calmedtics.shared.generated.resources.download_failed_title
import calmedtics.shared.generated.resources.status_failed
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.calmed.calmedtics.logging.isDevelopment
import com.calmed.calmedtics.navigation.AppNavigator
import com.calmed.calmedtics.navigation.AppRoute
import com.calmed.calmedtics.navigation.LocalAppViewModelStoreOwner
import com.calmed.calmedtics.navigation.appRouteConfig
import com.calmed.calmedtics.service.specification.DownloadEventType
import com.calmed.calmedtics.service.specification.IVideoDownloadManager
import com.calmed.calmedtics.theme.AppTheme
import com.calmed.calmedtics.ui.component.ToastCenter
import com.calmed.calmedtics.ui.component.ToastKind
import io.ktor.client.HttpClient
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalCoilApi::class, KoinExperimentalAPI::class)
@Composable
fun App() {
	val httpClient: HttpClient = koinInject()
	setSingletonImageLoaderFactory { context ->
		ImageLoader.Builder(context)
			.components {
				add(KtorNetworkFetcherFactory(httpClient))
			}
			.crossfade(true)
			.apply { if (isDevelopment) logger(DebugLogger()) }
			.build()
	}

	val navigator: AppNavigator = koinInject()
	val videoDownloadManager: IVideoDownloadManager = koinInject()

	val hostViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
		"No host ViewModelStoreOwner was provided"
	}

	val backStack = rememberNavBackStack(appRouteConfig, AppRoute.Splash)
	navigator.attach(backStack)
	val entryProvider = koinEntryProvider<NavKey>()

	AppTheme {
		LaunchedEffect(Unit) {
			videoDownloadManager.events.collect { event ->
				val title = event.title?.takeIf { it.isNotBlank() }
				when (event.type) {
					DownloadEventType.Completed ->
						if (title != null) {
							ToastCenter.show(
								Res.string.download_completed_title,
								title,
								kind = ToastKind.Success
							)
						} else {
							ToastCenter.show(
								Res.string.download_completed,
								kind = ToastKind.Success
							)
						}

					DownloadEventType.Failed ->
						if (title != null) {
							ToastCenter.show(
								Res.string.download_failed_title,
								title,
								kind = ToastKind.Error
							)
						} else {
							ToastCenter.show(
								Res.string.status_failed,
								kind = ToastKind.Error
							)
						}

					DownloadEventType.Expired ->
						if (title != null) {
							ToastCenter.show(
								Res.string.download_failed_title,
								title,
								kind = ToastKind.Error
							)
						} else {
							ToastCenter.show(
								Res.string.status_failed,
								kind = ToastKind.Error
							)
						}
				}
			}
		}

		CompositionLocalProvider(LocalAppViewModelStoreOwner provides hostViewModelStoreOwner) {
			NavDisplay(
				backStack = backStack,
				onBack = { navigator.goBack() },
				entryDecorators = listOf(
					rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
					rememberViewModelStoreNavEntryDecorator<NavKey>(hostViewModelStoreOwner)
				),
				entryProvider = entryProvider
			)
		}
	}
}
