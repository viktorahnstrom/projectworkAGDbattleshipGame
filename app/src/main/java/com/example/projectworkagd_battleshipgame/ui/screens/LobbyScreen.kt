package com.example.projectworkagd_battleshipgame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectworkagd_battleshipgame.R
import com.example.projectworkagd_battleshipgame.data.models.Player
import com.example.projectworkagd_battleshipgame.ui.theme.BlueColor
import com.example.projectworkagd_battleshipgame.ui.theme.ErrorRed
import com.example.projectworkagd_battleshipgame.ui.viewmodels.LobbyViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.projectworkagd_battleshipgame.ui.components.ChallengeDialog
import com.example.projectworkagd_battleshipgame.ui.theme.YellowColor

@Composable
fun BattleshipsBackground (
    showTitle: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (showTitle) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Battleships title",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp)
                    .size(350.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = playerName,
                        onValueChange = {
                            playerName = it
                            errorMessage = null
                        },
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
                        onClick = {
                            viewModel.joinLobby(playerName)
                            isConnected = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlueColor,
                            contentColor = Color.White
                        ),
                    ) {
                        Text("Connect", color = Color.White)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.gamelobby),
                        contentDescription = "Game Lobby Title",
                        contentScale = ContentScale.Fit,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(4.dp, YellowColor, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(2.5.dp)
                    ) {
                        items(players) { player ->
                            PlayerItem(
                                player = player,
                                onChallenge = { viewModel.challengePlayer(player)}
                            )
                        }
                    }
                }
            }
        }
    }

    challengeState?.let { state ->
        when (state) {
            is LobbyViewModel.ChallengeState.Sending -> {
                if (state.accepted) {
                    DisposableEffect(Unit) {
                        navController.navigate("preparation") {
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
                        viewModel.acceptChallenge(state.challengeId)
                        navController.navigate("preparation")
                    },
                    onDecline = { viewModel.declineChallenge(state.challengeId) },
                    onDismiss = { viewModel.declineChallenge(state.challengeId) }
                )
            }
        }
    }
}



@Composable
private fun PlayerItem(
    player: Player,
    onChallenge: () -> Unit
) {
    var showChallengeConfirmation by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { showChallengeConfirmation = true }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(player.name, color = Color.White)
    }

    if (showChallengeConfirmation) {
        AlertDialog(
            onDismissRequest = { showChallengeConfirmation = false },
            title = { Text("Challenge Player") },
            text =  { Text("Do you want to challenge ${player.name}?") },
            confirmButton = {
                Button(onClick = {
                    onChallenge()
                    showChallengeConfirmation = false
                }) {
                    Text("Challenge")
                }
            },
            dismissButton = {
                Button(onClick = { showChallengeConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}