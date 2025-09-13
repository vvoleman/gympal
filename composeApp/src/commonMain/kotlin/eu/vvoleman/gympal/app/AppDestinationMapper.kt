package eu.vvoleman.gympal.app

import androidx.navigation.NavHostController

class AppDestinationMapper {

    private var navController: NavHostController? = null

    fun navigate(destination: Route) {
        navController?.navigate(destination)
    }

    fun setNavController(navController: NavHostController) {
        this.navController = navController
    }
}
