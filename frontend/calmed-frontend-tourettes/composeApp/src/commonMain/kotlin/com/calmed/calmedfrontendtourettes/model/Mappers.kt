package com.calmed.calmedfrontendtourettes.model

import com.calmed.calmedfrontendtourettes.model.dto.response.MessageDto
import com.calmed.calmedfrontendtourettes.model.joined.MessageJoined
import com.calmed.calmedfrontendtourettes.model.raw.Message
import com.calmed.calmedfrontendtourettes.model.raw.MessageEntity

fun Message.join(): MessageJoined{
	return MessageJoined(
		id = this.id,
		text = this.text,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt,
	)
}

fun MessageJoined.toRaw(): Message{
	return Message(
		id = this.id,
		text = this.text,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt,
	)
}
fun MessageJoined.toDto(): MessageDto{
	return MessageDto(
		id = this.id,
		text = this.text,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt,
	)
}
fun MessageDto.toRaw(): Message{
	return Message(
		id = this.id,
		text = this.text,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt,
	)
}

fun MessageDto.toJoined(): MessageJoined {
	return MessageJoined(
		id = this.id,
		text = this.text,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt,
	)
}
fun MessageEntity.join(): MessageJoined{
	return MessageJoined(
		id = this.id,
		text = this.text,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt,
	)
}
fun MessageJoined.toEntity(): MessageEntity{
	return MessageEntity(
		id = this.id,
		text = this.text,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt,
	)
}