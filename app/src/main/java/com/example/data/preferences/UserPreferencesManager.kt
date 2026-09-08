package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.components.HistoryTimeSplit
import com.example.ui.theme.AnimationIntensity
import com.example.ui.theme.AppFontStyle
import com.example.ui.theme.AppThemeSkin
import com.example.ui.theme.ComponentSizeStyle
import com.example.ui.theme.HapticIntensity
import com.example.ui.theme.ShapeStructureStyle
import com.example.ui.theme.SoundIntensity
import com.example.ui.theme.ThemeMode

class UserPreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var appThemeSkin: AppThemeSkin
        get() {
            val name = prefs.getString(KEY_APP_THEME_SKIN, AppThemeSkin.DARK_CHARCOAL.name)
                ?: AppThemeSkin.DARK_CHARCOAL.name
            return try {
                AppThemeSkin.valueOf(name)
            } catch (e: Exception) {
                AppThemeSkin.DARK_CHARCOAL
            }
        }
        set(value) {
            prefs.edit().putString(KEY_APP_THEME_SKIN, value.name).apply()
        }

    var fontStyle: AppFontStyle
        get() {
            val name = prefs.getString(KEY_FONT_STYLE, AppFontStyle.SYSTEM.name)
                ?: AppFontStyle.SYSTEM.name
            return try {
                AppFontStyle.valueOf(name)
            } catch (e: Exception) {
                AppFontStyle.SYSTEM
            }
        }
        set(value) {
            prefs.edit().putString(KEY_FONT_STYLE, value.name).apply()
        }

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

    var componentSizeStyle: ComponentSizeStyle
        get() {
            val name = prefs.getString(KEY_COMPONENT_SIZE, ComponentSizeStyle.STANDARD.name)
                ?: ComponentSizeStyle.STANDARD.name
            return try {
                ComponentSizeStyle.valueOf(name)
            } catch (e: Exception) {
                ComponentSizeStyle.STANDARD
            }
        }
        set(value) {
            prefs.edit().putString(KEY_COMPONENT_SIZE, value.name).apply()
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

    var animationIntensity: AnimationIntensity
        get() {
            val name = prefs.getString(KEY_ANIMATION_INTENSITY, AnimationIntensity.BALANCED.name)
                ?: AnimationIntensity.BALANCED.name
            return try {
                AnimationIntensity.valueOf(name)
            } catch (e: Exception) {
                AnimationIntensity.BALANCED
            }
        }
        set(value) {
            prefs.edit().putString(KEY_ANIMATION_INTENSITY, value.name).apply()
        }

    var hapticIntensity: HapticIntensity
        get() {
            val name = prefs.getString(KEY_HAPTIC_INTENSITY, HapticIntensity.MEDIUM.name)
                ?: HapticIntensity.MEDIUM.name
            return try {
                HapticIntensity.valueOf(name)
            } catch (e: Exception) {
                HapticIntensity.MEDIUM
            }
        }
        set(value) {
            prefs.edit().putString(KEY_HAPTIC_INTENSITY, value.name).apply()
        }

    var soundIntensity: SoundIntensity
        get() {
            val name = prefs.getString(KEY_SOUND_INTENSITY, SoundIntensity.STANDARD.name)
                ?: SoundIntensity.STANDARD.name
            return try {
                SoundIntensity.valueOf(name)
            } catch (e: Exception) {
                SoundIntensity.STANDARD
            }
        }
        set(value) {
            prefs.edit().putString(KEY_SOUND_INTENSITY, value.name).apply()
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

    var lastBackupTimestamp: Long
        get() = prefs.getLong(KEY_LAST_BACKUP_TIMESTAMP, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_BACKUP_TIMESTAMP, value).apply()

    // Backward compatibility helpers
    var enableAnimations: Boolean
        get() = animationIntensity != AnimationIntensity.OFF
        set(value) {
            animationIntensity = if (value) AnimationIntensity.BALANCED else AnimationIntensity.OFF
        }

    var hapticEnabled: Boolean
        get() = hapticIntensity != HapticIntensity.OFF
        set(value) {
            hapticIntensity = if (value) HapticIntensity.MEDIUM else HapticIntensity.OFF
        }

    var soundEnabled: Boolean
        get() = soundIntensity != SoundIntensity.OFF
        set(value) {
            soundIntensity = if (value) SoundIntensity.STANDARD else SoundIntensity.OFF
        }

    companion object {
        private const val PREFS_NAME = "calculator_user_prefs"
        private const val KEY_APP_THEME_SKIN = "pref_app_theme_skin"
        private const val KEY_FONT_STYLE = "pref_font_style"
        private const val KEY_SHAPE_STRUCTURE = "pref_shape_structure"
        private const val KEY_COMPONENT_SIZE = "pref_component_size"
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_ANIMATION_INTENSITY = "pref_animation_intensity"
        private const val KEY_HAPTIC_INTENSITY = "pref_haptic_intensity"
        private const val KEY_SOUND_INTENSITY = "pref_sound_intensity"
        private const val KEY_HISTORY_TIME_SPLIT = "pref_history_time_split"
        private const val KEY_SHOW_DOUBLE_ZERO = "pref_show_double_zero"
        private const val KEY_LAST_BACKUP_TIMESTAMP = "pref_last_backup_timestamp"
    }
}
