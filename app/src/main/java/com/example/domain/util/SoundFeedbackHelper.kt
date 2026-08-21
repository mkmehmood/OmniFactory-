package com.example.domain.util

import android.content.Context
import android.media.AudioManager
import android.view.SoundEffectConstants
import android.view.View

object SoundFeedbackHelper {

    fun playClickSound(context: Context, view: View?, isSoundEnabled: Boolean) {
        if (!isSoundEnabled) return

        try {
            view?.playSoundEffect(SoundEffectConstants.CLICK)
        } catch (_: Throwable) {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.6f)
            } catch (_: Throwable) {}
        }
    }
}
