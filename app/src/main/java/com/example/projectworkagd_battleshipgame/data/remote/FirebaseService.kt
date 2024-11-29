package com.example.projectworkagd_battleshipgame.data.remote

import android.os.Handler
import android.os.Looper
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log
import com.example.projectworkagd_battleshipgame.data.models.Challenge
import com.example.projectworkagd_battleshipgame.data.models.Game
import com.example.projectworkagd_battleshipgame.data.models.GameStatus
import com.example.projectworkagd_battleshipgame.data.models.Player
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class FirebaseService {
    val db = Firebase.firestore
    private var hasNavigated = false


    // ===== Player Management =====
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
            .whereEqualTo("online", true)
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
            .update("online", isOnline)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Updated player $playerId online status to $isOnline")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error updating player status", e)
            }
    }



    // ===== Game Management =====
    fun createGame(game: Game) {
        db.collection("games").document(game.id)
            .set(game)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Game successfully created: ${game.id}")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error creating game: ${game.id}", e)
            }
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



    // ===== Game Readiness Management =====
    fun observeGameReadiness(gameId: String, onBothReady: () -> Unit) {
        if (gameId.isBlank()) return

        db.collection("games").document(gameId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FirebaseService", "Error observing game readiness", e)
                    return@addSnapshotListener
                }

                val game = snapshot?.toObject(Game::class.java)
                if (game != null && game.player1Ready && game.player2Ready && !hasNavigated) {
                    hasNavigated = true
                    onBothReady()
                }
            }
    }

    fun updatePlayerReadiness(gameId: String, playerId: String, isPlayer1: Boolean) {
        if (gameId.isBlank()) return

        db.collection("games").document(gameId)
            .get()
            .addOnSuccessListener { document ->
                val game = document.toObject(Game::class.java)
                if (game != null) {
                    val readyField = if (game.player1Id == playerId) "player1Ready" else "player2Ready"
                    db.collection("games").document(gameId)
                        .update(readyField, true)
                }
            }
    }



    // ===== Challenge Management =====
    fun createChallenge(fromPlayerId: String, toPlayerId: String, challengeId: String) {
        val challenge = Challenge(
            id = challengeId,
            fromPlayerId = fromPlayerId,
            toPlayerId = toPlayerId,
            timestamp = System.currentTimeMillis()
        )
        db.collection("challenges").document(challenge.id)
            .set(challenge)
            .addOnSuccessListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    deleteChallenge(challenge.id)
                }, 30000)
            }
    }

    fun observeChallenges(playerId: String, onChallengesUpdate: (List<Challenge>) -> Unit) {
        db.collection("challenges")
            .whereEqualTo("fromPlayerId", playerId)
            .addSnapshotListener { snapshot, e ->
                handleChallengeSnapshot(snapshot, e, onChallengesUpdate)
            }

        db.collection("challenges")
            .whereEqualTo("toPlayerId", playerId)
            .addSnapshotListener { snapshot, e ->
                handleChallengeSnapshot(snapshot, e, onChallengesUpdate)
            }
    }

    fun handleChallengeAccepted(challengeId: String, gameId: String) {
        val batch = db.batch()
        val challengeRef = db.collection("challenges").document(challengeId)

        batch.update(challengeRef, mapOf(
            "status" to "accepted",
            "gameId" to gameId
        ))

        batch.commit()
            .addOnSuccessListener {
                Log.d("FirebaseService", "Challenge accepted with gameId: $gameId")
            }
    }

    fun deleteChallenge(challengeId: String) {
        db.collection("challenges").document(challengeId)
            .delete()
            .addOnSuccessListener {
                Log.d("FirebaseService", "Challenge deleted: $challengeId")
            }
    }



    // ===== Game State Management =====
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
    }



    // ===== Private Helper Methods =====
    private fun handleChallengeSnapshot(
        snapshot: QuerySnapshot?,
        e: FirebaseFirestoreException?,
        onChallengesUpdate: (List<Challenge>) -> Unit
    ) {
        if (e != null) {
            Log.e("FirebaseService", "Challenge listen failed", e)
            return
        }

        val challenges = snapshot?.documents?.mapNotNull {
            it.toObject(Challenge::class.java)
        } ?: emptyList()

        onChallengesUpdate(challenges)
    }

    private fun updatePlayerGameId(playerId: String, gameId: String?) {
        db.collection("players").document(playerId)
            .update("currentGameId", gameId)
    }
}