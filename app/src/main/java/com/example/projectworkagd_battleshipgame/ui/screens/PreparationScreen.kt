package com.example.projectworkagd_battleshipgame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.projectworkagd_battleshipgame.data.models.Ship
import com.example.projectworkagd_battleshipgame.ui.theme.BlueColor
import com.example.projectworkagd_battleshipgame.ui.viewmodels.PreparationViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.projectworkagd_battleshipgame.R
import com.example.projectworkagd_battleshipgame.ui.components.BattleshipsBackground
import com.example.projectworkagd_battleshipgame.ui.components.BoardGrid
import com.example.projectworkagd_battleshipgame.ui.viewmodels.GameViewModel


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

    val isPlayerReady by gameViewModel.player1Ready.collectAsState()

    LaunchedEffect(Unit) {
        gameViewModel.observeGameReadiness {
            if (!hasNavigated) {
                hasNavigated = true
                navController.navigate("game/$gameId/$playerId") {
                    popUpTo("preparation") { inclusive = true }
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
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(if (isVertical) "Switch to Horizontal" else "Switch to Vertical")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    gameViewModel.markPlayerReady()
                },
                enabled = !isPlayerReady
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