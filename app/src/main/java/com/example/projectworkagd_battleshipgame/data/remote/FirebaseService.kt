package com.example.projectworkagd_battleshipgame.data.remote

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log
import com.example.projectworkagd_battleshipgame.data.models.Challenge
import com.example.projectworkagd_battleshipgame.data.models.Game
import com.example.projectworkagd_battleshipgame.data.models.GameStatus
import com.example.projectworkagd_battleshipgame.data.models.Player
import java.util.UUID

class FirebaseService {
    private val db = Firebase.firestore

    fun createPlayer(player: Player) {
        db.collection("players").document(player.id)
            .set(player)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Player created: ${player.id}")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error creating player", e)
            }
    }

    fun observePlayers(onPlayersUpdate: (List<Player>) -> Unit) {
        db.collection("players")
            .whereEqualTo("isOnline", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FirebaseService", "Listen failed", e)
                    return@addSnapshotListener
                }
                val players = snapshot?.documents?.mapNotNull {
                    it.toObject(Player::class.java)
                } ?: emptyList()
                onPlayersUpdate(players)
            }
    }

    fun updatePlayerStatus(playerId: String, isOnline: Boolean) {
        db.collection("players").document(playerId)
            .update("isOnline", isOnline)
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error updating player status", e)
            }
    }

    fun createChallenge(fromPlayerId: String, toPlayerId: String) {
        val challenge = Challenge(
            id = UUID.randomUUID().toString(),
            fromPlayerId = fromPlayerId,
            toPlayerId = toPlayerId,
            timestamp = System.currentTimeMillis()
        )
        db.collection("challenges").document(challenge.id)
            .set(challenge)
    }

    fun observeChallenges(playerId: String, onChallengesUpdate: (List<Challenge>) -> Unit) {
        db.collection("challenges")
            .whereEqualTo("toPlayerId", playerId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FirebaseService", "Challenge listen failed", e)
                    return@addSnapshotListener
                }
                val challenges = snapshot?.documents?.mapNotNull {
                    it.toObject(Challenge::class.java)
                } ?: emptyList()
                onChallengesUpdate(challenges)
            }
    }

    fun deleteChallenge(challengeId: String) {
        db.collection("challenges").document(challengeId)
            .delete()
            .addOnSuccessListener {
                Log.d("FirebaseService", "Challenge deleted: $challengeId")
            }
    }

    fun createGame(game: Game) {
        db.collection("games").document(game.id)
            .set(game)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Game created: ${game.id}")
                updatePlayerGameId(game.player1Id, game.id)
                updatePlayerGameId(game.player2Id, game.id)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error creating game", e)
            }
    }

    private fun updatePlayerGameId(playerId: String, gameId: String?) {
        db.collection("players").document(playerId)
            .update("currentGameId", gameId)
    }

    fun observeGame(gameId: String, onUpdate: (Game) -> Unit) {
        db.collection("games").document(gameId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FirebaseService", "Game listen failed", e)
                    return@addSnapshotListener
                }
                snapshot?.toObject(Game::class.java)?.let { onUpdate(it) }
            }
    }

    fun updateGameState(gameId: String, updates: Map<String, Any>) {
        db.collection("games").document(gameId)
            .update(updates)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Game state updated: $gameId")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error updating game state", e)
            }
    }

    fun handleGameOver(gameId: String, winnerId: String) {
        db.collection("games").document(gameId)
            .update(mapOf(
                "status" to GameStatus.FINISHED,
                "winner" to winnerId
            ))
            .addOnSuccessListener {
                deleteGame(gameId)
            }
    }

    fun handleChallengeAccepted(challengeId: String, gameId: String) {
        deleteChallenge(challengeId)
    }

    fun deleteGame(gameId: String) {
        db.collection("games").document(gameId)
            .get()
            .addOnSuccessListener { document ->
                document.toObject(Game::class.java)?.let { game ->
                    updatePlayerGameId(game.player1Id, null)
                    updatePlayerGameId(game.player2Id, null)
                }
                document.reference.delete()
                Log.d("FirebaseService", "Game deleted: $gameId")
            }
    }

    fun handlePlayerExit(playerId: String) {
        db.collection("challenges")
            .whereEqualTo("fromPlayerId", playerId)
            .get()
            .addOnSuccessListener { challenges ->
                challenges.forEach { it.reference.delete() }
                removePlayer(playerId)
            }
    }

    fun removePlayer(playerId: String) {
        db.collection("players").document(playerId)
            .get()
            .addOnSuccessListener { document ->
                document.toObject(Player::class.java)?.let { player ->
                    player.currentGameId?.let { deleteGame(it) }
                }
                document.reference.delete()
                Log.d("FirebaseService", "Player removed: $playerId")
            }
    }
}