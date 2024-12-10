package com.example.projectworkagd_battleshipgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projectworkagd_battleshipgame.ui.screens.GameScreen
import com.example.projectworkagd_battleshipgame.ui.screens.LobbyScreen
import com.example.projectworkagd_battleshipgame.ui.screens.PreparationScreen
import com.example.projectworkagd_battleshipgame.ui.theme.ProjectworkAGDbattleshipGameTheme
import com.example.projectworkagd_battleshipgame.ui.viewmodels.GameViewModel
import com.example.projectworkagd_battleshipgame.ui.viewmodels.LobbyViewModel
import com.google.firebase.Firebase
import com.google.firebase.initialize

class MainActivity : ComponentActivity() {
    private val lobbyViewModel: LobbyViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)

        setContent {
            ProjectworkAGDbattleshipGameTheme {
                NavigationComponent()
            }
        }
    }

    @Composable
    private fun NavigationComponent() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "lobby") {
            // Lobby Route
            composable("lobby") {
                LobbyScreen(navController, lobbyViewModel)
            }

            composable(
                route = "preparation/{gameId}/{playerId}",
                arguments = listOf(
                    navArgument("gameId") { type = NavType.StringType },
                    navArgument("playerId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
                val playerId = backStackEntry.arguments?.getString("playerId") ?: ""
                PreparationScreen(
                    navController = navController,
                    gameId = gameId,
                    playerId = playerId
                )
            }

            composable(
                route = "game/{gameId}/{playerId}",
                arguments = listOf(
                    navArgument("gameId") { type = NavType.StringType },
                    navArgument("playerId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
                val playerId = backStackEntry.arguments?.getString("playerId") ?: ""
                GameScreen(
                    navController = navController,
                    viewModel = GameViewModel(playerId = playerId, gameId = gameId)
                )
            }
        }
    }
}
