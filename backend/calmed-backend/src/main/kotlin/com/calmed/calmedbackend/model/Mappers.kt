package com.calmed.calmedbackend.model

import com.calmed.calmedbackend.model.dto.response.MessageDto
import com.calmed.calmedbackend.model.joined.MessageJoined
import com.calmed.calmedbackend.model.raw.message.Message
import com.calmed.calmedbackend.model.raw.message.MessageEntity

enum class MapMode {
	CREATE, UPDATE
}

fun MessageEntity.toRaw(): Message = Message(
	id = this.id.value,
	text = this.text,
	createdAt = this.createdAt,
	updatedAt = this.updatedAt
)

fun MessageEntity.setFrom(d: Message, mode: MapMode) {
	text = d.text
	createdAt = d.createdAt
	updatedAt = d.updatedAt
	when (mode) {
		MapMode.CREATE -> {
			createdAt = d.createdAt
			updatedAt = d.updatedAt
		}

		MapMode.UPDATE -> {
			updatedAt = d.updatedAt
		}
	}
}

fun Message.join(): MessageJoined {
	return MessageJoined(
		id = this.id,
		text = this.text,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun MessageJoined.toDto(): MessageDto {
	return MessageDto(
		id = this.id,
		text = this.text,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}
