package com.example.projectworkagd_battleshipgame.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.projectworkagd_battleshipgame.ui.components.BattleshipsBackground
import com.example.projectworkagd_battleshipgame.ui.components.BoardGrid
import com.example.projectworkagd_battleshipgame.ui.viewmodels.GameViewModel
import com.example.projectworkagd_battleshipgame.data.models.GameStatus

@Composable
fun GameScreen(
    navController: NavController,
    viewModel: GameViewModel
) {
    val gameState by viewModel.gameState.collectAsState()
    val myBoard by viewModel.board.collectAsState()
    val opponentBoard by viewModel.opponentBoard.collectAsState()
    val isMyTurn = gameState.currentPlayerId == viewModel.getCurrentPlayerId()
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        BattleshipsBackground(showTitle = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isMyTurn) "Your Turn" else "Opponent's Turn",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Opponent's Board",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BoardGrid(
                    board = opponentBoard,
                    onCellClick = { x, y ->
                        if (isMyTurn && gameState.status == GameStatus.IN_PROGRESS) {
                            selectedCell = Pair(x, y)
                        }
                    },
                    selectedCell = selectedCell
                )
            }

            if (selectedCell != null && isMyTurn && gameState.status == GameStatus.IN_PROGRESS) {
                Button(
                    onClick = {
                        val (x, y) = selectedCell!!
                        viewModel.makeMove(x, y)
                        selectedCell = null
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Fire!")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your Board",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BoardGrid(
                    board = myBoard,
                    onCellClick = { _, _ ->  },
                    selectedCell = null
                )
            }

            when (gameState.status) {
                GameStatus.FINISHED -> {
                    Text(
                        text = if (gameState.winner == viewModel.getCurrentPlayerId())
                            "You Won!" else "Game Over - You Lost",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                    Button(
                        onClick = { navController.navigate("lobby") {
                            popUpTo("lobby") { inclusive = true }
                        }},
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Return to Lobby")
                    }
                }
                else -> {
                    if (gameState.error != null) {
                        Text(
                            text = gameState.error!!,
                            color = Color.Red,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}