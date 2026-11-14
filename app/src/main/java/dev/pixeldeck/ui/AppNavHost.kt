package dev.pixeldeck.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.pixeldeck.AppContainer
import dev.pixeldeck.data.settings.AppSettings
import dev.pixeldeck.ui.detail.GameDetailScreen
import dev.pixeldeck.ui.library.LibraryScreen
import dev.pixeldeck.ui.library.LibraryViewModel
import dev.pixeldeck.ui.player.PlayerScreen
import dev.pixeldeck.ui.settings.SettingsScreen

@Composable
fun AppNavHost(
    container: AppContainer,
    settings: AppSettings,
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "library",
        enterTransition = {
            fadeIn(tween(220)) + slideInHorizontally(tween(260)) { it / 10 }
        },
        exitTransition = {
            fadeOut(tween(160))
        },
        popEnterTransition = {
            fadeIn(tween(180))
        },
        popExitTransition = {
            fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { it / 10 }
        },
    ) {
        composable("library") {
            val libraryViewModel: LibraryViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        LibraryViewModel(
                            context = context.applicationContext,
                            repository = container.games,
                            scanner = container.scanner,
                            folders = container.folders,
                        )
                    }
                },
            )

            LibraryScreen(
                viewModel = libraryViewModel,
                settings = settings,
                onGameClick = { gameId -> navController.navigate("game/$gameId") },
                onOpenSettings = { navController.navigate("settings") },
            )
        }

        composable("settings") {
            SettingsScreen(
                container = container,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = "game/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.LongType }),
        ) { entry ->
            val gameId = entry.arguments?.getLong("gameId") ?: 0L
            GameDetailScreen(
                container = container,
                gameId = gameId,
                settings = settings,
                onBack = { navController.popBackStack() },
                onPlay = { id -> navController.navigate("play/$id") },
            )
        }

        composable(
            route = "play/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.LongType }),
        ) { entry ->
            val gameId = entry.arguments?.getLong("gameId") ?: 0L
            PlayerScreen(
                container = container,
                gameId = gameId,
                settings = settings,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
