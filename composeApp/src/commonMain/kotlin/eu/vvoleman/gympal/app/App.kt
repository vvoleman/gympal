package eu.vvoleman.gympal.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import eu.vvoleman.gympal.common.domain.service.LoggerInterface
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    MaterialTheme {
        val koin = getKoin()
        val navController = rememberNavController()
        val logger = koinInject<LoggerInterface>()

//        LaunchedEffect(user) {
//            if (user is AuthState.None) {
//                return@LaunchedEffect
//            }
//            val currentDestination = navController.currentBackStackEntry?.destination
//            val isOnSignIn = currentDestination?.hasRoute<Route.UnauthenticatedGraph>() ?: false
//
//            if (user is AuthState.Unauthenticated && !isOnSignIn) {
//                navController.navigate(Route.UnauthenticatedGraph) {
//                    popUpTo(0) { inclusive = true }
//                    launchSingleTop = true
//                }
//            } else if (user is AuthState.Authenticated && isOnSignIn) {
//                navController.navigate(Route.AuthenticatedGraph) {
//                    popUpTo(0) { inclusive = true }
//                    launchSingleTop = true
//                }
//            }
//        }

        LaunchedEffect(Unit) {
            val appDestinationMapper = koin.get<AppDestinationMapper>()
            appDestinationMapper.setNavController(navController)
        }


        NavHost(navController = navController, startDestination = Route.Home) {
            composable<Route.Home> { backStackEntry ->
//                // This is a placeholder for the loading state
                Text("Home", modifier = Modifier)
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) { it ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                    ) {
                        Button(

                            onClick = {
                                logger.info("Navigating to Workouts")
                                navController.navigate(Route.Workouts)
                            },
                            modifier = Modifier
                        ) {
                            Text("Go to Workouts")
                        }
                    }
                }
            }
            composable<Route.Workouts> { backStackEntry ->
                Button(
                    onClick = {
                        logger.info("Navigating to Home")
                        navController.navigate(Route.Home) {
                            popUpTo(Route.Home) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                ) {
                    Text("Go to Home")
                }
            }
        }
    }
}