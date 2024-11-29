package com.example.projectworkagd_battleshipgame.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectworkagd_battleshipgame.data.models.Board
import com.example.projectworkagd_battleshipgame.data.models.GameState
import com.example.projectworkagd_battleshipgame.data.remote.FirebaseService
import com.example.projectworkagd_battleshipgame.data.repositories.GameRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val playerId: String,
    private val gameId: String,
    private val gameRepository: GameRepository = GameRepository(FirebaseService()),
    private val firebaseService: FirebaseService = FirebaseService()
) : ViewModel() {
    private val db = Firebase.firestore

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _board = MutableStateFlow(Board())
    val board: StateFlow<Board> = _board.asStateFlow()

    private val _player1Ready = MutableStateFlow(false)
    val player1Ready: StateFlow<Boolean> = _player1Ready.asStateFlow()

    private val _player2Ready = MutableStateFlow(false)
    val player2Ready: StateFlow<Boolean> = _player2Ready.asStateFlow()

    fun markPlayerReady() {
        Log.d("GameViewModel", "Marking player ready for gameId: $gameId, playerId: $playerId")
        gameRepository.getGame(gameId) { game ->
            if (game != null) {
                val isPlayer1 = game.player1Id == playerId
                firebaseService.updatePlayerReadiness(gameId, playerId, isPlayer1)
            }
        }
    }

    fun observeGameReadiness(onBothReady: () -> Unit) {
        firebaseService.observeGameReadiness(gameId, onBothReady)
    }

    fun areBothPlayersReady(): Boolean {
        return _player1Ready.value && _player2Ready.value
    }

    fun makeMove(x: Int, y: Int) {
        viewModelScope.launch {
            gameRepository.makeMove(gameId, x, y, playerId)
            updateBoard(x, y)
        }
    }

    private fun updateBoard(x: Int, y: Int) {
        val currentBoard = board.value
        currentBoard.cells[y][x].isHit = true
        _board.value = currentBoard
    }
}