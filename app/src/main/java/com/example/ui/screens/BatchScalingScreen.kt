package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.ui.components.LiquidGlassTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calculator.BatchScalingOutput
import com.example.domain.calculator.BatchSubMode
import com.example.domain.calculator.RoundingOption
import com.example.domain.calculator.ScalingMode
import com.example.ui.BatchScalingUiState
import com.example.ui.ComponentInput
import com.example.ui.ScalingPreset
import com.example.ui.components.BlinkingCursorPointer
import com.example.ui.components.InputFieldCopyPasteMenu
import com.example.ui.components.SmartStationKeypad
import com.example.ui.theme.DigitalMonospaceFont

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BatchScalingScreen(
    uiState: BatchScalingUiState,
    onAddComponent: () -> Unit,
    onRemoveComponent: (String) -> Unit,
    onUpdateComponentName: (String, String) -> Unit,
    onUpdateComponentAmount: (String, String) -> Unit,
    onToggleComponentLock: (String) -> Unit,
    onSetScalingMode: (ScalingMode) -> Unit,
    onSetBatchSubMode: (BatchSubMode) -> Unit,
    onSetTargetTotal: (String) -> Unit,
    onSetBatchSize: (String) -> Unit,
    onSetBatchCount: (String) -> Unit,
    onSetAvailableAmount: (String) -> Unit,
    onSetRoundingOption: (RoundingOption) -> Unit,
    onToggleFormulaExpanded: () -> Unit,
    onApplyMultiplier: (Double) -> Unit,
    onLoadPreset: (ScalingPreset) -> Unit,
    onClearAll: () -> Unit,
    onCopyResults: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val scrollState = rememberScrollState()

    val surfaceBg = if (isDark) Color(0x14FFFFFF) else Color(0x0C000000)
    val cardBorderColor = if (isDark) Color(0x22FFFFFF) else Color(0x22000000)

    var focusedFieldId by remember { mutableStateOf<String?>(null) }
    var focusedFieldLabel by remember { mutableStateOf<String?>(null) }

    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible) {
        if (isImeVisible) {
            focusedFieldId = null
            focusedFieldLabel = null
        }
    }

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            focusedFieldId = null
            focusedFieldLabel = null
        }
    }

    fun appendNumericToken(current: String, token: String): String {
        return when (token) {
            "." -> if (current.contains(".")) current else if (current.isEmpty()) "0." else "$current."
            "00" -> if (current.isEmpty() || current == "0") "0" else current + "00"
            else -> if (current == "0") token else current + token
        }
    }

    fun backspaceNumericToken(current: String): String {
        return if (current.isNotEmpty()) current.dropLast(1) else ""
    }

    val handleAppend: (String) -> Unit = { token ->
        val id = focusedFieldId
        if (id != null) {
            if (id.startsWith("comp_amount_")) {
                val compId = id.removePrefix("comp_amount_")
                val current = uiState.components.find { it.id == compId }?.amountText ?: ""
                onUpdateComponentAmount(compId, appendNumericToken(current, token))
            } else when (id) {
                "target_total" -> onSetTargetTotal(appendNumericToken(uiState.targetTotalText, token))
                "batch_size" -> onSetBatchSize(appendNumericToken(uiState.batchSizeText, token))
                "batch_count" -> onSetBatchCount(appendNumericToken(uiState.batchCountText, token))
                "available_amount" -> onSetAvailableAmount(appendNumericToken(uiState.availableAmountText, token))
            }
        }
    }

    val handleBackspace: () -> Unit = {
        val id = focusedFieldId
        if (id != null) {
            if (id.startsWith("comp_amount_")) {
                val compId = id.removePrefix("comp_amount_")
                val current = uiState.components.find { it.id == compId }?.amountText ?: ""
                onUpdateComponentAmount(compId, backspaceNumericToken(current))
            } else when (id) {
                "target_total" -> onSetTargetTotal(backspaceNumericToken(uiState.targetTotalText))
                "batch_size" -> onSetBatchSize(backspaceNumericToken(uiState.batchSizeText))
                "batch_count" -> onSetBatchCount(backspaceNumericToken(uiState.batchCountText))
                "available_amount" -> onSetAvailableAmount(backspaceNumericToken(uiState.availableAmountText))
            }
        }
    }

    val handleClear: () -> Unit = {
        val id = focusedFieldId
        if (id != null) {
            if (id.startsWith("comp_amount_")) {
                val compId = id.removePrefix("comp_amount_")
                onUpdateComponentAmount(compId, "")
            } else when (id) {
                "target_total" -> onSetTargetTotal("")
                "batch_size" -> onSetBatchSize("")
                "batch_count" -> onSetBatchCount("")
                "available_amount" -> onSetAvailableAmount("")
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("batch_scaling_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = if (focusedFieldId != null) 320.dp else 24.dp)
        ) {
            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Batch Scaler",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Industrial Batch Distribution",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TextButton(
                    onClick = onClearAll,
                    modifier = Modifier.testTag("clear_batch_components_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Mode Selector (Target Total vs Batch Size)
            ModeTabSelector(
                currentMode = uiState.scalingMode,
                onSelectMode = onSetScalingMode,
                primaryColor = primaryColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Component Ingredients List Card
            ComponentsEditorCard(
                components = uiState.components,
                originalTotal = uiState.calculationOutput.originalGrandTotal,
                focusedFieldId = focusedFieldId,
                onFocusField = { id, label ->
                    focusedFieldId = id
                    focusedFieldLabel = label
                },
                onAddComponent = onAddComponent,
                onRemoveComponent = onRemoveComponent,
                onUpdateName = onUpdateComponentName,
                onUpdateAmount = onUpdateComponentAmount,
                onToggleLock = onToggleComponentLock,
                primaryColor = primaryColor,
                surfaceBg = surfaceBg,
                borderColor = cardBorderColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Scaling Target / Batch Parameters Card
            ScalingParametersCard(
                scalingMode = uiState.scalingMode,
                batchSubMode = uiState.batchSubMode,
                targetTotalText = uiState.targetTotalText,
                batchSizeText = uiState.batchSizeText,
                batchCountText = uiState.batchCountText,
                availableAmountText = uiState.availableAmountText,
                output = uiState.calculationOutput,
                focusedFieldId = focusedFieldId,
                onFocusField = { id, label ->
                    focusedFieldId = id
                    focusedFieldLabel = label
                },
                onSetBatchSubMode = onSetBatchSubMode,
                onSetTargetTotal = onSetTargetTotal,
                onSetBatchSize = onSetBatchSize,
                onSetBatchCount = onSetBatchCount,
                onSetAvailableAmount = onSetAvailableAmount,
                primaryColor = primaryColor,
                surfaceBg = surfaceBg,
                borderColor = cardBorderColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Error message if invalid
            if (!uiState.calculationOutput.isValid && uiState.calculationOutput.errorMessage != null) {
                ErrorCard(
                    message = uiState.calculationOutput.errorMessage,
                    isDark = isDark
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 4. Essential Results Tables
            if (uiState.calculationOutput.isValid) {
                ResultsSection(
                    output = uiState.calculationOutput,
                    onCopyResults = onCopyResults,
                    primaryColor = primaryColor,
                    surfaceBg = surfaceBg,
                    borderColor = cardBorderColor,
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // SMART STATION KEYPAD POPUP FROM BOTTOM
        AnimatedVisibility(
            visible = focusedFieldId != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SmartStationKeypad(
                activeFieldLabel = focusedFieldLabel,
                onDigit = { handleAppend(it) },
                onDoubleZero = { handleAppend("00") },
                onDecimal = { handleAppend(".") },
                onBackspace = { handleBackspace() },
                onClear = { handleClear() },
                onDone = {
                    focusedFieldId = null
                    focusedFieldLabel = null
                },
                showDoubleZeroKey = true,
                showDecimalKey = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun ModeTabSelector(
    currentMode: ScalingMode,
    onSelectMode: (ScalingMode) -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (isDark) Color(0x22FFFFFF) else Color(0x22000000),
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        color = if (isDark) Color(0x10FFFFFF) else Color(0x0C000000)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            // Mode 1: Target Total
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectMode(ScalingMode.TARGET_TOTAL) }
                    .testTag("tab_mode_target_total"),
                shape = RoundedCornerShape(10.dp),
                color = if (currentMode == ScalingMode.TARGET_TOTAL) primaryColor else Color.Transparent
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Target Total",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentMode == ScalingMode.TARGET_TOTAL) {
                            if (isDark) Color.Black else Color.White
                        } else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Mode 2: Batch Size
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectMode(ScalingMode.BATCH_SIZE) }
                    .testTag("tab_mode_batch_size"),
                shape = RoundedCornerShape(10.dp),
                color = if (currentMode == ScalingMode.BATCH_SIZE) primaryColor else Color.Transparent
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Batch Sizing",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentMode == ScalingMode.BATCH_SIZE) {
                            if (isDark) Color.Black else Color.White
                        } else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ComponentsEditorCard(
    components: List<ComponentInput>,
    originalTotal: Double,
    focusedFieldId: String?,
    onFocusField: (String, String) -> Unit,
    onAddComponent: () -> Unit,
    onRemoveComponent: (String) -> Unit,
    onUpdateName: (String, String) -> Unit,
    onUpdateAmount: (String, String) -> Unit,
    onToggleLock: (String) -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Component List",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = primaryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Original Total: ${String.format(java.util.Locale.US, "%.1f", originalTotal)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = primaryColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Component Rows
            components.forEachIndexed { index, component ->
                val fieldId = "comp_amount_${component.id}"
                val isFocused = focusedFieldId == fieldId
                val fieldLabel = "${component.name.ifBlank { "Component ${index + 1}" }} Amount"

                ComponentRowItem(
                    index = index,
                    component = component,
                    isAmountFocused = isFocused,
                    onFocusAmount = { onFocusField(fieldId, fieldLabel) },
                    onDismissKeypad = { onFocusField("", "") },
                    onNameChange = { onUpdateName(component.id, it) },
                    onAmountChange = { onUpdateAmount(component.id, it) },
                    onToggleLock = { onToggleLock(component.id) },
                    onDelete = { onRemoveComponent(component.id) },
                    canDelete = components.size > 1,
                    primaryColor = primaryColor,
                    isDark = isDark
                )
                if (index < components.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAddComponent,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_component_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor.copy(alpha = 0.15f),
                    contentColor = primaryColor
                ),
                contentPadding = PaddingValues(vertical = 9.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add Component",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ComponentRowItem(
    index: Int,
    component: ComponentInput,
    isAmountFocused: Boolean,
    onFocusAmount: () -> Unit,
    onDismissKeypad: () -> Unit = {},
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onToggleLock: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean,
    primaryColor: Color,
    isDark: Boolean
) {
    var showCopyPasteMenu by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isAmountFocused) primaryColor else if (component.isLocked) Color(0x66F59E0B) else (if (isDark) Color(0x18FFFFFF) else Color(0x12000000)),
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (component.isLocked) {
            Color(0xFFF59E0B).copy(alpha = 0.08f)
        } else {
            if (isDark) Color(0x10FFFFFF) else Color(0x06000000)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index number
            Text(
                text = "${index + 1}.",
                fontSize = 12.sp,
                fontFamily = DigitalMonospaceFont,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(18.dp)
            )

            // Name Field
            LiquidGlassTextField(
                value = component.name,
                onValueChange = onNameChange,
                placeholder = { Text("Name", fontSize = 12.sp) },
                singleLine = true,
                accentColor = primaryColor,
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("component_name_input_$index")
                    .onFocusChanged {
                        if (it.isFocused) {
                            onDismissKeypad()
                        }
                    },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Amount Field
            Box(
                modifier = Modifier.weight(0.9f)
            ) {
                LiquidGlassTextField(
                    value = component.amountText,
                    onValueChange = onAmountChange,
                    placeholder = { Text("0", fontSize = 12.sp) },
                    suffix = {
                        if (isAmountFocused) {
                            BlinkingCursorPointer(color = primaryColor)
                        }
                    },
                    singleLine = true,
                    readOnly = true,
                    forceFocused = isAmountFocused,
                    accentColor = primaryColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("component_amount_input_$index"),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp,
                        fontFamily = DigitalMonospaceFont,
                        fontWeight = FontWeight.Bold
                    )
                )

                // Clickable overlay with tap and long-press
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(component.id) {
                            detectTapGestures(
                                onTap = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    onFocusAmount()
                                },
                                onLongPress = { showCopyPasteMenu = true }
                            )
                        }
                )

                InputFieldCopyPasteMenu(
                    expanded = showCopyPasteMenu,
                    onDismissRequest = { showCopyPasteMenu = false },
                    value = component.amountText,
                    onPaste = { onAmountChange(it) }
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Lock Toggle Button
            IconButton(
                onClick = onToggleLock,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (component.isLocked) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color.Transparent)
                    .testTag("component_lock_toggle_$index")
            ) {
                Icon(
                    imageVector = if (component.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = if (component.isLocked) "Locked (Fixed)" else "Unlocked",
                    tint = if (component.isLocked) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Delete Button
            if (canDelete) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("component_delete_$index")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Component",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScalingParametersCard(
    scalingMode: ScalingMode,
    batchSubMode: BatchSubMode,
    targetTotalText: String,
    batchSizeText: String,
    batchCountText: String,
    availableAmountText: String,
    output: BatchScalingOutput,
    focusedFieldId: String?,
    onFocusField: (String, String) -> Unit,
    onSetBatchSubMode: (BatchSubMode) -> Unit,
    onSetTargetTotal: (String) -> Unit,
    onSetBatchSize: (String) -> Unit,
    onSetBatchCount: (String) -> Unit,
    onSetAvailableAmount: (String) -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean
) {
    var showTargetTotalCopyPaste by remember { mutableStateOf(false) }
    var showBatchSizeCopyPaste by remember { mutableStateOf(false) }
    var showBatchCountCopyPaste by remember { mutableStateOf(false) }
    var showAvailableAmountCopyPaste by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (scalingMode == ScalingMode.TARGET_TOTAL) "Scaling Target" else "Batch Parameters",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (scalingMode == ScalingMode.TARGET_TOTAL) {
                // Target Total Mode
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LiquidGlassTextField(
                        value = targetTotalText,
                        onValueChange = onSetTargetTotal,
                        label = { Text("Target Grand Total") },
                        placeholder = { Text("0") },
                        suffix = {
                            if (focusedFieldId == "target_total") {
                                BlinkingCursorPointer(color = primaryColor)
                            }
                        },
                        singleLine = true,
                        readOnly = true,
                        forceFocused = (focusedFieldId == "target_total"),
                        accentColor = primaryColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_total_input"),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 15.sp,
                            fontFamily = DigitalMonospaceFont,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        onFocusField("target_total", "Target Grand Total")
                                    },
                                    onLongPress = { showTargetTotalCopyPaste = true }
                                )
                            }
                    )

                    InputFieldCopyPasteMenu(
                        expanded = showTargetTotalCopyPaste,
                        onDismissRequest = { showTargetTotalCopyPaste = false },
                        value = targetTotalText,
                        onPaste = { onSetTargetTotal(it) }
                    )
                }
            } else {
                // Batch Size Mode
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LiquidGlassTextField(
                        value = batchSizeText,
                        onValueChange = onSetBatchSize,
                        label = { Text("Batch Size (weight/quantity per unit)") },
                        placeholder = { Text("0") },
                        suffix = {
                            if (focusedFieldId == "batch_size") {
                                BlinkingCursorPointer(color = primaryColor)
                            }
                        },
                        singleLine = true,
                        readOnly = true,
                        forceFocused = (focusedFieldId == "batch_size"),
                        accentColor = primaryColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("batch_size_input"),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 15.sp,
                            fontFamily = DigitalMonospaceFont,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        onFocusField("batch_size", "Batch Size")
                                    },
                                    onLongPress = { showBatchSizeCopyPaste = true }
                                )
                            }
                    )

                    InputFieldCopyPasteMenu(
                        expanded = showBatchSizeCopyPaste,
                        onDismissRequest = { showBatchSizeCopyPaste = false },
                        value = batchSizeText,
                        onPaste = { onSetBatchSize(it) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Submode Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = batchSubMode == BatchSubMode.BY_BATCH_COUNT,
                        onClick = { onSetBatchSubMode(BatchSubMode.BY_BATCH_COUNT) },
                        label = { Text("By Number of Batches", fontSize = 12.sp) },
                        leadingIcon = if (batchSubMode == BatchSubMode.BY_BATCH_COUNT) {
                            { Icon(Icons.Default.Check, null, Modifier.size(14.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = if (isDark) Color.Black else Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("batch_submode_count")
                    )

                    FilterChip(
                        selected = batchSubMode == BatchSubMode.BY_AVAILABLE_AMOUNT,
                        onClick = { onSetBatchSubMode(BatchSubMode.BY_AVAILABLE_AMOUNT) },
                        label = { Text("By Total Available", fontSize = 12.sp) },
                        leadingIcon = if (batchSubMode == BatchSubMode.BY_AVAILABLE_AMOUNT) {
                            { Icon(Icons.Default.Check, null, Modifier.size(14.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = if (isDark) Color.Black else Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("batch_submode_available")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (batchSubMode == BatchSubMode.BY_BATCH_COUNT) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LiquidGlassTextField(
                            value = batchCountText,
                            onValueChange = onSetBatchCount,
                            label = { Text("Number of Batches") },
                            placeholder = { Text("1") },
                            suffix = {
                                if (focusedFieldId == "batch_count") {
                                    BlinkingCursorPointer(color = primaryColor)
                                }
                            },
                            singleLine = true,
                            readOnly = true,
                            forceFocused = (focusedFieldId == "batch_count"),
                            accentColor = primaryColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("batch_count_input"),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 15.sp,
                                fontFamily = DigitalMonospaceFont,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            onFocusField("batch_count", "Number of Batches")
                                        },
                                        onLongPress = { showBatchCountCopyPaste = true }
                                    )
                                }
                        )

                        InputFieldCopyPasteMenu(
                            expanded = showBatchCountCopyPaste,
                            onDismissRequest = { showBatchCountCopyPaste = false },
                            value = batchCountText,
                            onPaste = { onSetBatchCount(it) }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LiquidGlassTextField(
                            value = availableAmountText,
                            onValueChange = onSetAvailableAmount,
                            label = { Text("Total Available Amount") },
                            placeholder = { Text("0") },
                            suffix = {
                                if (focusedFieldId == "available_amount") {
                                    BlinkingCursorPointer(color = primaryColor)
                                }
                            },
                            singleLine = true,
                            readOnly = true,
                            forceFocused = (focusedFieldId == "available_amount"),
                            accentColor = primaryColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("available_amount_input"),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 15.sp,
                                fontFamily = DigitalMonospaceFont,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            onFocusField("available_amount", "Total Available Amount")
                                        },
                                        onLongPress = { showAvailableAmountCopyPaste = true }
                                    )
                                }
                        )

                        InputFieldCopyPasteMenu(
                            expanded = showAvailableAmountCopyPaste,
                            onDismissRequest = { showAvailableAmountCopyPaste = false },
                            value = availableAmountText,
                            onPaste = { onSetAvailableAmount(it) }
                        )
                    }

                    if (output.remainderAmount != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Fits ${output.fullBatchesCount} full batches • Remainder: ${String.format(java.util.Locale.US, "%.1f", output.remainderAmount)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsSection(
    output: BatchScalingOutput,
    onCopyResults: (String) -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with copy button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Results",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = {
                        val text = buildFormattedResultsCopy(output)
                        onCopyResults(text)
                    },
                    modifier = Modifier.testTag("copy_batch_results_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Results",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Scaled Components Table
            Text(
                text = "SCALED DISTRIBUTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            ScaledResultsTable(
                output = output,
                primaryColor = primaryColor,
                isDark = isDark
            )

            // 2. Per-Batch Breakdown Table if in Batch Mode
            if (output.isBatchMode && output.numberOfBatches > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "PER-BATCH BREAKDOWN (${output.fullBatchesCount} BATCHES)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                PerBatchBreakdownTable(
                    output = output,
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
private fun ScaledResultsTable(
    output: BatchScalingOutput,
    primaryColor: Color,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isDark) Color(0x22FFFFFF) else Color(0x22000000),
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0x0EFFFFFF) else Color(0x06000000)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Table Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Component",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = "Original",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.0f)
                )
                Text(
                    text = "Scaled",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = primaryColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.1f)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = if (isDark) Color(0x18FFFFFF) else Color(0x14000000)
            )

            // Rows
            output.results.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1.3f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.isLocked) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }

                    Text(
                        text = item.originalAmountFormatted,
                        fontSize = 12.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.0f)
                    )

                    Text(
                        text = item.scaledAmountFormatted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = if (item.isLocked) Color(0xFFF59E0B) else primaryColor,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.1f)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = if (isDark) Color(0x18FFFFFF) else Color(0x14000000)
            )

            // Totals Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = String.format(java.util.Locale.US, "%.1f", output.originalGrandTotal),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.0f)
                )
                Text(
                    text = String.format(java.util.Locale.US, "%.1f", output.scaledGrandTotal),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = primaryColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.1f)
                )
            }
        }
    }
}

@Composable
private fun PerBatchBreakdownTable(
    output: BatchScalingOutput,
    primaryColor: Color,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isDark) Color(0x22FFFFFF) else Color(0x22000000),
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0x0EFFFFFF) else Color(0x06000000)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Component",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = "Per 1 Batch",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = Color(0xFF10B981),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.0f)
                )
                Text(
                    text = "Total (${output.fullBatchesCount}x)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = primaryColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.1f)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = if (isDark) Color(0x18FFFFFF) else Color(0x14000000)
            )

            output.results.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1.2f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = item.perBatchAmountFormatted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = DigitalMonospaceFont,
                        color = Color(0xFF10B981),
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.0f)
                    )

                    Text(
                        text = item.scaledAmountFormatted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = primaryColor,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.1f)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = if (isDark) Color(0x18FFFFFF) else Color(0x14000000)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BATCH TOTAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = String.format(java.util.Locale.US, "%.1f", output.singleBatchGrandTotal),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = Color(0xFF10B981),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.0f)
                )
                Text(
                    text = String.format(java.util.Locale.US, "%.1f", output.scaledGrandTotal),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = primaryColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.1f)
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun buildFormattedResultsCopy(output: BatchScalingOutput): String {
    return buildString {
        appendLine("=== BATCH & RATIO SCALING ===")
        appendLine("Original Total: ${String.format(java.util.Locale.US, "%.1f", output.originalGrandTotal)}")
        appendLine("Scaled Total: ${String.format(java.util.Locale.US, "%.1f", output.scaledGrandTotal)}")
        if (output.isBatchMode) {
            appendLine("Batches: ${output.fullBatchesCount}x (${String.format(java.util.Locale.US, "%.1f", output.singleBatchGrandTotal)} per batch)")
            if (output.remainderAmount != null) {
                appendLine("Remainder: ${String.format(java.util.Locale.US, "%.1f", output.remainderAmount)}")
            }
        }
        appendLine("\n--- SCALED COMPONENTS ---")
        output.results.forEach {
            val lockTag = if (it.isLocked) " [LOCKED]" else ""
            appendLine("• ${it.name}$lockTag: ${it.scaledAmountFormatted} (Orig: ${it.originalAmountFormatted})")
        }
        if (output.isBatchMode) {
            appendLine("\n--- PER BATCH ---")
            output.results.forEach {
                appendLine("• ${it.name}: ${it.perBatchAmountFormatted}")
            }
        }
    }
}
