package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Matrix
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.background.AppBackgroundState
import com.example.ui.background.LocalAppBackground
import com.example.ui.background.rememberBackgroundBitmap
import com.example.ui.theme.DigitalMonospaceFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Settings block: pick a gallery picture, crop and rotate it, then tune blur and fade.
 * The picture shows inside the calculator display and behind every other section.
 */
@Composable
fun BackgroundPictureSection(modifier: Modifier = Modifier) {
    val state = LocalAppBackground.current ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showCrop by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) { state.importOriginal(uri) }
                if (ok) showCrop = true
                else Toast.makeText(context, "Could not open that picture", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(
                "Background Picture",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Choose a photo, crop it, and set how soft it looks. It appears inside the calculator display and behind every other section.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        BackgroundPreview(state)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SoftButton(
                onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                accentColor = MaterialTheme.colorScheme.primary,
                filled = true,
                modifier = Modifier.weight(1f)
            ) { Text(if (state.hasImage) "Change picture" else "Choose from gallery", fontSize = 13.sp) }
            if (state.hasImage && state.hasOriginal) {
                SoftButton(onClick = { showCrop = true }) { Text("Crop", fontSize = 13.sp) }
            }
            if (state.hasImage) {
                SoftButton(onClick = { state.remove() }) { Text("Remove", fontSize = 13.sp) }
            }
        }

        if (state.hasImage) {
            SliderRow("Blur", "${state.blur.roundToInt()}", state.blur, 0f..25f) { state.updateBlur(it) }
            SliderRow("Fade", "${(state.fade * 100).roundToInt()}%", state.fade, 0f..0.9f) { state.updateFade(it) }
            ToggleRow("Show in calculator display", state.showInDisplay) { state.updateShowInDisplay(it) }
            ToggleRow("Use as app background", state.showAsBackground) { state.updateShowAsBackground(it) }
        }
    }

    if (showCrop) {
        BackgroundCropDialog(state = state, onDismiss = { showCrop = false })
    }
}

@Composable
private fun BackgroundPreview(state: AppBackgroundState) {
    val bitmap = if (state.hasImage) rememberBackgroundBitmap(state, maxSide = 720) else null
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .softWell(shape),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Background preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(shape)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = state.fade))
            )
            Text(
                "12,345.67",
                fontFamily = DigitalMonospaceFont,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text("No picture selected", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SliderRow(label: String, valueText: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(valueText, fontSize = 13.sp, fontFamily = DigitalMonospaceFont, color = MaterialTheme.colorScheme.primary)
        }
        SoftSlider(value = value, onValueChange = onChange, valueRange = range)
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, modifier = Modifier.weight(1f))
        SoftSwitch(checked = checked, onCheckedChange = onChange)
    }
}

/** Slider with a recessed track and a raised thumb. */
@Composable
fun SoftSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    val currentOnChange by rememberUpdatedState(onValueChange)
    var widthPx by remember { mutableFloatStateOf(0f) }
    val thumb = 28.dp
    val thumbPx = with(androidx.compose.ui.platform.LocalDensity.current) { thumb.toPx() }
    val span = valueRange.endInclusive - valueRange.start
    val fraction = ((value - valueRange.start) / span).coerceIn(0f, 1f)

    fun update(x: Float) {
        val usable = (widthPx - thumbPx).coerceAtLeast(1f)
        val f = ((x - thumbPx / 2f) / usable).coerceIn(0f, 1f)
        currentOnChange(valueRange.start + f * span)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .onSizeChanged { widthPx = it.width.toFloat() }
            .pointerInput(valueRange) { detectTapGestures { update(it.x) } }
            .pointerInput(valueRange) { detectDragGestures { change, _ -> update(change.position.x) } },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(10.dp).softWell(CircleShape))
        Box(
            modifier = Modifier
                .offset { IntOffset(((widthPx - thumbPx) * fraction).roundToInt(), 0) }
                .size(thumb)
                .softPlate(CircleShape)
        )
    }
}

private enum class CropShape(val label: String) { SCREEN("Screen"), PORTRAIT("3:4"), SQUARE("1:1") }

/**
 * Full-screen cropper: pinch to zoom, drag to move, rotate in 90° steps, pick a frame shape.
 * Starts from the stored original, so reopening it never degrades the picture.
 */
@Composable
private fun BackgroundCropDialog(state: AppBackgroundState, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    val config = androidx.compose.ui.platform.LocalConfiguration.current
    val screenAspect = config.screenWidthDp.toFloat() / config.screenHeightDp.toFloat()

    var rotation by remember { mutableIntStateOf(0) }
    var shape by remember { mutableStateOf(CropShape.SCREEN) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var saving by remember { mutableStateOf(false) }
    val saveHolder = remember { arrayOfNulls<() -> Unit>(1) }

    val source by produceState<Bitmap?>(null) { value = withContext(Dispatchers.IO) { state.loadOriginal() } }
    val rotated by produceState<Bitmap?>(null, source, rotation) {
        val src = source
        value = if (src == null) null else withContext(Dispatchers.Default) {
            if (rotation % 360 == 0) src else {
                val m = Matrix().apply { postRotate(rotation.toFloat()) }
                Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
            }
        }
    }
    val aspect = when (shape) {
        CropShape.SCREEN -> screenAspect
        CropShape.PORTRAIT -> 3f / 4f
        CropShape.SQUARE -> 1f
    }
    LaunchedEffect(rotation, shape) { zoom = 1f; pan = Offset.Zero }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        SoftSurface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SoftIconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cancel") }
                    Text(
                        "Crop picture",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                    )
                    SoftIconButton(onClick = { rotation = (rotation + 90) % 360 }) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    SoftIconButton(
                        enabled = rotated != null && !saving,
                        onClick = { saveHolder[0]?.invoke() }
                    ) { Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary) }
                }

                BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val maxWpx = constraints.maxWidth.toFloat()
                    val maxHpx = constraints.maxHeight.toFloat()
                    val frameW = if (maxWpx / maxHpx > aspect) maxHpx * aspect else maxWpx
                    val frameH = frameW / aspect
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val bmp = rotated

                    fun clamp(o: Offset, z: Float): Offset {
                        val b = bmp ?: return o
                        val s = max(frameW / b.width, frameH / b.height) * z
                        val mx = max(0f, (b.width * s - frameW) / 2f)
                        val my = max(0f, (b.height * s - frameH) / 2f)
                        return Offset(o.x.coerceIn(-mx, mx), o.y.coerceIn(-my, my))
                    }

                    fun cropAndSave() {
                        val b = bmp ?: return
                        saving = true
                        scope.launch {
                            val ok = withContext(Dispatchers.Default) {
                                val s = max(frameW / b.width, frameH / b.height) * zoom
                                val left = (frameW - b.width * s) / 2f + pan.x
                                val top = (frameH - b.height * s) / 2f + pan.y
                                val sx = ((0f - left) / s).roundToInt().coerceIn(0, b.width - 1)
                                val sy = ((0f - top) / s).roundToInt().coerceIn(0, b.height - 1)
                                val sw = (frameW / s).roundToInt().coerceIn(1, b.width - sx)
                                val sh = (frameH / s).roundToInt().coerceIn(1, b.height - sy)
                                val region = Bitmap.createBitmap(b, sx, sy, sw, sh)
                                val outW = min(1080, sw)
                                val outH = (outW * sh.toFloat() / sw).roundToInt().coerceAtLeast(1)
                                val out = if (outW == sw) region else Bitmap.createScaledBitmap(region, outW, outH, true)
                                state.saveCropped(out)
                            }
                            saving = false
                            if (ok) onDismiss()
                        }
                    }
                    saveHolder[0] = ::cropAndSave

                    Box(
                        modifier = Modifier
                            .size(with(density) { frameW.toDp() }, with(density) { frameH.toDp() })
                            .softWell(RoundedCornerShape(8.dp))
                            .clipToBounds()
                            .pointerInput(bmp, aspect) {
                                detectTransformGestures { _, p, z, _ ->
                                    zoom = (zoom * z).coerceIn(1f, 6f)
                                    pan = clamp(pan + p, zoom)
                                }
                            }
                    ) {
                        if (bmp != null) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val s = max(size.width / bmp.width, size.height / bmp.height) * zoom
                                val dw = (bmp.width * s).roundToInt()
                                val dh = (bmp.height * s).roundToInt()
                                val c = clamp(pan, zoom)
                                drawImage(
                                    image = bmp.asImageBitmap(),
                                    srcSize = IntSize(bmp.width, bmp.height),
                                    dstOffset = IntOffset(
                                        ((size.width - dw) / 2f + c.x).roundToInt(),
                                        ((size.height - dh) / 2f + c.y).roundToInt()
                                    ),
                                    dstSize = IntSize(dw, dh)
                                )
                                // rule-of-thirds guides
                                val guide = Color.White.copy(alpha = 0.55f)
                                for (i in 1..2) {
                                    drawLine(guide, Offset(size.width * i / 3f, 0f), Offset(size.width * i / 3f, size.height), 1f)
                                    drawLine(guide, Offset(0f, size.height * i / 3f), Offset(size.width, size.height * i / 3f), 1f)
                                }
                                drawRect(Color.White.copy(alpha = 0.8f), style = Stroke(width = 2f))
                            }
                        } else {
                            Text("Loading…", modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    CropShape.values().forEach { option ->
                        DottedFilterChip(
                            selected = shape == option,
                            onClick = { shape = option },
                            label = option.label
                        )
                    }
                }
                Text(
                    "Pinch to zoom, drag to move.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SoftButton(
                    onClick = { saveHolder[0]?.invoke() },
                    enabled = rotated != null && !saving,
                    accentColor = MaterialTheme.colorScheme.primary,
                    filled = true,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (saving) "Saving…" else "Use this picture", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}
