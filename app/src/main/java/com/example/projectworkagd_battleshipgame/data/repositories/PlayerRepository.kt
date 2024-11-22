package com.example.projectworkagd_battleshipgame.data.repositories

import com.example.projectworkagd_battleshipgame.data.models.Player
import com.example.projectworkagd_battleshipgame.data.remote.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerRepository (private val firebaseService: FirebaseService) {
    private val _currentPlayer = MutableStateFlow<Player?>(null)
    val currentPlayer: StateFlow<Player?> = _currentPlayer.asStateFlow()

    fun createPlayer(name: String) {
        val player = Player(name = name)
        firebaseService.updatePlayerStatus(player.id, true)
        _currentPlayer.value = player
    }

    fun updatePlayerStatus(playerId: String, isOnline: Boolean) {
        firebaseService.updatePlayerStatus(playerId, isOnline)
    }
}