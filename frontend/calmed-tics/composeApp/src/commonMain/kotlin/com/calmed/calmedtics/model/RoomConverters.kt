package com.calmed.calmedtics.model

import androidx.room.TypeConverter
import com.calmed.calmedtics.model.raw.PaymentType
import com.calmed.calmedtics.model.raw.TickFrequency
import com.calmed.calmedtics.model.raw.TickType

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
	fun fromPaymentType(value: PaymentType?): String? = value?.name

	@TypeConverter
	fun toPaymentType(value: String?): PaymentType? = value?.let { PaymentType.valueOf(it) }
}
