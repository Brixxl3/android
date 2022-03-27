package com.livetl.android.data.stream

import com.livetl.android.data.feed.FeedService
import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import timber.log.Timber
import javax.inject.Inject

class StreamService @Inject constructor(
    private val feedService: FeedService,
    private val videoIdParser: VideoIdParser,
    private val client: HttpClient,
) {

    suspend fun getStreamInfo(pageUrl: String): StreamInfo {
        val videoId = videoIdParser.getVideoId(pageUrl)

        return try {
            Timber.d("Fetching stream: $videoId")
            val stream = feedService.getVideoInfo(videoId)

            val chatContinuation: String? = when (stream.isLive) {
                true -> null
                false -> getChatContinuation(stream.id)
            }

            StreamInfo(
                videoId = stream.id,
                title = stream.title,
                author = stream.channel.name,
                shortDescription = stream.description,
                isLive = stream.isLive,
                chatContinuation = chatContinuation,
            )
        } catch (e: ClientRequestException) {
            Timber.e(e, "Error getting video info for $videoId from HoloDex")

            StreamInfo(
                videoId = videoId,
                title = "",
                author = "",
                shortDescription = "",
                isLive = false,
                chatContinuation = getChatContinuation(videoId),
            )
        }
    }

    private suspend fun getChatContinuation(videoId: String): String? {
        val result = client.get<HttpResponse>("https://www.youtube.com/watch?v=$videoId") {
            headers {
                set("User-Agent", USER_AGENT)
            }
        }
        val matches = CHAT_CONTINUATION_PATTERN.matcher(result.readText())
        return if (matches.find()) {
            matches.group(1)
        } else {
            Timber.w("Chat continuation found for $videoId")
            null
        }
    }
}

const val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36 Edg/91.0.864.54"
private val CHAT_CONTINUATION_PATTERN by lazy { """continuation":"(\w+)"""".toPattern() }
