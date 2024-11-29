package com.example.projectworkagd_battleshipgame.data.repositories

import android.util.Log
import com.example.projectworkagd_battleshipgame.data.models.Board
import com.example.projectworkagd_battleshipgame.data.models.Game
import com.example.projectworkagd_battleshipgame.data.models.GameStatus
import com.example.projectworkagd_battleshipgame.data.models.Move
import com.example.projectworkagd_battleshipgame.data.remote.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameRepository(private val firebaseService: FirebaseService) {
    // ===== State Management =====
    private val _currentGame = MutableStateFlow<Game?>(null)
    val currentGame: StateFlow<Game?> = _currentGame.asStateFlow()
    private var currentPlayerId: String? = null

    // ===== Player Management =====
    fun getCurrentPlayerId(): String? = currentPlayerId

    fun setCurrentPlayerId(playerId: String) {
        currentPlayerId = playerId
    }

    // ===== Game Creation and Retrieval =====
    fun createGame(gameId: String, player1Id: String, player2Id: String) {
        Log.d("GameRepository", "Creating game with player1Id: $player1Id, player2Id: $player2Id")
        val game = Game(
            id = gameId,
            player1Id = player1Id,
            player2Id = player2Id,
            player1Ready = false,
            player2Ready = false
        )

        firebaseService.db.collection("games")
            .document(gameId)
            .set(game)
            .addOnSuccessListener {
                Log.d("GameRepository", "Game created successfully with ID: $gameId")
            }
            .addOnFailureListener { e ->
                Log.e("GameRepository", "Error creating game", e)
            }
    }

    fun getGame(gameId: String, callback: (Game?) -> Unit) {
        firebaseService.db.collection("games").document(gameId)
            .get()
            .addOnSuccessListener { document ->
                callback(document.toObject(Game::class.java))
            }
            .addOnFailureListener { e ->
                Log.e("GameRepository", "Error getting game", e)
                callback(null)
            }
    }


    // ===== Board Management =====
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


    // ===== Game Moves =====
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
}
