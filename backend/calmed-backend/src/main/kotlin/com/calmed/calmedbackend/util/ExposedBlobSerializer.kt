package com.calmed.calmedbackend.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob

object ExposedBlobSerializer : KSerializer<ExposedBlob> {
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ExposedBlob", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: ExposedBlob) = encoder.encodeString(value.bytes.toString(Charsets.UTF_8))

	override fun deserialize(decoder: Decoder): ExposedBlob = ExposedBlob(decoder.decodeString().toByteArray())
}