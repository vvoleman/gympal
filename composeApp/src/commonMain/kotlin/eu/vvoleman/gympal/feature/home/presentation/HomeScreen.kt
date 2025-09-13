package eu.vvoleman.gympal.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import eu.vvoleman.gympal.BuildKonfig
import eu.vvoleman.gympal.app.AppDestinationMapper
import eu.vvoleman.gympal.app.Route
import eu.vvoleman.gympal.common.domain.service.LoggerInterface
import org.koin.compose.koinInject

object HomeScreen : Screen {
    @Composable
    override fun Content() {
        val logger = koinInject<LoggerInterface>()
        val destinationMapper = koinInject<AppDestinationMapper>()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            bottomBar = {
                HomeBottomBar(
                    onHome = { /* already here */ },
                    onWorkouts = { destinationMapper.replaceAll(Route.Workouts) },
                    selected = Route.Home
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to GymPal!!!",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Track your workouts and see your progress.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        logger.info("Navigating to Workouts")
                        destinationMapper.navigate(Route.Workouts)
                    }
                ) {
                    Text("Start a workout, ${BuildKonfig.GP_API_BASE_URL}")
                }
            }
        }
    }
}

@Composable
private fun HomeBottomBar(
    selected: Route,
    onHome: () -> Unit,
    onWorkouts: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == Route.Home,
            onClick = onHome,
            label = { Text("Home") },
            icon = { }
        )
        NavigationBarItem(
            selected = selected == Route.Workouts,
            onClick = onWorkouts,
            label = { Text("Workouts") },
            icon = { }
        )
    }
}