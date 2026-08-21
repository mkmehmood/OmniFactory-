package com.example.domain.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.SoundEffectConstants
import android.view.View

object SoundFeedbackHelper {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (_: Throwable) {}
    }

    fun playClickSound(context: Context, view: View?, isSoundEnabled: Boolean) {
        if (!isSoundEnabled) return

        try {
            // First attempt to play system click sound
            view?.playSoundEffect(SoundEffectConstants.CLICK)
        } catch (_: Throwable) {}

        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.8f)
        } catch (_: Throwable) {}

        try {
            // ToneGenerator ensures a crisp audible tap even if system touch sounds are disabled in OS settings
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 20)
        } catch (_: Throwable) {}
    }
}
