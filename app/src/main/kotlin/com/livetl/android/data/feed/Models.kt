package com.livetl.android.data.feed

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Feed(
    val live: List<Stream>,
    val upcoming: List<Stream>,
    val ended: List<Stream>,
)

@Immutable
@Serializable
data class Stream(
    val yt_video_key: String,
    val title: String,
    val live_schedule: String,
    val live_start: String?,
    val live_end: String?,
    val live_viewers: Int?,
    val channel: Channel,
) {
//    val thumbnail = "https://img.youtube.com/vi/$yt_video_key/hqdefault.jpg"
}

@Immutable
@Serializable
data class Channel(
    val name: String,
    val photo: String,
)