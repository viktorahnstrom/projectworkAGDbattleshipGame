package com.example.projectworkagd_battleshipgame.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun ChallengeDialog(
    challengerName: String,
    isSender: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(30) }

    LaunchedEffect(key1 = true) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0) {
            onDismiss()
            onDecline()

        }
    }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
            onDecline()
        },
        title = { Text(if (isSender) "Waiting for response..." else "New Challenge!") },
        text = {
            Text(
                if (isSender)
                    "Waiting for $challengerName  to respond \nTime left: $timeLeft seconds"
                else
                    "$challengerName has challenged you!\nTime left: $timeLeft seconds"
            )
        },
        confirmButton = {
            if (!isSender) {
                Button(onClick = onAccept) {
                    Text("Accept")
                }
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onDismiss()
                    onDecline()
                }
            ) {
                Text(if (isSender) "Cancel" else "Decline")
            }
        }
    )
}