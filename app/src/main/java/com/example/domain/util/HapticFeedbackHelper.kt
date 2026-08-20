package com.example.domain.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

object HapticFeedbackHelper {

    fun performKeypadTap(context: Context, view: View?, isHapticEnabled: Boolean) {
        if (!isHapticEnabled) return

        // 1. Perform View haptic feedback with flags to ensure responsiveness
        try {
            view?.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
        } catch (_: Throwable) {
            try {
                view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } catch (_: Throwable) {}
        }

        // 2. Direct hardware vibrator activation for guaranteed tactile sensation
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(28L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(28L)
                }
            }
        } catch (_: Throwable) {
            // Ignore security or hardware exceptions gracefully
        }
    }
}
