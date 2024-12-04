package com.example.projectworkagd_battleshipgame.data.repositories

import com.example.projectworkagd_battleshipgame.data.models.Board
import com.example.projectworkagd_battleshipgame.data.models.Game
import com.example.projectworkagd_battleshipgame.data.models.GameStatus
import com.example.projectworkagd_battleshipgame.data.remote.FirebaseService
import java.util.UUID

class GameRepository(private val firebaseService: FirebaseService) {

    fun createGame(playerId: String, opponentId: String): String {
        val gameId = UUID.randomUUID().toString()
        val game = Game(
            id = gameId,
            player1Id = playerId,
            player2Id = opponentId,
            currentTurn = playerId,
            status = GameStatus.SETUP
        )
        firebaseService.createGame(game)
        return gameId
    }

    suspend fun makeMove(gameId: String, x: Int, y: Int, playerId: String) {
        val game = firebaseService.getGame(gameId) ?: throw IllegalStateException("Game not found")

        val isPlayer1 = playerId == game.player1Id
        val targetBoardString = if (isPlayer1) game.board2String else game.board1String
        val targetBoard = targetBoardString?.split(",")?.map { it.toList() }?.toMutableList()
            ?: throw IllegalStateException("Target board not found")

        val row = targetBoard[y].toMutableList()
        val wasHit = row[x] == 'S'
        row[x] = if (wasHit) 'H' else 'M'
        targetBoard[y] = row

        val updatedBoardString = targetBoard.joinToString(",") { it.joinToString("") }

        val updates = mutableMapOf<String, Any>()
        if (isPlayer1) {
            updates["board2String"] = updatedBoardString
        } else {
            updates["board1String"] = updatedBoardString
        }

        if (!wasHit) {
            val nextTurn = if (playerId == game.player1Id) game.player2Id else game.player1Id
            updates["currentTurn"] = nextTurn
        }

        firebaseService.updateGameState(gameId, updates)
    }

    suspend fun markPlayerReady(gameId: String, playerId: String, board: List<List<Board.Cell>>) {
        val game = firebaseService.getGame(gameId) ?: throw IllegalStateException("Game not found")
        val updates = mutableMapOf<String, Any>()

        val boardString = board.joinToString(",") { row ->
            row.joinToString("") { cell ->
                when (cell.state) {
                    Board.CellState.EMPTY -> "E"
                    Board.CellState.SHIP -> "S"
                    Board.CellState.HIT -> "H"
                    Board.CellState.MISS -> "M"
                }
            }
        }

        if (playerId == game.player1Id) {
            updates["board1String"] = boardString
            updates["player1Ready"] = true
        } else {
            updates["board2String"] = boardString
            updates["player2Ready"] = true
        }

        firebaseService.updateGameState(gameId, updates)
    }
}