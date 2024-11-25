package com.example.projectworkagd_battleshipgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.projectworkagd_battleshipgame.data.models.Ship
import com.example.projectworkagd_battleshipgame.ui.theme.BlueColor
import com.example.projectworkagd_battleshipgame.ui.viewmodels.PreparationViewModel
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectworkagd_battleshipgame.ui.components.BoardGrid


@Composable
fun PreparationScreen(
    navController: NavController,
    viewModel: PreparationViewModel = viewModel()
) {
    val ships by viewModel.ships.collectAsState()
    val board by viewModel.board.collectAsState()
    var selectedShip by remember { mutableStateOf<Ship?>(null) }
    var isVertical by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        BattleshipsBackground(showTitle = false)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Place Your Ships",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                BoardGrid(
                    board = board,
                    onCellClick = { x, y ->
                        selectedShip?.let { ship ->
                            viewModel.placeShip(ship, x, y, isVertical)
                            selectedShip = null
                        }
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.Black.copy(alpha = 0.4f),
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlueColor
                ),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(if (isVertical) "Horizontal" else "Vertical")
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