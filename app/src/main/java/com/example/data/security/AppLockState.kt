package com.example.data.security

import android.content.Context
import android.os.SystemClock
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * App lock: one PIN, plus the list of sections that ask for it.
 *
 * The PIN is never stored. Only a salted PBKDF2 hash is kept, so it cannot be read back.
 * Wrong guesses are rate-limited. There is deliberately no recovery path: anyone who can reset a
 * PIN without knowing it could also bypass the lock.
 */
class AppLockState private constructor(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("app_lock", Context.MODE_PRIVATE)

    var hasPin by mutableStateOf(prefs.contains(KEY_HASH))
        private set

    /** Length of the stored PIN, so the lock screen can submit by itself on the last digit. */
    var pinLength by mutableIntStateOf(prefs.getInt(KEY_LENGTH, 4))
        private set

    /** Section keys that require the PIN. */
    val lockedSections = mutableStateListOf<String>().apply {
        addAll(prefs.getStringSet(KEY_SECTIONS, emptySet()).orEmpty())
    }

    /** Ask for the PIN again after the app has been in the background. */
    var relockOnBackground by mutableStateOf(prefs.getBoolean(KEY_RELOCK, true))
        private set

    /** True once the right PIN was entered; stays true until the app is backgrounded (if enabled). */
    var sessionUnlocked by mutableStateOf(false)
        private set

    var failedAttempts by mutableIntStateOf(0)
        private set
    private var lockedUntil by mutableLongStateOf(0L)

    enum class Result { OK, WRONG, LOCKED_OUT }

    fun isSectionLocked(key: String): Boolean = hasPin && !sessionUnlocked && key in lockedSections

    fun remainingLockoutSeconds(): Int {
        val left = lockedUntil - SystemClock.elapsedRealtime()
        return if (left > 0) ((left + 999) / 1000).toInt() else 0
    }

    /** Checks [pin]; on success unlocks every locked section until the next relock. */
    fun unlock(pin: String): Result {
        if (remainingLockoutSeconds() > 0) return Result.LOCKED_OUT
        return if (matches(pin)) {
            failedAttempts = 0
            sessionUnlocked = true
            Result.OK
        } else {
            registerFailure()
            if (remainingLockoutSeconds() > 0) Result.LOCKED_OUT else Result.WRONG
        }
    }

    /** Verifies without unlocking, for confirming the current PIN before changing or removing it. */
    fun verify(pin: String): Result {
        if (remainingLockoutSeconds() > 0) return Result.LOCKED_OUT
        return if (matches(pin)) {
            failedAttempts = 0
            Result.OK
        } else {
            registerFailure()
            if (remainingLockoutSeconds() > 0) Result.LOCKED_OUT else Result.WRONG
        }
    }

    fun setPin(pin: String) {
        require(pin.length in MIN_LENGTH..MAX_LENGTH && pin.all { it.isDigit() })
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_HASH, hash(pin, salt))
            .putInt(KEY_LENGTH, pin.length)
            .apply()
        pinLength = pin.length
        hasPin = true
        sessionUnlocked = true // the person who just set it is clearly allowed in
    }

    fun removePin() {
        prefs.edit().remove(KEY_HASH).remove(KEY_SALT).remove(KEY_LENGTH).remove(KEY_SECTIONS).apply()
        hasPin = false
        lockedSections.clear()
        sessionUnlocked = false
        failedAttempts = 0
    }

    fun setSectionLocked(key: String, locked: Boolean) {
        if (locked && key !in lockedSections) lockedSections.add(key)
        if (!locked) lockedSections.remove(key)
        prefs.edit().putStringSet(KEY_SECTIONS, lockedSections.toSet()).apply()
    }

    fun updateRelockOnBackground(value: Boolean) {
        relockOnBackground = value
        prefs.edit().putBoolean(KEY_RELOCK, value).apply()
    }

    fun onAppBackgrounded() {
        if (relockOnBackground) sessionUnlocked = false
    }

    fun lockNow() {
        sessionUnlocked = false
    }

    private fun registerFailure() {
        failedAttempts += 1
        if (failedAttempts % ATTEMPTS_BEFORE_LOCKOUT == 0) {
            val steps = failedAttempts / ATTEMPTS_BEFORE_LOCKOUT
            val seconds = (30 * steps).coerceAtMost(300)
            lockedUntil = SystemClock.elapsedRealtime() + seconds * 1000L
        }
    }

    private fun matches(pin: String): Boolean {
        val saltB64 = prefs.getString(KEY_SALT, null) ?: return false
        val expected = prefs.getString(KEY_HASH, null) ?: return false
        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        return MessageDigest.isEqual(hash(pin, salt).toByteArray(), expected.toByteArray())
    }

    private fun hash(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, 20_000, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    companion object {
        const val MIN_LENGTH = 4
        const val MAX_LENGTH = 8
        private const val ATTEMPTS_BEFORE_LOCKOUT = 5
        private const val KEY_HASH = "hash"
        private const val KEY_SALT = "salt"
        private const val KEY_LENGTH = "length"
        private const val KEY_SECTIONS = "sections"
        private const val KEY_RELOCK = "relock"

        @Volatile
        private var instance: AppLockState? = null

        fun get(context: Context): AppLockState =
            instance ?: synchronized(this) {
                instance ?: AppLockState(context.applicationContext).also { instance = it }
            }
    }
}
