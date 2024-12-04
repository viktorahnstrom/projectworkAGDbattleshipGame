package com.example.projectworkagd_battleshipgame.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.projectworkagd_battleshipgame.R
import com.example.projectworkagd_battleshipgame.data.models.Ship
import com.example.projectworkagd_battleshipgame.ui.components.BattleshipsBackground
import com.example.projectworkagd_battleshipgame.ui.components.BoardGrid
import com.example.projectworkagd_battleshipgame.ui.theme.BlueColor
import com.example.projectworkagd_battleshipgame.ui.viewmodels.GameViewModel
import com.example.projectworkagd_battleshipgame.ui.viewmodels.PreparationViewModel

@Composable
fun PreparationScreen(
    navController: NavController,
    gameId: String,
    playerId: String,
    preparationViewModel: PreparationViewModel = viewModel(),
    gameViewModel: GameViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GameViewModel(playerId = playerId, gameId = gameId) as T
            }
        }
    )
) {
    val ships by preparationViewModel.ships.collectAsState()
    val board by preparationViewModel.board.collectAsState()
    var selectedShip by remember { mutableStateOf<Ship?>(null) }
    var isVertical by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }
    val gameState by gameViewModel.gameState.collectAsState()

    LaunchedEffect(Unit) {
        gameViewModel.observeGameReadiness {
            Log.d("PreparationScreen", "Game readiness callback triggered")
            if (!hasNavigated) {
                hasNavigated = true
                try {
                    navController.navigate("game/$gameId/$playerId") {
                        popUpTo("preparation") { inclusive = true }
                    }
                    Log.d("PreparationScreen", "Navigation successful")
                } catch (e: Exception) {
                    Log.e("PreparationScreen", "Navigation failed", e)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BattleshipsBackground(showTitle = false)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.yourboard),
                contentDescription = "Your Board title",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp, top = 32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF001a33).copy(alpha = 0.6f),
                                Color(0xFF003366).copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                BoardGrid(
                    board = board,
                    onCellClick = { x, y ->
                        selectedShip?.let { ship ->
                            preparationViewModel.placeShip(ship, x, y, isVertical)
                            selectedShip = null
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF001a33).copy(alpha = 0.6f),
                                Color(0xFF003366).copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ships.forEach { ship ->
                    ShipItem(
                        ship = ship,
                        isSelected = selectedShip == ship,
                        onClick = {
                            selectedShip = if (selectedShip == ship) null else ship
                        }
                    )
                }
            }

            Button(
                onClick = { isVertical = !isVertical },
                colors = ButtonDefaults.buttonColors(containerColor = BlueColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(if (isVertical) "Switch to Horizontal" else "Switch to Vertical")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Log.d("PreparationScreen", "Ready button clicked")
                    gameViewModel.markPlayerReady(gameId, preparationViewModel.board.value.cells)
                    Log.d("PreparationScreen", "markPlayerReady called successfully")
                },
                enabled = preparationViewModel.allShipsPlaced,
                colors = ButtonDefaults.buttonColors(containerColor = BlueColor)
            ) {
                Text("Ready")
            }
        }
    }
}

@Composable
private fun ShipItem(
    ship: Ship,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(
                color = if (isSelected) BlueColor else Color.Gray,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = !ship.isPlaced, onClick = onClick)
            .padding(8.dp)
    ) {
        Text(
            text = "${ship.length}",
            color = Color.White
        )
    }
}