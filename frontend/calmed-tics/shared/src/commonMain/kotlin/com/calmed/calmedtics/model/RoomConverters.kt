package com.calmed.calmedtics.model

import androidx.room.TypeConverter
import com.calmed.calmedtics.model.raw.PaymentProvider
import com.calmed.calmedtics.model.raw.TicFrequency
import com.calmed.calmedtics.model.raw.TicType
import com.calmed.calmedtics.model.raw.TicDuration

class RoomConverters {
	@TypeConverter
	fun fromTicType(value: TicType?): String? = value?.name

	@TypeConverter
	fun toTicType(value: String?): TicType? = value?.let { TicType.valueOf(it) }

	@TypeConverter
	fun fromTicFrequency(value: TicFrequency?): String? = value?.name

	@TypeConverter
	fun toTicFrequency(value: String?): TicFrequency? = value?.let { TicFrequency.valueOf(it) }

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
