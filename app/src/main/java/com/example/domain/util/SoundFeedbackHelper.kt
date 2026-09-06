package com.example.domain.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.SoundEffectConstants
import android.view.View
import com.example.ui.theme.SoundIntensity

object SoundFeedbackHelper {
    @Volatile
    private var cachedAudioManager: AudioManager? = null
    @Volatile
    private var audioManagerInitialized = false

    @Volatile
    private var toneGen: ToneGenerator? = null

    private fun getAudioManager(context: Context): AudioManager? {
        if (!audioManagerInitialized) {
            synchronized(this) {
                if (!audioManagerInitialized) {
                    cachedAudioManager = try {
                        context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                    } catch (_: Throwable) {
                        null
                    }
                    audioManagerInitialized = true
                }
            }
        }
        return cachedAudioManager
    }

    private fun getOrCreateToneGenerator(): ToneGenerator? {
        if (toneGen == null) {
            synchronized(this) {
                if (toneGen == null) {
                    toneGen = try {
                        ToneGenerator(AudioManager.STREAM_MUSIC, 85)
                    } catch (_: Throwable) {
                        null
                    }
                }
            }
        }
        return toneGen
    }

    fun playClickSound(
        context: Context,
        view: View?,
        intensity: SoundIntensity
    ) {
        if (intensity == SoundIntensity.OFF) return

        // 1. Play standard View sound effect (hardware-managed, zero allocation)
        try {
            view?.playSoundEffect(SoundEffectConstants.CLICK)
        } catch (_: Throwable) {}

        // 2. Play AudioManager FX key click with appropriate volume scale
        try {
            val audioManager = getAudioManager(context)
            val fxVolume = when (intensity) {
                SoundIntensity.OFF -> 0.0f
                SoundIntensity.SOFT -> 0.35f
                SoundIntensity.STANDARD -> 0.70f
                SoundIntensity.LOUD -> 0.95f
                SoundIntensity.EXTREME_BOOST -> 1.0f
            }
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, fxVolume)
        } catch (_: Throwable) {}

        // 3. Optional boost tone only for EXTREME_BOOST to prevent audio buffer contention
        if (intensity == SoundIntensity.EXTREME_BOOST) {
            try {
                getOrCreateToneGenerator()?.startTone(ToneGenerator.TONE_PROP_BEEP, 18)
            } catch (_: Throwable) {}
        }
    }
}
