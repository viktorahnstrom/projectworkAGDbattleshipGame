package com.example.projectworkagd_battleshipgame.data.remote

import android.util.Log
import com.example.projectworkagd_battleshipgame.data.models.Player
import com.google.android.gms.games.Game
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue

class FirebaseService {
    private val database = Firebase.database.reference
    private val auth = Firebase.auth

    fun createPlayer(player: Player) {
        database.child("players").child(player.id).setValue(player)
    }

    fun observePlayers(onPlayersUpdate: (List<Player>) -> Unit) {
        database.child("players").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val playerList = mutableListOf<Player>()
                for (playerSnapshot in snapshot.children) {
                    playerSnapshot.getValue<Player>()?.let {
                        playersList.add(it)
                    }
                }
                onPlayersUpdate(playersList)
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

    fun observeGame(gameId: String, onUpdate: (game) -> Unit) {
        database.child("games").child(gameId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<Game>()?.let { onUpdate(it) }
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