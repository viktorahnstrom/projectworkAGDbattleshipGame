package com.example.projectworkagd_battleshipgame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.projectworkagd_battleshipgame.R
import com.example.projectworkagd_battleshipgame.data.models.Player
import com.example.projectworkagd_battleshipgame.ui.components.*
import com.example.projectworkagd_battleshipgame.ui.theme.BlueColor
import com.example.projectworkagd_battleshipgame.ui.theme.ErrorRed
import com.example.projectworkagd_battleshipgame.ui.viewmodels.LobbyViewModel

@Composable
fun LobbyScreen(
    navController: NavController,
    viewModel: LobbyViewModel = viewModel()
) {
    val players by viewModel.players.collectAsState()
    val challengeState by viewModel.challengeState.collectAsState()
    var isConnected by remember { mutableStateOf(false) }
    var playerName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        BattleshipsBackground(showTitle = true)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 380.dp, start = 32.dp, end = 32.dp)
                .padding(24.dp)
        ) {
            if (!isConnected) {
                ConnectSection(
                    playerName = playerName,
                    onNameChange = {
                        playerName = it
                        errorMessage = null
                    },
                    errorMessage = errorMessage,
                    onConnect = {
                        viewModel.joinLobby(playerName)
                        isConnected = true
                    }
                )
            } else {
                LobbyContent(
                    players = players,
                    onChallengePlayer = { viewModel.challengePlayer(it) }
                )
            }
        }
    }

    challengeState?.let { state ->
        when (state) {
            is LobbyViewModel.ChallengeState.Sending -> {
                if (state.accepted) {
                    DisposableEffect(Unit) {
                        navController.navigate("preparation/${state.gameId}/${viewModel.getCurrentPlayerId()}") {
                            popUpTo("lobby") { inclusive = true }
                            launchSingleTop = true
                        }
                        onDispose { }
                    }
                } else {
                    ChallengeDialog(
                        challengerName = state.opponentName,
                        isSender = true,
                        onAccept = { /* not used here */ },
                        onDecline = { viewModel.declineChallenge(state.challengeId) },
                        onDismiss = { viewModel.declineChallenge(state.challengeId) }
                    )
                }
            }
            is LobbyViewModel.ChallengeState.Receiving -> {
                ChallengeDialog(
                    challengerName = players.find { it.id == state.challengerId }?.name ?: "Unknown",
                    isSender = false,
                    onAccept = {
                        val gameId = viewModel.acceptChallenge(state.challengeId, state.challengerId)
                        navController.navigate("preparation/$gameId/${viewModel.getCurrentPlayerId()}") {
                            popUpTo("lobby") { inclusive = true }
                        }
                    },
                    onDecline = { viewModel.declineChallenge(state.challengeId) },
                    onDismiss = { viewModel.declineChallenge(state.challengeId) }
                )
                LaunchedEffect(state.gameId) {
                    state.gameId?.let { gameId ->
                        navController.navigate("preparation/$gameId/${viewModel.getCurrentPlayerId()}") {
                            popUpTo("lobby") { inclusive = true }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectSection(
    playerName: String,
    onNameChange: (String) -> Unit,
    errorMessage: String?,
    onConnect: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = playerName,
            onValueChange = onNameChange,
            label = { Text("Name:", color = Color.White) },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = if (errorMessage != null) ErrorRed else BlueColor,
                unfocusedBorderColor = if (errorMessage != null) ErrorRed else Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                containerColor = Color.Black.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let {
            Text(
                text = it,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 8.dp)
                    .background(
                        color = ErrorRed,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onConnect,
            colors = ButtonDefaults.buttonColors(
                containerColor = BlueColor,
                contentColor = Color.White
            ),
        ) {
            Text("Connect", color = Color.White)
        }
    }
}

@Composable
private fun LobbyContent(
    players: List<Player>,
    onChallengePlayer: (Player) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.gamelobby),
            contentDescription = "Game Lobby Title",
            contentScale = ContentScale.Fit,
        )
    }

    PlayerList(
        players = players,
        onChallengePlayer = onChallengePlayer
    )
}