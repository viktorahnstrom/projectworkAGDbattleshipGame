package com.example.projectworkagd_battleshipgame.data.models

data class Ship(
    val id: String = java.util.UUID.randomUUID().toString(),
    val length: Int,
    var isPlaced: Boolean = false
)