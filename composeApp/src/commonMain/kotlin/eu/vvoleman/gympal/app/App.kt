package eu.vvoleman.gympal.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import eu.vvoleman.gympal.common.domain.service.LoggerInterface
import eu.vvoleman.gympal.feature.home.presentation.HomeScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.getKoin
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    MaterialTheme {
        val koin = getKoin()
        val logger = koinInject<LoggerInterface>()

        Navigator(HomeScreen) { navigator ->
            LaunchedEffect(Unit) {
                val appDestinationMapper = koin.get<AppDestinationMapper>()
                appDestinationMapper.setNavigator(navigator)
            }
            CurrentScreen()
        }
    }
}