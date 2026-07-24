package it.bailettitommaso.fairly.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.bailettitommaso.fairly.ui.HomeScreen
import it.bailettitommaso.fairly.ui.auth.LoginScreen
import it.bailettitommaso.fairly.ui.boot.BootScreen
import it.bailettitommaso.fairly.ui.boot.BootState
import it.bailettitommaso.fairly.ui.boot.BootViewModel
import it.bailettitommaso.fairly.ui.offline.ConnectivityViewModel
import it.bailettitommaso.fairly.ui.offline.OfflineScreen
import it.bailettitommaso.fairly.util.ConnectivityObserver

@Composable
fun FairlyNavGraph(bootViewModel: BootViewModel) {
    val navController = rememberNavController()
    val bootState by bootViewModel.state.collectAsStateWithLifecycle()

    // The boot check owns the top-level routing decision.
    LaunchedEffect(bootState) {
        when (bootState) {
            BootState.Loading -> Unit
            is BootState.Authenticated -> navController.navigateReplacing(Route.Home)
            BootState.Unauthenticated -> navController.navigateReplacing(Route.Login)
            BootState.Offline -> navController.navigateReplacing(Route.Offline)
        }
    }

    NavHost(navController = navController, startDestination = Route.Boot) {
        composable<Route.Boot> {
            BootScreen()
        }
        composable<Route.Login> {
            LoginScreen(onLoggedIn = { navController.navigateReplacing(Route.Home) })
        }
        composable<Route.Offline> {
            val connectivityViewModel: ConnectivityViewModel = hiltViewModel()
            val status by connectivityViewModel.status.collectAsStateWithLifecycle()
            LaunchedEffect(status) {
                if (status == ConnectivityObserver.Status.AVAILABLE) {
                    bootViewModel.retry()
                }
            }
            OfflineScreen(onRetry = bootViewModel::retry)
        }
        composable<Route.Home>(
            enterTransition = {
                if (initialState.destination.hasRoute<Route.Login>()) {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(400))
                } else {
                    fadeIn(tween(300))
                }
            },
        ) {
            HomeScreen(onLoggedOut = { navController.navigateReplacing(Route.Login) })
        }
    }
}

/** Navigates to a top-level destination, clearing the back stack (these screens are mutually exclusive). */
private fun NavController.navigateReplacing(route: Route) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
