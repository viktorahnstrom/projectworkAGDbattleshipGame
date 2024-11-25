package com.example.projectworkagd_battleshipgame.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.projectworkagd_battleshipgame.data.remote.FirebaseService
import com.example.projectworkagd_battleshipgame.data.repositories.PlayerRepository

class LobbyViewModel(
    private val playerRepository: PlayerRepository = PlayerRepository(FirebaseService())
) : ViewModel() {
    val players = playerRepository.players
    val currentPlayer = playerRepository.currentPlayer

    fun joinLobby(playerName: String) {
        playerRepository.createPlayer(playerName)
    }

    fun challengePlayer(opponentId: String) {
        currentPlayer.value?.let { player ->
            // challenge logic will be implemented
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerRepository.cleanup()
    }

}