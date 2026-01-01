package com.calmed.calmedbackend.util

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object HttpStatusCodeSerializer : KSerializer<HttpStatusCode> {
	override val descriptor: SerialDescriptor =
		PrimitiveSerialDescriptor("HttpStatusCode", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: HttpStatusCode) {
		val serialized = "code: ${value.value}, description: ${value.description}"
		encoder.encodeString(serialized)
	}

	override fun deserialize(decoder: Decoder): HttpStatusCode {
		val decoded = decoder.decodeString()
		val codePart = decoded.substringAfter("code: ").substringBefore(",").trim()
		val code = codePart.toIntOrNull() ?: throw IllegalArgumentException("Invalid code in HttpStatusCode string: $decoded")
		return HttpStatusCode.fromValue(code)
	}
}