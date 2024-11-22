package com.example.projectworkagd_battleshipgame.data.models

data class Board (
    val size: Int = 10,
    val cells: MutableList<MutableList<Cell>> = MutableList(size) {
        MutableList(size) { Cell() }
    }
)