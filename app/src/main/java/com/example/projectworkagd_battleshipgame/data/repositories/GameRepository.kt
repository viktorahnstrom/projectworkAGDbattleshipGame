package com.example.projectworkagd_battleshipgame.data.repositories

import androidx.compose.ui.input.pointer.PointerEventType.Companion.Move
import com.example.projectworkagd_battleshipgame.data.remote.FirebaseService
import com.google.android.gms.games.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameRepository(private val firebaseService: FirebaseService) {
    private val _currentGame = MutableStateFlow<Game?>(null)
    val currentGame: StateFlow<Game?> = _currentGame.asStateFlow()

    fun createGame(player1Id: String, player2Id: String) {
        val newGame = Game(
            player1Id = player1Id,
            player2Id = player2Id,
            status = GameStatus.SETUP
        )
        firebaseService.createGame(newGame)
    }

    fun observeGame(gameId: String) {
        firebaseService.observeGame(gameId) { game ->
            _currentGame.value = game
        }
    }

    fun makeMove(gameId: String, x: Int, y: Int, playerId: String) {
        val move = mapOf(
            "lastMove" to Move(x, y, playerId),
            "currentTurn" to if (_currentGame.value?.currentTurn == playerId)
                _currentGame.value?.player2Id else _currentGame.value?.player1Id
        )
        firebaseService.updateGameState(gameId, move)
    }
}
