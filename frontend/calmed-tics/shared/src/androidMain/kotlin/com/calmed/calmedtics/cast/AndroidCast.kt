package com.calmed.calmedtics.cast

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.cast.Cast
import androidx.media3.cast.CastParams
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.CastMediaControlIntent

@OptIn(UnstableApi::class)
fun initializeCast(context: Context) {
	val params = CastParams.Builder()
		.setReceiverApplicationId(
			CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
		)
		.build()

	Cast.getSingletonInstance(context).initialize(params)
}
