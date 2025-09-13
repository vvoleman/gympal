package eu.vvoleman.gympal.app

// Type-safe Route DTOs decoupled from UI implementation
sealed interface Route {
    data object Home : Route
    data object Workouts : Route
}