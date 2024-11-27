package com.example.projectworkagd_battleshipgame.data.repositories

import com.example.projectworkagd_battleshipgame.data.models.Board
import com.example.projectworkagd_battleshipgame.data.models.Game
import com.example.projectworkagd_battleshipgame.data.models.GameStatus
import com.example.projectworkagd_battleshipgame.data.models.Move
import com.example.projectworkagd_battleshipgame.data.remote.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameRepository(private val firebaseService: FirebaseService) {
    private val _currentGame = MutableStateFlow<Game?>(null)
    val currentGame: StateFlow<Game?> = _currentGame.asStateFlow()

    private var currentPlayerId: String? = null

    fun getCurrentPlayerId(): String? = currentPlayerId

    fun startGame(gameId: String) {
        firebaseService.updateGameState(gameId, mapOf(
            "status" to GameStatus.IN_PROGRESS
        ))
    }

    fun updateBoard(gameId: String, playerId: String, board: Board) {
        currentGame.value?.let { game ->
            val updates = when (playerId) {
                game.player1Id -> mapOf("board1" to board)
                game.player2Id -> mapOf("board2" to board)
                else -> return
            }
            firebaseService.updateGameState(gameId, updates)
        }
    }

    fun setCurrentPlayerId(playerId: String) {
        currentPlayerId = playerId
    }

    fun updateGameState(gameId: String, updates: Map<String, Any>) {
        firebaseService.updateGameState(gameId, updates)
    }

    fun createGame(player1Id: String, player2Id: String) {
        val newGame = Game(
            player1Id = player1Id,
            player2Id = player2Id,
            status = GameStatus.SETUP
        )
        firebaseService.createGame(newGame)
    }

    fun observeGame(gameId: String, onUpdate: (Game) -> Unit) {
        firebaseService.observeGame(gameId) { game ->
            _currentGame.value = game
            onUpdate(game)
        }
    }



    fun makeMove(gameId: String, x: Int, y: Int, playerId: String) {
        val currentGame = _currentGame.value ?: return

        val nextTurn = if (playerId == currentGame.player1Id) {
            currentGame.player2Id
        } else {
            currentGame.player1Id
        }

        val move = mapOf(
            "lastMove" to Move(x, y, playerId),
            "currentTurn" to nextTurn
        )

        firebaseService.updateGameState(gameId, move)
    }

    fun setPlayerReady(playerId: String) {
        _currentGame.value?.let { game ->
            val updates = when (playerId) {
                game.player1Id -> mapOf("player1Ready" to true)
                game.player2Id -> mapOf("player2Ready" to true)
                else -> return
            }
            firebaseService.updateGameState(game.id, updates)
        }
    }

    fun observePlayersReadyStatus(onReadyUpdate: (Boolean, Boolean) -> Unit) {
        _currentGame.value?.let { game ->
            firebaseService.observeGame(game.id) { updatedGame ->
                onReadyUpdate(updatedGame.player1Ready, updatedGame.player2Ready)
            }
        }
    }
}
