package com.calmed.calmedfrontendtourettes.http

import com.calmed.calmedfrontendtourettes.model.dto.TokenDto
import com.calmed.calmedfrontendtourettes.model.dto.request.LoginUserDto
import com.calmed.calmedfrontendtourettes.model.dto.response.MessageDto
import com.calmed.calmedfrontendtourettes.model.raw.Message

interface IAppApi{
	suspend fun getAllMessages(): List<MessageDto>
	suspend fun getMessageById(id: String): MessageDto?
	suspend fun createMessage(message: Message): MessageDto?
	suspend fun updateMessage(message: Message): MessageDto?
	suspend fun deleteMessage(id: String): Boolean

	suspend fun login(loginUserDto: LoginUserDto): TokenDto?
	suspend fun forgotPassword(email: String): String?
	suspend fun logout(): Boolean
	suspend fun refreshToken(): TokenDto?
}