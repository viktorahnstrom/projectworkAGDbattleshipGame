package com.example.projectworkagd_battleshipgame.data.models

import android.content.Context
import android.media.MediaPlayer
import android.provider.MediaStore.Audio.Media
import com.example.projectworkagd_battleshipgame.R

class SoundManager(private val context: Context) {
    private var missSound: MediaPlayer? = null
    private var hitSound: MediaPlayer? = null
    private var victorySound: MediaPlayer? = null
    private var lostSound: MediaPlayer? = null

    init {
        missSound = MediaPlayer.create(context, R.raw.splash)
        hitSound = MediaPlayer.create(context, R.raw.explosion)
        victorySound = MediaPlayer.create(context, R.raw.won)
        lostSound = MediaPlayer.create(context, R.raw.lost)
    }

    fun playMissSound() {
        missSound?.let { player ->
            if (player.isPlaying) {
                player.seekTo(0)
            }
            player.start()
        }
    }

    fun playHitSound() {
        hitSound?.let { player ->
            if (player.isPlaying) {
                player.seekTo(0)
            }
            player.start()
        }
    }

    fun playVictorySound() {
        victorySound?.let { player ->
            if (player.isPlaying) {
                player.seekTo(0)
            }
            player.start()
        }
    }

    fun playDefeatSound() {
        lostSound?.let { player ->
            if (player.isPlaying) {
                player.seekTo(0)
            }
            player.start()
        }
    }

    fun release() {
        missSound?.release()
        missSound = null
        hitSound?.release()
        hitSound = null
        victorySound?.release()
        victorySound = null
        lostSound?.release()
        lostSound = null
    }
}