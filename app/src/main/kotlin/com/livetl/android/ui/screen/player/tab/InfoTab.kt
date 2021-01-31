package com.livetl.android.ui.screen.player.tab

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.livetl.android.model.Stream

@Composable
fun InfoTab(
    stream: Stream?
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
    ) {
        if (stream != null) {
            Text(
                text = stream.title,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                text = stream.author,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                text = "Live: ${stream.isLive}",
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(text = stream.shortDescription)
        }
    }
}
