package com.livetl.android.ui.screen.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.livetl.android.data.feed.StreamStatus
import com.livetl.android.data.stream.StreamRepository
import com.livetl.android.ui.screen.home.tab.StreamsTabViewModel
import com.livetl.android.util.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val streamRepository: StreamRepository, val prefs: AppPreferences) :
    ViewModel() {
    val tabs =
        StreamStatus.entries.map {
            it to
                StreamsTabViewModel(
                    streamRepository = streamRepository,
                    prefs = prefs,
                    status = it,
                )
        }

    val state = MutableStateFlow(State())

    fun showOpenVideoDialog() {
        state.update { it.copy(showOpenVideoDialog = true) }
    }

    fun hideOpenVideoDialog() {
        state.update { it.copy(showOpenVideoDialog = false) }
    }

    fun setOpenVideoUrl(value: String) {
        state.update { it.copy(openVideoUrl = value) }
    }

    @Immutable
    data class State(val showOpenVideoDialog: Boolean = false, val openVideoUrl: String = "")
}
