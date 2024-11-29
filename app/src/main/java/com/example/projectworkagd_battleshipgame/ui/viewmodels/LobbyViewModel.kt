package com.example.projectworkagd_battleshipgame.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectworkagd_battleshipgame.data.models.Player
import com.example.projectworkagd_battleshipgame.data.remote.FirebaseService
import com.example.projectworkagd_battleshipgame.data.repositories.GameRepository
import com.example.projectworkagd_battleshipgame.data.repositories.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class LobbyViewModel(
    private val playerRepository: PlayerRepository = PlayerRepository(FirebaseService()),
    private val firebaseService: FirebaseService = FirebaseService(),
    private val gameRepository: GameRepository = GameRepository(FirebaseService())
) : ViewModel() {
    val players = playerRepository.players
    private val currentPlayer = playerRepository.currentPlayer

    private val _challengeState = MutableStateFlow<ChallengeState?>(null)
    val challengeState: StateFlow<ChallengeState?> = _challengeState.asStateFlow()

    init {
        observeChallenges()
    }

    sealed class ChallengeState {
        data class Sending(
            val challengeId: String,
            val opponentName: String,
            val accepted: Boolean = false,
            val gameId: String = ""
        ) : ChallengeState()

        data class Receiving(
            val challengeId: String,
            val challengerId: String
        ) : ChallengeState()
    }

    fun getCurrentPlayerId(): String {
        return currentPlayer.value?.id ?: throw IllegalStateException("No current player found")
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

    fun acceptChallenge(challengeId: String, opponentId: String): String {
        val gameId = UUID.randomUUID().toString() // Generate a unique gameId
        val challengerId = currentPlayer.value?.id ?: throw IllegalStateException("Current player is null")
        Log.d("ViewModel", "Accepted challenge with gameId: $gameId")

        firebaseService.handleChallengeAccepted(challengeId, gameId)

        gameRepository.createGame(gameId, challengerId, opponentId)

        return gameId
    }

    fun declineChallenge(challengeId: String) {
        firebaseService.deleteChallenge(challengeId)
        _challengeState.value = null
    }

    private var hasNavigated = false


    private fun observeChallenges() {
        viewModelScope.launch {
            currentPlayer.collect { player ->
                player?.let { currentPlayer ->
                    firebaseService.observeChallenges(currentPlayer.id) { challenges ->
                        challenges.firstOrNull()?.let { challenge ->
                            when {
                                challenge.toPlayerId == currentPlayer.id -> {
                                    _challengeState.value = ChallengeState.Receiving(challenge.id, challenge.fromPlayerId)
                                }
                                challenge.fromPlayerId == currentPlayer.id -> {
                                    if (challenge.status == "accepted" && !hasNavigated) {
                                        hasNavigated = true
                                        _challengeState.value = ChallengeState.Sending(
                                            challengeId = challenge.id,
                                            opponentName = players.value.find { it.id == challenge.toPlayerId }?.name ?: "Unknown",
                                            accepted = true,
                                            gameId = challenge.gameId ?: ""
                                        )
                                    } else if (challenge.status != "accepted") {
                                        _challengeState.value = ChallengeState.Sending(
                                            challengeId = challenge.id,
                                            opponentName = players.value.find { it.id == challenge.toPlayerId }?.name ?: "Unknown",
                                            accepted = false
                                        )
                                    }
                                }
                            }
                        } ?: run {
                            _challengeState.value = null
                            hasNavigated = false
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerRepository.cleanup()
    }
}