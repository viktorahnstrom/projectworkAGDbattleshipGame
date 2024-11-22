package com.example.projectworkagd_battleshipgame.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectworkagd_battleshipgame.data.models.Player
import com.example.projectworkagd_battleshipgame.data.remote.FirebaseService
import com.example.projectworkagd_battleshipgame.data.repositories.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LobbyViewModel(
    private val playerRepository: PlayerRepository = PlayerRepository(FirebaseService())
) : ViewModel() {
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    fun joinLobby(playerName: String) {
        viewModelScope.launch {
            playerRepository.createPlayer(playerName)
        }
    }

    fun challengePlayer(opponentId: String) {
        viewModelScope.launch {
            playerRepository.currentPlayer.value?.let { currentPlayer ->
                // will implement challenge logic here later
            }
        }
    }
}