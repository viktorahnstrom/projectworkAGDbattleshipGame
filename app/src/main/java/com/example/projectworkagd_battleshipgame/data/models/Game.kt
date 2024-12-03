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

    val board1: List<List<Board.CellState>>?
        get() = board1String?.split(",")?.map { row ->
            row.map { cell ->
                when (cell) {
                    'E' -> Board.CellState.EMPTY
                    'S' -> Board.CellState.SHIP
                    'H' -> Board.CellState.HIT
                    'M' -> Board.CellState.MISS
                    else -> Board.CellState.EMPTY
                }
            }
        }

    val board2: List<List<Board.CellState>>?
        get() = board2String?.split(",")?.map { row ->
            row.map { cell ->
                when (cell) {
                    'E' -> Board.CellState.EMPTY
                    'S' -> Board.CellState.SHIP
                    'H' -> Board.CellState.HIT
                    'M' -> Board.CellState.MISS
                    else -> Board.CellState.EMPTY
                }
            }
        }
}