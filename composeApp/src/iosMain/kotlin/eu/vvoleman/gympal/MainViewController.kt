package eu.vvoleman.gympal

import androidx.compose.ui.window.ComposeUIViewController
import eu.vvoleman.gympal.app.App
import eu.vvoleman.gympal.di.initKoin
import eu.vvoleman.gympal.di.platformModule

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}