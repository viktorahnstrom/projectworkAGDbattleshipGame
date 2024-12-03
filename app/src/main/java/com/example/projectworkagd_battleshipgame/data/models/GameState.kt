package com.example.projectworkagd_battleshipgame.data.models

data class GameState(
    val isLoading: Boolean = true,
    val status: GameStatus = GameStatus.SETUP,
    val currentPlayerId: String = "",
    val player1Id: String = "",
    val player2Id: String = "",
    val player1Ready: Boolean = false,
    val player2Ready: Boolean = false,
    val error: String? = null,
    val winner: String? = null
)