package com.example.projectworkagd_battleshipgame.data.models

data class Player(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val isOnline: Boolean = false,
    val currentGameId: String? = null
)