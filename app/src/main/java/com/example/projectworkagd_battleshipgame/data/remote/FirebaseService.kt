package com.example.projectworkagd_battleshipgame.data.remote

import android.util.Log
import com.example.projectworkagd_battleshipgame.data.models.Game
import com.example.projectworkagd_battleshipgame.data.models.Player
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database


class FirebaseService {
    private val database = Firebase.database.reference
    private val auth = Firebase.auth

    fun createPlayer(player: Player) {
        database.child("players").child(player.id).setValue(player)
    }

    fun observePlayers(onPlayersUpdate: (List<Player>) -> Unit) {
        database.child("players").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = snapshot.children.mapNotNull {
                    it.getValue(Player::class.java)
                }
                onPlayersUpdate(players)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseService", "Players observation failed: ${error.message}")
            }
        })
    }

    fun updatePlayerStatus(playerId: String, isOnline: Boolean) {
        database.child("players").child(playerId).child("isOnline").setValue(isOnline)
    }

    fun createGame(game: Game) {
        database.child("games").child(game.id).setValue(game)
    }

    fun observeGame(gameId: String, onUpdate: (Game) -> Unit) {
        database.child("games").child(gameId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val game = snapshot.getValue(Game::class.java)
                    game?.let { onUpdate(it) }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseService", "Game observation failed: ${error.message}")
                }
            })
    }

    fun updateGameState(gameId: String, updates: Map<String, Any>) {
        database.child("games").child(gameId).updateChildren(updates)
    }
}