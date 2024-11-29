package com.example.projectworkagd_battleshipgame.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.projectworkagd_battleshipgame.data.models.Player

@Composable
fun PlayerItem(
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
            text = { Text("Do you want to challenge ${player.name}?") },
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