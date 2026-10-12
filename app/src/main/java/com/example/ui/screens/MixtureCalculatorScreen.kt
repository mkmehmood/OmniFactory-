package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.mixture.CalcCorrectionRow
import com.example.domain.mixture.CalcIngType
import com.example.domain.mixture.CalcMode
import com.example.domain.mixture.CalcUnit
import com.example.domain.mixture.FormulaCalcEngine
import com.example.ui.CalcTab
import com.example.ui.MixtureCalculatorViewModel
import com.example.ui.components.DottedFilterChip
import com.example.ui.components.DottedLineButton
import com.example.ui.components.LiquidGlassTextField
import com.example.ui.components.SpatialFloatingPill
import com.example.ui.components.SpatialGlassCard
import com.example.ui.components.SpatialGlassStatCard
import com.example.ui.components.straightDottedLine
import com.example.ui.theme.DigitalMonospaceFont

private val AmberColor = Color(0xFFF59E0B)
private val DangerColor = Color(0xFFEF4444)

/**
 * Mixture Calculator (native Compose).
 *
 * Step 1 "Formula": batch settings, ingredients (saved names, Fixed / Additive), scaled results, saved formulas, backup.
 * Step 2 "Re-balance": batch correction against an editable target formula.
 * Step 3 "Report": comparison of formula, scaled batch and correction, with copyable reports.
 *
 * State lives in [MixtureCalculatorViewModel]; storage, backup and restore go through
 * MixtureCalculatorStore (so the Settings backup carries the calculator data as well).
 */
@Composable
fun MixtureCalculatorScreen(
    onNavigateToHistory: () -> Unit = {},
    modifier: Modifier = Modifier,
    calcViewModel: MixtureCalculatorViewModel = viewModel()
) {
    val vm = calcViewModel
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(vm) {
        vm.messages.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }
    LaunchedEffect(vm.tab) { scrollState.scrollTo(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        CalcHeader(
            unit = vm.inputs.unit,
            tab = vm.tab,
            onToggleUnit = vm::toggleUnit,
            onReset = vm::resetAll,
            onSelectTab = vm::selectTab
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (vm.tab) {
                CalcTab.FORMULA -> FormulaTab(vm, onNavigateToHistory)
                CalcTab.REBALANCE -> RebalanceTab(vm)
                CalcTab.REPORT -> ReportTab(vm)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ====================================================================== header

@Composable
private fun CalcHeader(
    unit: CalcUnit,
    tab: CalcTab,
    onToggleUnit: () -> Unit,
    onReset: () -> Unit,
    onSelectTab: (CalcTab) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Science, contentDescription = null, tint = primary, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Mixture Calculator",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Batch & Correction",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DottedLineButton(
                onClick = onToggleUnit,
                isSelected = true,
                accentColor = primary,
                testTag = "calc_unit_toggle"
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = primary, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Unit: ${unit.label}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = primary
                )
            }
            IconButton(onClick = onReset, modifier = Modifier.testTag("calc_reset")) {
                Icon(
                    Icons.Default.RestartAlt,
                    contentDescription = "Reset",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcTab.values().forEachIndexed { index, t ->
                SpatialFloatingPill(
                    text = "${index + 1}. ${t.title}",
                    isSelected = tab == t,
                    onClick = { onSelectTab(t) },
                    modifier = Modifier.weight(1f),
                    testTag = "calc_tab_${t.name.lowercase()}"
                )
            }
        }
    }
}

// ====================================================================== tab 1: formula

@Composable
private fun FormulaTab(vm: MixtureCalculatorViewModel, onNavigateToHistory: () -> Unit) {
    val inputs = vm.inputs
    val d = vm.derived
    val unit = inputs.unit
    val primary = MaterialTheme.colorScheme.primary
    val batch = d.batch
    val unitLabel = unitLabelOf(inputs.baseText, unit)

    // ---- dashboard
    SpatialGlassStatCard(
        title = "Total Batch Weight",
        value = FormulaCalcEngine.fmt(batch.grandTotalKg, unit),
        subtitle = (if (unit == CalcUnit.KG) "Grams" else "Kilograms") +
            ": ${FormulaCalcEngine.fmtAlt(batch.grandTotalKg, unit)}  ·  100%",
        modifier = Modifier.fillMaxWidth(),
        icon = Icons.Default.Science,
        accentColor = primary,
        testTag = "calc_card_total"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        SpatialGlassStatCard(
            title = "Multiplier",
            value = FormulaCalcEngine.fmtMultiplier(batch.multiplier),
            modifier = Modifier.weight(1f),
            accentColor = AmberColor,
            testTag = "calc_card_multiplier"
        )
        SpatialGlassStatCard(
            title = "Base Units ($unitLabel)",
            value = String.format(java.util.Locale.US, "%.2f", batch.unitsCount),
            modifier = Modifier.weight(1f),
            accentColor = primary,
            testTag = "calc_card_units"
        )
    }

    // ---- mode
    SectionCard(title = "Mode") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            DottedFilterChip(
                selected = inputs.mode == CalcMode.UNIT_BASED,
                onClick = { vm.setMode(CalcMode.UNIT_BASED) },
                label = "Unit → Batch",
                modifier = Modifier.weight(1f),
                testTag = "calc_mode_unit"
            )
            DottedFilterChip(
                selected = inputs.mode == CalcMode.BATCH_EXTRACT,
                onClick = { vm.setMode(CalcMode.BATCH_EXTRACT) },
                label = "Batch → Unit",
                modifier = Modifier.weight(1f),
                testTag = "calc_mode_batch"
            )
        }
        Text(
            if (inputs.mode == CalcMode.UNIT_BASED) "Amounts below describe ONE base unit; scale it up to a large batch."
            else "Amounts below describe the WHOLE batch; the per-unit formula is extracted from it.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // ---- batch settings
    SectionCard(title = "Batch Settings") {
        NumberField(
            label = "Base Unit Size (${unit.label})",
            value = inputs.baseText,
            onChange = vm::setBase,
            suffix = unit.label,
            modifier = Modifier.fillMaxWidth(),
            testTag = "calc_base"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            NumberField(
                label = "Target Batch (${unit.label})",
                value = inputs.capText,
                onChange = vm::setCap,
                suffix = unit.label,
                modifier = Modifier.weight(1f),
                trailing = if (inputs.capText.isNotEmpty() || inputs.multText.isNotEmpty()) {
                    {
                        IconButton(onClick = vm::clearCap, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear target batch", modifier = Modifier.size(16.dp))
                        }
                    }
                } else null,
                testTag = "calc_cap"
            )
            NumberField(
                label = "Batch Multiplier",
                value = inputs.multText,
                onChange = vm::setMultiplier,
                placeholder = "1",
                modifier = Modifier.weight(1f),
                testTag = "calc_multiplier"
            )
        }
    }

    // ---- ingredients
    SectionCard(
        title = "Formula Ingredients",
        action = {
            ActionButton("Add Ingredient", Icons.Default.Add, vm::addRow, testTag = "calc_add_ingredient")
        }
    ) {
        SavedNamesManager(vm)
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

        val nameOptions = remember(vm.savedNames) {
            listOf(DropdownOption("", "— None / Hide —")) + vm.savedNames.map { DropdownOption(it, it) }
        }
        inputs.rows.forEach { row ->
            val isAuto = d.auto?.id == row.id && FormulaCalcEngine.parseNum(row.valueText) <= 0
            val options = if (row.name.isNotEmpty() && vm.savedNames.none { it == row.name })
                nameOptions + DropdownOption(row.name, row.name) else nameOptions
            RowCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DropdownField(
                        valueText = row.name,
                        emptyText = "— None / Hide —",
                        options = options,
                        onSelect = { vm.setRowName(row.id, it) },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { vm.deleteRow(row.id) }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete ingredient", tint = DangerColor)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    DropdownField(
                        valueText = row.type.label,
                        emptyText = "",
                        options = CalcIngType.values().map { DropdownOption(it.key, it.label) },
                        onSelect = { vm.setRowType(row.id, CalcIngType.fromKey(it)) },
                        modifier = Modifier.weight(1f)
                    )
                    NumberField(
                        label = "Amount",
                        value = row.valueText,
                        onChange = { vm.setRowValue(row.id, it) },
                        suffix = unit.label,
                        placeholder = if (isAuto) FormulaCalcEngine.addInputText(d.auto?.kg ?: 0.0, unit) else "0",
                        placeholderColor = if (isAuto) AmberColor else null,
                        modifier = Modifier.weight(1f),
                        testTag = "calc_ing_value_${row.id}"
                    )
                }
            }
        }

        d.auto?.let { auto ->
            val what = if (inputs.mode == CalcMode.UNIT_BASED) "base unit" else "target batch"
            Text(
                if (auto.othersKg > auto.totalKg) {
                    "Auto: other ingredients (${FormulaCalcEngine.fmt(auto.othersKg, unit)}) already exceed the $what " +
                        "(${FormulaCalcEngine.fmt(auto.totalKg, unit)}), so ${auto.name} = 0."
                } else {
                    "Auto: ${auto.name} = ${FormulaCalcEngine.fmt(auto.kg, unit)}  ($what " +
                        "${FormulaCalcEngine.fmt(auto.totalKg, unit)} − others ${FormulaCalcEngine.fmt(auto.othersKg, unit)})"
                },
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = AmberColor
            )
        }

        TotalLine("Total (per base unit)", FormulaCalcEngine.fmt(batch.unitTotalKg, unit))
    }

    // ---- results
    SectionCard(
        title = "Results",
        action = {
            ActionButton("Copy Report", Icons.Default.ContentCopy, vm::copyBatchReport, testTag = "calc_copy_batch")
        }
    ) {
        if (batch.rows.isEmpty()) {
            EmptyHint("Pick an ingredient name and amount above to see the scaled batch.")
        }
        batch.rows.forEach { r ->
            val share = if (batch.grandTotalKg > 0) r.finalKg / batch.grandTotalKg * 100 else 0.0
            RowCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        r.name.ifBlank { "–" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    TypeBadge(r.type)
                }
                LabelValue("Per Unit ($unitLabel)", FormulaCalcEngine.fmt(r.unitKg, unit))
                LabelValue("Final Weight", FormulaCalcEngine.fmt(r.finalKg, unit), valueColor = primary, bold = true)
                LabelValue("Share", String.format(java.util.Locale.US, "%.1f%%", share))
            }
        }
        if (batch.rows.isNotEmpty()) {
            TotalLine("Total (unit)", FormulaCalcEngine.fmt(batch.unitTotalKg, unit))
            TotalLine(
                "Total (batch)",
                FormulaCalcEngine.fmt(batch.grandTotalKg, unit),
                valueColor = primary,
                trailing = if (batch.grandTotalKg > 0) "100.0%" else "0.0%"
            )
        }
    }

    // ---- saved formulas & backup
    SavedFormulasAndBackup(vm, onNavigateToHistory)
}

@Composable
private fun SavedNamesManager(vm: MixtureCalculatorViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Saved names — pick one to update or delete, or type a new one to save",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DropdownField(
            valueText = vm.selectedName,
            emptyText = "— Saved names (${vm.savedNames.size}) —",
            options = listOf(DropdownOption("", "— Saved names (${vm.savedNames.size}) —")) +
                vm.savedNames.map { DropdownOption(it, it) },
            onSelect = { vm.onSelectName(it) },
            modifier = Modifier.fillMaxWidth()
        )
        LiquidGlassTextField(
            value = vm.newNameInput,
            onValueChange = vm::setNewNameInput,
            label = { Text("Name (Urdu / local)", fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("calc_new_name")
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ActionButton("Save New", Icons.Default.Check, vm::saveNewName, Modifier.weight(1f), testTag = "calc_name_save")
            ActionButton("Update", Icons.Default.Edit, vm::updateSelectedName, Modifier.weight(1f), testTag = "calc_name_update")
            ActionButton("Delete", Icons.Default.Delete, vm::deleteSelectedName, Modifier.weight(1f), accent = DangerColor, testTag = "calc_name_delete")
        }
    }
}

@Composable
private fun SavedFormulasAndBackup(vm: MixtureCalculatorViewModel, onNavigateToHistory: () -> Unit) {
    var pendingConfirm by remember { mutableStateOf<(() -> Unit)?>(null) }
    var fileReplace by remember { mutableStateOf(false) }

    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> if (uri != null) vm.exportToUri(uri) }
    val openLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) vm.importFromUri(uri, fileReplace) }

    SectionCard(title = "Saved Formulas & Backup") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            LiquidGlassTextField(
                value = vm.formulaNameInput,
                onValueChange = vm::setFormulaNameInput,
                label = { Text("Formula name", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("calc_formula_name")
            )
            ActionButton("Save", Icons.Default.Bookmark, vm::saveFormula, testTag = "calc_formula_save")
        }

        val countLabel = "— Saved formulas (${vm.formulas.size}) —"
        DropdownField(
            valueText = vm.formulas.firstOrNull { it.id == vm.selectedFormulaId }?.name ?: "",
            emptyText = countLabel,
            options = listOf(DropdownOption("", countLabel)) + vm.formulas.map { DropdownOption(it.id.toString(), it.name) },
            onSelect = { vm.selectFormula(it.toLongOrNull()) },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ActionButton("Load", Icons.Default.Restore, vm::loadFormula, Modifier.weight(1f), testTag = "calc_formula_load")
            ActionButton("Delete", Icons.Default.Delete, vm::deleteFormula, Modifier.weight(1f), accent = DangerColor, testTag = "calc_formula_delete")
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

        Text(
            "Backup includes saved names, saved formulas and your current work. The app's Settings backup includes it automatically.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LiquidGlassTextField(
            value = vm.restoreText,
            onValueChange = vm::setRestoreText,
            label = { Text("Paste a backup here to restore", fontSize = 11.sp) },
            singleLine = false,
            minLines = 3,
            maxLines = 6,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("calc_restore_text")
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ActionButton("Copy Backup", Icons.Default.ContentCopy, vm::copyBackup, Modifier.weight(1f), testTag = "calc_backup_copy")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ActionButton("Restore: Merge", Icons.Default.Upload, { vm.restoreFromText(false) }, Modifier.weight(1f), testTag = "calc_restore_merge")
            ActionButton(
                "Restore: Replace", Icons.Default.Upload,
                {
                    pendingConfirm = { vm.restoreFromText(true) }
                },
                Modifier.weight(1f), accent = AmberColor, testTag = "calc_restore_replace"
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

        Text("Backup file", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ActionButton(
                "Save File", Icons.Default.Download,
                { saveLauncher.launch("mixture_calculator_backup.json") },
                Modifier.weight(1f), testTag = "calc_file_save"
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ActionButton(
                "Import: Merge", Icons.Default.Upload,
                {
                    fileReplace = false
                    openLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                },
                Modifier.weight(1f), testTag = "calc_file_merge"
            )
            ActionButton(
                "Import: Replace", Icons.Default.Upload,
                {
                    pendingConfirm = {
                        fileReplace = true
                        openLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    }
                },
                Modifier.weight(1f), accent = AmberColor, testTag = "calc_file_replace"
            )
        }

        ActionButton(
            "Mixture Recipes & Backups", Icons.Default.Science, onNavigateToHistory,
            Modifier.fillMaxWidth(), testTag = "calc_open_recipes"
        )
    }

    pendingConfirm?.let { action ->
        AlertDialog(
            onDismissRequest = { pendingConfirm = null },
            title = { Text("Replace calculator data?") },
            text = {
                Text("This replaces your saved formulas and current work with the backup. Saved names stay unless the backup has its own. Merge keeps what you have instead.")
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingConfirm = null
                    action()
                }) { Text("Replace", color = DangerColor) }
            },
            dismissButton = {
                TextButton(onClick = { pendingConfirm = null }) { Text("Cancel") }
            }
        )
    }
}

// ====================================================================== tab 2: re-balance

@Composable
private fun RebalanceTab(vm: MixtureCalculatorViewModel) {
    val inputs = vm.inputs
    val d = vm.derived
    val c = d.correction
    val unit = inputs.unit
    val primary = MaterialTheme.colorScheme.primary

    SectionCard(title = "Batch Correction") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            NumberField(
                label = "Original Batch (${unit.label})",
                value = inputs.origText,
                onChange = vm::setOrig,
                suffix = unit.label,
                modifier = Modifier.weight(1f),
                testTag = "calc_orig"
            )
            NumberField(
                label = "Used / Lost (${unit.label})",
                value = inputs.usedText,
                onChange = vm::setUsed,
                suffix = unit.label,
                modifier = Modifier.weight(1f),
                testTag = "calc_used"
            )
        }
        TotalLine("Remaining Batch", FormulaCalcEngine.fmt(c.remKg, unit), valueColor = primary)
        if (!c.active) {
            EmptyHint(
                if (d.batch.grandTotalKg <= 0) "Enter a formula on the Formula tab first."
                else "Enter the original batch and the amount used / lost to see what to add."
            )
        }
    }

    SectionCard(
        title = "Target Formula",
        action = {
            ActionButton("Reload", Icons.Default.Refresh, vm::syncTargetWithOriginal, testTag = "calc_target_reload")
        }
    ) {
        val named = inputs.rows.filter { it.name.isNotBlank() }
        if (named.isEmpty()) EmptyHint("No ingredients yet.")
        named.forEach { row ->
            RowCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        row.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    TypeBadge(row.type)
                }
                NumberField(
                    label = "Target amount",
                    value = vm.targetTextFor(row.id),
                    onChange = { vm.setTarget(row.id, it) },
                    suffix = unit.label,
                    accent = AmberColor,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "calc_target_${row.id}"
                )
            }
        }
    }

    SectionCard(
        title = "Required Additions",
        action = {
            Column(horizontalAlignment = Alignment.End) {
                Text("New total", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    FormulaCalcEngine.fmt(c.newTotalKg, unit),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = primary
                )
            }
        }
    ) {
        Text(
            "Type a new amount in any Add box — all other ingredients update automatically to keep the formula ratio.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (c.manual) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Adjusted manually. Others scaled to match.",
                    fontSize = 11.5.sp,
                    color = AmberColor,
                    modifier = Modifier.weight(1f)
                )
                ActionButton("Minimum", Icons.Default.Refresh, vm::resetCorrectionScale, accent = AmberColor, testTag = "calc_min_scale")
            }
        }
        c.rows.forEach { r ->
            RowCard {
                Text(
                    r.name.ifBlank { "–" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LabelValue("In Remaining", FormulaCalcEngine.fmt(r.remKg, unit))
                LabelValue("Required", FormulaCalcEngine.fmt(r.targetKg, unit))
                if (r.editable) {
                    AddAmountField(r, unit) { vm.onAddEdit(r.id, it) }
                } else {
                    LabelValue("Add", FormulaCalcEngine.fmt(r.addKg, unit), valueColor = AmberColor, bold = true)
                }
            }
        }
        if (c.active) {
            TotalLine("Total in remaining", FormulaCalcEngine.fmt(c.remTotalKg, unit))
            TotalLine("Total required", FormulaCalcEngine.fmt(c.newTotalKg, unit))
            TotalLine("Total to add", FormulaCalcEngine.fmt(c.addTotalKg, unit), valueColor = AmberColor)
        }
    }
}

@Composable
private fun AddAmountField(row: CalcCorrectionRow, unit: CalcUnit, onEdit: (String) -> Unit) {
    val derivedText = FormulaCalcEngine.addInputText(row.addKg, unit)
    var local by remember(row.id) { mutableStateOf<String?>(null) }
    var focused by remember(row.id) { mutableStateOf(false) }
    NumberField(
        label = "Add (${unit.label})",
        value = if (focused) (local ?: derivedText) else derivedText,
        onChange = {
            val clean = FormulaCalcEngine.sanitizeNumeric(it)
            local = clean
            onEdit(clean)
        },
        suffix = unit.label,
        accent = AmberColor,
        textColor = if (row.addKg > 0) AmberColor else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                focused = it.hasFocus
                if (!it.hasFocus) local = null
            },
        testTag = "calc_add_${row.id}"
    )
}

// ====================================================================== tab 3: report

@Composable
private fun ReportTab(vm: MixtureCalculatorViewModel) {
    val inputs = vm.inputs
    val d = vm.derived
    val unit = inputs.unit
    val primary = MaterialTheme.colorScheme.primary
    val batch = d.batch
    val unitLabel = unitLabelOf(inputs.baseText, unit)

    SectionCard(
        title = "Comparison",
        action = {
            ActionButton("Copy Full Report", Icons.Default.ContentCopy, vm::copyComparisonReport, testTag = "calc_copy_full")
        }
    ) {
        if (batch.rows.isEmpty()) EmptyHint("Enter a formula on the Formula tab first.")
        var addTotal = 0.0
        batch.rows.forEach { r ->
            val add = d.correction.addFor(r.id)
            addTotal += add
            RowCard {
                Text(
                    r.name.ifBlank { "–" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LabelValue("Per Unit ($unitLabel)", FormulaCalcEngine.fmt(r.unitKg, unit))
                LabelValue("Scaled Batch", FormulaCalcEngine.fmt(r.finalKg, unit), valueColor = primary, bold = true)
                LabelValue(
                    "Correction Addition",
                    if (add > 0) FormulaCalcEngine.fmt(add, unit) else "–",
                    valueColor = if (add > 0) AmberColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    bold = add > 0
                )
            }
        }
        if (batch.rows.isNotEmpty()) {
            TotalLine("Total (unit)", FormulaCalcEngine.fmt(batch.unitTotalKg, unit))
            TotalLine("Total (batch)", FormulaCalcEngine.fmt(batch.grandTotalKg, unit), valueColor = primary)
            TotalLine(
                "Total to add",
                if (addTotal > 0) FormulaCalcEngine.fmt(addTotal, unit) else "–",
                valueColor = AmberColor
            )
        }
    }
}

// ====================================================================== shared building blocks

private fun unitLabelOf(baseText: String, unit: CalcUnit): String {
    val base = FormulaCalcEngine.parseNum(baseText)
    return if (base > 0) "$baseText${unit.label}" else "–"
}

private data class DropdownOption(val value: String, val label: String)

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    SpatialGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = 4.dp,
        testTag = "calc_section_${title.lowercase().replace(" ", "_").replace("&", "and")}"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    title.uppercase(),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (action != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    action()
                }
            }
            content()
        }
    }
}

@Composable
private fun RowCard(content: @Composable ColumnScope.() -> Unit) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val border = if (isDark) Color(0x28FFFFFF) else Color(0x1E000000)
    val fill = if (isDark) Color(0x12FFFFFF) else Color(0x08000000)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(fill, RoundedCornerShape(12.dp))
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = content
    )
}

@Composable
private fun LabelValue(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label.uppercase(),
            fontSize = 10.5.sp,
            letterSpacing = 0.4.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            fontFamily = DigitalMonospaceFont,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun TotalLine(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .straightDottedLine(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f), 1.dp)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = DigitalMonospaceFont,
            color = valueColor
        )
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                trailing,
                fontSize = 12.sp,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun TypeBadge(type: CalcIngType) {
    val color = if (type == CalcIngType.FIXED_BASE) AmberColor else MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        type.label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    testTag: String? = null
) {
    DottedLineButton(
        onClick = onClick,
        modifier = modifier,
        isSelected = true,
        accentColor = accent,
        testTag = testTag
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = DigitalMonospaceFont,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    placeholder: String = "0",
    placeholderColor: Color? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: (@Composable () -> Unit)? = null,
    testTag: String? = null
) {
    LiquidGlassTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        textStyle = LocalTextStyle.current.copy(
            fontFamily = DigitalMonospaceFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = textColor
        ),
        label = { Text(label, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        placeholder = {
            Text(
                placeholder,
                fontFamily = DigitalMonospaceFont,
                fontWeight = if (placeholderColor != null) FontWeight.Bold else FontWeight.Normal,
                color = placeholderColor ?: Color.Unspecified
            )
        },
        suffix = if (suffix != null) {
            { Text(suffix, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else null,
        trailingIcon = trailing,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        accentColor = accent,
        testTag = testTag
    )
}

@Composable
private fun DropdownField(
    valueText: String,
    emptyText: String,
    options: List<DropdownOption>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val line = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.50f else 0.40f)
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .straightDottedLine(line, 1.3.dp)
                .clickable { expanded = true }
                .padding(horizontal = 4.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (valueText.isEmpty()) emptyText else valueText,
                fontSize = 14.sp,
                fontWeight = if (valueText.isEmpty()) FontWeight.Normal else FontWeight.Bold,
                color = if (valueText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    onClick = {
                        expanded = false
                        onSelect(option.value)
                    }
                )
            }
        }
    }
}
