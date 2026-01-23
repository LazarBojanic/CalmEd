package com.calmed.calmedfrontendtourettes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.calmed.calmedfrontendtourettes.auth.setGoogleAuthActivityProvider

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setGoogleAuthActivityProvider { this }

		setContent {
			App()
		}
	}
}