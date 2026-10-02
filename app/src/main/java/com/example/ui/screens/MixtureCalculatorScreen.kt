package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import com.example.ui.components.MixtureHistorySheet
import com.example.ui.components.MixtureSaveDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.ui.components.LiquidGlassTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import com.example.ui.components.straightDottedLine
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.components.BlinkingCursorPointer
import com.example.ui.components.InputFieldCopyPasteMenu
import com.example.domain.mixture.BatchDivisionResult
import com.example.domain.mixture.BatchOperationMode
import com.example.domain.mixture.MixtureAdjustmentMode
import com.example.domain.mixture.RatioCalculationResult
import com.example.domain.mixture.RecalibrationResult
import com.example.domain.mixture.TargetIngredient
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Lock
import com.example.ui.components.SmartStationKeypad
import com.example.ui.MixtureUiState
import com.example.ui.MixtureViewModel
import com.example.ui.theme.DigitalMonospaceFont

@Composable
fun MixtureCalculatorScreen(
    viewModel: MixtureViewModel,
    onNavigateToHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val persistentRecipes by viewModel.persistentRecipes.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceBg = if (isDark) Color(0x18FFFFFF) else Color(0x0C000000)
    val cardBorderColor = if (isDark) Color(0x28FFFFFF) else Color(0x1E000000)

    var focusedFieldId by remember { mutableStateOf<String?>(null) }
    var focusedFieldLabel by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val isImeOpen = WindowInsets.ime.getBottom(density) > 0

    // Dismiss dialpad automatically on scroll or when system letter keyboard opens or on step switch
    LaunchedEffect(scrollState.isScrollInProgress, isImeOpen, uiState.currentStep) {
        if (scrollState.isScrollInProgress || isImeOpen) {
            focusedFieldId = null
            focusedFieldLabel = null
        }
    }

    fun appendNumericToken(current: String, token: String): String {
        return when (token) {
            ".", "," -> if (current.contains(".")) current else if (current.isEmpty()) "0." else "$current."
            "00" -> if (current.isEmpty() || current == "0") "0" else current + "00"
            else -> if (current == "0") token else current + token
        }
    }

    val handleAppend: (String) -> Unit = { token ->
        when {
            focusedFieldId == "step1_qty" -> {
                viewModel.setNewIngredientQty(appendNumericToken(uiState.newIngredientQty, token))
            }
            focusedFieldId?.startsWith("step3_actual_") == true -> {
                val ingredientId = focusedFieldId!!.removePrefix("step3_actual_")
                val currentVal = uiState.actualAddedTexts[ingredientId] ?: ""
                viewModel.setActualAdded(ingredientId, appendNumericToken(currentVal, token))
            }
            focusedFieldId == "step5_batch_count" -> {
                viewModel.setBatchCountText(appendNumericToken(uiState.batchCountText, token))
            }
        }
    }

    val handleBackspace: () -> Unit = {
        when {
            focusedFieldId == "step1_qty" -> {
                if (uiState.newIngredientQty.isNotEmpty()) {
                    viewModel.setNewIngredientQty(uiState.newIngredientQty.dropLast(1))
                }
            }
            focusedFieldId?.startsWith("step3_actual_") == true -> {
                val ingredientId = focusedFieldId!!.removePrefix("step3_actual_")
                val currentVal = uiState.actualAddedTexts[ingredientId] ?: ""
                if (currentVal.isNotEmpty()) {
                    viewModel.setActualAdded(ingredientId, currentVal.dropLast(1))
                }
            }
            focusedFieldId == "step5_batch_count" -> {
                if (uiState.batchCountText.isNotEmpty()) {
                    viewModel.setBatchCountText(uiState.batchCountText.dropLast(1))
                }
            }
        }
    }

    val handleClear: () -> Unit = {
        when {
            focusedFieldId == "step1_qty" -> {
                viewModel.setNewIngredientQty("")
            }
            focusedFieldId?.startsWith("step3_actual_") == true -> {
                val ingredientId = focusedFieldId!!.removePrefix("step3_actual_")
                viewModel.setActualAdded(ingredientId, "")
            }
            focusedFieldId == "step5_batch_count" -> {
                viewModel.setBatchCountText("")
            }
        }
    }

    val stepTitles = listOf(
        "ENTER TARGET INGREDIENTS",
        "TARGET RECIPE & SIMPLIFIED RATIO",
        "REFERENCE & ACTUAL INGREDIENTS ADDED",
        "RECALIBRATED MIXTURE PROPORTIONS",
        "BATCH SCALING: DIVIDE OR MULTIPLY",
        "WHAT WOULD YOU LIKE TO DO NEXT?"
    )

    val primaryButtonLabels = listOf(
        "Proceed to Step 2: Calculate Ratio",
        "Added Ingredient?",
        "Recalibrate Ratio",
        "Batch Scaling (÷ / ×)",
        "Options",
        "" // No primary button on Step 6
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("mixture_calculator_screen")
    ) {
        // Decorative large soft-blurred circular gradient shape anchored at bottom-of-screen
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(360.dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isDark) 0.15f else 0.08f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = if (isDark) 0.10f else 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = if (focusedFieldId != null) 300.dp else 0.dp)
        ) {
            // Top Toolbar: Formulation Title & Quick Save
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.currentRecipeTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (uiState.targetIngredients.isNotEmpty()) {
                    Surface(
                        onClick = {
                            viewModel.saveCurrentRecipeToDb()
                            Toast.makeText(context, "Formulation saved to history", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                        modifier = Modifier.testTag("quick_save_mixture_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Save to history",
                                tint = primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Save",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = primaryColor
                            )
                        }
                    }
                }
            }

            // Top: Horizontal Step Indicator (6 circles in a row)
            HorizontalStepIndicator(
                currentStep = uiState.currentStep,
                totalSteps = 6,
                onStepClick = { viewModel.goToStep(it) },
                primaryColor = primaryColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Step Title: "STEP N: [DESCRIPTION]" in bold accent color, uppercase styling
            Text(
                text = "STEP ${uiState.currentStep}: ${stepTitles[uiState.currentStep - 1]}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = primaryColor,
                letterSpacing = 1.2.sp,
                modifier = Modifier.testTag("mixture_step_title")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Step Body (Scrollable content)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    when (uiState.currentStep) {
                        1 -> Step1EnterTargetIngredients(
                            ingredients = uiState.targetIngredients,
                            nameInput = uiState.newIngredientName,
                            qtyInput = uiState.newIngredientQty,
                            unitInput = uiState.newIngredientUnit,
                            isQtyFocused = focusedFieldId == "step1_qty",
                            onFocusQty = {
                                if (focusedFieldId == "step1_qty") {
                                    focusedFieldId = null
                                    focusedFieldLabel = null
                                } else {
                                    focusedFieldId = "step1_qty"
                                    focusedFieldLabel = "Target Quantity"
                                }
                            },
                            onDismissKeypad = {
                                focusedFieldId = null
                                focusedFieldLabel = null
                            },
                            onNameChange = { viewModel.setNewIngredientName(it) },
                            onQtyChange = { viewModel.setNewIngredientQty(it) },
                            onUnitChange = { viewModel.setNewIngredientUnit(it) },
                            onAddIngredient = {
                                focusedFieldId = null
                                focusedFieldLabel = null
                                viewModel.addTargetIngredient()
                            },
                            onRemoveIngredient = { viewModel.removeTargetIngredient(it) },
                            onLoadSample = { viewModel.loadDefaultSample() },
                            primaryColor = primaryColor,
                            surfaceBg = surfaceBg,
                            borderColor = cardBorderColor,
                            isDark = isDark
                        )
                        2 -> Step2TargetRecipeAndRatio(
                            ratioResult = uiState.simplifiedRatio,
                            ingredients = uiState.targetIngredients,
                            onCopyRatio = {
                                clipboardManager.setText(AnnotatedString(uiState.simplifiedRatio.simplifiedRatioString))
                                Toast.makeText(context, "Ratio copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            primaryColor = primaryColor,
                            surfaceBg = surfaceBg,
                            borderColor = cardBorderColor,
                            isDark = isDark
                        )
                        3 -> Step3ReferenceAndActualAdded(
                            ingredients = uiState.targetIngredients,
                            anchorId = uiState.anchorIngredientId,
                            actualAddedTexts = uiState.actualAddedTexts,
                            focusedFieldId = focusedFieldId,
                            selectedMode = uiState.selectedAdjustmentMode,
                            onSelectMode = { viewModel.setAdjustmentMode(it) },
                            onSetAnchorOnly = { viewModel.setAllActualsToZeroExceptAnchor() },
                            onSetAllTargets = { viewModel.setAllActualsToTarget() },
                            onFocusField = { id, label ->
                                if (focusedFieldId == id) {
                                    focusedFieldId = null
                                    focusedFieldLabel = null
                                } else {
                                    focusedFieldId = id
                                    focusedFieldLabel = label
                                }
                            },
                            onSelectAnchor = { viewModel.setAnchorIngredient(it) },
                            onActualAddedChange = { id, text -> viewModel.setActualAdded(id, text) },
                            primaryColor = primaryColor,
                            surfaceBg = surfaceBg,
                            borderColor = cardBorderColor,
                            isDark = isDark
                        )
                        4 -> Step4RecalibratedProportions(
                            recalibrationResult = uiState.recalibrationResult,
                            selectedMode = uiState.selectedAdjustmentMode,
                            onSelectMode = { viewModel.setAdjustmentMode(it) },
                            showCompareBoth = uiState.showCompareBothModes,
                            onToggleCompareBoth = { viewModel.toggleCompareBothModes() },
                            onCopyAdjustments = {
                                val report = buildRecalibrationReportText(uiState.recalibrationResult)
                                clipboardManager.setText(AnnotatedString(report))
                                Toast.makeText(context, "Proportions copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            primaryColor = primaryColor,
                            surfaceBg = surfaceBg,
                            borderColor = cardBorderColor,
                            isDark = isDark
                        )
                        5 -> Step5BatchScaling(
                            recalibrationResult = uiState.recalibrationResult,
                            batchDivisionResult = uiState.batchDivisionResult,
                            selectedMode = uiState.selectedAdjustmentMode,
                            onSelectMode = { viewModel.setAdjustmentMode(it) },
                            batchOperationMode = uiState.batchOperationMode,
                            onSelectBatchOperationMode = { viewModel.setBatchOperationMode(it) },
                            batchCountText = uiState.batchCountText,
                            batchCount = uiState.batchCount,
                            isBatchCountFocused = focusedFieldId == "step5_batch_count",
                            onFocusBatchCount = {
                                if (focusedFieldId == "step5_batch_count") {
                                    focusedFieldId = null
                                    focusedFieldLabel = null
                                } else {
                                    focusedFieldId = "step5_batch_count"
                                    focusedFieldLabel = if (uiState.batchOperationMode == BatchOperationMode.DIVIDE) "Batches to Divide" else "Batch Multiplier"
                                }
                            },
                            onBatchCountChange = { viewModel.setBatchCountText(it) },
                            onIncrement = { viewModel.incrementBatchCount() },
                            onDecrement = { viewModel.decrementBatchCount() },
                            primaryColor = primaryColor,
                            surfaceBg = surfaceBg,
                            borderColor = cardBorderColor,
                            isDark = isDark
                        )
                        6 -> Step6NextActions(
                            onSaveRecipe = {
                                viewModel.saveCurrentRecipeToDb()
                                Toast.makeText(context, "Formulation saved to history", Toast.LENGTH_SHORT).show()
                            },
                            onOpenHistory = {
                                viewModel.setDatabaseBrowserSheetOpen(true)
                                onNavigateToHistory()
                            },
                            onEditTargets = { viewModel.goToStep(1) },
                            onNewBatches = { viewModel.goToStep(5) },
                            onReset = { viewModel.resetAll() },
                            primaryColor = primaryColor,
                            surfaceBg = surfaceBg,
                            borderColor = cardBorderColor,
                            isDark = isDark
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom Navigation Row (Steps 1 to 5)
            if (uiState.currentStep < 6) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back / Reset circular button on left
                    Surface(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(1.dp, cardBorderColor, CircleShape)
                            .clickable {
                                focusedFieldId = null
                                focusedFieldLabel = null
                                if (uiState.currentStep > 1) {
                                    viewModel.prevStep()
                                } else {
                                    viewModel.resetAll()
                                }
                            }
                            .testTag("mixture_back_button"),
                        shape = CircleShape,
                        color = surfaceBg
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (uiState.currentStep > 1) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.RestartAlt,
                                contentDescription = if (uiState.currentStep > 1) "Previous Step" else "Reset",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Primary Action Button filling remaining width
                    val buttonLabel = primaryButtonLabels[uiState.currentStep - 1]
                    val isStep1Valid = uiState.targetIngredients.isNotEmpty()

                    Button(
                        onClick = {
                            focusedFieldId = null
                            focusedFieldLabel = null
                            if (uiState.currentStep == 1 && uiState.targetIngredients.isEmpty()) {
                                Toast.makeText(context, "Please add at least 1 target ingredient first", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.nextStep()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("mixture_primary_action_button"),
                        shape = RoundedCornerShape(16.dp),
                        enabled = uiState.currentStep != 1 || isStep1Valid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 5.dp)
                    ) {
                        Text(
                            text = buttonLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Bottom-docked Smart Dialpad (slides in from bottom, single consistent keypad)
        AnimatedVisibility(
            visible = focusedFieldId != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            SmartStationKeypad(
                activeFieldLabel = focusedFieldLabel,
                onDigit = handleAppend,
                onDoubleZero = { handleAppend("00") },
                onDecimal = { handleAppend(".") },
                onBackspace = handleBackspace,
                onClear = handleClear,
                onDone = {
                    focusedFieldId = null
                    focusedFieldLabel = null
                },
                showDoubleZeroKey = true,
                showDecimalKey = true
            )
        }

    }
}

/**
 * Shared Horizontal Step Indicator (6 circles with checkmarks / numbers / lines)
 */
@Composable
private fun HorizontalStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    onStepClick: (Int) -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("step_indicator_row"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..totalSteps) {
            val isCompleted = step < currentStep
            val isCurrent = step == currentStep

            val circleColor = when {
                isCompleted -> Color(0xFF10B981)
                isCurrent -> primaryColor
                else -> if (isDark) Color(0x22FFFFFF) else Color(0x18000000)
            }

            val contentColor = when {
                isCompleted -> Color.White
                isCurrent -> if (isDark) Color.Black else Color.White
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            }

            // Step Circle
            Surface(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isCurrent) primaryColor else circleColor,
                        shape = CircleShape
                    )
                    .clickable { onStepClick(step) }
                    .testTag("step_circle_$step"),
                shape = CircleShape,
                color = circleColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed Step $step",
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "$step",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = contentColor
                        )
                    }
                }
            }

            // Connecting line (except after last step)
            if (step < totalSteps) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            if (step < currentStep) Color(0xFF10B981).copy(alpha = 0.7f)
                            else if (isDark) Color(0x22FFFFFF) else Color(0x18000000)
                        )
                )
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 1 — Enter Target Ingredients
// -------------------------------------------------------------
@Composable
private fun Step1EnterTargetIngredients(
    ingredients: List<TargetIngredient>,
    nameInput: String,
    qtyInput: String,
    unitInput: String,
    isQtyFocused: Boolean,
    onFocusQty: () -> Unit,
    onDismissKeypad: () -> Unit = {},
    onNameChange: (String) -> Unit,
    onQtyChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onAddIngredient: () -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onLoadSample: () -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        // Target Ingredients List Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TARGET INGREDIENTS (${ingredients.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (ingredients.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = surfaceBg
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No target ingredients added yet",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Use the form below to add components",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ingredients.forEach { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .testTag("target_ingredient_card_${item.id}"),
                        shape = RoundedCornerShape(12.dp),
                        color = surfaceBg
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Target Quantity: ${formatNumber(item.targetQuantity)} ${item.unit}",
                                    fontSize = 12.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    color = primaryColor
                                )
                            }

                            IconButton(
                                onClick = { onRemoveIngredient(item.id) },
                                modifier = Modifier.size(32.dp).testTag("delete_ingredient_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove ${item.name}",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Ingredient Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_ingredient_form_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "ADD NEW INGREDIENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Text Input: Ingredient Name
                LiquidGlassTextField(
                    value = nameInput,
                    onValueChange = onNameChange,
                    label = { Text("Ingredient Name") },
                    placeholder = { Text("Enter ingredient name") },
                    singleLine = true,
                    accentColor = primaryColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ingredient_name_input")
                        .onFocusChanged {
                            if (it.isFocused) {
                                onDismissKeypad()
                            }
                        }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Two Side-by-Side Inputs: Quantity (numeric) and Unit (text, default "kg")
                var showQtyCopyPasteMenu by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.weight(1.3f)
                    ) {
                        LiquidGlassTextField(
                            value = qtyInput,
                            onValueChange = onQtyChange,
                            readOnly = true,
                            label = { Text("Target Qty") },
                            placeholder = { Text("0") },
                            suffix = {
                                if (isQtyFocused) {
                                    BlinkingCursorPointer(color = primaryColor)
                                }
                            },
                            singleLine = true,
                            forceFocused = isQtyFocused,
                            accentColor = primaryColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ingredient_qty_input")
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            onFocusQty()
                                        },
                                        onLongPress = { showQtyCopyPasteMenu = true }
                                    )
                                }
                        )

                        InputFieldCopyPasteMenu(
                            expanded = showQtyCopyPasteMenu,
                            onDismissRequest = { showQtyCopyPasteMenu = false },
                            value = qtyInput,
                            onPaste = { onQtyChange(it) }
                        )
                    }

                    LiquidGlassTextField(
                        value = unitInput,
                        onValueChange = onUnitChange,
                        label = { Text("Unit") },
                        placeholder = { Text("kg / g / L") },
                        singleLine = true,
                        accentColor = primaryColor,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ingredient_unit_input")
                            .onFocusChanged {
                                if (it.isFocused) {
                                    onDismissKeypad()
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Full-Width Button: "+ Add to Target List" (Frameless with straight dotted line)
                Surface(
                    onClick = { onAddIngredient() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .straightDottedLine(
                            color = primaryColor,
                            strokeWidth = 1.3.dp,
                            dotLength = 4.dp,
                            spaceLength = 3.dp
                        )
                        .testTag("add_to_target_list_button"),
                    shape = RectangleShape,
                    color = Color.Transparent
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add to Target List",
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            fontSize = 13.sp,
                            color = primaryColor
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 2 — Target Recipe & Simplified Ratio
// -------------------------------------------------------------
@Composable
private fun Step2TargetRecipeAndRatio(
    ratioResult: RatioCalculationResult,
    ingredients: List<TargetIngredient>,
    onCopyRatio: () -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Highlighted Info Box: "Calculated Simplified Ratio:"
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .testTag("step2_simplified_ratio_box"),
            shape = RoundedCornerShape(16.dp),
            color = primaryColor.copy(alpha = 0.12f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CALCULATED SIMPLIFIED RATIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = primaryColor,
                        letterSpacing = 1.sp
                    )

                    IconButton(
                        onClick = onCopyRatio,
                        modifier = Modifier.size(28.dp).testTag("copy_ratio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy ratio",
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = ratioResult.simplifiedRatioString,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Auto-simplified to smallest whole-number ratio (GCD reduced)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Target Recipe Details Table
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("target_recipe_details_table"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "TARGET RECIPE DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .background(if (isDark) Color(0x10FFFFFF) else Color(0x08000000), RoundedCornerShape(8.dp)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ingredient",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.5f)
                    )
                    Text(
                        text = "Target Qty",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Unit",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Table Rows
                ingredients.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1.5f)
                        )
                        Text(
                            text = formatNumber(item.targetQuantity),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = primaryColor,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = item.unit,
                            fontSize = 12.sp,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                    HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Table Footer: Total Weight
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = primaryColor.copy(alpha = 0.10f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL MIXTURE WEIGHT:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = primaryColor
                        )
                        Text(
                            text = ratioResult.formattedTotalWeight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 3 — Reference & Actual Ingredients Added
// -------------------------------------------------------------
@Composable
private fun Step3ReferenceAndActualAdded(
    ingredients: List<TargetIngredient>,
    anchorId: String,
    actualAddedTexts: Map<String, String>,
    focusedFieldId: String?,
    selectedMode: MixtureAdjustmentMode,
    onSelectMode: (MixtureAdjustmentMode) -> Unit,
    onSetAnchorOnly: () -> Unit,
    onSetAllTargets: () -> Unit,
    onFocusField: (id: String, label: String) -> Unit,
    onSelectAnchor: (String) -> Unit,
    onActualAddedChange: (String, String) -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SELECT CALIBRATION ANCHOR & IN-TUB QUANTITIES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = DigitalMonospaceFont,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        val totalInitialWeight = ingredients.sumOf { it.targetQuantity }
        val primaryUnit = ingredients.firstOrNull()?.unit ?: "kg"

        // Banner: Constant Total Weight Locked
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.5.dp, Color(0xFF06B6D4).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .testTag("step3_locked_total_weight_banner"),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF06B6D4).copy(alpha = 0.12f)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF06B6D4).copy(alpha = 0.20f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOTAL MIXTURE WEIGHT (LOCKED)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = Color(0xFF06B6D4),
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatNumber(totalInitialWeight)} $primaryUnit (Constant Initial Entry)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Update the exact anchor quantity below. Other components will adjust proportionally to keep this total mixture weight constant.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Adjustment Pathway / Balancing Mode Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("adjustment_pathway_mode_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ADJUSTMENT PATHWAY / BALANCING MODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = primaryColor,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Choose how to balance the mixture when you update your anchor ingredient:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Constant Total Weight Option (Locked Initial Total)
                val isConstant = selectedMode == MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT
                val constBorder = if (isConstant) Color(0xFF06B6D4) else borderColor
                val constBg = if (isConstant) Color(0xFF06B6D4).copy(alpha = 0.12f) else if (isDark) Color(0x10FFFFFF) else Color(0x06000000)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(if (isConstant) 1.5.dp else 1.dp, constBorder, RoundedCornerShape(12.dp))
                        .clickable { onSelectMode(MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT) }
                        .testTag("pathway_constant_weight_button"),
                    shape = RoundedCornerShape(12.dp),
                    color = constBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isConstant) Color(0xFF06B6D4) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Constant Total Weight (Locked)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = if (isConstant) Color(0xFF06B6D4) else MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF06B6D4).copy(alpha = 0.20f)
                                ) {
                                    Text(
                                        text = "RECOMMENDED",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = DigitalMonospaceFont,
                                        color = Color(0xFF06B6D4),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Keep total weight constant at ${formatNumber(totalInitialWeight)} $primaryUnit. Suggests proportional adjustments for other components.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                        }
                        if (isConstant) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF06B6D4),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mode Selector Buttons Row for Option 1 and Option 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: In-Tub Additions (Cannot Remove)
                    val isOption1 = selectedMode == MixtureAdjustmentMode.ADD_TO_BALANCE
                    val opt1Border = if (isOption1) Color(0xFF10B981) else borderColor
                    val opt1Bg = if (isOption1) Color(0xFF10B981).copy(alpha = 0.12f) else if (isDark) Color(0x10FFFFFF) else Color(0x06000000)

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(if (isOption1) 1.5.dp else 1.dp, opt1Border, RoundedCornerShape(12.dp))
                        .clickable { onSelectMode(MixtureAdjustmentMode.ADD_TO_BALANCE) }
                        .testTag("pathway_option1_button"),
                    shape = RoundedCornerShape(12.dp),
                    color = opt1Bg
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Option 1: Add",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = if (isOption1) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                            )
                            if (isOption1) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Scale & Add (+)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Add other components to match scaled anchor.",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 12.sp
                        )
                    }
                }

                // Option 2: Proportional Reductions (Remove Excess)
                val isOption2 = selectedMode == MixtureAdjustmentMode.REDUCE_TO_TARGET
                val opt2Border = if (isOption2) Color(0xFFF59E0B) else borderColor
                val opt2Bg = if (isOption2) Color(0xFFF59E0B).copy(alpha = 0.12f) else if (isDark) Color(0x10FFFFFF) else Color(0x06000000)

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(if (isOption2) 1.5.dp else 1.dp, opt2Border, RoundedCornerShape(12.dp))
                        .clickable { onSelectMode(MixtureAdjustmentMode.REDUCE_TO_TARGET) }
                        .testTag("pathway_option2_button"),
                    shape = RoundedCornerShape(12.dp),
                    color = opt2Bg
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Option 2: Reduce",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = if (isOption2) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface
                            )
                            if (isOption2) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Scale & Reduce (-)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Scale to anchor; reduce/remove excess.",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }
    }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick In-Tub Filling Shortcuts Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .clickable { onSetAnchorOnly() }
                    .testTag("set_anchor_only_button"),
                shape = RoundedCornerShape(10.dp),
                color = surfaceBg
            ) {
                Text(
                    text = "Anchor Only in Tub (0 for rest)",
                    fontSize = 10.sp,
                    fontFamily = DigitalMonospaceFont,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
                )
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .clickable { onSetAllTargets() }
                    .testTag("set_all_targets_button"),
                shape = RoundedCornerShape(10.dp),
                color = surfaceBg
            ) {
                Text(
                    text = "All Target Quantities in Tub",
                    fontSize = 10.sp,
                    fontFamily = DigitalMonospaceFont,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cards per ingredient
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ingredients.forEach { item ->
                val isAnchor = item.id == anchorId
                val actualText = actualAddedTexts[item.id] ?: ""
                val fieldKey = "step3_actual_${item.id}"
                val isFocused = focusedFieldId == fieldKey
                var showCopyPasteMenu by remember { mutableStateOf(false) }

                val cardBg = if (isAnchor) primaryColor.copy(alpha = 0.12f) else surfaceBg
                val cardBorder = if (isAnchor) primaryColor.copy(alpha = 0.6f) else borderColor

                val actualDouble = actualText.replace(",", ".").replace(" ", "").trim().toDoubleOrNull()
                val hasActual = actualDouble != null
                val ratio = if (hasActual && item.targetQuantity > 0) actualDouble!! / item.targetQuantity else 1.0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(if (isAnchor) 1.5.dp else 1.dp, cardBorder, RoundedCornerShape(16.dp))
                        .clickable { onSelectAnchor(item.id) }
                        .testTag("actual_ingredient_card_${item.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isAnchor) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = if (isAnchor) "Selected Anchor" else "Select as Anchor",
                                    tint = if (isAnchor) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = item.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Target recipe: ${formatNumber(item.targetQuantity)} ${item.unit}",
                                        fontSize = 11.sp,
                                        fontFamily = DigitalMonospaceFont,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (hasActual && kotlin.math.abs(ratio - 1.0) > 0.005) {
                                    val isOvershot = ratio > 1.0
                                    val deltaPct = kotlin.math.abs((ratio - 1.0) * 100).toInt()
                                    val badgeColor = if (isOvershot) Color(0xFF10B981) else Color(0xFFF59E0B)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = badgeColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (isOvershot) "+$deltaPct% (+)" else "-$deltaPct% (-)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = DigitalMonospaceFont,
                                            color = badgeColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                if (isAnchor) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = primaryColor,
                                        modifier = Modifier.testTag("calibration_anchor_badge")
                                    ) {
                                        Text(
                                            text = "CALIBRATION ANCHOR",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = DigitalMonospaceFont,
                                            color = if (isDark) Color.Black else Color.White,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Input field: Actual Added ([unit])
                        val keyboardController = LocalSoftwareKeyboardController.current
                        val focusManager = LocalFocusManager.current

                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LiquidGlassTextField(
                                value = actualText,
                                onValueChange = { onActualAddedChange(item.id, it) },
                                readOnly = true,
                                label = { Text("Actual in Tub (${item.unit})") },
                                placeholder = { Text(formatNumber(item.targetQuantity)) },
                                suffix = {
                                    if (isFocused) {
                                        BlinkingCursorPointer(color = primaryColor)
                                    }
                                },
                                singleLine = true,
                                forceFocused = isFocused,
                                accentColor = primaryColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("actual_added_input_${item.id}")
                            )

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .pointerInput(item.id) {
                                        detectTapGestures(
                                            onTap = {
                                                focusManager.clearFocus()
                                                keyboardController?.hide()
                                                onSelectAnchor(item.id)
                                                onFocusField(fieldKey, "Actual ${item.name} (${item.unit})")
                                            },
                                            onLongPress = { showCopyPasteMenu = true }
                                        )
                                    }
                            )

                            InputFieldCopyPasteMenu(
                                expanded = showCopyPasteMenu,
                                onDismissRequest = { showCopyPasteMenu = false },
                                value = actualText,
                                onPaste = { onActualAddedChange(item.id, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 4 — Recalibrated Mixture Proportions
// -------------------------------------------------------------
@Composable
private fun Step4RecalibratedProportions(
    recalibrationResult: RecalibrationResult,
    selectedMode: MixtureAdjustmentMode,
    onSelectMode: (MixtureAdjustmentMode) -> Unit,
    showCompareBoth: Boolean,
    onToggleCompareBoth: () -> Unit,
    onCopyAdjustments: () -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean
) {
    val isConstantWeight = selectedMode == MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT
    val isOption1 = selectedMode == MixtureAdjustmentMode.ADD_TO_BALANCE
    val isOption2 = selectedMode == MixtureAdjustmentMode.REDUCE_TO_TARGET
    val activeProportions = when (selectedMode) {
        MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT -> recalibrationResult.constantWeightProportions
        MixtureAdjustmentMode.ADD_TO_BALANCE -> recalibrationResult.addModeProportions
        MixtureAdjustmentMode.REDUCE_TO_TARGET -> recalibrationResult.reduceModeProportions
    }
    val primaryUnit = recalibrationResult.proportions.firstOrNull()?.unit ?: "kg"

    Column(modifier = Modifier.fillMaxWidth()) {
        // Top Header Row with Title & Quick Copy Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECALIBRATION & PROPORTION ADJUSTMENTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp
            )

            IconButton(
                onClick = onCopyAdjustments,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("copy_proportions_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy report",
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // DUAL OPTION SELECTOR & COMPARE BAR
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("step4_option_mode_switcher_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Constant Weight Tab: Locked Initial Total
                    val constSelected = !showCompareBoth && isConstantWeight
                    val constBorder = if (constSelected) Color(0xFF06B6D4) else borderColor.copy(alpha = 0.5f)
                    val constBg = if (constSelected) Color(0xFF06B6D4).copy(alpha = 0.15f) else Color.Transparent

                    Surface(
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(if (constSelected) 1.5.dp else 1.dp, constBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                if (showCompareBoth) onToggleCompareBoth()
                                onSelectMode(MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT)
                            }
                            .testTag("step4_tab_constant_weight"),
                        shape = RoundedCornerShape(10.dp),
                        color = constBg
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (constSelected) Color(0xFF06B6D4) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Constant",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = if (constSelected) Color(0xFF06B6D4) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Locked Total",
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    // Option 1 Tab: In-Tub Additions (Cannot Remove)
                    val opt1Selected = !showCompareBoth && isOption1
                    val opt1Border = if (opt1Selected) Color(0xFF10B981) else borderColor.copy(alpha = 0.5f)
                    val opt1Bg = if (opt1Selected) Color(0xFF10B981).copy(alpha = 0.15f) else Color.Transparent

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(if (opt1Selected) 1.5.dp else 1.dp, opt1Border, RoundedCornerShape(10.dp))
                            .clickable {
                                if (showCompareBoth) onToggleCompareBoth()
                                onSelectMode(MixtureAdjustmentMode.ADD_TO_BALANCE)
                            }
                            .testTag("step4_tab_option1_add"),
                        shape = RoundedCornerShape(10.dp),
                        color = opt1Bg
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    tint = if (opt1Selected) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Opt 1: Add",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = if (opt1Selected) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Scale Anchor",
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    // Option 2 Tab: Proportional Reductions (Remove Excess)
                    val opt2Selected = !showCompareBoth && isOption2
                    val opt2Border = if (opt2Selected) Color(0xFFF59E0B) else borderColor.copy(alpha = 0.5f)
                    val opt2Bg = if (opt2Selected) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color.Transparent

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(if (opt2Selected) 1.5.dp else 1.dp, opt2Border, RoundedCornerShape(10.dp))
                            .clickable {
                                if (showCompareBoth) onToggleCompareBoth()
                                onSelectMode(MixtureAdjustmentMode.REDUCE_TO_TARGET)
                            }
                            .testTag("step4_tab_option2_reduce"),
                        shape = RoundedCornerShape(10.dp),
                        color = opt2Bg
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RemoveCircleOutline,
                                    contentDescription = null,
                                    tint = if (opt2Selected) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Opt 2: Red",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = if (opt2Selected) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Scale Anchor",
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    // Compare Both Tab
                    val compareBorder = if (showCompareBoth) primaryColor else borderColor.copy(alpha = 0.5f)
                    val compareBg = if (showCompareBoth) primaryColor.copy(alpha = 0.15f) else Color.Transparent

                    Surface(
                        modifier = Modifier
                            .weight(0.9f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(if (showCompareBoth) 1.5.dp else 1.dp, compareBorder, RoundedCornerShape(10.dp))
                            .clickable { onToggleCompareBoth() }
                            .testTag("step4_tab_compare_both"),
                        shape = RoundedCornerShape(10.dp),
                        color = compareBg
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = null,
                                tint = if (showCompareBoth) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Compare",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = if (showCompareBoth) primaryColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // VIEW RENDERING: Either Compare View or Specific Option View
        if (showCompareBoth) {
            // ==========================================
            // COMPARISON VIEW: DUAL OPTIONS SIDE-BY-SIDE
            // ==========================================
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Dual Summary Header Card
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("step4_compare_summary_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SIDE-BY-SIDE OPTION COMPARISON",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = primaryColor,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Option 1 Mini Card
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.08f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "OPTION 1: IN-TUB",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = DigitalMonospaceFont,
                                        color = Color(0xFF10B981)
                                    )
                                    Text(
                                        text = "Scale: ${recalibrationResult.formattedInTubScaleFactor}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = DigitalMonospaceFont,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Total Weight: ${recalibrationResult.formattedAddModeTotalWeight}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "To Add: +${recalibrationResult.formattedTotalStillNeededWeight}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF10B981)
                                    )
                                    Text(
                                        text = "Removals: 0 (Add only)",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Option 2 Mini Card
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.08f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "OPTION 2: REDUCE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = DigitalMonospaceFont,
                                        color = Color(0xFFF59E0B)
                                    )
                                    Text(
                                        text = "Scale: ${recalibrationResult.formattedStrictAnchorScaleFactor}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = DigitalMonospaceFont,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Total Weight: ${recalibrationResult.formattedReduceModeTotalWeight}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "To Reduce: -${recalibrationResult.formattedTotalExcessWeight}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (recalibrationResult.hasExcessInTub) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "To Add: +${formatNumber(recalibrationResult.reduceModeProportions.sumOf { it.stillNeeded })} $primaryUnit",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Comparative Table Card
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("compare_table_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "COMPONENT ADJUSTMENT COMPARISON",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                                .background(if (isDark) Color(0x10FFFFFF) else Color(0x08000000), RoundedCornerShape(8.dp)),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ingredient (In Tub)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1.3f)
                            )
                            Text(
                                text = "Option 1 (To Add)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = Color(0xFF10B981),
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = "Option 2 (Action)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = Color(0xFFF59E0B),
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.2f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Table Rows
                        recalibrationResult.addModeProportions.indices.forEach { index ->
                            val addRow = recalibrationResult.addModeProportions[index]
                            val reduceRow = recalibrationResult.reduceModeProportions.getOrNull(index) ?: addRow

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.3f)) {
                                    Text(
                                        text = addRow.ingredientName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "In tub: ${addRow.formattedAlreadyAdded}",
                                        fontSize = 9.sp,
                                        fontFamily = DigitalMonospaceFont,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Option 1 Column: What to add
                                Column(
                                    modifier = Modifier.weight(1.2f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    if (addRow.isAnchor) {
                                        Text(
                                            text = "0 (Anchor Scaled)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = DigitalMonospaceFont,
                                            color = primaryColor
                                        )
                                        Text(
                                            text = "Set: ${addRow.formattedNewTotal}",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else if (addRow.stillNeeded > 0.0001) {
                                        Text(
                                            text = "+${addRow.formattedStillNeeded}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = DigitalMonospaceFont,
                                            color = Color(0xFF10B981)
                                        )
                                        Text(
                                            text = "Total: ${addRow.formattedNewTotal}",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Text(
                                            text = "0 (In Tub >= Target)",
                                            fontSize = 10.sp,
                                            fontFamily = DigitalMonospaceFont,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Option 2 Column: Action (reduce or add)
                                Column(
                                    modifier = Modifier.weight(1.2f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    when {
                                        reduceRow.excessAmount > 0.0001 -> {
                                            Text(
                                                text = "-${reduceRow.formattedExcess}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = DigitalMonospaceFont,
                                                color = Color(0xFFEF4444)
                                            )
                                            Text(
                                                text = "REDUCE / REMOVE",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFEF4444)
                                            )
                                        }
                                        reduceRow.stillNeeded > 0.0001 -> {
                                            Text(
                                                text = "+${reduceRow.formattedStillNeeded}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = DigitalMonospaceFont,
                                                color = Color(0xFF10B981)
                                            )
                                            Text(
                                                text = "ADD TO TUB",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                        else -> {
                                            Text(
                                                text = "0 Balanced",
                                                fontSize = 10.sp,
                                                fontFamily = DigitalMonospaceFont,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = borderColor.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // SINGLE OPTION VIEW (Option 1 OR Option 2)
            // ==========================================
            val bannerAccent = when {
                isConstantWeight -> Color(0xFF06B6D4)
                isOption1 -> Color(0xFF10B981)
                else -> Color(0xFFF59E0B)
            }

            // 1. Info Banner Box explaining the active mode
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, bannerAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("step4_scale_factor_box"),
                shape = RoundedCornerShape(16.dp),
                color = bannerAccent.copy(alpha = 0.10f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when {
                                    isConstantWeight -> Icons.Default.Lock
                                    isOption1 -> Icons.Default.AddCircleOutline
                                    else -> Icons.Default.RemoveCircleOutline
                                },
                                contentDescription = null,
                                tint = bannerAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    isConstantWeight -> "CONSTANT TOTAL WEIGHT (LOCKED)"
                                    isOption1 -> "OPTION 1: IN-TUB ADDITIONS"
                                    else -> "OPTION 2: PROPORTIONAL REDUCTIONS"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = bannerAccent,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = bannerAccent
                        ) {
                            Text(
                                text = when {
                                    isConstantWeight -> "TOTAL LOCKED"
                                    isOption1 -> "CANNOT REMOVE"
                                    else -> "SCALE TO ANCHOR"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = if (isDark) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = recalibrationResult.scaleExplanation,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 Metric Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Metric 1: Scale Factor / Locked Total
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Color(0x18FFFFFF) else Color(0x0C000000)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = if (isConstantWeight) "LOCKED TOTAL" else if (isOption1) "IN-TUB SCALE" else "ANCHOR SCALE",
                                    fontSize = 8.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isConstantWeight) recalibrationResult.formattedLockedInitialTotalWeight else recalibrationResult.formattedScaleFactor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Metric 2: Total Weight / Anchor
                        Surface(
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Color(0x18FFFFFF) else Color(0x0C000000)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = if (isConstantWeight) "EXACT ANCHOR" else "TOTAL WEIGHT",
                                    fontSize = 8.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isConstantWeight) "${formatNumber(recalibrationResult.anchorActualAdded)} $primaryUnit" else recalibrationResult.formattedTotalNewWeight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Metric 3: Total Action (Add or Excess or Adjust)
                        Surface(
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Color(0x18FFFFFF) else Color(0x0C000000)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = when {
                                        isConstantWeight -> "ADJUSTMENT"
                                        isOption1 -> "TOTAL TO ADD"
                                        else -> "EXCESS TO REDUCE"
                                    },
                                    fontSize = 8.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    color = bannerAccent
                                )
                                Text(
                                    text = when {
                                        isConstantWeight -> "Proportional"
                                        isOption1 -> "+${recalibrationResult.formattedTotalStillNeededWeight}"
                                        else -> "-${recalibrationResult.formattedTotalExcessWeight}"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = bannerAccent
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Proportion Adjustments Detail Table
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("proportions_adjustment_table"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = when {
                            isConstantWeight -> "COMPONENT ADJUSTMENT SUGGESTIONS (CONSTANT TOTAL WEIGHT)"
                            isOption1 -> "IN-TUB BALANCED PROPORTIONS"
                            else -> "SCALED ANCHOR REDUCTION PROPORTIONS"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .background(if (isDark) Color(0x10FFFFFF) else Color(0x08000000), RoundedCornerShape(8.dp)),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ingredient",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1.3f)
                        )
                        Text(
                            text = if (isConstantWeight) "Initial Target" else if (isOption1) "Balanced" else "Scaled Req",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (isConstantWeight) "Adjusted Qty" else "In Tub",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (isConstantWeight) "Suggestion (Delta)" else if (isOption1) "To Add (+)" else "Action",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = bannerAccent,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.4f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Table Rows
                    activeProportions.forEach { prop ->
                        val hasStillNeeded = prop.stillNeeded > 0.001
                        val hasExcess = prop.excessAmount > 0.001

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.3f)) {
                                Text(
                                    text = prop.ingredientName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (prop.isAnchor) {
                                        Text(
                                            text = if (isConstantWeight) "Anchor (Exact)" else "Anchor",
                                            fontSize = 9.sp,
                                            fontFamily = DigitalMonospaceFont,
                                            color = bannerAccent
                                        )
                                    }
                                    if (isOption1 && prop.isLimitingFactor && !prop.isAnchor) {
                                        Text(
                                            text = "Limiting in Tub",
                                            fontSize = 9.sp,
                                            fontFamily = DigitalMonospaceFont,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = if (isConstantWeight) prop.formattedTarget else prop.formattedNewTotal,
                                fontSize = 11.sp,
                                fontFamily = DigitalMonospaceFont,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = if (isConstantWeight) prop.formattedNewTotal else prop.formattedAlreadyAdded,
                                fontSize = 11.sp,
                                fontFamily = DigitalMonospaceFont,
                                color = if (isConstantWeight) bannerAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isConstantWeight) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )

                            // Emphasized Action Column
                            Column(
                                modifier = Modifier.weight(1.4f),
                                horizontalAlignment = Alignment.End
                            ) {
                                if (isConstantWeight) {
                                    // Constant Total Weight Mode: Show exact component adjustment suggestion
                                    if (prop.isAnchor) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF06B6D4).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "${prop.formattedSuggestedAdjustmentDelta} [Anchor]",
                                                fontSize = 10.sp,
                                                fontFamily = DigitalMonospaceFont,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF06B6D4),
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        val isReduction = prop.suggestedAdjustmentDelta < -0.0001
                                        val isAddition = prop.suggestedAdjustmentDelta > 0.0001
                                        val deltaColor = if (isReduction) Color(0xFFF59E0B) else if (isAddition) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                        val deltaLabel = if (isReduction) "Reduce" else if (isAddition) "Add" else "Keep"

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = deltaColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "${prop.formattedSuggestedAdjustmentDelta} [$deltaLabel]",
                                                fontSize = 10.sp,
                                                fontFamily = DigitalMonospaceFont,
                                                fontWeight = FontWeight.Bold,
                                                color = deltaColor,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (hasStillNeeded) {
                                            Text(
                                                text = "+${prop.formattedStillNeeded} in tub",
                                                fontSize = 8.sp,
                                                fontFamily = DigitalMonospaceFont,
                                                color = Color(0xFF10B981)
                                            )
                                        } else if (hasExcess) {
                                            Text(
                                                text = "-${prop.formattedExcess} in tub",
                                                fontSize = 8.sp,
                                                fontFamily = DigitalMonospaceFont,
                                                color = Color(0xFFEF4444)
                                            )
                                        }
                                    }
                                } else if (isOption1) {
                                    // Option 1: Always Additions (Anchor is locked at scaled quantity, calculates other components to add)
                                    if (prop.isAnchor) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = primaryColor.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "0 (Anchor Scaled)",
                                                fontSize = 10.sp,
                                                fontFamily = DigitalMonospaceFont,
                                                fontWeight = FontWeight.SemiBold,
                                                color = primaryColor,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (hasStillNeeded) Color(0xFF10B981).copy(alpha = 0.15f) else Color.Transparent
                                        ) {
                                            Text(
                                                text = if (hasStillNeeded) "+${prop.formattedStillNeeded} ADD" else "0 (In Tub >= Target)",
                                                fontSize = 10.sp,
                                                fontFamily = DigitalMonospaceFont,
                                                fontWeight = if (hasStillNeeded) FontWeight.Bold else FontWeight.Normal,
                                                color = if (hasStillNeeded) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                } else {
                                    // Option 2: Reductions or Additions
                                    when {
                                        hasExcess -> {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFEF4444).copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "-${prop.formattedExcess} REDUCE",
                                                    fontSize = 10.sp,
                                                    fontFamily = DigitalMonospaceFont,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFEF4444),
                                                    textAlign = TextAlign.End,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        hasStillNeeded -> {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "+${prop.formattedStillNeeded} ADD",
                                                    fontSize = 10.sp,
                                                    fontFamily = DigitalMonospaceFont,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF10B981),
                                                    textAlign = TextAlign.End,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        else -> {
                                            Text(
                                                text = "0 Balanced",
                                                fontSize = 10.sp,
                                                fontFamily = DigitalMonospaceFont,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.End
                                            )
                                        }
                                    }

                                    if (prop.reductionFromOriginal > 0.0001) {
                                        Text(
                                            text = "(-${prop.formattedReductionFromOriginal})",
                                            fontSize = 8.sp,
                                            fontFamily = DigitalMonospaceFont,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = borderColor.copy(alpha = 0.4f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Footer: Total Recalibrated Mixture Weight
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = bannerAccent.copy(alpha = 0.10f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isConstantWeight) "TOTAL MIXTURE WEIGHT (CONSTANT):" else if (isOption1) "TOTAL IN-TUB BALANCED WEIGHT:" else "TOTAL SCALED TARGET WEIGHT:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = bannerAccent
                            )
                            Text(
                                text = if (isConstantWeight) recalibrationResult.formattedLockedInitialTotalWeight else recalibrationResult.formattedTotalNewWeight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Switch Option Suggestion Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .clickable {
                        onSelectMode(
                            when (selectedMode) {
                                MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT -> MixtureAdjustmentMode.ADD_TO_BALANCE
                                MixtureAdjustmentMode.ADD_TO_BALANCE -> MixtureAdjustmentMode.REDUCE_TO_TARGET
                                MixtureAdjustmentMode.REDUCE_TO_TARGET -> MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT
                            }
                        )
                    }
                    .testTag("switch_to_alternate_mode_button"),
                shape = RoundedCornerShape(12.dp),
                color = surfaceBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (selectedMode) {
                                MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT -> "Want to scale to anchor by adding other components only?"
                                MixtureAdjustmentMode.ADD_TO_BALANCE -> "Want to strictly scale down to anchor by removing excess?"
                                MixtureAdjustmentMode.REDUCE_TO_TARGET -> "Want to lock initial total mixture weight and balance others?"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (selectedMode) {
                                MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT -> "Switch to Option 1: In-Tub Additions (Add only)"
                                MixtureAdjustmentMode.ADD_TO_BALANCE -> "Switch to Option 2: Proportional Reductions"
                                MixtureAdjustmentMode.REDUCE_TO_TARGET -> "Switch to Constant Total Weight (Locked Total)"
                            },
                            fontSize = 10.sp,
                            color = primaryColor
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 5 — Batch Scaling: Divide or Multiply
// -------------------------------------------------------------
@Composable
private fun Step5BatchScaling(
    recalibrationResult: RecalibrationResult,
    batchDivisionResult: BatchDivisionResult,
    selectedMode: MixtureAdjustmentMode,
    onSelectMode: (MixtureAdjustmentMode) -> Unit,
    batchOperationMode: BatchOperationMode,
    onSelectBatchOperationMode: (BatchOperationMode) -> Unit,
    batchCountText: String,
    batchCount: Int,
    isBatchCountFocused: Boolean,
    onFocusBatchCount: () -> Unit,
    onBatchCountChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean
) {
    val isDivide = batchOperationMode == BatchOperationMode.DIVIDE
    val primaryUnit = recalibrationResult.proportions.firstOrNull()?.unit ?: "kg"

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Dual Mode Selector Card: Divide vs Multiply
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("batch_scaling_mode_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "BATCH SCALING OPERATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Divide Tab
                    val divideSelected = isDivide
                    val divideBorder = if (divideSelected) primaryColor else borderColor.copy(alpha = 0.5f)
                    val divideBg = if (divideSelected) primaryColor.copy(alpha = 0.15f) else Color.Transparent

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(if (divideSelected) 1.5.dp else 1.dp, divideBorder, RoundedCornerShape(12.dp))
                            .clickable { onSelectBatchOperationMode(BatchOperationMode.DIVIDE) }
                            .testTag("batch_mode_divide_button"),
                        shape = RoundedCornerShape(12.dp),
                        color = divideBg
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "÷",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (divideSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "DIVIDE MIXTURE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = if (divideSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Split mixture into equal batches",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Multiply Tab
                    val multiplySelected = !isDivide
                    val multiplyBorder = if (multiplySelected) Color(0xFFF59E0B) else borderColor.copy(alpha = 0.5f)
                    val multiplyBg = if (multiplySelected) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color.Transparent

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(if (multiplySelected) 1.5.dp else 1.dp, multiplyBorder, RoundedCornerShape(12.dp))
                            .clickable { onSelectBatchOperationMode(BatchOperationMode.MULTIPLY) }
                            .testTag("batch_mode_multiply_button"),
                        shape = RoundedCornerShape(12.dp),
                        color = multiplyBg
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "×",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (multiplySelected) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MULTIPLY MIXTURE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = if (multiplySelected) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Multiply single batch to total mix",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Basis Mode Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (selectedMode) {
                        MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT -> Color(0xFF06B6D4).copy(alpha = 0.12f)
                        MixtureAdjustmentMode.ADD_TO_BALANCE -> Color(0xFF10B981).copy(alpha = 0.12f)
                        MixtureAdjustmentMode.REDUCE_TO_TARGET -> Color(0xFFF59E0B).copy(alpha = 0.12f)
                    },
                    modifier = Modifier.clickable {
                        onSelectMode(
                            when (selectedMode) {
                                MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT -> MixtureAdjustmentMode.ADD_TO_BALANCE
                                MixtureAdjustmentMode.ADD_TO_BALANCE -> MixtureAdjustmentMode.REDUCE_TO_TARGET
                                MixtureAdjustmentMode.REDUCE_TO_TARGET -> MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT
                            }
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = when (selectedMode) {
                                MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT -> "Basis: Constant Total Weight (${recalibrationResult.formattedLockedInitialTotalWeight})"
                                MixtureAdjustmentMode.ADD_TO_BALANCE -> "Basis: Option 1 (In-Tub Additions: ${recalibrationResult.formattedTotalNewWeight})"
                                MixtureAdjustmentMode.REDUCE_TO_TARGET -> "Basis: Option 2 (Proportional Reductions: ${recalibrationResult.formattedTotalNewWeight})"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = when (selectedMode) {
                                MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT -> Color(0xFF06B6D4)
                                MixtureAdjustmentMode.ADD_TO_BALANCE -> Color(0xFF10B981)
                                MixtureAdjustmentMode.REDUCE_TO_TARGET -> Color(0xFFF59E0B)
                            }
                        )
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch basis mode",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Formula explanation banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDivide) primaryColor.copy(alpha = 0.08f) else Color(0xFFF59E0B).copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isDivide) primaryColor else Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = batchDivisionResult.explanation,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isDivide) "How many equal batches to divide into?" else "How many batches to multiply to get total mixture?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                var showBatchCopyPasteMenu by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Decrement Stepper
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable { onDecrement() }
                            .testTag("decrement_batch_count_button"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0x18FFFFFF) else Color(0x0C000000)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                        }
                    }

                    // Numeric input
                    val keyboardController = LocalSoftwareKeyboardController.current
                    val focusManager = LocalFocusManager.current

                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        LiquidGlassTextField(
                            value = batchCountText,
                            onValueChange = onBatchCountChange,
                            readOnly = true,
                            suffix = {
                                if (isBatchCountFocused) {
                                    BlinkingCursorPointer(color = primaryColor)
                                }
                            },
                            singleLine = true,
                            forceFocused = isBatchCountFocused,
                            accentColor = if (isDivide) primaryColor else Color(0xFFF59E0B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("batch_count_input"),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                textAlign = TextAlign.Center
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
                                            onFocusBatchCount()
                                        },
                                        onLongPress = { showBatchCopyPasteMenu = true }
                                    )
                                }
                        )

                        InputFieldCopyPasteMenu(
                            expanded = showBatchCopyPasteMenu,
                            onDismissRequest = { showBatchCopyPasteMenu = false },
                            value = batchCountText,
                            onPaste = { onBatchCountChange(it) }
                        )
                    }

                    // Increment Stepper
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable { onIncrement() }
                            .testTag("increment_batch_count_button"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0x18FFFFFF) else Color(0x0C000000)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick preset buttons
                val presets = if (isDivide) listOf(2, 3, 4, 5, 6, 8) else listOf(1, 2, 3, 5, 10, 20)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        val isSelected = batchCount == preset
                        val chipColor = if (isDivide) primaryColor else Color(0xFFF59E0B)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) chipColor else borderColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onBatchCountChange(preset.toString()) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) chipColor.copy(alpha = 0.2f) else Color.Transparent
                        ) {
                            Text(
                                text = "${preset}x",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = DigitalMonospaceFont,
                                color = if (isSelected) chipColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Quantity Table Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quantity_per_batch_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isDivide) {
                        "QUANTITY PER BATCH (DIVIDE TOTAL BY $batchCount BATCHES)"
                    } else {
                        "TOTAL MIXTURE (MULTIPLY SINGLE BATCH BY $batchCount BATCHES)"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = if (isDivide) primaryColor else Color(0xFFF59E0B),
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .background(if (isDark) Color(0x10FFFFFF) else Color(0x08000000), RoundedCornerShape(8.dp)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ingredient",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.4f)
                    )
                    Text(
                        text = if (isDivide) "Per Batch (÷)" else "Per Batch (1x)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = if (isDivide) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.2f)
                    )
                    Text(
                        text = if (isDivide) "Total Mix" else "Total Scaled (×)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = if (!isDivide) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.3f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Table Rows
                batchDivisionResult.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.ingredientName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1.4f)
                        )
                        Text(
                            text = item.formattedPerBatch,
                            fontSize = 12.sp,
                            fontWeight = if (isDivide) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = DigitalMonospaceFont,
                            color = if (isDivide) primaryColor else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = item.formattedTotal,
                            fontSize = 12.sp,
                            fontWeight = if (!isDivide) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = DigitalMonospaceFont,
                            color = if (!isDivide) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                    HorizontalDivider(color = borderColor.copy(alpha = 0.4f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer: Total Weight Summary
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDivide) primaryColor.copy(alpha = 0.12f) else Color(0xFFF59E0B).copy(alpha = 0.12f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isDivide) "WEIGHT PER BATCH (÷):" else "SINGLE BATCH WEIGHT:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = if (isDivide) primaryColor else Color(0xFFF59E0B)
                            )
                            Text(
                                text = batchDivisionResult.formattedWeightPerBatch,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isDivide) "TOTAL MIXTURE DIVIDED:" else "TOTAL PRODUCTION MIXTURE (×):",
                                fontSize = 10.sp,
                                fontFamily = DigitalMonospaceFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = batchDivisionResult.formattedResultWeight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = if (!isDivide) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 6 — What Would You Like To Do Next?
// -------------------------------------------------------------
@Composable
private fun Step6NextActions(
    onSaveRecipe: () -> Unit,
    onOpenHistory: () -> Unit,
    onEditTargets: () -> Unit,
    onNewBatches: () -> Unit,
    onReset: () -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Explanatory paragraph confirming process completion
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .testTag("step6_completion_card"),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF10B981).copy(alpha = 0.10f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Recalibration & Batch Scaling Complete!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Your mixture has been calibrated to exact target proportions and divided into equal batches. Choose what you would like to do next below:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "SELECT NEXT ACTION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = DigitalMonospaceFont,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action: Save Formulation to History
        NextActionCard(
            title = "Save Formulation to History",
            description = "Save calibrated proportions, anchor, and batches to persistent database",
            icon = Icons.Default.Bookmark,
            iconTint = Color(0xFFF59E0B),
            onClick = onSaveRecipe,
            testTag = "action_card_save_formulation",
            surfaceBg = surfaceBg,
            borderColor = borderColor
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Option 1: Adjust Ratio & Targets (Step 1)
        NextActionCard(
            title = "Adjust Ratio & Targets",
            description = "Edit ingredient names or target quantities (Step 1)",
            icon = Icons.Default.Edit,
            iconTint = Color(0xFF38BDF8),
            onClick = onEditTargets,
            testTag = "action_card_adjust_targets",
            surfaceBg = surfaceBg,
            borderColor = borderColor
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Option 2: Calculate New Batch Division (Step 5)
        NextActionCard(
            title = "Calculate New Batch Division",
            description = "Change the number of equal divisions / batches from the same mixture",
            icon = Icons.Default.Check,
            iconTint = Color(0xFF10B981),
            onClick = onNewBatches,
            testTag = "action_card_new_batches",
            surfaceBg = surfaceBg,
            borderColor = borderColor
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Option 3: Start Fresh / Reset (Destructive / red-tinted)
        NextActionCard(
            title = "Start Fresh / Reset",
            description = "Clear all inputs and start with fresh ingredients",
            icon = Icons.Default.RestartAlt,
            iconTint = Color(0xFFEF4444),
            isDestructive = true,
            onClick = onReset,
            testTag = "action_card_start_fresh",
            surfaceBg = surfaceBg,
            borderColor = borderColor
        )
    }
}

@Composable
private fun NextActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String,
    surfaceBg: Color,
    borderColor: Color,
    isDestructive: Boolean = false
) {
    val cardBg = if (isDestructive) Color(0xFFEF4444).copy(alpha = 0.08f) else surfaceBg
    val cardBorder = if (isDestructive) Color(0xFFEF4444).copy(alpha = 0.35f) else borderColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDestructive) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (isDestructive) Color(0xFFEF4444).copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun formatNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        String.format(java.util.Locale.US, "%,.0f", value)
    } else {
        String.format(java.util.Locale.US, "%,.2f", value)
    }
}

private fun buildRecalibrationReportText(result: RecalibrationResult): String {
    val unit = result.proportions.firstOrNull()?.unit ?: "kg"
    return buildString {
        appendLine("========================================")
        appendLine("    MIXTURE FORMULATION CALIBRATION     ")
        appendLine("========================================")
        appendLine("Anchor Component: ${result.anchorIngredientName}")
        appendLine("Target Recipe: ${formatNumber(result.anchorTargetQty)} $unit")
        appendLine("Actual in Tub: ${formatNumber(result.anchorActualAdded)} $unit")
        appendLine("Scaled Anchor Factor: ${result.formattedStrictAnchorScaleFactor}")
        appendLine("Active Adjustment Pathway: ${result.adjustmentMode.title}")
        appendLine("")
        appendLine("----------------------------------------")
        appendLine("LOCKED TOTAL WEIGHT (CONSTANT MIXTURE)")
        appendLine("----------------------------------------")
        appendLine("Locked Total Initial Mixture Weight: ${result.formattedLockedInitialTotalWeight}")
        appendLine("Anchor Updated (Exact): ${result.anchorIngredientName} = ${formatNumber(result.anchorActualAdded)} $unit")
        result.constantWeightProportions.forEach { prop ->
            val deltaStr = prop.formattedSuggestedAdjustmentDelta
            val action = if (prop.isAnchor) "[Anchor (Exact)]" else if (prop.suggestedAdjustmentDelta > 0.0001) "[Add]" else if (prop.suggestedAdjustmentDelta < -0.0001) "[Reduce]" else "[Keep]"
            appendLine("• ${prop.ingredientName}: Target=${prop.formattedTarget} | Adjusted Required=${prop.formattedNewTotal} | Suggested Delta: $deltaStr $action")
        }
        appendLine("")
        appendLine("----------------------------------------")
        appendLine("OPTION 1: ADD OTHER COMPONENTS TO MATCH SCALED ANCHOR")
        appendLine("----------------------------------------")
        appendLine("Anchor Locked: ${result.anchorIngredientName} = ${formatNumber(result.anchorActualAdded)} $unit (0 to add)")
        appendLine("Total Material to Add: +${result.formattedTotalStillNeededWeight}")
        appendLine("Total Mixture Weight: ${result.formattedTotalNewWeight}")
        result.addModeProportions.forEach { prop ->
            val toAdd = if (prop.isAnchor) {
                "0 $unit [Anchor Scaled / 0 to Add]"
            } else if (prop.stillNeeded > 0.0001) {
                "+${prop.formattedStillNeeded} [ADD TO TUB]"
            } else {
                "0 $unit [In Tub >= Target]"
            }
            appendLine("• ${prop.ingredientName}: Scaled Target=${prop.formattedNewTotal} | In-Tub=${prop.formattedAlreadyAdded} | To Add: $toAdd")
        }
        appendLine("")
        appendLine("----------------------------------------")
        appendLine("OPTION 2: PROPORTIONAL REDUCTIONS & TUB EXCESS")
        appendLine("----------------------------------------")
        appendLine("Total Recipe Reduction: -${result.formattedTotalReduction}")
        if (result.hasExcessInTub) {
            appendLine("TOTAL EXCESS IN TUB TO REDUCE/REMOVE: -${result.formattedTotalExcessWeight}")
        }
        result.reduceModeProportions.forEach { prop ->
            val action = when {
                prop.excessAmount > 0.0001 -> "-${prop.formattedExcess} [REDUCE / REMOVE FROM TUB]"
                prop.stillNeeded > 0.0001 -> "+${prop.formattedStillNeeded} [ADD TO TUB]"
                else -> "0 $unit [Balanced]"
            }
            val origReduction = if (prop.reductionFromOriginal > 0.0001) " (-${prop.formattedReductionFromOriginal} vs original)" else ""
            appendLine("• ${prop.ingredientName}: Target=${prop.formattedTarget} | In-Tub=${prop.formattedAlreadyAdded} | Scaled Target=${prop.formattedNewTotal} | Action: $action$origReduction")
        }
        appendLine("========================================")
    }
}
