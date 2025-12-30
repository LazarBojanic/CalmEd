package com.calmed.calmedfrontendtourettes.viewmodel

import com.calmed.calmedfrontendtourettes.model.joined.MessageJoined
import com.calmed.calmedfrontendtourettes.service.specification.IMessageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WelcomeViewModel(
	private val messageService: IMessageService
) {

	private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

	private val _messages = MutableStateFlow<List<MessageJoined>>(emptyList())
	val messages: StateFlow<List<MessageJoined>> = _messages

	private val _isLoading = MutableStateFlow(true)
	val isLoading: StateFlow<Boolean> = _isLoading

	private val _error = MutableStateFlow<String?>(null)
	val error: StateFlow<String?> = _error

	init {
		loadMessages()
	}

	private fun loadMessages() {
		viewModelScope.launch {
			try {
				_isLoading.value = true
				_messages.value = messageService.requestGetAll()
			} catch (e: Exception) {
				_error.value = e.message ?: "Unknown error"
			} finally {
				_isLoading.value = false
			}
		}
	}
}