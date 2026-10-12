package com.example.ui.screens

import com.example.ui.components.DottedFilterChip
import com.example.ui.components.SoftAlertDialog
import com.example.ui.components.SoftButton
import com.example.ui.components.SoftIconButton
import com.example.ui.components.softWell
import com.example.ui.components.SoftSurface
import com.example.ui.components.SoftCard
import com.example.ui.theme.SoftColors
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.MixtureRecipeEntity
import com.example.ui.MixtureViewModel
import com.example.ui.components.LiquidGlassTextField
import com.example.ui.theme.DigitalMonospaceFont
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MixtureHistoryScreen(
    viewModel: MixtureViewModel,
    onNavigateBack: () -> Unit,
    onLoadRecipe: (MixtureRecipeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    val recipes by viewModel.persistentRecipes.collectAsState()
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceBg = MaterialTheme.colorScheme.surface
    val cardBorderColor = MaterialTheme.colorScheme.outline

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    var itemToEdit by remember { mutableStateOf<MixtureRecipeEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<MixtureRecipeEntity?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    val filteredRecipes = remember(recipes, searchQuery, selectedCategory, showOnlyFavorites) {
        recipes.filter { recipe ->
            val matchesSearch = searchQuery.isBlank() ||
                recipe.title.contains(searchQuery, ignoreCase = true) ||
                recipe.category.contains(searchQuery, ignoreCase = true) ||
                recipe.description.contains(searchQuery, ignoreCase = true) ||
                recipe.anchorIngredientName.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "ALL" || recipe.category.equals(selectedCategory, ignoreCase = true)
            val matchesFav = !showOnlyFavorites || recipe.isFavorite
            matchesSearch && matchesCategory && matchesFav
        }
    }

    val categories = remember(recipes) {
        listOf("ALL") + recipes.map { it.category }.filter { it.isNotBlank() }.distinct()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("mixture_history_screen")
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SoftIconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("mixture_history_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Mixture Calculator",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Mixture History",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${filteredRecipes.size} of ${recipes.size} formulations",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (recipes.isNotEmpty()) {
                SoftButton(
                    onClick = { showClearAllDialog = true },
                    modifier = Modifier.testTag("mixture_history_clear_all_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Clear All",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Search Bar
        LiquidGlassTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search formulations, categories, anchors...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    SoftIconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            accentColor = primaryColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .testTag("mixture_history_search_input")
        )

        // Filter Chips (Categories & Favorites)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DottedFilterChip(
                selected = showOnlyFavorites,
                onClick = { showOnlyFavorites = !showOnlyFavorites },
                label = "Starred",
                leadingIcon = if (showOnlyFavorites) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                accentColor = SoftColors.warning
            )

            categories.forEach { cat ->
                val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                DottedFilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = cat,
                    accentColor = primaryColor
                )
            }
        }

        // List of Formulations
        if (filteredRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .softWell(CircleShape, tint = primaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (recipes.isEmpty()) "No saved formulations yet" else "No matching formulations found",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (recipes.isEmpty()) {
                            "Calibrate a recipe in the Mixture Calculator and tap 'Save Formulation' at the end to save it."
                        } else {
                            "Try adjusting your search query or filter chips."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredRecipes, key = { it.id }) { recipe ->
                    MixtureHistoryCard(
                        recipe = recipe,
                        onLoad = { onLoadRecipe(recipe) },
                        onEdit = { itemToEdit = recipe },
                        onDelete = { itemToDelete = recipe },
                        onToggleFavorite = { viewModel.toggleFavorite(recipe) },
                        surfaceBg = surfaceBg,
                        borderColor = cardBorderColor,
                        primaryColor = primaryColor,
                        isDark = isDark
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Edit Dialog
    if (itemToEdit != null) {
        val target = itemToEdit!!
        var editTitle by remember { mutableStateOf(target.title) }
        var editCategory by remember { mutableStateOf(target.category) }
        var editDescription by remember { mutableStateOf(target.description) }

        SoftAlertDialog(
            onDismissRequest = { itemToEdit = null },
            title = {
                Text("Edit Formulation", fontWeight = FontWeight.Bold, fontFamily = DigitalMonospaceFont)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LiquidGlassTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        accentColor = primaryColor,
                        modifier = Modifier.fillMaxWidth().testTag("edit_mixture_title_input")
                    )
                    LiquidGlassTextField(
                        value = editCategory,
                        onValueChange = { editCategory = it },
                        label = { Text("Category") },
                        singleLine = true,
                        accentColor = primaryColor,
                        modifier = Modifier.fillMaxWidth().testTag("edit_mixture_category_input")
                    )
                    LiquidGlassTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Description / Notes") },
                        singleLine = false,
                        accentColor = primaryColor,
                        modifier = Modifier.fillMaxWidth().testTag("edit_mixture_notes_input")
                    )
                }
            },
            confirmButton = {
                SoftButton(
                    onClick = {
                        viewModel.updateRecipeInDb(target, editTitle, editCategory, editDescription)
                        itemToEdit = null
                    }, accentColor = primaryColor, filled = true) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                SoftButton(onClick = { itemToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Single Confirmation Dialog
    if (itemToDelete != null) {
        val target = itemToDelete!!
        SoftAlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Formulation?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${target.title}' from your history?") },
            confirmButton = {
                SoftButton(
                    onClick = {
                        viewModel.deleteRecipeFromDb(target)
                        itemToDelete = null
                    }, accentColor = MaterialTheme.colorScheme.error, filled = true) {
                    Text("Delete")
                }
            },
            dismissButton = {
                SoftButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Confirmation Dialog
    if (showClearAllDialog) {
        SoftAlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear All Mixture History?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all saved mixture formulations from the database. Note: You can also restore backups from the Settings screen.") },
            confirmButton = {
                SoftButton(
                    onClick = {
                        viewModel.clearAllRecipes()
                        showClearAllDialog = false
                    }, accentColor = MaterialTheme.colorScheme.error, filled = true) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                SoftButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
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
    surfaceBg: Color,
    borderColor: Color,
    primaryColor: Color,
    isDark: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()) }
    val formattedDate = remember(recipe.timestamp) { dateFormatter.format(Date(recipe.timestamp)) }

    val ingredientsList = remember(recipe.ingredientsJson) {
        val list = mutableListOf<Triple<String, Double, String>>()
        try {
            val arr = JSONArray(recipe.ingredientsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Triple(
                        obj.optString("name", "Component"),
                        obj.optDouble("quantity", 0.0),
                        obj.optString("unit", recipe.unit)
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore
        }
        list
    }

    SoftCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mixture_history_card_${recipe.id}"),
        shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Top Row: Title, Favorite & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = recipe.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        SoftSurface(
                            shape = RoundedCornerShape(6.dp),
                            color = primaryColor.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = recipe.category.ifBlank { "Recipe" },
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SoftIconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (recipe.isFavorite) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (recipe.isFavorite) "Starred" else "Star",
                            tint = if (recipe.isFavorite) SoftColors.warning else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    SoftIconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    SoftIconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (recipe.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Key Specs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .softWell(RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "ANCHOR", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = DigitalMonospaceFont)
                    Text(text = recipe.anchorIngredientName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "BATCHES", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = DigitalMonospaceFont)
                    Text(text = "${recipe.batchCount}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "TOTAL WEIGHT", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = DigitalMonospaceFont)
                    Text(
                        text = "${String.format(Locale.US, "%.1f", recipe.totalTargetWeight)} ${recipe.unit}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftColors.positive
                    )
                }
            }

            // Expandable Ingredients Breakdown
            if (ingredientsList.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${ingredientsList.size} Ingredients Breakdown",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ingredientsList.forEach { (name, qty, unit) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "• $name", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", qty)} $unit",
                                    fontSize = 11.5.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action: Load into Calculator
            SoftButton(
                onClick = onLoad,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("load_mixture_recipe_${recipe.id}"),
                shape = RoundedCornerShape(10.dp), accentColor = primaryColor, filled = true) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Load into Mixture Calculator", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
