package com.example.projectworkagd_battleshipgame.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projectworkagd_battleshipgame.data.models.Player
import com.example.projectworkagd_battleshipgame.ui.theme.YellowColor

@Composable
fun PlayerList(
    players: List<Player>,
    onChallengePlayer: (Player) -> Unit
) {
    Box(
        modifier = Modifier
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
                    onChallenge = { onChallengePlayer(player) }
                )
            }
        }
    }

}