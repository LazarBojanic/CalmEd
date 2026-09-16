package com.calmed.calmedtics.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import org.koin.compose.viewmodel.koinViewModel

val LocalAppViewModelStoreOwner = staticCompositionLocalOf<ViewModelStoreOwner> {
	error("LocalAppViewModelStoreOwner was not provided")
}

@Composable
inline fun <reified T : ViewModel> koinAppViewModel(): T =
	koinViewModel(viewModelStoreOwner = LocalAppViewModelStoreOwner.current)
