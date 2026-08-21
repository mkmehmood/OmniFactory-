package com.example.domain.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import com.example.ui.theme.HapticIntensity

object HapticFeedbackHelper {

    fun performKeypadTap(
        context: Context,
        view: View?,
        intensity: HapticIntensity
    ) {
        if (intensity == HapticIntensity.OFF) return

        // 1. Perform View haptic feedback with flags
        try {
            val feedbackConstant = when (intensity) {
                HapticIntensity.LIGHT -> HapticFeedbackConstants.CLOCK_TICK
                HapticIntensity.MEDIUM -> HapticFeedbackConstants.KEYBOARD_TAP
                HapticIntensity.STRONG, HapticIntensity.EXTREME -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        HapticFeedbackConstants.CONFIRM
                    } else {
                        HapticFeedbackConstants.KEYBOARD_TAP
                    }
                }
                HapticIntensity.OFF -> HapticFeedbackConstants.KEYBOARD_TAP
            }

            view?.performHapticFeedback(
                feedbackConstant,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
        } catch (_: Throwable) {
            try {
                view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } catch (_: Throwable) {}
        }

        // 2. Direct hardware vibrator activation for exact tactile sensation matching intensity
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                when (intensity) {
                    HapticIntensity.OFF -> {}
                    HapticIntensity.LIGHT -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(14L, 70))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(14L)
                        }
                    }
                    HapticIntensity.MEDIUM -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(26L, 160))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(26L)
                        }
                    }
                    HapticIntensity.STRONG -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(42L, 255))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(42L)
                        }
                    }
                    HapticIntensity.EXTREME -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val timings = longArrayOf(0, 32, 18, 48)
                            val amplitudes = intArrayOf(0, 255, 0, 255)
                            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(60L)
                        }
                    }
                }
            }
        } catch (_: Throwable) {}
    }
}
