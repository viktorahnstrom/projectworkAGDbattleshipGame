package com.example.projectworkagd_battleshipgame.data.repositories

import com.example.projectworkagd_battleshipgame.data.models.Player
import com.example.projectworkagd_battleshipgame.data.remote.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.Thread.State

class PlayerRepository (private val firebaseService: FirebaseService) {
    private val _currentPlayer = MutableStateFlow<Player?>(null)
    val currentPlayer: StateFlow<Player?> = _currentPlayer.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    init {
        observePlayers()
    }

    fun createPlayer(name: String) {
        val player = Player(
            name = name,
            online = true
        )
        firebaseService.createPlayer(player)
        _currentPlayer.value = player
    }

    fun observePlayers() {
        firebaseService.observePlayers { playersList ->
            _players.value = playersList
        }
    }

    fun updatePlayerStatus(playerId: String, isOnline: Boolean) {
        firebaseService.updatePlayerStatus(playerId, isOnline)
    }

    fun cleanup() {
        _currentPlayer.value?.let { player ->
            updatePlayerStatus(player.id, false)
        }
    }
}