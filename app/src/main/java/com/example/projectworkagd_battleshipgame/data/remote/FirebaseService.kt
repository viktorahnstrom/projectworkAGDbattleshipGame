package com.example.projectworkagd_battleshipgame.data.remote

import android.util.Log
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

    fun updatePlayerStatus(playerId: String, isOnline: Boolean) {
        database.child("players").child("isOnline").setValue(isOnline)
    }
}