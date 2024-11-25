package com.example.projectworkagd_battleshipgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.NavHost
import com.example.projectworkagd_battleshipgame.ui.screens.LobbyScreen
import com.example.projectworkagd_battleshipgame.ui.screens.PreparationScreen
import com.example.projectworkagd_battleshipgame.ui.theme.ProjectworkAGDbattleshipGameTheme
import com.example.projectworkagd_battleshipgame.ui.viewmodels.GameViewModel
import com.example.projectworkagd_battleshipgame.ui.viewmodels.LobbyViewModel
import com.example.projectworkagd_battleshipgame.ui.viewmodels.PreparationViewModel
import com.google.firebase.Firebase
import com.google.firebase.initialize

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()
    private val lobbyViewModel: LobbyViewModel by viewModels()
    private val preparationViewModel: PreparationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)

        setContent {
            ProjectworkAGDbattleshipGameTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "lobby") {
                    composable("lobby") {
                        LobbyScreen(navController, lobbyViewModel)
                    }
                    composable("preparation") {
                        PreparationScreen(navController, preparationViewModel)
                    }
                    composable("game") {
                        GameScreen(navController, gameViewModel)
                    }
                }
            }
        }
    }
}
