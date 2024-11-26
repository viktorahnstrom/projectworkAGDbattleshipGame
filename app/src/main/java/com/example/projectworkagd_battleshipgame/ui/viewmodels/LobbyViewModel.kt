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
import java.util.UUID

class LobbyViewModel(
    private val playerRepository: PlayerRepository = PlayerRepository(FirebaseService()),
    private val firebaseService: FirebaseService = FirebaseService()
) : ViewModel() {
    val players = playerRepository.players
    val currentPlayer = playerRepository.currentPlayer

    private val _challengeState = MutableStateFlow<ChallengeState?>(null)
    val challengeState: StateFlow<ChallengeState?> = _challengeState.asStateFlow()

    init {
        observeChallenges()
    }

    fun joinLobby(playerName: String) {
        playerRepository.createPlayer(playerName)
    }

    fun challengePlayer(opponent: Player) {
        currentPlayer.value?.let { player ->
            if (player.id != opponent.id) {
                val challengeId = UUID.randomUUID().toString()
                firebaseService.createChallenge(player.id, opponent.id, challengeId)
                _challengeState.value = ChallengeState.Sending(challengeId, opponent.name)
            }
        }
    }

    fun acceptChallenge(challengeId: String) {
        val gameId = UUID.randomUUID().toString()
        firebaseService.handleChallengeAccepted(challengeId, gameId)
        _challengeState.value = null
    }

    fun declineChallenge(challengeId: String) {
        firebaseService.deleteChallenge(challengeId)
        _challengeState.value = null
    }

    private fun observeChallenges() {
        viewModelScope.launch {
            currentPlayer.collect { player ->
                player?.let { currentPlayer ->
                    firebaseService.observeChallenges(currentPlayer.id) { challenges ->
                        val receivedChallenge = challenges.firstOrNull { it.toPlayerId == currentPlayer.id }
                        val sentChallenge = challenges.firstOrNull { it.fromPlayerId == currentPlayer.id }

                        when {
                            receivedChallenge != null -> {
                                _challengeState.value = ChallengeState.Receiving(receivedChallenge.id, receivedChallenge.fromPlayerId)
                            }
                            sentChallenge != null -> {
                                _challengeState.value = ChallengeState.Sending(sentChallenge.id, players.value.find { it.id == sentChallenge.toPlayerId }?.name ?: "Unknown")
                            }
                            else -> {
                                _challengeState.value = null
                            }
                        }
                    }
                }
            }
        }
    }

    sealed class ChallengeState {
        data class Sending(val challengeId: String, val opponentName: String) : ChallengeState()
        data class Receiving(val challengeId: String, val challengerId: String): ChallengeState()
    }

    override fun onCleared() {
        super.onCleared()
        playerRepository.cleanup()
    }



}