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
import kotlinx.coroutines.tasks.await

class FirebaseService {
    val db = Firebase.firestore

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


    fun observeGameReadiness(gameId: String, callback: (Boolean) -> Unit) {
        db.collection("games").document(gameId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseService", "Game readiness listen failed", error)
                    return@addSnapshotListener
                }

                snapshot?.let { doc ->
                    val player1Ready = doc.getBoolean("player1Ready") ?: false
                    val player2Ready = doc.getBoolean("player2Ready") ?: false
                    val status = doc.getString("status")

                    Log.d("FirebaseService", "Game readiness update - P1: $player1Ready, P2: $player2Ready, Status: $status")

                    callback(player1Ready || player2Ready)

                    if (player1Ready && player2Ready && status == GameStatus.SETUP.toString()) {
                        updateGameStatus(gameId, GameStatus.IN_PROGRESS)
                        Log.d("FirebaseService", "Both players ready, triggering navigation")
                    }
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

    fun updateGameStatus(gameId: String, status: GameStatus) {
        db.collection("games").document(gameId)
            .update("status", status.toString())
            .addOnSuccessListener {
                Log.d("FirebaseService", "Game status updated to: $status")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error updating game status", e)
            }
    }

    fun handleGameOver(gameId: String, winnerId: String) {
        val updates = mapOf(
            "status" to GameStatus.FINISHED.toString(),
            "winner" to winnerId,
            "currentTurn" to ""  // Clear the current turn to prevent further moves
        )

        db.collection("games").document(gameId)
            .update(updates)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Game over completed successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Game over failed", e)
            }
    }

    suspend fun getGame(gameId: String): Game? {
        return try {
            val document = db.collection("games").document(gameId).get().await()
            document.toObject(Game::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error getting game", e)
            null
        }
    }

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

}