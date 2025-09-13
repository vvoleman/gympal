package eu.vvoleman.gympal.app

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import eu.vvoleman.gympal.features.home.presentation.HomeScreen
import eu.vvoleman.gympal.features.workouts.presentation.WorkoutsScreen

class AppDestinationMapper {

    private var navigator: Navigator? = null

    // DTO-based navigation
    fun navigate(route: Route) {
        navigator?.push(map(route))
    }

    fun replaceAll(route: Route) {
        navigator?.replaceAll(map(route))
    }

    // Screen-based navigation (kept for flexibility)
    fun push(destination: Screen) {
        navigator?.push(destination)
    }

    fun replaceAll(destination: Screen) {
        navigator?.replaceAll(destination)
    }

    fun setNavigator(navigator: Navigator) {
        this.navigator = navigator
    }

    private fun map(route: Route): Screen = when (route) {
        Route.Home -> HomeScreen
        Route.Workouts -> WorkoutsScreen
    }
}
