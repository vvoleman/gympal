package eu.vvoleman.gympal

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import eu.vvoleman.gympal.app.App
import eu.vvoleman.gympal.di.initKoin

fun main() = application {
    initKoin()

    Window(
        onCloseRequest = ::exitApplication,
        title = "GymPal",
    ) {
        App()
    }
}