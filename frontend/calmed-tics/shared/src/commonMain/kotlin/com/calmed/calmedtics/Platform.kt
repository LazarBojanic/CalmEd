package com.calmed.calmedtics

interface Platform {
	val name: String
}

expect fun getPlatform(): Platform