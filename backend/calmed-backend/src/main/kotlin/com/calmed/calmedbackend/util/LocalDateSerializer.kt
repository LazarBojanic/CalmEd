package com.calmed.calmedbackend.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

object LocalDateSerializer : KSerializer<LocalDate> {
	override val descriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
	override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString()) // yyyy-MM-dd
	override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}