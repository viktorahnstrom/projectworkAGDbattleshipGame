package com.example.projectworkagd_battleshipgame.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.projectworkagd_battleshipgame.ui.viewmodels.GameViewModel
import androidx.compose.ui.Modifier
import com.example.projectworkagd_battleshipgame.ui.components.BattleshipsBackground

@Composable
fun GameScreen(
    navController: NavController,
    viewModel: GameViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        BattleshipsBackground(showTitle = false)
        Text(
            text = "Game Screen",
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}