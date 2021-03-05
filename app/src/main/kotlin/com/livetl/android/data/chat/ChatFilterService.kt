package com.livetl.android.data.chat

import com.livetl.android.util.PreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChatFilterService(
    chatService: ChatService,
    private val prefs: PreferencesHelper,
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>>
        get() = _messages

    init {
        chatService.messages
            .onEach {
                _messages.value = it.filter(this::shouldFilter)
            }
            .launchIn(scope)
    }

    private fun shouldFilter(message: ChatMessage): Boolean {
        if (prefs.showModMessages().get() && message.author.isModerator) {
            return true
        }

        if (prefs.allowedUsers().get().contains(message.author.id)) {
            return true
        }

        if (prefs.blockedUsers().get().contains(message.author.id)) {
            return false
        }

        // TODO: proper language filtering
        return message.getTextContent()
            .startsWith("[EN]")
    }
}
