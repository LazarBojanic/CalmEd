package com.calmed.calmedfrontendtourettes.model

import androidx.room.TypeConverter
import com.calmed.calmedfrontendtourettes.model.raw.TickFrequency
import com.calmed.calmedfrontendtourettes.model.raw.TickType

class RoomConverters {
	@TypeConverter
	fun fromTickType(value: TickType?): String? = value?.name

	@TypeConverter
	fun toTickType(value: String?): TickType? = value?.let { TickType.valueOf(it) }

	@TypeConverter
	fun fromTickFrequency(value: TickFrequency?): String? = value?.name

	@TypeConverter
	fun toTickFrequency(value: String?): TickFrequency? = value?.let { TickFrequency.valueOf(it) }
}