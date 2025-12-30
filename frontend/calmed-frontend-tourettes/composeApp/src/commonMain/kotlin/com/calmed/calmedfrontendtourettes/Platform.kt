package com.calmed.calmedfrontendtourettes

interface Platform {
	val name: String
}

expect fun getPlatform(): Platform