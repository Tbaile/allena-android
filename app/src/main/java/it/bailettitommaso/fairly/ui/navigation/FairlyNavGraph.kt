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
import androidx.navigation.toRoute
import it.bailettitommaso.fairly.ui.HomeScreen
import it.bailettitommaso.fairly.ui.auth.ChangePasswordScreen
import it.bailettitommaso.fairly.ui.auth.LoginScreen
import it.bailettitommaso.fairly.ui.boot.BootScreen
import it.bailettitommaso.fairly.ui.boot.BootState
import it.bailettitommaso.fairly.ui.boot.BootViewModel
import it.bailettitommaso.fairly.ui.exercises.ExerciseDetailScreen
import it.bailettitommaso.fairly.domain.repository.SessionResult
import it.bailettitommaso.fairly.ui.offline.ConnectivityViewModel
import it.bailettitommaso.fairly.ui.offline.OfflineScreen
import it.bailettitommaso.fairly.ui.settings.SettingsScreen
import it.bailettitommaso.fairly.ui.workouts.WorkoutPlanDetailScreen
import it.bailettitommaso.fairly.ui.workouts.WorkoutPlayerScreen

@Composable
fun FairlyNavGraph(bootViewModel: BootViewModel) {
    val navController = rememberNavController()
    val bootState by bootViewModel.state.collectAsStateWithLifecycle()

    // The boot check owns the top-level routing decision, but only on a genuine state change:
    // after an activity recreation the effect replays, and re-routing would wipe the back stack.
    LaunchedEffect(bootState) {
        val state = bootState
        if (!bootViewModel.shouldRoute(state)) return@LaunchedEffect

        when (state) {
            BootState.Loading -> Unit
            is BootState.Authenticated -> navController.navigateReplacing(
                if (state.user.mustChangePassword) Route.ChangePassword(forced = true) else Route.Home,
            )
            BootState.Unauthenticated -> navController.navigateReplacing(Route.Login)
            is BootState.Unreachable -> navController.navigateReplacing(Route.Offline)
        }
    }

    NavHost(navController = navController, startDestination = Route.Boot) {
        composable<Route.Boot> {
            BootScreen()
        }
        composable<Route.Login> {
            LoginScreen(
                onLoggedIn = { mustChangePassword ->
                    navController.navigateReplacing(
                        if (mustChangePassword) Route.ChangePassword(forced = true) else Route.Home,
                    )
                },
            )
        }
        composable<Route.ChangePassword> { backStackEntry ->
            val route: Route.ChangePassword = backStackEntry.toRoute()
            ChangePasswordScreen(
                onDone = {
                    if (route.forced) {
                        navController.navigateReplacing(Route.Home)
                    } else {
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.Offline> {
            val connectivityViewModel: ConnectivityViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                connectivityViewModel.reconnected.collect { bootViewModel.retry() }
            }
            val retrying by bootViewModel.retrying.collectAsStateWithLifecycle()
            OfflineScreen(
                cause = (bootState as? BootState.Unreachable)?.cause
                    ?: SessionResult.Unreachable.Cause.NETWORK,
                retrying = retrying,
                onRetry = bootViewModel::retry,
            )
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
            HomeScreen(
                onLoggedOut = { navController.navigateReplacing(Route.Login) },
                onChangePassword = { navController.navigate(Route.ChangePassword(forced = false)) },
                onExerciseClick = { id -> navController.navigate(Route.ExerciseDetail(id)) },
                onWorkoutPlanClick = { id -> navController.navigate(Route.WorkoutPlanDetail(id)) },
                onSettings = { navController.navigate(Route.Settings) },
            )
        }
        composable<Route.ExerciseDetail>(
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
            },
        ) {
            ExerciseDetailScreen(onBack = { navController.popBackStack() })
        }
        composable<Route.WorkoutPlanDetail>(
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
            },
        ) {
            WorkoutPlanDetailScreen(
                onBack = { navController.popBackStack() },
                onStartWorkout = { id -> navController.navigate(Route.WorkoutPlayer(id)) },
            )
        }
        composable<Route.WorkoutPlayer>(
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = tween(300))
            },
        ) {
            WorkoutPlayerScreen(onExit = { navController.popBackStack() })
        }
        composable<Route.Settings>(
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
            },
        ) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

/**
 * Navigates to a top-level destination, clearing the back stack (these screens are mutually
 * exclusive). Bails out when already there: `popUpTo(inclusive)` destroys and rebuilds the current
 * entry, so without this guard a repeated state resolution recreates the screen and its ViewModels.
 */
private fun NavController.navigateReplacing(route: Route) {
    if (currentDestination?.hasRoute(route::class) == true) return
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
