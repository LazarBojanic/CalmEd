package com.calmed.calmedtics.viewmodel

data class SessionRouting(
	val confirmOverEighteen: Boolean,
	val isOnboarded: Boolean,
	val isPaid: Boolean,
	val shouldShowWelcomeVideo: Boolean,
	val shouldShowCourseOverview: Boolean,
)
