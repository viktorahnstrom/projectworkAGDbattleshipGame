package com.example.projectworkagd_battleshipgame.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectworkagd_battleshipgame.data.models.Board
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

    fun placeShip(ship: Ship, x: Int, y: Int, isVertical: Boolean) {
        viewModelScope.launch {
            if (isValidPlacement(ship, x, y, isVertical)) {
                updateBoard(ship, x, y, isVertical)
                updateShipStatus(ship)
            }
        }
    }

    private fun isValidPlacement(ship: Ship, x: Int, y: Int, isVertical: Boolean): Boolean {
        // will implement validation logic here
            if (x + ship.length > board.size) return false
        return true
    }

    private fun updateBoard(ship: Ship, x: Int, y: Int, isVertical: Boolean) {
        val currentBoard = _board.value
        // will implement board update logic
        _board.value = currentBoard
    }

    private fun updateShipStatus(ship: Ship) {
        val currentShips = _ships.value.toMutableList()
        val index = currentShips.indexOfFirst { it.id == ship.id }
        currentShips[index] = ship.copy(isPlaced = true)
        _ships.value = currentShips
    }
}