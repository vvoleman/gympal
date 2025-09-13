package eu.vvoleman.gympal.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import eu.vvoleman.gympal.app.AppDestinationMapper
import eu.vvoleman.gympal.app.Route
import eu.vvoleman.gympal.common.domain.entity.UserModel
import eu.vvoleman.gympal.common.domain.repository.user.CreateUserRepository
import eu.vvoleman.gympal.common.domain.repository.user.DeleteUserRepository
import eu.vvoleman.gympal.common.domain.repository.user.GetAllUsersRepository
import eu.vvoleman.gympal.common.domain.service.LogLevel
import eu.vvoleman.gympal.common.domain.service.LoggerInterface
import eu.vvoleman.gympal.common.domain.util.currentLocalDateTime
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

object HomeScreen : Screen {
    @Composable
    override fun Content() {
        val logger = koinInject<LoggerInterface>()
        val destinationMapper = koinInject<AppDestinationMapper>()
        val getAllUsersRepository = koinInject<GetAllUsersRepository>()
        val createUserRepository = koinInject<CreateUserRepository>()
        val deleteUserRepository = koinInject<DeleteUserRepository>()

        val userFlow = remember { getAllUsersRepository.getAllUsersFlow() }
        val users by userFlow.collectAsState(initial = emptyList())

        HomeView(
            users = users,
            onLog = { message, level -> logger.log(level, message, ) },
            onNavigate = { route -> destinationMapper.replaceAll(route) },
            onBottomBarChange = { route -> destinationMapper.replaceAll(route) },
            onUserCreated = {
                createUserRepository.createUser(it)
                logger.log(LogLevel.INFO, "User created: ${it.name}")
            },
            onUserDeleted = {
                deleteUserRepository.deleteUser(it.id)
                logger.log(LogLevel.INFO, "User deleted: ${it.name}")
            }
        )
    }
}

@Composable
@Preview
fun HomeScreenPreview() {
    HomeView(
        users = listOf(
            UserModel(id = "123", name = "Alice", email = "test@test.cz"),
            UserModel(id = "345", name = "Bob", email = "bob@test.cz"),
        ),
        onLog = { _, _ -> },
        onNavigate = {},
        onBottomBarChange = {},
        onUserCreated = {},
        onUserDeleted = {},
    )
}

@Composable
fun HomeView(
    users: List<UserModel>,
    onLog: (String, LogLevel) -> Unit,
    onNavigate: (Route) -> Unit,
    onBottomBarChange: (Route) -> Unit,
    onUserCreated: suspend (UserModel) -> Unit,
    onUserDeleted: suspend (UserModel) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        floatingActionButton = {
            // + button round
            FloatingActionButton(onClick = {
                onLog("FAB clicked", LogLevel.INFO)
                val newUser = UserModel(
                    id = currentLocalDateTime().toString(),
                    name = "User${users.size + 1}",
                    email = "user${users.size  + 1}@test.com",
                )
                coroutineScope.launch {
                    onUserCreated(newUser)
                }
            }) {
                Text("+")
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        bottomBar = {
            HomeBottomBar(
                onHome = { /* already here */ },
                onWorkouts = {
                    onLog("Navigating to Workouts", LogLevel.INFO)
                    onNavigate(Route.Workouts)
                },
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
                text = "Welcome to GymPal!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Track your workouts and see your progress.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            // List all users for demo purposes
            users.forEach { user ->
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(horizontal = 60.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "👤 ${user.email}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        // remove button
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = {
                            coroutineScope.launch {
                                onUserDeleted(user)
                            }
                        }) {
                            Text("❌")
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
            if (users.isEmpty()) {
                Text(
                    text = "No users found.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(4.dp))
            }
            Button(
                onClick = {
                    onLog("Navigating to Workouts", LogLevel.INFO)
                    onNavigate(Route.Workouts)
                }
            ) {
                Text("Start a workout")
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