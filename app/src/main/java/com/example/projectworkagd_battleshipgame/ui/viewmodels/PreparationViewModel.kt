package com.example.projectworkagd_battleshipgame.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.projectworkagd_battleshipgame.data.models.Board
import com.example.projectworkagd_battleshipgame.data.models.Ship
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreparationViewModel : ViewModel() {
    private val _board = MutableStateFlow(createEmptyBoard())
    val board: StateFlow<Board> = _board

    private val _ships = MutableStateFlow(createInitialShips())
    val ships: StateFlow<List<Ship>> = _ships

    private val _allShipsPlaced = MutableStateFlow(false)
    val allShipsPlaced: Boolean
        get() = _allShipsPlaced.value

    private fun createEmptyBoard(): Board {
        return Board()
    }

    private fun createInitialShips(): List<Ship> {
        return listOf(
            Ship(length = 4, id = "ship1"),
            Ship(length = 3, id = "ship2"),
            Ship(length = 3, id = "ship3"),
            Ship(length = 2, id = "ship4"),
            Ship(length = 2, id = "ship5"),
            Ship(length = 1, id = "ship6")
        )
    }

    fun placeShip(ship: Ship, x: Int, y: Int, isVertical: Boolean) {
        if (ship.isPlaced) return

        val newBoard = _board.value.copy()
        if (canPlaceShip(newBoard, ship, x, y, isVertical)) {
            for (i in 0 until ship.length) {
                val posX = if (isVertical) x else x + i
                val posY = if (isVertical) y + i else y
                newBoard.cells[posY][posX].state = Board.CellState.SHIP
            }

            val updatedShips = _ships.value.map {
                if (it.id == ship.id) it.copy(isPlaced = true) else it
            }

            _board.value = newBoard
            _ships.value = updatedShips
            _allShipsPlaced.value = updatedShips.all { it.isPlaced }
        }
    }

    private fun canPlaceShip(board: Board, ship: Ship, x: Int, y: Int, isVertical: Boolean): Boolean {
        val boardSize = board.cells.size

        if (isVertical) {
            if (y + ship.length > boardSize) return false
        } else {
            if (x + ship.length > boardSize) return false
        }

        for (i in -1..ship.length) {
            for (j in -1..1) {
                val checkX = if (isVertical) x + j else x + i
                val checkY = if (isVertical) y + i else y + j

                if (checkX in 0 until boardSize && checkY in 0 until boardSize) {
                    if (board.cells[checkY][checkX].state == Board.CellState.SHIP) {
                        return false
                    }
                }
            }
        }

        return true
    }
}