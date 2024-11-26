package com.example.projectworkagd_battleshipgame.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectworkagd_battleshipgame.data.models.Board
import com.example.projectworkagd_battleshipgame.data.models.Game
import com.example.projectworkagd_battleshipgame.data.models.Ship
import com.example.projectworkagd_battleshipgame.data.remote.FirebaseService
import com.example.projectworkagd_battleshipgame.data.repositories.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PreparationViewModel(
    private val gameRepository: GameRepository = GameRepository(FirebaseService())
) : ViewModel() {
    private val _ships = MutableStateFlow<List<Ship>>(
        listOf(
            Ship(length = 4),
            Ship(length = 3),
            Ship(length = 3),
            Ship(length = 2),
            Ship(length = 2),
            Ship(length = 1)
        )
    )
    val ships: StateFlow<List<Ship>> = _ships.asStateFlow()

    private val _board = MutableStateFlow<Board>(Board())
    val board: StateFlow<Board> = _board.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _bothPlayersReady = MutableStateFlow(false)
    val bothPlayersReady: StateFlow<Boolean> = _bothPlayersReady.asStateFlow()

    private val _currentGame = MutableStateFlow<Game?>(null)
    val currentGame: StateFlow<Game?> = _currentGame.asStateFlow()

    init {
        viewModelScope.launch {
            gameRepository.currentGame.collect { game ->
                _currentGame.value = game
                game?.let { observePlayersReadyStatus() }
            }
        }
    }

    fun placeShip(ship: Ship, x: Int, y: Int, isVertical: Boolean) {
        viewModelScope.launch {
            if (isValidPlacement(ship, x, y, isVertical)) {
                updateBoard(ship, x, y, isVertical)
                updateShipStatus(ship)
            }
        }
    }

    private fun isValidPlacement(ship: Ship, x: Int, y: Int, isVertical: Boolean): Boolean {
        val board = _board.value

        if (isVertical) {
            if (y + ship.length > board.size) return false
        } else {
            if (x + ship.length > board.size) return false
        }

        val startX = maxOf(0, x - 1)
        val endX = minOf(board.size - 1, if (isVertical) x + 1 else x + ship.length)
        val startY = maxOf(0, y - 1)
        val endY = minOf(board.size - 1, if (isVertical) y + ship.length else y + 1)

        for (checkY in startY..endY) {
            for (checkX in startX..endX) {
                if (board.cells[checkY][checkX].shipId != null) return false
            }
        }
        return true
    }

    private fun updateBoard(ship: Ship, x: Int, y: Int, isVertical: Boolean) {
        val currentBoard = _board.value
        for (i in 0 until ship.length) {
            val updateX = if (isVertical) x else x + i
            val updateY = if (isVertical) y + i else y
            currentBoard.cells[updateY][updateX].shipId = ship.id
        }
        _board.value = currentBoard
    }

    private fun updateShipStatus(ship: Ship) {
        val currentShips = _ships.value.toMutableList()
        val index = currentShips.indexOfFirst { it.id == ship.id }
        currentShips[index] = ship.copy(isPlaced = true)
        _ships.value = currentShips
    }

    fun setReady() {
        viewModelScope.launch {
            _isReady.value = true
            _currentGame.value?.let { game ->
                val playerId = gameRepository.getCurrentPlayerId()
                val updates = if (game.player1Id == playerId) {
                    mapOf("player1Ready" to true)
                } else {
                    mapOf("player2Ready" to true)
                }
                gameRepository.updateGameState(game.id, updates)
            }
        }
    }

    private fun observePlayersReadyStatus() {
        viewModelScope.launch {
            _currentGame.value?.let { game ->
                gameRepository.observeGame(game.id) { updatedGame ->
                    _bothPlayersReady.value = updatedGame.player1Ready && updatedGame.player2Ready
                }
            }
        }
    }
}