package com.example.projectworkagd_battleshipgame.ui.screens

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.projectworkagd_battleshipgame.R
import com.example.projectworkagd_battleshipgame.ui.components.BattleshipsBackground
import com.example.projectworkagd_battleshipgame.ui.components.BoardGrid
import com.example.projectworkagd_battleshipgame.ui.viewmodels.GameViewModel
import com.example.projectworkagd_battleshipgame.data.models.GameStatus
import com.example.projectworkagd_battleshipgame.ui.theme.BlueColor

@Composable
fun GameScreen(
    navController: NavController,
    viewModel: GameViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as Application)
    )
) {
    val gameState by viewModel.gameState.collectAsState()
    val myBoard by viewModel.board.collectAsState()
    val opponentBoard by viewModel.opponentBoard.collectAsState()
    val isMyTurn = gameState.currentPlayerId == viewModel.getCurrentPlayerId()
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var showGameOverDialog by remember { mutableStateOf(false) }

    LaunchedEffect(gameState.status) {
        if (gameState.status == GameStatus.FINISHED) {
            showGameOverDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BattleshipsBackground(showTitle = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(
                    id = if (isMyTurn) R.drawable.yourturn else R.drawable.opponentsturn
                ),
                contentDescription = if (isMyTurn) "Your Turn" else "Opponent's Turn",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            )

            Image(
                painter = painterResource(R.drawable.enemyboard),
                contentDescription = "Enemy Board"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(8.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF001a33).copy(alpha = 0.6f),
                                Color(0xFF003366).copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                BoardGrid(
                    board = opponentBoard,
                    onCellClick = { x, y ->
                        if (isMyTurn && gameState.status == GameStatus.IN_PROGRESS) {
                            selectedCell = Pair(x, y)
                        }
                    },
                    selectedCell = selectedCell,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    val (x, y) = selectedCell!!
                    viewModel.makeMove(x, y)
                    selectedCell = null
                },
                enabled = selectedCell != null && isMyTurn && gameState.status == GameStatus.IN_PROGRESS,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedCell != null) Color.Red else BlueColor,
                    disabledContainerColor = BlueColor
                ),
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(150.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.fire),
                    contentDescription = "Fire button",
                    modifier = Modifier.size(30.dp)
                )
            }

            Image(
                painter = painterResource(R.drawable.yourboard),
                contentDescription = "Your Board"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(horizontal = 8.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF001a33).copy(alpha = 0.6f),
                                Color(0xFF003366).copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                BoardGrid(
                    board = myBoard,
                    onCellClick = { _, _ -> },
                    selectedCell = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (gameState.error != null) {
                Text(
                    text = gameState.error!!,
                    color = Color.Red,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        if (showGameOverDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (gameState.winner == viewModel.getCurrentPlayerId())
                                    R.drawable.youwon else R.drawable.youlost
                            ),
                            contentDescription = if (gameState.winner == viewModel.getCurrentPlayerId())
                                "Victory Image" else "Defeat Image",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(bottom = 8.dp)
                        )
                    }
                },
                text = {
                    Text(
                        text = if (gameState.winner == viewModel.getCurrentPlayerId())
                            "Congratulations, you won the battle!"
                        else "Better luck next time, admiral!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(top = 0.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            navController.navigate("lobby") {
                                popUpTo("lobby") { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueColor)
                    ) {
                        Text("Return to Lobby")
                    }
                },
                containerColor = Color(0xFF001a33).copy(alpha = 0.85f),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
