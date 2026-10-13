package com.example.ui.background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * The user's background picture: picked from the gallery, cropped, then stored privately in the app.
 * The same picture is shown (blurred and faded to taste) behind every section and inside the
 * calculator's display window.
 */
class AppBackgroundState(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("app_background", Context.MODE_PRIVATE)

    /** Gallery pick, upright and downscaled. The crop dialog always starts from this file. */
    val originalFile = File(appContext.filesDir, "background_original.jpg")

    /** The cropped result that is actually displayed. */
    val croppedFile = File(appContext.filesDir, "background_cropped.jpg")

    /** Bumped whenever the stored picture changes so cached bitmaps reload. */
    var version by mutableIntStateOf(prefs.getInt(KEY_VERSION, 0))
        private set
    var hasImage by mutableStateOf(croppedFile.exists())
        private set
    var hasOriginal by mutableStateOf(originalFile.exists())
        private set

    /** 0..25: how soft the picture is. */
    var blur by mutableFloatStateOf(prefs.getFloat(KEY_BLUR, 6f))
        private set

    /** 0..0.9: how much of the theme colour is laid over the picture so content stays readable. */
    var fade by mutableFloatStateOf(prefs.getFloat(KEY_FADE, 0.35f))
        private set
    var showInDisplay by mutableStateOf(prefs.getBoolean(KEY_DISPLAY, true))
        private set
    var showAsBackground by mutableStateOf(prefs.getBoolean(KEY_BACKGROUND, true))
        private set

    fun updateBlur(value: Float) {
        blur = value.coerceIn(0f, 25f)
        prefs.edit().putFloat(KEY_BLUR, blur).apply()
    }

    fun updateFade(value: Float) {
        fade = value.coerceIn(0f, 0.9f)
        prefs.edit().putFloat(KEY_FADE, fade).apply()
    }

    fun updateShowInDisplay(value: Boolean) {
        showInDisplay = value
        prefs.edit().putBoolean(KEY_DISPLAY, value).apply()
    }

    fun updateShowAsBackground(value: Boolean) {
        showAsBackground = value
        prefs.edit().putBoolean(KEY_BACKGROUND, value).apply()
    }

    /** Reads a gallery picture, fixes its EXIF rotation, downsizes it and keeps it as the crop source. */
    fun importOriginal(uri: Uri): Boolean {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            appContext.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, MAX_ORIGINAL_SIDE)
            }
            val decoded = appContext.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            } ?: return false
            val orientation = try {
                appContext.contentResolver.openInputStream(uri)?.use {
                    ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (e: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }
            val upright = applyExif(decoded, orientation)
            originalFile.outputStream().use { upright.compress(Bitmap.CompressFormat.JPEG, 92, it) }
            hasOriginal = true
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loadOriginal(): Bitmap? = try {
        if (originalFile.exists()) BitmapFactory.decodeFile(originalFile.absolutePath) else null
    } catch (e: Exception) {
        null
    }

    fun saveCropped(bitmap: Bitmap): Boolean {
        return try {
            croppedFile.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            version += 1
            prefs.edit().putInt(KEY_VERSION, version).apply()
            hasImage = true
            true
        } catch (e: Exception) {
            false
        }
    }

    fun remove() {
        croppedFile.delete()
        originalFile.delete()
        hasImage = false
        hasOriginal = false
        version += 1
        prefs.edit().putInt(KEY_VERSION, version).apply()
    }

    /** Decodes the stored picture, softened by [blurAmount] (0..25). Runs off the main thread. */
    internal fun loadForDisplay(blurAmount: Float, maxSide: Int): Bitmap? {
        if (!croppedFile.exists()) return null
        return try {
            val side = if (blurAmount > 0f) minOf(maxSide, BLUR_WORKING_SIDE) else maxSide
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(croppedFile.absolutePath, bounds)
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, side)
                inMutable = true
            }
            val decoded = BitmapFactory.decodeFile(croppedFile.absolutePath, opts) ?: return null
            var bmp = decoded
            val longest = max(bmp.width, bmp.height)
            if (longest > side) {
                val s = side.toFloat() / longest
                bmp = Bitmap.createScaledBitmap(bmp, (bmp.width * s).roundToInt(), (bmp.height * s).roundToInt(), true)
            }
            if (blurAmount > 0f) {
                val radius = (blurAmount * 0.8f).roundToInt().coerceAtLeast(1)
                val work = bmp.copy(Bitmap.Config.ARGB_8888, true)
                val px = IntArray(work.width * work.height)
                work.getPixels(px, 0, work.width, 0, 0, work.width, work.height)
                boxBlur(px, work.width, work.height, radius)
                work.setPixels(px, 0, work.width, 0, 0, work.width, work.height)
                bmp = work
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        const val KEY_VERSION = "version"
        const val KEY_BLUR = "blur"
        const val KEY_FADE = "fade"
        const val KEY_DISPLAY = "display"
        const val KEY_BACKGROUND = "background"
        const val MAX_ORIGINAL_SIDE = 2048
        const val BLUR_WORKING_SIDE = 360
    }
}

val LocalAppBackground = staticCompositionLocalOf<AppBackgroundState?> { null }

private fun sampleSizeFor(w: Int, h: Int, target: Int): Int {
    var s = 1
    var longest = max(w, h)
    while (longest / 2 >= target) {
        longest /= 2
        s *= 2
    }
    return s
}

private fun applyExif(src: Bitmap, orientation: Int): Bitmap {
    val m = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.postScale(1f, -1f)
        else -> return src
    }
    return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
}

/** Two-pass separable box blur, applied twice, which closely approximates a gaussian. */
private fun boxBlur(px: IntArray, w: Int, h: Int, r: Int) {
    val tmp = IntArray(px.size)
    repeat(2) {
        blurPass(px, tmp, w, h, r, true)
        blurPass(tmp, px, w, h, r, false)
    }
}

private fun blurPass(src: IntArray, dst: IntArray, w: Int, h: Int, r: Int, horizontal: Boolean) {
    val len = if (horizontal) w else h
    val lines = if (horizontal) h else w
    val stride = if (horizontal) 1 else w
    val div = 2 * r + 1
    for (line in 0 until lines) {
        val base = if (horizontal) line * w else line
        var a = 0
        var rr = 0
        var g = 0
        var b = 0
        for (k in -r..r) {
            val p = src[base + k.coerceIn(0, len - 1) * stride]
            a += p ushr 24
            rr += (p shr 16) and 255
            g += (p shr 8) and 255
            b += p and 255
        }
        for (i in 0 until len) {
            dst[base + i * stride] = ((a / div) shl 24) or ((rr / div) shl 16) or ((g / div) shl 8) or (b / div)
            val add = src[base + (i + r + 1).coerceAtMost(len - 1) * stride]
            val rem = src[base + (i - r).coerceAtLeast(0) * stride]
            a += (add ushr 24) - (rem ushr 24)
            rr += ((add shr 16) and 255) - ((rem shr 16) and 255)
            g += ((add shr 8) and 255) - ((rem shr 8) and 255)
            b += (add and 255) - (rem and 255)
        }
    }
}

/** The stored picture as an [ImageBitmap], reloaded when the picture or the blur changes. */
@Composable
fun rememberBackgroundBitmap(state: AppBackgroundState, maxSide: Int = 1080): ImageBitmap? {
    val blurKey = (state.blur * 2f).roundToInt()
    val loaded by produceState<ImageBitmap?>(null, state.version, state.hasImage, blurKey, maxSide) {
        value = if (!state.hasImage) null else withContext(Dispatchers.Default) {
            state.loadForDisplay(blurKey / 2f, maxSide)?.asImageBitmap()
        }
    }
    return loaded
}

/** Full-screen picture behind a section; draws nothing when no picture is set or it is switched off. */
@Composable
fun AppBackgroundLayer(modifier: Modifier = Modifier) {
    val state = LocalAppBackground.current ?: return
    if (!state.hasImage || !state.showAsBackground) return
    val bitmap = rememberBackgroundBitmap(state, maxSide = 1080) ?: return
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = state.fade))
        )
    }
}

/**
 * Picture inside the calculator's display window. Place it first inside the window's Box so the
 * numbers draw on top; a floor on the fade keeps digits readable on bright photos.
 */
@Composable
fun BoxScope.CalculatorDisplayPicture() {
    val state = LocalAppBackground.current ?: return
    if (!state.hasImage || !state.showInDisplay) return
    val bitmap = rememberBackgroundBitmap(state, maxSide = 1080) ?: return
    Image(
        bitmap = bitmap,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.matchParentSize()
    )
    Box(
        modifier = Modifier
            .matchParentSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = max(state.fade, 0.3f)))
    )
    // A shade falling from the upper lip keeps the window reading as recessed
    Box(
        modifier = Modifier
            .matchParentSize()
            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.28f), Color.Transparent), endY = 90f))
    )
}
