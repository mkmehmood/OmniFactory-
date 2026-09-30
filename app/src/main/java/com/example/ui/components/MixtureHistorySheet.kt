package com.example.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.CheckCircle
import com.example.domain.mixture.MixtureCalculatorEngine
import com.example.domain.mixture.TargetIngredient
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.MixtureRecipeEntity
import com.example.data.db.RestoreMode
import com.example.ui.theme.DigitalMonospaceFont
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MixtureHistorySheet(
    recipes: List<MixtureRecipeEntity>,
    onDismiss: () -> Unit,
    onLoadRecipe: (MixtureRecipeEntity) -> Unit,
    onEditRecipe: (MixtureRecipeEntity, String, String, String) -> Unit,
    onDeleteRecipe: (MixtureRecipeEntity) -> Unit,
    onToggleFavorite: (MixtureRecipeEntity) -> Unit,
    onClearAll: () -> Unit,
    onExportBackupJson: suspend () -> String,
    onRestoreBackupJson: (String, RestoreMode) -> Unit,
    onSaveToUri: (android.net.Uri) -> Unit,
    onLoadFromUri: (android.net.Uri, RestoreMode) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceBg = if (isDark) Color(0x18FFFFFF) else Color(0x0C000000)
    val borderColor = if (isDark) Color(0x28FFFFFF) else Color(0x1E000000)

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var editingEntity by remember { mutableStateOf<MixtureRecipeEntity?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showBackupRestoreModal by remember { mutableStateOf(false) }

    val categories = listOf("ALL", "Structural", "Chemical", "Culinary", "Industrial", "Custom")

    val filteredRecipes = remember(recipes, searchQuery, selectedCategory) {
        recipes.filter { item ->
            val matchesCategory = selectedCategory == "ALL" || item.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true) ||
                    item.anchorIngredientName.contains(searchQuery, ignoreCase = true) ||
                    item.ingredientsJson.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("mixture_history_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mixture History & Formulations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "${recipes.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontFamily = DigitalMonospaceFont,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Backup / Restore button
                    IconButton(
                        onClick = { showBackupRestoreModal = true },
                        modifier = Modifier.size(36.dp).testTag("mixture_backup_restore_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Backup & Restore Formulations",
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (recipes.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirm = true },
                            modifier = Modifier.size(36.dp).testTag("mixture_clear_all_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear all",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp).testTag("close_mixture_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            LiquidGlassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search saved formulations, ingredients...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                accentColor = primaryColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mixture_history_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    DottedFilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = cat,
                        accentColor = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Formulations List
            if (filteredRecipes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (recipes.isEmpty()) "No saved formulations yet" else "No matching formulations found",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (recipes.isEmpty()) "Calculate a mixture and tap Save Formulation" else "Try clearing your search query or filter",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredRecipes, key = { it.id }) { recipe ->
                        MixtureHistoryCard(
                            recipe = recipe,
                            onLoad = {
                                onLoadRecipe(recipe)
                                onDismiss()
                            },
                            onEdit = { editingEntity = recipe },
                            onDelete = { onDeleteRecipe(recipe) },
                            onToggleFavorite = { onToggleFavorite(recipe) },
                            onCopy = {
                                val summary = formatRecipeSummary(recipe)
                                clipboardManager.setText(AnnotatedString(summary))
                                Toast.makeText(context, "Formulation copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            surfaceBg = surfaceBg,
                            borderColor = borderColor,
                            primaryColor = primaryColor
                        )
                    }
                }
            }
        }
    }

    // Edit Dialog
    if (editingEntity != null) {
        val entity = editingEntity!!
        var editTitle by remember(entity) { mutableStateOf(entity.title) }
        var editCategory by remember(entity) { mutableStateOf(entity.category) }
        var editDescription by remember(entity) { mutableStateOf(entity.description) }

        AlertDialog(
            onDismissRequest = { editingEntity = null },
            title = {
                Text(
                    text = "Edit Formulation Details",
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LiquidGlassTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        accentColor = primaryColor,
                        modifier = Modifier.fillMaxWidth().testTag("edit_mixture_title")
                    )

                    LiquidGlassTextField(
                        value = editCategory,
                        onValueChange = { editCategory = it },
                        label = { Text("Category") },
                        placeholder = { Text("Structural / Chemical / Culinary...") },
                        singleLine = true,
                        accentColor = primaryColor,
                        modifier = Modifier.fillMaxWidth().testTag("edit_mixture_category")
                    )

                    LiquidGlassTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Description / Notes") },
                        singleLine = false,
                        accentColor = primaryColor,
                        modifier = Modifier.fillMaxWidth().testTag("edit_mixture_desc")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEditRecipe(entity, editTitle, editCategory, editDescription)
                        editingEntity = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingEntity = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Confirmation Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Formulations?") },
            text = { Text("This will permanently remove all saved mixture formulations from the database. This action cannot be undone unless you have a backup.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Standalone Backup & Restore Modal for Mixtures
    if (showBackupRestoreModal) {
        MixtureBackupRestoreDialog(
            onDismiss = { showBackupRestoreModal = false },
            onExportJson = onExportBackupJson,
            onRestoreJson = { json, mode ->
                onRestoreBackupJson(json, mode)
                showBackupRestoreModal = false
            },
            onSaveToUri = onSaveToUri,
            onLoadFromUri = { uri, mode ->
                onLoadFromUri(uri, mode)
                showBackupRestoreModal = false
            }
        )
    }
}

@Composable
private fun MixtureHistoryCard(
    recipe: MixtureRecipeEntity,
    onLoad: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    surfaceBg: Color,
    borderColor: Color,
    primaryColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val dateStr = remember(recipe.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
        sdf.format(Date(recipe.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .testTag("mixture_card_${recipe.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title, Favorite, Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateStr,
                        fontSize = 10.5.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = primaryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = recipe.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp).testTag("fav_btn_${recipe.id}")
                    ) {
                        Icon(
                            imageVector = if (recipe.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (recipe.isFavorite) "Favorite" else "Not favorite",
                            tint = if (recipe.isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Recipe Key Stats Row (Anchor, Total Weight, Batches)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x0A000000), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Anchor", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = recipe.anchorIngredientName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Weight", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${formatDouble(recipe.totalTargetWeight)} ${recipe.unit}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = primaryColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Batches", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${recipe.batchCount}x",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Description / Note if present
            if (recipe.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = recipe.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Expandable Full Multi-Step Calculation Process Breakdown
            AnimatedVisibility(visible = expanded) {
                val calc = remember(recipe) { getOrComputeMixtureCalculation(recipe) }

                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 2 & 3: Simplified Ratio & Calibration Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = primaryColor.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "FORMULATION RATIO & CALIBRATION",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = primaryColor
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "• Simplified Ratio: ${calc.simplifiedRatio}",
                                fontSize = 10.sp,
                                fontFamily = DigitalMonospaceFont,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = "• Calibration Anchor: ${calc.anchorName} (${formatDouble(calc.anchorTargetQty)} ${recipe.unit} Target → ${formatDouble(calc.anchorActualAdded)} ${recipe.unit} Added)",
                                fontSize = 10.sp,
                                fontFamily = DigitalMonospaceFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "• Scaling Factor: ",
                                    fontSize = 10.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = primaryColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%.2fx", calc.scaleFactor),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = DigitalMonospaceFont,
                                        color = primaryColor,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            if (calc.scaleExplanation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = calc.scaleExplanation,
                                    fontSize = 9.5.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step 4: Recalibrated Proportions & Correction Breakdown
                    Text(
                        text = "RECALIBRATED PROPORTIONS & NEEDED AMOUNTS:",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    calc.proportions.forEach { prop ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, borderColor.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = prop.name,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (prop.isAnchor) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = primaryColor.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "ANCHOR",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = DigitalMonospaceFont,
                                                    color = primaryColor,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "${formatDouble(prop.newRequiredTotal)} ${prop.unit}",
                                        fontSize = 12.sp,
                                        fontFamily = DigitalMonospaceFont,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Target: ${formatDouble(prop.targetQty)} ${prop.unit} | Added: ${formatDouble(prop.alreadyAdded)} ${prop.unit}",
                                        fontSize = 10.sp,
                                        fontFamily = DigitalMonospaceFont,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (prop.stillNeeded > 0.001) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "+${formatDouble(prop.stillNeeded)} ${prop.unit} needed",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = DigitalMonospaceFont,
                                                color = Color(0xFFF59E0B),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "COMPLETED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = DigitalMonospaceFont,
                                                color = Color(0xFF10B981),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Step 5: Batch Division
                    if (calc.batchCount > 1 || calc.batchItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "BATCH DIVISION (${calc.batchCount} BATCHES · ${formatDouble(calc.totalWeightPerBatch)} ${recipe.unit}/BATCH):",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        calc.batchItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ${item.name}",
                                    fontSize = 10.5.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${formatDouble(item.perBatch)} ${item.unit} / batch",
                                    fontSize = 10.5.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle expand breakdown
                Row(
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Hide Details" else "View Details",
                        fontSize = 11.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = primaryColor
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(32.dp).testTag("copy_mixture_${recipe.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Formulation",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp).testTag("edit_mixture_${recipe.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Details",
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_mixture_${recipe.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Button(
                        onClick = onLoad,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp).testTag("load_mixture_${recipe.id}")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Load", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MixtureSaveDialog(
    initialTitle: String,
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, description: String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle.ifBlank { "Custom Formulation" }) }
    var category by remember { mutableStateOf("Structural") }
    var description by remember { mutableStateOf("") }
    val primaryColor = MaterialTheme.colorScheme.primary

    val predefinedCategories = listOf("Structural", "Chemical", "Culinary", "Industrial", "Custom")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Science, contentDescription = null, tint = primaryColor, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Mixture Formulation", fontWeight = FontWeight.Bold, fontFamily = DigitalMonospaceFont)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Save this recipe with target proportions, actual ratios, and batch scaling to history.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LiquidGlassTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Formulation Title") },
                    singleLine = true,
                    accentColor = primaryColor,
                    modifier = Modifier.fillMaxWidth().testTag("save_mixture_title_input")
                )

                LiquidGlassTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    accentColor = primaryColor,
                    modifier = Modifier.fillMaxWidth().testTag("save_mixture_category_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    predefinedCategories.take(3).forEach { cat ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (category == cat) primaryColor.copy(alpha = 0.2f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (category == cat) primaryColor else Color(0x30FFFFFF)),
                            modifier = Modifier
                                .clickable { category = cat }
                                .padding(2.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 10.sp,
                                fontFamily = DigitalMonospaceFont,
                                color = if (category == cat) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                LiquidGlassTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Description (Optional)") },
                    singleLine = false,
                    accentColor = primaryColor,
                    modifier = Modifier.fillMaxWidth().testTag("save_mixture_desc_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, category, description) },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                modifier = Modifier.testTag("confirm_save_mixture_btn")
            ) {
                Text("Save to History", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MixtureBackupRestoreDialog(
    onDismiss: () -> Unit,
    onExportJson: suspend () -> String,
    onRestoreJson: (String, RestoreMode) -> Unit,
    onSaveToUri: (android.net.Uri) -> Unit,
    onLoadFromUri: (android.net.Uri, RestoreMode) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val primaryColor = MaterialTheme.colorScheme.primary
    var activeTab by remember { mutableStateOf(0) } // 0: Export, 1: Import
    var restoreMode by remember { mutableStateOf(RestoreMode.MERGE) }
    var pastedJson by remember { mutableStateOf("") }
    var isExporting by remember { mutableStateOf(false) }

    val fileSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) onSaveToUri(uri)
    }

    val filePickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onLoadFromUri(uri, restoreMode)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (activeTab == 0) Icons.Default.Download else Icons.Default.Upload,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mixture Backup & Restore", fontWeight = FontWeight.Bold, fontFamily = DigitalMonospaceFont)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Tab switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x12FFFFFF), RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { activeTab = 0 },
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeTab == 0) primaryColor else Color.Transparent
                    ) {
                        Text(
                            text = "Export Backup",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = if (activeTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { activeTab = 1 },
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeTab == 1) primaryColor else Color.Transparent
                    ) {
                        Text(
                            text = "Restore / Import",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = if (activeTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                if (activeTab == 0) {
                    Text(
                        text = "Export your saved mixture formulations as a standalone JSON backup file.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                            fileSaveLauncher.launch("mixture_formulations_backup_$dateStr.json")
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Backup JSON File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isExporting = true
                                val json = onExportJson()
                                clipboardManager.setText(AnnotatedString(json))
                                isExporting = false
                                Toast.makeText(context, "Backup JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Backup JSON to Clipboard", fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = "Restore saved formulations from a backup file or paste JSON.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Restore mode selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { restoreMode = RestoreMode.MERGE }
                        ) {
                            RadioButton(
                                selected = restoreMode == RestoreMode.MERGE,
                                onClick = { restoreMode = RestoreMode.MERGE }
                            )
                            Text("Merge", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { restoreMode = RestoreMode.OVERWRITE }
                        ) {
                            RadioButton(
                                selected = restoreMode == RestoreMode.OVERWRITE,
                                onClick = { restoreMode = RestoreMode.OVERWRITE }
                            )
                            Text("Overwrite Existing", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = { filePickLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select Backup File (.json)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    LiquidGlassTextField(
                        value = pastedJson,
                        onValueChange = { pastedJson = it },
                        placeholder = { Text("Or paste backup JSON here...") },
                        singleLine = false,
                        accentColor = primaryColor,
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )

                    if (pastedJson.isNotBlank()) {
                        Button(
                            onClick = { onRestoreJson(pastedJson, restoreMode) },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore from Pasted JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun formatDouble(v: Double): String {
    return if (v % 1.0 == 0.0) String.format(Locale.US, "%,.0f", v) else String.format(Locale.US, "%,.2f", v)
}

data class HistoryMixtureCalculation(
    val simplifiedRatio: String,
    val anchorName: String,
    val anchorTargetQty: Double,
    val anchorActualAdded: Double,
    val scaleFactor: Double,
    val scaleExplanation: String,
    val proportions: List<HistoryProportionItem>,
    val totalNewWeight: Double,
    val batchCount: Int,
    val batchItems: List<HistoryBatchDivisionItem>,
    val totalWeightPerBatch: Double,
    val unit: String
)

data class HistoryProportionItem(
    val name: String,
    val unit: String,
    val targetQty: Double,
    val newRequiredTotal: Double,
    val alreadyAdded: Double,
    val stillNeeded: Double,
    val isAnchor: Boolean
)

data class HistoryBatchDivisionItem(
    val name: String,
    val perBatch: Double,
    val unit: String,
    val totalAmount: Double
)

private fun getOrComputeMixtureCalculation(recipe: MixtureRecipeEntity): HistoryMixtureCalculation {
    val propList = mutableListOf<HistoryProportionItem>()
    val batchList = mutableListOf<HistoryBatchDivisionItem>()

    var ratioStr = recipe.simplifiedRatioString
    var anchorTarget = recipe.anchorTargetQuantity
    var anchorActual = recipe.anchorActualAdded
    var explanation = recipe.scaleExplanation
    var totalNewWeight = recipe.totalNewMixtureWeight
    var totalWeightPerBatch = recipe.totalWeightPerBatch
    val unit = recipe.unit

    try {
        if (recipe.recalibratedProportionsJson.isNotBlank() && recipe.recalibratedProportionsJson != "[]") {
            val array = JSONArray(recipe.recalibratedProportionsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                propList.add(
                    HistoryProportionItem(
                        name = obj.optString("ingredientName", "Ingredient ${i + 1}"),
                        unit = obj.optString("unit", unit),
                        targetQty = obj.optDouble("targetQty", 0.0),
                        newRequiredTotal = obj.optDouble("newRequiredTotal", 0.0),
                        alreadyAdded = obj.optDouble("alreadyAdded", 0.0),
                        stillNeeded = obj.optDouble("stillNeeded", 0.0),
                        isAnchor = obj.optBoolean("isAnchor", false)
                    )
                )
            }
        }

        if (recipe.batchDivisionJson.isNotBlank() && recipe.batchDivisionJson != "[]") {
            val array = JSONArray(recipe.batchDivisionJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                batchList.add(
                    HistoryBatchDivisionItem(
                        name = obj.optString("ingredientName", "Ingredient ${i + 1}"),
                        perBatch = obj.optDouble("amountPerBatch", 0.0),
                        unit = obj.optString("unit", unit),
                        totalAmount = obj.optDouble("totalAmount", 0.0)
                    )
                )
            }
        }
    } catch (e: Exception) {
        // fallback
    }

    if (propList.isEmpty()) {
        val parsed = parseIngredientsJson(recipe.ingredientsJson)
        val targetIngredients = parsed.mapIndexed { idx, (name, qtyStr, u) ->
            TargetIngredient(
                id = "ing_hist_$idx",
                name = name,
                targetQuantity = qtyStr.replace(",", "").toDoubleOrNull() ?: 100.0,
                unit = u
            )
        }

        val ratioResult = MixtureCalculatorEngine.calculateSimplifiedRatio(targetIngredients)
        ratioStr = ratioResult.simplifiedRatioString

        val anchorId = targetIngredients.find { it.name.contains(recipe.anchorIngredientName, ignoreCase = true) }?.id
            ?: targetIngredients.firstOrNull()?.id ?: ""

        val recalibResult = MixtureCalculatorEngine.recalibrate(targetIngredients, anchorId, emptyMap())
        explanation = recalibResult.scaleExplanation
        totalNewWeight = recalibResult.totalNewMixtureWeight
        anchorTarget = recalibResult.anchorTargetQty
        anchorActual = recalibResult.anchorActualAdded

        for (p in recalibResult.proportions) {
            propList.add(
                HistoryProportionItem(
                    name = p.ingredientName,
                    unit = p.unit,
                    targetQty = p.targetQty,
                    newRequiredTotal = p.newRequiredTotal,
                    alreadyAdded = p.alreadyAdded,
                    stillNeeded = p.stillNeeded,
                    isAnchor = p.isAnchor
                )
            )
        }

        val batchCountVal = if (recipe.batchCount <= 0) 1 else recipe.batchCount
        val batchResult = MixtureCalculatorEngine.divideBatches(recalibResult, batchCountVal)
        totalWeightPerBatch = batchResult.totalWeightPerBatch

        for (b in batchResult.items) {
            batchList.add(
                HistoryBatchDivisionItem(
                    name = b.ingredientName,
                    perBatch = b.amountPerBatch,
                    unit = b.unit,
                    totalAmount = b.totalAmount
                )
            )
        }
    }

    return HistoryMixtureCalculation(
        simplifiedRatio = ratioStr.ifBlank { "Calculated Formulation" },
        anchorName = recipe.anchorIngredientName.ifBlank { propList.firstOrNull { it.isAnchor }?.name ?: "Primary" },
        anchorTargetQty = if (anchorTarget > 0.0) anchorTarget else propList.firstOrNull { it.isAnchor }?.targetQty ?: 0.0,
        anchorActualAdded = if (anchorActual > 0.0) anchorActual else propList.firstOrNull { it.isAnchor }?.alreadyAdded ?: 0.0,
        scaleFactor = recipe.scaleFactor,
        scaleExplanation = explanation,
        proportions = propList,
        totalNewWeight = if (totalNewWeight > 0.0) totalNewWeight else recipe.totalTargetWeight,
        batchCount = if (recipe.batchCount <= 0) 1 else recipe.batchCount,
        batchItems = batchList,
        totalWeightPerBatch = if (totalWeightPerBatch > 0.0) totalWeightPerBatch else (totalNewWeight / (if (recipe.batchCount <= 0) 1 else recipe.batchCount)),
        unit = unit
    )
}

private fun formatRecipeSummary(recipe: MixtureRecipeEntity): String {
    val calc = getOrComputeMixtureCalculation(recipe)
    return buildString {
        appendLine("==========================================")
        appendLine("MIXTURE FORMULATION COMPLETE CALCULATION")
        appendLine("Title: ${recipe.title} (${recipe.category})")
        appendLine("==========================================")
        appendLine("STEP 1 & 2: TARGET RECIPE & SIMPLIFIED RATIO")
        appendLine("Simplified Ratio: ${calc.simplifiedRatio}")
        appendLine("Target Total Weight: ${formatDouble(recipe.totalTargetWeight)} ${recipe.unit}")
        appendLine("\nSTEP 3 & 4: CALIBRATION & RECALIBRATION")
        appendLine("Anchor Ingredient: ${calc.anchorName}")
        appendLine("Target vs Actual Added: ${formatDouble(calc.anchorTargetQty)} ${recipe.unit} -> ${formatDouble(calc.anchorActualAdded)} ${recipe.unit}")
        appendLine("Scale Factor: ${String.format(Locale.US, "%.4fx", calc.scaleFactor)}")
        if (calc.scaleExplanation.isNotBlank()) {
            appendLine("Explanation: ${calc.scaleExplanation}")
        }
        appendLine("Recalibrated Total Mixture Weight: ${formatDouble(calc.totalNewWeight)} ${recipe.unit}")

        appendLine("\n--- RECALIBRATED PROPORTIONS & NEEDED AMOUNTS ---")
        calc.proportions.forEach { prop ->
            val anchorTag = if (prop.isAnchor) " [ANCHOR]" else ""
            appendLine("• ${prop.name}$anchorTag:")
            appendLine("    Target:        ${formatDouble(prop.targetQty)} ${prop.unit}")
            appendLine("    New Required:  ${formatDouble(prop.newRequiredTotal)} ${prop.unit}")
            appendLine("    Already Added: ${formatDouble(prop.alreadyAdded)} ${prop.unit}")
            appendLine("    Still Needed:  ${formatDouble(prop.stillNeeded)} ${prop.unit}")
        }

        appendLine("\nSTEP 5: BATCH DIVISION")
        appendLine("Batch Count: ${calc.batchCount} Batches")
        appendLine("Weight Per Batch: ${formatDouble(calc.totalWeightPerBatch)} ${recipe.unit}")
        calc.batchItems.forEach { item ->
            appendLine("  • ${item.name}: ${formatDouble(item.perBatch)} ${item.unit} / batch (Total: ${formatDouble(item.totalAmount)} ${item.unit})")
        }

        if (recipe.description.isNotBlank()) {
            appendLine("\nNotes: ${recipe.description}")
        }
    }
}

private fun parseIngredientsJson(json: String): List<Triple<String, String, String>> {
    val list = mutableListOf<Triple<String, String, String>>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val name = obj.optString("name", "Component")
            val qty = formatDouble(obj.optDouble("quantity", 0.0))
            val unit = obj.optString("unit", "kg")
            list.add(Triple(name, qty, unit))
        }
    } catch (e: Exception) {
        // fallback
    }
    return list
}
