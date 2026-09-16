package com.calmed.calmedtics.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

class AppNavigator {
	private var backStack: NavBackStack<NavKey>? = null

	fun attach(backStack: NavBackStack<NavKey>) {
		this.backStack = backStack
	}

	private fun requireBackStack(): NavBackStack<NavKey> =
		backStack ?: error("AppNavigator back stack has not been attached")

	fun goTo(route: NavKey) {
		requireBackStack().add(route)
	}

	fun resetTo(route: NavKey) {
		val stack = requireBackStack()
		stack.clear()
		stack.add(route)
	}

	fun goBack() {
		val stack = requireBackStack()
		if (stack.size > 1) stack.removeLastOrNull()
	}
}
