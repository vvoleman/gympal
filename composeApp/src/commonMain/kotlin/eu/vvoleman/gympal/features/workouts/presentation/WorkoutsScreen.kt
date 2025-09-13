package eu.vvoleman.gympal.features.workouts.presentation

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
import eu.vvoleman.gympal.app.AppDestinationMapper
import eu.vvoleman.gympal.app.Route
import eu.vvoleman.gympal.common.domain.service.LoggerInterface
import org.koin.compose.koinInject

object WorkoutsScreen : Screen {
    @Composable
    override fun Content() {
        val logger = koinInject<LoggerInterface>()
        val destinationMapper = koinInject<AppDestinationMapper>()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            bottomBar = {
                WorkoutsBottomBar(
                    onHome = { destinationMapper.replaceAll(Route.Home) },
                    onWorkouts = { /* already here */ },
                    selected = Route.Workouts
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
                    text = "Your Workouts",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "No workouts yet. Create your first routine!",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        logger.info("Navigating to Home")
                        destinationMapper.replaceAll(Route.Home)
                    }
                ) {
                    Text("Back to Home")
                }
            }
        }
    }
}

@Composable
private fun WorkoutsBottomBar(
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