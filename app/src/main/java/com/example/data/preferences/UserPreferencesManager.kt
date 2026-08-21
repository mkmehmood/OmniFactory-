package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.components.HistoryTimeSplit
import com.example.ui.theme.AccentTheme
import com.example.ui.theme.ShapeStructureStyle
import com.example.ui.theme.ThemeMode

class UserPreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var shapeStructureStyle: ShapeStructureStyle
        get() {
            val name = prefs.getString(KEY_SHAPE_STRUCTURE, ShapeStructureStyle.ROUNDED.name)
                ?: ShapeStructureStyle.ROUNDED.name
            return try {
                ShapeStructureStyle.valueOf(name)
            } catch (e: Exception) {
                ShapeStructureStyle.ROUNDED
            }
        }
        set(value) {
            prefs.edit().putString(KEY_SHAPE_STRUCTURE, value.name).apply()
        }

    var themeMode: ThemeMode
        get() {
            val name = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
            return try {
                ThemeMode.valueOf(name)
            } catch (e: Exception) {
                ThemeMode.DARK
            }
        }
        set(value) {
            prefs.edit().putString(KEY_THEME_MODE, value.name).apply()
        }

    var accentTheme: AccentTheme
        get() {
            val name = prefs.getString(KEY_ACCENT_THEME, AccentTheme.AQUA.name) ?: AccentTheme.AQUA.name
            return try {
                AccentTheme.valueOf(name)
            } catch (e: Exception) {
                AccentTheme.AQUA
            }
        }
        set(value) {
            prefs.edit().putString(KEY_ACCENT_THEME, value.name).apply()
        }

    var lastHistoryTimeSplit: HistoryTimeSplit
        get() {
            val name = prefs.getString(KEY_HISTORY_TIME_SPLIT, HistoryTimeSplit.ALL_TIME.name)
                ?: HistoryTimeSplit.ALL_TIME.name
            return try {
                HistoryTimeSplit.valueOf(name)
            } catch (e: Exception) {
                HistoryTimeSplit.ALL_TIME
            }
        }
        set(value) {
            prefs.edit().putString(KEY_HISTORY_TIME_SPLIT, value.name).apply()
        }

    var showDoubleZeroKey: Boolean
        get() = prefs.getBoolean(KEY_SHOW_DOUBLE_ZERO, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_DOUBLE_ZERO, value).apply()

    var enableAnimations: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_ANIMATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_ANIMATIONS, value).apply()

    var hapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    companion object {
        private const val PREFS_NAME = "calculator_user_prefs"
        private const val KEY_SHAPE_STRUCTURE = "pref_shape_structure"
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_ACCENT_THEME = "pref_accent_theme"
        private const val KEY_HISTORY_TIME_SPLIT = "pref_history_time_split"
        private const val KEY_SHOW_DOUBLE_ZERO = "pref_show_double_zero"
        private const val KEY_ENABLE_ANIMATIONS = "pref_enable_animations"
        private const val KEY_HAPTIC_ENABLED = "pref_haptic_enabled"
        private const val KEY_SOUND_ENABLED = "pref_sound_enabled"
    }
}
