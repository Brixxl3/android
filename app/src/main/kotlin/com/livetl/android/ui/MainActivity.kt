package com.livetl.android.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import com.livetl.android.ui.navigation.Route
import com.livetl.android.ui.navigation.mainNavHost
import com.livetl.android.ui.navigation.navigateToPlayer
import com.livetl.android.ui.theme.LiveTLTheme
import com.livetl.android.util.AppPreferences
import com.livetl.android.util.waitUntil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var prefs: AppPreferences

    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val startRoute = when (prefs.showWelcomeScreen().get()) {
            true -> Route.Welcome
            false -> Route.Home
        }

        setContent {
            LiveTLTheme {
                navController = mainNavHost(
                    startRoute = startRoute,
                )
            }
        }

        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        when (intent.action) {
            Intent.ACTION_VIEW -> handleVideoIntent(intent.dataString)

            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    handleVideoIntent(intent.getStringExtra(Intent.EXTRA_TEXT))
                }
            }
        }
    }

    private fun handleVideoIntent(data: String?) {
        data?.let {
            lifecycleScope.launch {
                // The app might take some time to actually load up
                waitUntil({ navController != null }) {
                    navController?.navigateToPlayer(it)
                }
            }
        }
    }
}
