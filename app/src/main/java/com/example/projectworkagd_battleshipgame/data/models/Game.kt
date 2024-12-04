package com.example.projectworkagd_battleshipgame.data.models

enum class GameStatus {
    SETUP,
    IN_PROGRESS,
    FINISHED
}

data class Game(
    val id: String = "",
    val player1Id: String = "",
    val player2Id: String = "",
    val player1Ready: Boolean = false,
    val player2Ready: Boolean = false,
    val status: GameStatus = GameStatus.SETUP,
    var currentTurn: String = "",
    val winner: String? = null,
    val board1String: String? = null,
    val board2String: String? = null
) {
    init {
        if (currentTurn.isEmpty()) {
            currentTurn = player1Id
        }
    }
}