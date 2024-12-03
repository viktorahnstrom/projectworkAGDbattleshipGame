package com.example.projectworkagd_battleshipgame.data.models

data class Board(
    val cells: List<List<Cell>> = List(10) { row ->
        List(10) { col ->
            Cell(row, col, CellState.EMPTY)
        }
    }
) {
    data class Cell(
        val x: Int,
        val y: Int,
        var state: CellState = CellState.EMPTY
    )

    enum class CellState {
        EMPTY,
        SHIP,
        HIT,
        MISS
    }
}