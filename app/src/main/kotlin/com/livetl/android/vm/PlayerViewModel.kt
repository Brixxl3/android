package com.livetl.android.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livetl.android.data.chat.ChatFilterService
import com.livetl.android.data.chat.ChatService
import com.livetl.android.data.chat.MessageAuthor
import com.livetl.android.data.stream.StreamInfo
import com.livetl.android.data.stream.StreamService
import com.livetl.android.ui.screen.player.composable.chat.EmojiCache
import com.livetl.android.util.PreferencesHelper
import com.livetl.android.util.minusAssign
import com.livetl.android.util.plusAssign
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val streamService: StreamService,
    private val chatService: ChatService,
    private val chatFilterService: ChatFilterService,
    private val emojiCache: EmojiCache,
    val prefs: PreferencesHelper,
) : ViewModel() {

    val messages = chatService.messages.shareIn(viewModelScope, SharingStarted.Eagerly, 1)
    val filteredMessages = chatFilterService.messages.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    var videoAttemptedRetries = 0
    var currentSecond: Float = 0f

    fun getEmojiCache() = emojiCache

    fun clearEmojiCache() {
        emojiCache.cache.evictAll()
    }

    fun stopChat() {
        chatService.stop()
    }

    fun stopChatFilter() {
        chatFilterService.stop()
    }

    fun seekTo(videoId: String, second: Long) {
        chatService.seekTo(videoId, second)
    }

    suspend fun getStreamInfo(videoId: String): StreamInfo {
        return streamService.getStreamInfo(videoId)
    }

    suspend fun loadChat(videoId: String, isLive: Boolean) {
        chatService.load(videoId, isLive)
    }

    fun allowUser(author: MessageAuthor) {
        prefs.allowedUsers() += author.toPrefItem()
        prefs.blockedUsers() -= author.toPrefItem()
    }

    fun blockUser(author: MessageAuthor) {
        prefs.blockedUsers() += author.toPrefItem()
        prefs.allowedUsers() -= author.toPrefItem()
    }
}
