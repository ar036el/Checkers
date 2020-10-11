/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.managers

import android.media.MediaPlayer
import android.media.SoundPool
import el.arn.ultimatecheckers.appRoot
import el.arn.ultimatecheckers.managers.preferences_managers.GamePreferencesManager
import el.arn.ultimatecheckers.managers.preferences_managers.Preference
import el.arn.ultimatecheckers.managers.themed_resources.ThemedResources

interface SoundEffectsManager {
    fun playSoundEffectIfAny(soundEffectOption: SoundEffectOptions)
    enum class SoundEffectOptions { PieceCaptured, PieceMovedPlayer1, PieceMovedPlayer2, TurnedIntoKing } //todo should I put also win,lose&neutral?
}

class SoundEffectsManagerImplByMediaPlayerCycle : SoundEffectsManager {

    private val mediaPlayers = Array<MediaPlayer?>(5) { null }
    private var index = 0

    override fun playSoundEffectIfAny(soundEffectOption: SoundEffectsManager.SoundEffectOptions) {
        val res = getRawResFromEnum(soundEffectOption) ?: return

        clearCurrentMediaPlayerIfNotNull()
        playSoundFromCurrentMediaPlayer(res)
        advanceIndex()
    }

    private fun clearCurrentMediaPlayerIfNotNull() {
        if (mediaPlayers[index] != null) {
            mediaPlayers[index]?.stop()
            mediaPlayers[index]?.release()
            mediaPlayers[index] = null
        }
    }

    private fun playSoundFromCurrentMediaPlayer(soundRes: Int) {
        mediaPlayers[index] = MediaPlayer.create(appRoot.applicationContext, soundRes)
        if (mediaPlayers[index] != null) {
            mediaPlayers[index]?.start()
        }
    }

    private fun advanceIndex() {
        index = if (index == mediaPlayers.lastIndex) 0 else index + 1
    }

    private fun getRawResFromEnum(soundEffect: SoundEffectsManager.SoundEffectOptions): Int? {
        return when (soundEffect) {
            SoundEffectsManager.SoundEffectOptions.PieceCaptured -> ThemedResources.Raws.soundEffectPieceCaptured.getResource()
            SoundEffectsManager.SoundEffectOptions.PieceMovedPlayer1 -> ThemedResources.Raws.soundEffectPieceMovedPlayer1.getResource()
            SoundEffectsManager.SoundEffectOptions.PieceMovedPlayer2 -> ThemedResources.Raws.soundEffectPieceMovedPlayer2.getResource()
            SoundEffectsManager.SoundEffectOptions.TurnedIntoKing -> ThemedResources.Raws.soundEffectPieceTurnedIntoKing.getResource()
        }
    }

}

class SoundEffectsManagerImplBySoundPool(gamePreferencesManager: GamePreferencesManager) : SoundEffectsManager {

    private val maxStreams = 3
    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(maxStreams).build()
    init {
        loadSoundEffectsToSoundPool()
        gamePreferencesManager.soundEffectsTheme.addListener( object : Preference.Listener<Int> {
            override fun prefHasChanged(preference: Preference<Int>, value: Int) {
                loadSoundEffectsToSoundPool()
            }
        })
    }

    var soundId_PieceCaptured: Int? = null
    var soundId_PieceMovedPlayer1: Int? = null
    var soundId_PieceMovedPlayer2: Int? = null
    var soundId_TurnedIntoKing: Int? = null

    private fun loadSoundEffectsToSoundPool() {
        fun loadSoundIfAny(soundEffectOption: SoundEffectsManager.SoundEffectOptions): Int? = getSoundResource(soundEffectOption)?.let { soundPool.load(appRoot.applicationContext, it, 1) }
        soundId_PieceCaptured = loadSoundIfAny(SoundEffectsManager.SoundEffectOptions.PieceCaptured)
        soundId_PieceMovedPlayer1 = loadSoundIfAny(SoundEffectsManager.SoundEffectOptions.PieceMovedPlayer1)
        soundId_PieceMovedPlayer2 = loadSoundIfAny(SoundEffectsManager.SoundEffectOptions.PieceMovedPlayer2)
        soundId_TurnedIntoKing = loadSoundIfAny(SoundEffectsManager.SoundEffectOptions.TurnedIntoKing)
    }

    private fun getSoundResource(soundEffect: SoundEffectsManager.SoundEffectOptions): Int? {
        return when (soundEffect) {
            SoundEffectsManager.SoundEffectOptions.PieceCaptured -> ThemedResources.Raws.soundEffectPieceCaptured.getResource()
            SoundEffectsManager.SoundEffectOptions.PieceMovedPlayer1 -> ThemedResources.Raws.soundEffectPieceMovedPlayer1.getResource()
            SoundEffectsManager.SoundEffectOptions.PieceMovedPlayer2 -> ThemedResources.Raws.soundEffectPieceMovedPlayer2.getResource()
            SoundEffectsManager.SoundEffectOptions.TurnedIntoKing -> ThemedResources.Raws.soundEffectPieceTurnedIntoKing.getResource()
        }
    }

    override fun playSoundEffectIfAny(soundEffectOption: SoundEffectsManager.SoundEffectOptions) {
        val soundId = when (soundEffectOption) {
            SoundEffectsManager.SoundEffectOptions.PieceCaptured -> soundId_PieceCaptured
            SoundEffectsManager.SoundEffectOptions.PieceMovedPlayer1 -> soundId_PieceMovedPlayer1
            SoundEffectsManager.SoundEffectOptions.PieceMovedPlayer2 -> soundId_PieceMovedPlayer2
            SoundEffectsManager.SoundEffectOptions.TurnedIntoKing -> soundId_TurnedIntoKing
        }
        soundId?.let { soundPool.play(it, 1f, 1f, 1, 0, 1f) }
    }
}
