package com.example.projectworkagd_battleshipgame.data.models

import com.google.firebase.Timestamp
import java.util.UUID

data class Challenge(
    val id: String = UUID.randomUUID().toString(),
    val fromPlayerId: String= "",
    val toPlayerId: String = "",
    val timestamp: Long = 0
)