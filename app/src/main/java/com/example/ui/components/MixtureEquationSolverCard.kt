package com.example.ui.components

import com.example.ui.theme.SoftColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.mixture.BaseBatchResult
import com.example.domain.mixture.BatchRole
import com.example.domain.mixture.ComponentRole
import com.example.domain.mixture.EquationMode
import com.example.domain.mixture.ReferenceResult
import com.example.domain.mixture.ReferenceRole
import com.example.domain.mixture.TargetIngredient
import com.example.domain.mixture.TargetTotalResult

private val ErrorRed = SoftColors.negative
private val WarnAmber = SoftColors.warning
private val OkGreen = SoftColors.positive

/**
 * Solver controls for the non-anchor formulation methods. Part of Step 3; results flow into Step 4+.
 * Problems covered:
 *  - "Total batch is 2.3 kg. Keep C = 400 g and D = 150 g, split the rest between A and B by ratio."
 *  - "C and D are per 2.3 kg of A+B. A+B is actually 90 kg. How much C and D?"
 *
 * Stateless: all state lives in MixtureViewModel.
 */
@Composable
fun MixtureEquationSolverCard(
    ingredients: List<TargetIngredient>,
    mode: EquationMode,
    // Target total mode
    targetTotalText: String,
    targetTotalUnit: String,
    onTargetTotalChange: (String) -> Unit,
    onTargetUnitChange: (String) -> Unit,
    roles: Map<String, ComponentRole>,
    fixedTexts: Map<String, String>,
    onRoleChange: (String, ComponentRole) -> Unit,
    onFixedChange: (String, String) -> Unit,
    targetResult: TargetTotalResult?,
    // Per reference mode
    referenceRoles: Map<String, ReferenceRole>,
    referenceWeightText: String,
    referenceUnit: String,
    referenceResultUnit: String,
    onReferenceRoleChange: (String, ReferenceRole) -> Unit,
    onReferenceWeightChange: (String) -> Unit,
    onReferenceUnitChange: (String) -> Unit,
    onReferenceResultUnitChange: (String) -> Unit,
    referenceResult: ReferenceResult?,
    onUseAsBase: () -> Unit,
    // Base + additives mode
    batchRoles: Map<String, BatchRole>,
    baseTargetText: String,
    baseTargetUnit: String,
    batchMultiplierText: String,
    onBatchRoleChange: (String, BatchRole) -> Unit,
    onBaseTargetChange: (String) -> Unit,
    onBaseTargetUnitChange: (String) -> Unit,
    onBatchMultiplierChange: (String) -> Unit,
    baseBatchResult: BaseBatchResult?,
    // Optional: what is already in the tub (shared by all methods)
    inTubTexts: Map<String, String>,
    onInTubChange: (String, String) -> Unit,
    onClearInTub: () -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    SoftSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = mode.hint,
                fontSize = 11.sp,
                color = primaryColor.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (ingredients.isEmpty()) {
                Text(
                    text = "Add ingredients in Step 1 first.",
                    fontSize = 12.sp,
                    color = WarnAmber
                )
                return@Column
            }

            InTubBlock(
                ingredients = ingredients,
                texts = inTubTexts,
                onChange = onInTubChange,
                onClear = onClearInTub,
                primaryColor = primaryColor,
                borderColor = borderColor
            )

            when (mode) {
                EquationMode.ANCHOR -> Unit
                EquationMode.TARGET_TOTAL -> TargetTotalSection(
                    ingredients = ingredients,
                    targetTotalText = targetTotalText,
                    targetTotalUnit = targetTotalUnit,
                    onTargetTotalChange = onTargetTotalChange,
                    onTargetUnitChange = onTargetUnitChange,
                    roles = roles,
                    fixedTexts = fixedTexts,
                    onRoleChange = onRoleChange,
                    onFixedChange = onFixedChange,
                    result = targetResult,
                    primaryColor = primaryColor,
                    borderColor = borderColor
                )
                EquationMode.PER_REFERENCE -> PerReferenceSection(
                    ingredients = ingredients,
                    roles = referenceRoles,
                    referenceWeightText = referenceWeightText,
                    referenceUnit = referenceUnit,
                    referenceResultUnit = referenceResultUnit,
                    onRoleChange = onReferenceRoleChange,
                    onReferenceWeightChange = onReferenceWeightChange,
                    onReferenceUnitChange = onReferenceUnitChange,
                    onResultUnitChange = onReferenceResultUnitChange,
                    result = referenceResult,
                    onUseAsBase = onUseAsBase,
                    primaryColor = primaryColor,
                    borderColor = borderColor
                )
                EquationMode.BASE_BATCH -> BaseBatchSection(
                    ingredients = ingredients,
                    roles = batchRoles,
                    baseTargetText = baseTargetText,
                    baseTargetUnit = baseTargetUnit,
                    multiplierText = batchMultiplierText,
                    onRoleChange = onBatchRoleChange,
                    onBaseTargetChange = onBaseTargetChange,
                    onBaseUnitChange = onBaseTargetUnitChange,
                    onMultiplierChange = onBatchMultiplierChange,
                    result = baseBatchResult,
                    primaryColor = primaryColor,
                    borderColor = borderColor
                )
            }
        }
    }
}

@Composable
private fun InTubBlock(
    ingredients: List<TargetIngredient>,
    texts: Map<String, String>,
    onChange: (String, String) -> Unit,
    onClear: () -> Unit,
    primaryColor: Color,
    borderColor: Color
) {
    val hasValues = texts.values.any { it.isNotBlank() }
    var expanded by remember { mutableStateOf(hasValues) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Chip(
            label = if (expanded) "Hide in-tub amounts" else "Already in tub? (optional)",
            selected = expanded || hasValues,
            color = WarnAmber,
            onClick = { expanded = !expanded },
            modifier = Modifier.weight(1f)
        )
        if (hasValues) {
            Chip(
                label = "Clear",
                selected = false,
                color = ErrorRed,
                onClick = onClear,
                modifier = Modifier.weight(0.4f)
            )
        }
    }

    if (expanded) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter what is already mixed in. The result shows what to add, or what to remove if you are over.",
            fontSize = 11.sp,
            color = primaryColor.copy(alpha = 0.7f)
        )
        ingredients.forEach { item ->
            Spacer(modifier = Modifier.height(6.dp))
            LiquidGlassTextField(
                value = texts[item.id] ?: "",
                onValueChange = { onChange(item.id, it) },
                label = { Text("${item.name} in tub (${item.unit})") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                accentColor = WarnAmber,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    SoftDivider()
    Spacer(modifier = Modifier.height(12.dp))
}

/**
 * Method picker shown at the top of Step 3. Anchor Scale keeps the in-tub calibration flow; the other
 * methods solve a formulation that feeds the same result, batch-scaling and history steps.
 */
@Composable
fun FormulaMethodSelector(
    mode: EquationMode,
    onModeChange: (EquationMode) -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        EquationMode.values().toList().chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pair.forEach { m ->
                    Chip(
                        label = m.label,
                        selected = mode == m,
                        color = primaryColor,
                        onClick = { onModeChange(m) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetTotalSection(
    ingredients: List<TargetIngredient>,
    targetTotalText: String,
    targetTotalUnit: String,
    onTargetTotalChange: (String) -> Unit,
    onTargetUnitChange: (String) -> Unit,
    roles: Map<String, ComponentRole>,
    fixedTexts: Map<String, String>,
    onRoleChange: (String, ComponentRole) -> Unit,
    onFixedChange: (String, String) -> Unit,
    result: TargetTotalResult?,
    primaryColor: Color,
    borderColor: Color
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LiquidGlassTextField(
            value = targetTotalText,
            onValueChange = onTargetTotalChange,
            label = { Text("Target total") },
            placeholder = { Text("e.g. 2.3") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            accentColor = primaryColor,
            modifier = Modifier.weight(1.4f)
        )
        LiquidGlassTextField(
            value = targetTotalUnit,
            onValueChange = onTargetUnitChange,
            label = { Text("Unit") },
            placeholder = { Text("kg / g") },
            singleLine = true,
            accentColor = primaryColor,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = "Scale = share the remainder by original ratio. Fixed = keep this exact amount. Removed = 0.",
        fontSize = 11.sp,
        color = primaryColor.copy(alpha = 0.7f)
    )

    ingredients.forEach { item ->
        val role = roles[item.id] ?: ComponentRole.SCALE
        Spacer(modifier = Modifier.height(10.dp))
        SoftDivider()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${item.name}  (recipe: ${formatQty(item.targetQuantity)} ${item.unit})",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = primaryColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ComponentRole.values().forEach { r ->
                Chip(
                    label = r.label,
                    selected = role == r,
                    color = when (r) {
                        ComponentRole.SCALE -> primaryColor
                        ComponentRole.FIXED -> OkGreen
                        ComponentRole.REMOVED -> ErrorRed
                    },
                    onClick = { onRoleChange(item.id, r) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (role == ComponentRole.FIXED) {
            Spacer(modifier = Modifier.height(6.dp))
            LiquidGlassTextField(
                value = fixedTexts[item.id] ?: "",
                onValueChange = { onFixedChange(item.id, it) },
                label = { Text("Fixed amount (${item.unit})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                accentColor = OkGreen,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))
    TargetResultView(result = result, primaryColor = primaryColor, borderColor = borderColor)
}

@Composable
private fun TargetResultView(
    result: TargetTotalResult?,
    primaryColor: Color,
    borderColor: Color
) {
    if (result == null) {
        Text("Enter a target total to see the breakdown.", fontSize = 12.sp, color = primaryColor.copy(alpha = 0.7f))
        return
    }
    if (!result.isValid) {
        Text(
            text = result.errorMessage ?: "Cannot solve with these inputs.",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ErrorRed
        )
        result.steps.forEach { StepLine(it, primaryColor) }
        return
    }

    Text("Result", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryColor)
    Spacer(modifier = Modifier.height(6.dp))
    result.rows.forEach { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(row.name, fontSize = 12.sp, color = primaryColor)
                Text(
                    text = "${row.role.label}  |  was ${row.formattedBase}",
                    fontSize = 10.sp,
                    color = primaryColor.copy(alpha = 0.6f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(row.formattedResult, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                Text(row.formattedShare, fontSize = 10.sp, color = primaryColor.copy(alpha = 0.6f))
            }
        }
    }
    SoftDivider( modifier = Modifier.padding(vertical = 6.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Total", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
        Text(result.formattedAchieved, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OkGreen)
    }

    result.warnings.forEach {
        Spacer(modifier = Modifier.height(4.dp))
        Text(it, fontSize = 11.sp, color = WarnAmber)
    }
    Spacer(modifier = Modifier.height(8.dp))
    result.steps.forEach { StepLine(it, primaryColor) }
}

@Composable
private fun PerReferenceSection(
    ingredients: List<TargetIngredient>,
    roles: Map<String, ReferenceRole>,
    referenceWeightText: String,
    referenceUnit: String,
    referenceResultUnit: String,
    onRoleChange: (String, ReferenceRole) -> Unit,
    onReferenceWeightChange: (String) -> Unit,
    onReferenceUnitChange: (String) -> Unit,
    onResultUnitChange: (String) -> Unit,
    result: ReferenceResult?,
    onUseAsBase: () -> Unit,
    primaryColor: Color,
    borderColor: Color
) {
    Text(
        text = "Main mix = the actual mix you have (e.g. A + B). Per ref. = an amount quoted per the reference weight of main mix (e.g. C per 2.3 kg of A+B).",
        fontSize = 11.sp,
        color = primaryColor.copy(alpha = 0.7f)
    )
    Spacer(modifier = Modifier.height(10.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LiquidGlassTextField(
            value = referenceWeightText,
            onValueChange = onReferenceWeightChange,
            label = { Text("Reference weight") },
            placeholder = { Text("e.g. 2.3") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            accentColor = primaryColor,
            modifier = Modifier.weight(1.4f)
        )
        LiquidGlassTextField(
            value = referenceUnit,
            onValueChange = onReferenceUnitChange,
            label = { Text("Unit") },
            singleLine = true,
            accentColor = primaryColor,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    LiquidGlassTextField(
        value = referenceResultUnit,
        onValueChange = onResultUnitChange,
        label = { Text("Show results in") },
        placeholder = { Text("kg / g") },
        singleLine = true,
        accentColor = primaryColor,
        modifier = Modifier.fillMaxWidth()
    )

    ingredients.forEach { item ->
        val role = roles[item.id] ?: ReferenceRole.MAIN
        Spacer(modifier = Modifier.height(10.dp))
        SoftDivider()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${item.name}  (${formatQty(item.targetQuantity)} ${item.unit})",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = primaryColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ReferenceRole.values().forEach { r ->
                Chip(
                    label = r.label,
                    selected = role == r,
                    color = if (r == ReferenceRole.MAIN) primaryColor else OkGreen,
                    onClick = { onRoleChange(item.id, r) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (result == null) {
        Text("Enter a reference weight to see the breakdown.", fontSize = 12.sp, color = primaryColor.copy(alpha = 0.7f))
        return
    }
    if (!result.isValid) {
        Text(
            text = result.errorMessage ?: "Cannot solve with these inputs.",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ErrorRed
        )
        return
    }

    Text("Result", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryColor)
    Spacer(modifier = Modifier.height(6.dp))
    result.rows.forEach { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(row.name, fontSize = 12.sp, color = primaryColor)
                Text(row.role.label, fontSize = 10.sp, color = primaryColor.copy(alpha = 0.6f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Full: ${row.formattedFullScale}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                Text("Per ref: ${row.formattedPerReference}", fontSize = 10.sp, color = primaryColor.copy(alpha = 0.6f))
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    result.steps.forEach { StepLine(it, primaryColor) }

    Spacer(modifier = Modifier.height(10.dp))
    Chip(
        label = "Use per-reference recipe as base for Target Total",
        selected = true,
        color = OkGreen,
        onClick = onUseAsBase,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun BaseBatchSection(
    ingredients: List<TargetIngredient>,
    roles: Map<String, BatchRole>,
    baseTargetText: String,
    baseTargetUnit: String,
    multiplierText: String,
    onRoleChange: (String, BatchRole) -> Unit,
    onBaseTargetChange: (String) -> Unit,
    onBaseUnitChange: (String) -> Unit,
    onMultiplierChange: (String) -> Unit,
    result: BaseBatchResult?,
    primaryColor: Color,
    borderColor: Color
) {
    Text(
        text = "Ratio = shares the base left after fixed items, by its amount as a ratio (e.g. 80 : 10). " +
            "Fixed = exact weight inside the base. Additive = exact weight added on top of the base. " +
            "The multiplier scales every item.",
        fontSize = 11.sp,
        color = primaryColor.copy(alpha = 0.7f)
    )
    Spacer(modifier = Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LiquidGlassTextField(
            value = baseTargetText,
            onValueChange = onBaseTargetChange,
            label = { Text("Base target weight") },
            placeholder = { Text("e.g. 3") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            accentColor = primaryColor,
            modifier = Modifier.weight(1.4f)
        )
        LiquidGlassTextField(
            value = baseTargetUnit,
            onValueChange = onBaseUnitChange,
            label = { Text("Unit") },
            placeholder = { Text("kg / g") },
            singleLine = true,
            accentColor = primaryColor,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    LiquidGlassTextField(
        value = multiplierText,
        onValueChange = onMultiplierChange,
        label = { Text("Batch multiplier (x)") },
        placeholder = { Text("e.g. 10") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        accentColor = primaryColor,
        modifier = Modifier.fillMaxWidth()
    )

    ingredients.forEach { item ->
        val role = roles[item.id] ?: BatchRole.PROPORTIONAL
        Spacer(modifier = Modifier.height(10.dp))
        SoftDivider()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${item.name}  (${formatQty(item.targetQuantity)} ${item.unit})",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = primaryColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BatchRole.values().forEach { r ->
                Chip(
                    label = r.label,
                    selected = role == r,
                    color = when (r) {
                        BatchRole.PROPORTIONAL -> primaryColor
                        BatchRole.FIXED_BASE -> OkGreen
                        BatchRole.ADDITIVE -> WarnAmber
                    },
                    onClick = { onRoleChange(item.id, r) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (result == null) {
        Text("Enter a base target weight to see the breakdown.", fontSize = 12.sp, color = primaryColor.copy(alpha = 0.7f))
        return
    }
    if (!result.isValid) {
        Text(
            text = result.errorMessage ?: "Cannot solve with these inputs.",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ErrorRed
        )
        result.steps.forEach { StepLine(it, primaryColor) }
        return
    }

    Text("Result", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryColor)
    Spacer(modifier = Modifier.height(6.dp))
    result.rows.forEach { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(row.name, fontSize = 12.sp, color = primaryColor)
                Text(
                    text = "${row.role.label}  |  x1: ${row.formattedUnscaled}",
                    fontSize = 10.sp,
                    color = primaryColor.copy(alpha = 0.6f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(row.formattedFinal, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                Text(row.formattedShare, fontSize = 10.sp, color = primaryColor.copy(alpha = 0.6f))
            }
        }
    }
    SoftDivider( modifier = Modifier.padding(vertical = 6.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Unscaled total", fontSize = 12.sp, color = primaryColor)
        Text(result.formattedUnscaledTotal, fontSize = 12.sp, color = primaryColor)
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Final total", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
        Text(result.formattedFinalTotal, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OkGreen)
    }
    result.warnings.forEach {
        Spacer(modifier = Modifier.height(4.dp))
        Text(it, fontSize = 11.sp, color = WarnAmber)
    }
    Spacer(modifier = Modifier.height(8.dp))
    result.steps.forEach { StepLine(it, primaryColor) }
}

@Composable
private fun StepLine(text: String, color: Color) {
    Text(text = text, fontSize = 10.sp, color = color.copy(alpha = 0.65f))
}

@Composable
private fun Chip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SoftSurface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (selected) color.copy(alpha = 0.18f) else Color.Transparent) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = color,
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

private fun formatQty(value: Double): String =
    if (value % 1.0 == 0.0) String.format(java.util.Locale.US, "%,.0f", value)
    else String.format(java.util.Locale.US, "%,.3f", value).trimEnd('0').trimEnd('.')
