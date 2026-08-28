package com.calmed.calmedtics.model

import androidx.room.TypeConverter
import com.calmed.calmedtics.model.raw.PaymentProvider
import com.calmed.calmedtics.model.raw.TickFrequency
import com.calmed.calmedtics.model.raw.TickType
import com.calmed.calmedtics.model.raw.TicDuration

class RoomConverters {
	@TypeConverter
	fun fromTickType(value: TickType?): String? = value?.name

	@TypeConverter
	fun toTickType(value: String?): TickType? = value?.let { TickType.valueOf(it) }

	@TypeConverter
	fun fromTickFrequency(value: TickFrequency?): String? = value?.name

	@TypeConverter
	fun toTickFrequency(value: String?): TickFrequency? = value?.let { TickFrequency.valueOf(it) }

	@TypeConverter
	fun fromTicDuration(value: TicDuration?): String? = value?.name

	@TypeConverter
	fun toTicDuration(value: String?): TicDuration? =
		value?.let { TicDuration.valueOf(it) }

	@TypeConverter
	fun fromPaymentProvider(value: PaymentProvider?): String? = value?.name

	@TypeConverter
	fun toPaymentProvider(value: String?): PaymentProvider? = value?.let { PaymentProvider.valueOf(it) }
}
