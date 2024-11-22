package com.example.projectworkagd_battleshipgame.ui.viewmodels

import android.app.GameState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectworkagd_battleshipgame.data.models.Board
import com.example.projectworkagd_battleshipgame.data.remote.FirebaseService
import com.example.projectworkagd_battleshipgame.data.repositories.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val gameRepository: GameRepository = GameRepository(FirebaseService())
) : ViewModel () {
    private val _gameState = MutableStateFlow<GameState>(GameState.initial)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _board = MutableStateFlow<Board>(Board())
    val board: StateFlow<Board> = _board.asStateFlow()

    fun makeMove(x: Int, y: Int, playerId: String) {
        viewModelScope.launch {
            gameRepository.currentGame.value?.let { game ->
                gameRepository.makeMove(game.id, x, y, playerId)
                updateBoard(x, y)
            }
        }
    }

    private fun updateBoard(x: Int, y: Int) {
        val currentBoard = board.value
        currentBoard.cells[y][x].isHit = true
        _board.value = currentBoard
    }
}