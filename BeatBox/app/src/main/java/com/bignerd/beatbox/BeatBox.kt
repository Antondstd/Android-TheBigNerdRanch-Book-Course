package com.bignerd.beatbox

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.media.SoundPool
import android.util.Log
import java.io.IOException

private const val TAG = "BeatBox"
private const val SOUNDS_FOLDER = "sample_sounds"
private const val MAX_SOUNDS = 5

class BeatBox(private val assets: AssetManager) {
    val sounds: List<Sound>

    private val soundPool = SoundPool.Builder().setMaxStreams(MAX_SOUNDS).build()
    init {
        sounds = loadSounds()
    }

    private fun load(sound: Sound){
        val afd: AssetFileDescriptor = assets.openFd(sound.assetPath)
        val soundId = soundPool.load(afd,1)
        sound.id = soundId
    }

    fun release() = soundPool.release()

    fun play(sound: Sound){
        sound.id?.let {
            soundPool.play(it,1.0f,1.0f,1,0,1.0f)
        }
    }

    private fun loadSounds(): List<Sound> {
        val soundNames: Array<String>
        try {
            soundNames = assets.list(SOUNDS_FOLDER)!!
        } catch (e: Exception) {
            Log.e(TAG, "Could not list assets", e)
            return emptyList()
        }
        val sounds = mutableListOf<Sound>()
        soundNames.forEach { filename ->
            val assetPath = "$SOUNDS_FOLDER/$filename"
            val sound = Sound(assetPath)
            try {
                load(sound)
                sounds.add(sound)
            }
            catch (ieo:IOException){
                Log.e(TAG,"Could not load sound - $filename",ieo)
            }
        }
        return sounds
    }
}