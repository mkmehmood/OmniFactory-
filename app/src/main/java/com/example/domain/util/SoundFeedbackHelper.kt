package com.example.domain.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.SoundEffectConstants
import android.view.View
import com.example.ui.theme.SoundIntensity

object SoundFeedbackHelper {
    private var softToneGen: ToneGenerator? = null
    private var standardToneGen: ToneGenerator? = null
    private var loudToneGen: ToneGenerator? = null
    private var extremeToneGen: ToneGenerator? = null

    init {
        initGenerators()
    }

    private fun initGenerators() {
        try {
            softToneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 40)
            standardToneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            loudToneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 90)
            extremeToneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (_: Throwable) {}
    }

    fun playClickSound(
        context: Context,
        view: View?,
        intensity: SoundIntensity
    ) {
        if (intensity == SoundIntensity.OFF) return

        // 1. Play standard View sound effect
        try {
            view?.playSoundEffect(SoundEffectConstants.CLICK)
        } catch (_: Throwable) {}

        // 2. Play AudioManager FX key click with appropriate volume scale
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val fxVolume = when (intensity) {
                SoundIntensity.OFF -> 0.0f
                SoundIntensity.SOFT -> 0.4f
                SoundIntensity.STANDARD -> 0.75f
                SoundIntensity.LOUD -> 0.95f
                SoundIntensity.EXTREME_BOOST -> 1.0f
            }
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, fxVolume)
        } catch (_: Throwable) {}

        // 3. ToneGenerator for guaranteed clear audible playback
        try {
            when (intensity) {
                SoundIntensity.OFF -> {}
                SoundIntensity.SOFT -> {
                    if (softToneGen == null) softToneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 40)
                    softToneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 15)
                }
                SoundIntensity.STANDARD -> {
                    if (standardToneGen == null) standardToneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
                    standardToneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 20)
                }
                SoundIntensity.LOUD -> {
                    if (loudToneGen == null) loudToneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 90)
                    loudToneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 28)
                }
                SoundIntensity.EXTREME_BOOST -> {
                    // Extreme acoustic burst that rings clear, loud and uninhibited
                    if (extremeToneGen == null) extremeToneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                    extremeToneGen?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 35)
                }
            }
        } catch (_: Throwable) {
            try {
                initGenerators()
            } catch (_: Throwable) {}
        }
    }
}
