package com.example.projectworkagd_battleshipgame.data.models

data class GameState(
    val isLoading: Boolean = false,
    val status: GameStatus = GameStatus.SETUP,
    val currentPlayerId: String? = null,
    val error: String? = null
)