package com.livetl.android.ui.screen.home.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.livetl.android.R
import com.livetl.android.data.feed.Channel
import com.livetl.android.data.feed.Stream
import com.livetl.android.util.escapeHtmlEntities
import com.livetl.android.util.toDate
import com.livetl.android.util.toRelativeString

@Composable
fun Stream(
    modifier: Modifier = Modifier,
    stream: Stream,
    @StringRes timestampFormatStringRes: Int?,
    timestampSupplier: (Stream) -> String?,
    onClick: (Stream) -> Unit,
    onLongClick: (Stream) -> Unit,
) {
    Box(modifier = Modifier.height(IntrinsicSize.Min)) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(stream.thumbnail)
                .transformations()
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .alpha(0.1f)
                .blur(5.dp),
            contentScale = ContentScale.Crop,
        )

        Row(
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onClick(stream) },
                    onLongClick = { onLongClick(stream) },
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .requiredHeight(48.dp),
        ) {
            AsyncImage(
                model = stream.channel.photo,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(CircleShape),
            )

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stream.title.escapeHtmlEntities(),
                    modifier = Modifier.padding(start = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelSmall) {
                        Text(
                            stream.channel.name,
                            modifier = Modifier
                                .padding(start = 8.dp, end = 8.dp)
                                .weight(1f),
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        timestampSupplier(stream)?.let {
                            val relativeDateString = it.toDate().toRelativeString()
                            val timestampFormatString =
                                timestampFormatStringRes?.let { res -> stringResource(res) }
                            val timestampString =
                                timestampFormatString?.format(relativeDateString)
                                    ?: relativeDateString
                            Text(
                                timestampString,
                                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun StreamPreview() {
    Column {
        Stream(
            stream = Stream(
                id = "123",
                title = "Some very, extremely, quite long long long title for testing wow",
                description = "",
                status = "past",
                start_scheduled = "2020-01-01T00:00:00.000Z",
                start_actual = "2020-01-01T00:01:12.000Z",
                channel = Channel(
                    name = "Wow Such YouTube Channel",
                    org = "Hololive",
                    photo = "",
                ),
            ),
            timestampFormatStringRes = R.string.started_streaming,
            timestampSupplier = { "2020-01-01T00:01:12.000Z" },
            onClick = {},
            onLongClick = {},
        )
        Stream(
            stream = Stream(
                id = "123",
                title = "Short title",
                description = "",
                status = "upcoming",
                start_scheduled = "2030-01-01T00:01:12.000Z",
                channel = Channel(
                    name = "Smol Ch",
                    org = "Hololive",
                    photo = "",
                ),
            ),
            timestampFormatStringRes = null,
            timestampSupplier = { "2030-01-01T00:01:12.000Z" },
            onClick = {},
            onLongClick = {},
        )
    }
}
