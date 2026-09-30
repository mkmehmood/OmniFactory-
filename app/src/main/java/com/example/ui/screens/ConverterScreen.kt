package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.supportedCurrencies
import com.example.domain.converter.ConversionUnit
import com.example.domain.converter.ConverterCategory
import com.example.domain.converter.UnitRegistry
import com.example.ui.BmiUnitSystem
import com.example.ui.ConverterUiState
import com.example.ui.ConverterViewModel
import com.example.ui.components.BlinkingCursorPointer
import com.example.ui.components.CurrencySearchBottomSheet
import com.example.ui.components.straightDottedLine
import com.example.ui.components.SmartStationKeypad
import com.example.ui.theme.AnimationIntensity
import com.example.ui.theme.ComponentSizeStyle
import com.example.ui.theme.ShapeStructureStyle
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.toCardShape
import com.example.ui.theme.toInputShape

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConverterScreen(
    viewModel: ConverterViewModel,
    uiState: ConverterUiState,
    shapeStructureStyle: ShapeStructureStyle,
    componentSizeStyle: ComponentSizeStyle,
    animationIntensity: AnimationIntensity,
    onCopyToClipboard: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary

    // Smart Keypad Focus State
    var focusedFieldId by remember { mutableStateOf<String?>(null) }
    var focusedFieldLabel by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible) {
        if (isImeVisible) {
            focusedFieldId = null
            focusedFieldLabel = null
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusedFieldId = null
            focusedFieldLabel = null
        }
    }

    val handleFieldClicked: (String, String) -> Unit = { fieldId, label ->
        focusManager.clearFocus()
        keyboardController?.hide()
        focusedFieldId = fieldId
        focusedFieldLabel = label
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("converter_screen")
    ) {
        AnimatedContent(
            targetState = uiState.isCategoryGridActive,
            transitionSpec = {
                fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                        fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
            },
            label = "converter_view_toggle",
            modifier = Modifier.weight(1f)
        ) { inGridView ->
            if (inGridView) {
                // Categories Grid Hub Screen (Clean & Professional)
                ConverterCategoriesHub(
                    activeCategory = uiState.activeCategory,
                    onSelectCategory = { category ->
                        viewModel.selectCategoryAndOpenDedicatedScreen(category)
                        // Reset focus on category switch
                        focusedFieldId = null
                        focusedFieldLabel = null
                    },
                    shapeStructureStyle = shapeStructureStyle,
                    isDark = isDark,
                    primaryColor = primaryColor
                )
            } else {
                // Dedicated Screen for Selected Category
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        ConverterActiveHeader(
                            category = uiState.activeCategory,
                            shapeStructureStyle = shapeStructureStyle,
                            onBackToGrid = {
                                focusedFieldId = null
                                focusedFieldLabel = null
                                viewModel.showCategoryGrid()
                            },
                            isDark = isDark
                        )
                    }

                    when (uiState.activeCategory) {
                        ConverterCategory.CURRENCY -> {
                            item {
                                CurrencyConverterModule(
                                    viewModel = viewModel,
                                    uiState = uiState,
                                    focusedFieldId = focusedFieldId,
                                    onFieldClicked = handleFieldClicked,
                                    shapeStructureStyle = shapeStructureStyle,
                                    onCopyToClipboard = onCopyToClipboard,
                                    isDark = isDark,
                                    primaryColor = primaryColor
                                )
                            }
                        }
                        ConverterCategory.BMI -> {
                            item {
                                BmiConverterModule(
                                    viewModel = viewModel,
                                    uiState = uiState,
                                    focusedFieldId = focusedFieldId,
                                    onFieldClicked = handleFieldClicked,
                                    shapeStructureStyle = shapeStructureStyle,
                                    onCopyToClipboard = onCopyToClipboard,
                                    isDark = isDark,
                                    primaryColor = primaryColor
                                )
                            }
                        }
                        ConverterCategory.TEMPERATURE -> {
                            item {
                                TemperatureConverterModule(
                                    viewModel = viewModel,
                                    uiState = uiState,
                                    focusedFieldId = focusedFieldId,
                                    onFieldClicked = handleFieldClicked,
                                    shapeStructureStyle = shapeStructureStyle,
                                    onCopyToClipboard = onCopyToClipboard,
                                    isDark = isDark,
                                    primaryColor = primaryColor
                                )
                            }
                        }
                        ConverterCategory.NUMBER_SYSTEM -> {
                            item {
                                NumberSystemConverterModule(
                                    viewModel = viewModel,
                                    uiState = uiState,
                                    focusedFieldId = focusedFieldId,
                                    onFieldClicked = handleFieldClicked,
                                    shapeStructureStyle = shapeStructureStyle,
                                    onCopyToClipboard = onCopyToClipboard,
                                    isDark = isDark,
                                    primaryColor = primaryColor
                                )
                            }
                        }
                        else -> {
                            // Standard Units (Length, Area, Volume, Weight, Speed, Pressure, Power)
                            item {
                                StandardUnitsConverterModule(
                                    viewModel = viewModel,
                                    uiState = uiState,
                                    focusedFieldId = focusedFieldId,
                                    onFieldClicked = handleFieldClicked,
                                    shapeStructureStyle = shapeStructureStyle,
                                    onCopyToClipboard = onCopyToClipboard,
                                    isDark = isDark,
                                    primaryColor = primaryColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // SMART UNIFIED KEYPAD (Shows only when user clicks an input field for data entry)
        AnimatedVisibility(
            visible = !uiState.isCategoryGridActive && focusedFieldId != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            val currentField = focusedFieldId ?: ""
            val isHexField = currentField == "num_hex"
            val isBinaryField = currentField == "num_binary"
            val isOctalField = currentField == "num_octal"
            val showNegate = uiState.activeCategory == ConverterCategory.TEMPERATURE ||
                    uiState.activeCategory in listOf(
                        ConverterCategory.PRESSURE,
                        ConverterCategory.POWER
                    )

            SmartStationKeypad(
                activeFieldLabel = focusedFieldLabel,
                onDigit = { digit -> viewModel.appendTokenToField(currentField, digit) },
                onDoubleZero = { viewModel.appendTokenToField(currentField, "00") },
                onDecimal = { viewModel.appendTokenToField(currentField, ".") },
                onBackspace = { viewModel.backspaceField(currentField) },
                onClear = { viewModel.clearField(currentField) },
                onNegate = { viewModel.negateField(currentField) },
                onDone = {
                    focusedFieldId = null
                    focusedFieldLabel = null
                },
                showDoubleZeroKey = !isBinaryField,
                showNegateKey = showNegate,
                shapeStructureStyle = shapeStructureStyle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// DIRECTORY GRID HUB (CLEAN, MINIMAL & MODERN CATEGORIES HUB)
// -------------------------------------------------------------------------------------------------
@Composable
fun ConverterCategoriesHub(
    activeCategory: ConverterCategory,
    onSelectCategory: (ConverterCategory) -> Unit,
    shapeStructureStyle: ShapeStructureStyle,
    isDark: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val cardShape = shapeStructureStyle.toCardShape()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("converter_hub_grid")
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(ConverterCategory.values()) { category ->
                val isSelected = activeCategory == category
                val cardBg = if (isSelected) {
                    primaryColor.copy(alpha = 0.16f)
                } else {
                    if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                }
                val borderClr = if (isSelected) {
                    primaryColor.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .border(1.dp, borderClr, cardShape)
                        .clickable { onSelectCategory(category) }
                        .testTag("grid_item_${category.name}"),
                    shape = cardShape,
                    color = cardBg,
                    shadowElevation = if (isDark) 0.dp else 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Category Icon
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) primaryColor.copy(alpha = 0.25f) else primaryColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.shortName,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Name (Clean, high-legibility)
                        Text(
                            text = category.shortName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Quick Hub summary item
            item {
                val infoColor = primaryColor
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f),
                            cardShape
                        )
                        .clickable {
                            onSelectCategory(ConverterCategory.CURRENCY)
                        }
                        .testTag("grid_item_all_suites"),
                    shape = cardShape,
                    color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                    shadowElevation = if (isDark) 0.dp else 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(infoColor.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = null,
                                tint = infoColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "All Units",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = infoColor
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// ACTIVE CATEGORY HEADER
// -------------------------------------------------------------------------------------------------
@Composable
fun ConverterActiveHeader(
    category: ConverterCategory,
    shapeStructureStyle: ShapeStructureStyle,
    onBackToGrid: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val cardShape = shapeStructureStyle.toCardShape()
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.20f), cardShape)
            .testTag("converter_active_header"),
        shape = cardShape,
        color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(primaryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.title,
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = category.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = category.description,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Back to Categories Hub Button (Icon only - categories text removed)
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onBackToGrid() }
                    .testTag("back_to_grid_button"),
                shape = RoundedCornerShape(10.dp),
                color = primaryColor.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.45f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "All Categories",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 1. LIVE CURRENCY EXCHANGE RATE MODULE (SEARCH & CLICK MODAL PICKER)
// -------------------------------------------------------------------------------------------------
@Composable
private fun CurrencyConverterModule(
    viewModel: ConverterViewModel,
    uiState: ConverterUiState,
    focusedFieldId: String?,
    onFieldClicked: (String, String) -> Unit,
    shapeStructureStyle: ShapeStructureStyle,
    onCopyToClipboard: (String, String) -> Unit,
    isDark: Boolean,
    primaryColor: Color
) {
    val cardShape = shapeStructureStyle.toCardShape()
    val inputShape = shapeStructureStyle.toInputShape()
    val ratesState = uiState.currencyRates

    val fromCurrency = supportedCurrencies.find { it.code == uiState.currencyBase } ?: supportedCurrencies[0]
    val toCurrency = supportedCurrencies.find { it.code == uiState.currencyTarget } ?: supportedCurrencies[1]
    val convertedAmount = viewModel.getCurrencyConvertedAmount()
    val singleRate = viewModel.getCurrencySingleRate()

    // Search and Click Modal State
    var showFromSearchSheet by remember { mutableStateOf(false) }
    var showToSearchSheet by remember { mutableStateOf(false) }

    val isAmountFocused = focusedFieldId == "currency_amount"

    // Searchable Currency Bottom Sheets
    if (showFromSearchSheet) {
        CurrencySearchBottomSheet(
            title = "Select Source Currency",
            selectedCurrencyCode = uiState.currencyBase,
            onSelectCurrency = { selected ->
                viewModel.setCurrencyBase(selected.code)
                showFromSearchSheet = false
            },
            onDismissRequest = { showFromSearchSheet = false }
        )
    }

    if (showToSearchSheet) {
        CurrencySearchBottomSheet(
            title = "Select Target Currency",
            selectedCurrencyCode = uiState.currencyTarget,
            onSelectCurrency = { selected ->
                viewModel.setCurrencyTarget(selected.code)
                showToSearchSheet = false
            },
            onDismissRequest = { showToSearchSheet = false }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Status & Refresh Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f), cardShape),
            shape = cardShape,
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (ratesState.isLiveOnline) Color(0xFF10B981) else Color(0xFFF59E0B))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (ratesState.isLiveOnline) "Live Rates (${ratesState.lastUpdated.take(16)})" else "Cached Rates",
                        fontSize = 11.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.fetchLiveCurrencyRates() },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("refresh_rates_button")
                ) {
                    if (ratesState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Currency Rates",
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // FROM CURRENCY CARD
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(
                    1.dp,
                    if (isAmountFocused) primaryColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f),
                    cardShape
                ),
            shape = cardShape,
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "FROM (SOURCE)",
                    fontSize = 11.sp,
                    fontFamily = DigitalMonospaceFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Search & Click Picker Button
                    Surface(
                        modifier = Modifier
                            .clip(inputShape)
                            .border(1.dp, primaryColor.copy(alpha = 0.5f), inputShape)
                            .clickable { showFromSearchSheet = true }
                            .testTag("currency_from_picker"),
                        shape = inputShape,
                        color = primaryColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(primaryColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = fromCurrency.code,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = fromCurrency.symbol,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Currency",
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Smart Data-Entry Amount Input Box with Straight Dotted Line
                    val currencyDotColor = if (isAmountFocused) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.50f else 0.35f)
                    Surface(
                        modifier = Modifier
                            .width(170.dp)
                            .straightDottedLine(
                                color = currencyDotColor,
                                strokeWidth = if (isAmountFocused) 2.dp else 1.3.dp,
                                dotLength = 4.dp,
                                spaceLength = 3.dp
                            )
                            .clickable {
                                onFieldClicked("currency_amount", "Amount in ${fromCurrency.code}")
                            }
                            .testTag("currency_amount_input"),
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                        color = if (isAmountFocused) primaryColor.copy(alpha = 0.08f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (isAmountFocused) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editing",
                                    tint = primaryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = uiState.currencyAmount,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = if (isAmountFocused) primaryColor else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                // Quick Amount Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("10", "50", "100", "500", "1,000").forEach { preset ->
                        val clean = preset.replace(",", "")
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f), RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setCurrencyAmount(clean)
                                    onFieldClicked("currency_amount", "Amount in ${fromCurrency.code}")
                                },
                            color = if (uiState.currencyAmount == clean) primaryColor.copy(alpha = 0.2f) else Color.Transparent
                        ) {
                            Text(
                                text = preset,
                                fontSize = 11.sp,
                                fontFamily = DigitalMonospaceFont,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = if (uiState.currencyAmount == clean) primaryColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // SWAP BUTTON
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.dp, primaryColor.copy(alpha = 0.5f), CircleShape)
                    .clickable { viewModel.swapCurrency() }
                    .testTag("currency_swap_button"),
                shape = CircleShape,
                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap Currency",
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // TO (TARGET) CURRENCY CARD
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.dp, primaryColor.copy(alpha = 0.35f), cardShape),
            shape = cardShape,
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TO (CONVERTED RESULT)",
                    fontSize = 11.sp,
                    fontFamily = DigitalMonospaceFont,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Search & Click Picker Button
                    Surface(
                        modifier = Modifier
                            .clip(inputShape)
                            .border(1.dp, primaryColor.copy(alpha = 0.5f), inputShape)
                            .clickable { showToSearchSheet = true }
                            .testTag("currency_to_picker"),
                        shape = inputShape,
                        color = primaryColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(primaryColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = toCurrency.code,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = toCurrency.symbol,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Currency",
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Result Display & Copy Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = convertedAmount,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = primaryColor,
                            modifier = Modifier.testTag("currency_result_text")
                        )
                        IconButton(
                            onClick = { onCopyToClipboard(convertedAmount, "Converted Currency") },
                            modifier = Modifier.testTag("copy_currency_result_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = if (isDark) Color(0x18FFFFFF) else Color(0x18000000))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "1 ${fromCurrency.code} = $singleRate ${toCurrency.code}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = toCurrency.name,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 2. BMI CONVERTER & HEALTH GAUGE MODULE
// -------------------------------------------------------------------------------------------------
@Composable
private fun BmiConverterModule(
    viewModel: ConverterViewModel,
    uiState: ConverterUiState,
    focusedFieldId: String?,
    onFieldClicked: (String, String) -> Unit,
    shapeStructureStyle: ShapeStructureStyle,
    onCopyToClipboard: (String, String) -> Unit,
    isDark: Boolean,
    primaryColor: Color
) {
    val cardShape = shapeStructureStyle.toCardShape()
    val inputShape = shapeStructureStyle.toInputShape()
    val bmiResult = viewModel.calculateBmi()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Metric / Imperial Toggle
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f), cardShape),
            shape = cardShape,
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ) {
            Row(modifier = Modifier.padding(6.dp)) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(inputShape)
                        .clickable { viewModel.setBmiUnitSystem(BmiUnitSystem.METRIC) }
                        .testTag("bmi_toggle_metric"),
                    shape = inputShape,
                    color = if (uiState.bmiUnitSystem == BmiUnitSystem.METRIC) primaryColor.copy(alpha = 0.2f) else Color.Transparent
                ) {
                    Text(
                        text = "Metric (kg, cm)",
                        textAlign = TextAlign.Center,
                        fontWeight = if (uiState.bmiUnitSystem == BmiUnitSystem.METRIC) FontWeight.Bold else FontWeight.Normal,
                        color = if (uiState.bmiUnitSystem == BmiUnitSystem.METRIC) primaryColor else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(inputShape)
                        .clickable { viewModel.setBmiUnitSystem(BmiUnitSystem.IMPERIAL) }
                        .testTag("bmi_toggle_imperial"),
                    shape = inputShape,
                    color = if (uiState.bmiUnitSystem == BmiUnitSystem.IMPERIAL) primaryColor.copy(alpha = 0.2f) else Color.Transparent
                ) {
                    Text(
                        text = "Imperial (lbs, ft & in)",
                        textAlign = TextAlign.Center,
                        fontWeight = if (uiState.bmiUnitSystem == BmiUnitSystem.IMPERIAL) FontWeight.Bold else FontWeight.Normal,
                        color = if (uiState.bmiUnitSystem == BmiUnitSystem.IMPERIAL) primaryColor else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }

        // Smart Input Fields
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f), cardShape),
            shape = cardShape,
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (uiState.bmiUnitSystem == BmiUnitSystem.METRIC) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isWeightFocused = focusedFieldId == "bmi_weight_metric"
                        val isHeightFocused = focusedFieldId == "bmi_height_metric"

                        // Weight Metric
                        val weightDotColor = if (isWeightFocused) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.50f else 0.35f)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .straightDottedLine(
                                    color = weightDotColor,
                                    strokeWidth = if (isWeightFocused) 2.dp else 1.3.dp,
                                    dotLength = 4.dp,
                                    spaceLength = 3.dp
                                )
                                .clickable { onFieldClicked("bmi_weight_metric", "Weight (kg)") }
                                .testTag("bmi_input_weight_metric"),
                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                            color = if (isWeightFocused) primaryColor.copy(alpha = 0.08f) else Color.Transparent
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text("Weight (kg)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = uiState.bmiWeightMetricKg,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = if (isWeightFocused) primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Height Metric
                        val heightDotColor = if (isHeightFocused) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.50f else 0.35f)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .straightDottedLine(
                                    color = heightDotColor,
                                    strokeWidth = if (isHeightFocused) 2.dp else 1.3.dp,
                                    dotLength = 4.dp,
                                    spaceLength = 3.dp
                                )
                                .clickable { onFieldClicked("bmi_height_metric", "Height (cm)") }
                                .testTag("bmi_input_height_metric"),
                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                            color = if (isHeightFocused) primaryColor.copy(alpha = 0.08f) else Color.Transparent
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text("Height (cm)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = uiState.bmiHeightMetricCm,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = if (isHeightFocused) primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                } else {
                    val isImpWeightFocused = focusedFieldId == "bmi_weight_imperial"
                    val isFeetFocused = focusedFieldId == "bmi_feet_imperial"
                    val isInchesFocused = focusedFieldId == "bmi_inches_imperial"

                    val impWeightDotColor = if (isImpWeightFocused) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.50f else 0.35f)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .straightDottedLine(
                                color = impWeightDotColor,
                                strokeWidth = if (isImpWeightFocused) 2.dp else 1.3.dp,
                                dotLength = 4.dp,
                                spaceLength = 3.dp
                            )
                            .clickable { onFieldClicked("bmi_weight_imperial", "Weight (lbs)") }
                            .testTag("bmi_input_weight_imperial"),
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                        color = if (isImpWeightFocused) primaryColor.copy(alpha = 0.08f) else Color.Transparent
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text("Weight (lbs)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = uiState.bmiWeightImperialLbs,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = if (isImpWeightFocused) primaryColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val feetDotColor = if (isFeetFocused) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.50f else 0.35f)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .straightDottedLine(
                                    color = feetDotColor,
                                    strokeWidth = if (isFeetFocused) 2.dp else 1.3.dp,
                                    dotLength = 4.dp,
                                    spaceLength = 3.dp
                                )
                                .clickable { onFieldClicked("bmi_feet_imperial", "Height (Feet)") }
                                .testTag("bmi_input_feet"),
                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                            color = if (isFeetFocused) primaryColor.copy(alpha = 0.08f) else Color.Transparent
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text("Height (Feet)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = uiState.bmiHeightImperialFeet,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = if (isFeetFocused) primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        val inchesDotColor = if (isInchesFocused) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.50f else 0.35f)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .straightDottedLine(
                                    color = inchesDotColor,
                                    strokeWidth = if (isInchesFocused) 2.dp else 1.3.dp,
                                    dotLength = 4.dp,
                                    spaceLength = 3.dp
                                )
                                .clickable { onFieldClicked("bmi_inches_imperial", "Inches (0-11)") }
                                .testTag("bmi_input_inches"),
                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                            color = if (isInchesFocused) primaryColor.copy(alpha = 0.08f) else Color.Transparent
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text("Inches (0-11)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = uiState.bmiHeightImperialInches,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = if (isInchesFocused) primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // BMI GAUGE & RESULT CARD
        val categoryColor = Color(bmiResult.category.colorHex)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.dp, categoryColor.copy(alpha = 0.5f), cardShape),
            shape = cardShape,
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CALCULATED BMI",
                            fontSize = 11.sp,
                            fontFamily = DigitalMonospaceFont,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = bmiResult.formattedBmi,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = categoryColor,
                                modifier = Modifier.testTag("bmi_result_value")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "kg/m²",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = categoryColor.copy(alpha = 0.16f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, categoryColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = bmiResult.category.label,
                            color = categoryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar representation
                val progressFraction = ((bmiResult.bmiValue - 10.0) / 35.0).coerceIn(0.0, 1.0).toFloat()
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = categoryColor,
                    trackColor = if (isDark) Color(0x22FFFFFF) else Color(0x18000000)
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = if (isDark) Color(0x18FFFFFF) else Color(0x18000000))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Ideal Healthy Weight",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = bmiResult.idealWeightRangeText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "BMI Prime",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = bmiResult.primeBmi,
                            fontSize = 13.sp,
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

// -------------------------------------------------------------------------------------------------
// 3. STANDARD UNITS CONVERTER MODULE (Length, Area, Volume, Weight, Speed, Pressure, Power)
// -------------------------------------------------------------------------------------------------
@Composable
private fun StandardUnitsConverterModule(
    viewModel: ConverterViewModel,
    uiState: ConverterUiState,
    focusedFieldId: String?,
    onFieldClicked: (String, String) -> Unit,
    shapeStructureStyle: ShapeStructureStyle,
    onCopyToClipboard: (String, String) -> Unit,
    isDark: Boolean,
    primaryColor: Color
) {
    val cardShape = shapeStructureStyle.toCardShape()
    val inputShape = shapeStructureStyle.toInputShape()
    val units = UnitRegistry.getUnitsForCategory(uiState.activeCategory)
    val fromUnit = units.find { it.id == uiState.standardFromUnitId } ?: units.firstOrNull() ?: ConversionUnit("","","",1.0)
    val toUnit = units.find { it.id == uiState.standardToUnitId } ?: units.getOrNull(1) ?: fromUnit
    val convertedResult = viewModel.getStandardConvertedResult()
    val allMatrix = viewModel.getStandardAllUnitsMatrix()

    var showFromMenu by remember { mutableStateOf(false) }
    var showToMenu by remember { mutableStateOf(false) }

    val isInputFocused = focusedFieldId == "standard_input"

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // FROM CARD
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(
                    1.dp,
                    if (isInputFocused) primaryColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f),
                    cardShape
                ),
            shape = cardShape,
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "FROM UNIT",
                    fontSize = 11.sp,
                    fontFamily = DigitalMonospaceFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Unit selector dropdown
                    Box {
                        Surface(
                            modifier = Modifier
                                .clip(inputShape)
                                .border(1.dp, primaryColor.copy(alpha = 0.4f), inputShape)
                                .clickable { showFromMenu = true }
                                .testTag("standard_from_picker"),
                            shape = inputShape,
                            color = primaryColor.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${fromUnit.name} (${fromUnit.symbol})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showFromMenu,
                            onDismissRequest = { showFromMenu = false }
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text("${unit.name} (${unit.symbol})") },
                                    onClick = {
                                        viewModel.setStandardFromUnit(unit.id)
                                        showFromMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Value input box (smart keypad trigger) with straight dotted line
                    val stdDotColor = if (isInputFocused) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.50f else 0.35f)
                    Surface(
                        modifier = Modifier
                            .width(160.dp)
                            .straightDottedLine(
                                color = stdDotColor,
                                strokeWidth = if (isInputFocused) 2.dp else 1.3.dp,
                                dotLength = 4.dp,
                                spaceLength = 3.dp
                            )
                            .clickable {
                                onFieldClicked("standard_input", "${fromUnit.name} (${fromUnit.symbol})")
                            }
                            .testTag("standard_value_input"),
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                        color = if (isInputFocused) primaryColor.copy(alpha = 0.08f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (isInputFocused) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editing",
                                    tint = primaryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = uiState.standardInput,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = if (isInputFocused) primaryColor else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // SWAP BUTTON
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.dp, primaryColor.copy(alpha = 0.5f), CircleShape)
                    .clickable { viewModel.swapStandardUnits() }
                    .testTag("standard_swap_button"),
                shape = CircleShape,
                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap Units",
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // TO CARD
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.dp, primaryColor.copy(alpha = 0.35f), cardShape),
            shape = cardShape,
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TO (CONVERTED VALUE)",
                    fontSize = 11.sp,
                    fontFamily = DigitalMonospaceFont,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box {
                        Surface(
                            modifier = Modifier
                                .clip(inputShape)
                                .border(1.dp, primaryColor.copy(alpha = 0.4f), inputShape)
                                .clickable { showToMenu = true }
                                .testTag("standard_to_picker"),
                            shape = inputShape,
                            color = primaryColor.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${toUnit.name} (${toUnit.symbol})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showToMenu,
                            onDismissRequest = { showToMenu = false }
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text("${unit.name} (${unit.symbol})") },
                                    onClick = {
                                        viewModel.setStandardToUnit(unit.id)
                                        showToMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = convertedResult,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = primaryColor,
                            modifier = Modifier.testTag("standard_result_text")
                        )
                        IconButton(
                            onClick = { onCopyToClipboard(convertedResult, "${uiState.activeCategory.shortName} Result") },
                            modifier = Modifier.testTag("copy_standard_result")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // ALL UNITS MATRIX OVERVIEW
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f), cardShape),
            shape = cardShape,
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ALL CONVERTED UNITS BREAKDOWN",
                    fontSize = 11.sp,
                    fontFamily = DigitalMonospaceFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                allMatrix.forEachIndexed { idx, (unit, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${unit.name} (${unit.symbol})",
                            fontSize = 13.sp,
                            color = if (unit.id == toUnit.id) primaryColor else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = value,
                            fontSize = 13.sp,
                            fontFamily = DigitalMonospaceFont,
                            fontWeight = if (unit.id == toUnit.id) FontWeight.Bold else FontWeight.Normal,
                            color = if (unit.id == toUnit.id) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (idx < allMatrix.size - 1) {
                        HorizontalDivider(color = if (isDark) Color(0x0CFFFFFF) else Color(0x0A000000))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 4. TEMPERATURE CONVERTER MODULE
// -------------------------------------------------------------------------------------------------
@Composable
private fun TemperatureConverterModule(
    viewModel: ConverterViewModel,
    uiState: ConverterUiState,
    focusedFieldId: String?,
    onFieldClicked: (String, String) -> Unit,
    shapeStructureStyle: ShapeStructureStyle,
    onCopyToClipboard: (String, String) -> Unit,
    isDark: Boolean,
    primaryColor: Color
) {
    val cardShape = shapeStructureStyle.toCardShape()
    val inputShape = shapeStructureStyle.toInputShape()
    val tempResult = viewModel.getTempResult()
    val formula = UnitRegistry.getTemperatureFormulaDescription(uiState.tempFromUnit, uiState.tempToUnit)
    val matrix = viewModel.getTempAllMatrix()

    var showFromMenu by remember { mutableStateOf(false) }
    var showToMenu by remember { mutableStateOf(false) }

    val isTempFocused = focusedFieldId == "temp_input"

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Temperature Input Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(
                    1.dp,
                    if (isTempFocused) primaryColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f),
                    cardShape
                ),
            shape = cardShape,
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "FROM TEMPERATURE",
                    fontSize = 11.sp,
                    fontFamily = DigitalMonospaceFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box {
                        Surface(
                            modifier = Modifier
                                .clip(inputShape)
                                .border(1.dp, primaryColor.copy(alpha = 0.4f), inputShape)
                                .clickable { showFromMenu = true }
                                .testTag("temp_from_picker"),
                            shape = inputShape,
                            color = primaryColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${uiState.tempFromUnit.displayName} (${uiState.tempFromUnit.symbol})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showFromMenu,
                            onDismissRequest = { showFromMenu = false }
                        ) {
                            UnitRegistry.TempUnit.values().forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text("${unit.displayName} (${unit.symbol})") },
                                    onClick = {
                                        viewModel.setTempFrom(unit)
                                        showFromMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Smart Input Field with straight dotted line
                    val tempDotColor = if (isTempFocused) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.50f else 0.35f)
                    Surface(
                        modifier = Modifier
                            .width(160.dp)
                            .straightDottedLine(
                                color = tempDotColor,
                                strokeWidth = if (isTempFocused) 2.dp else 1.3.dp,
                                dotLength = 4.dp,
                                spaceLength = 3.dp
                            )
                            .clickable {
                                onFieldClicked("temp_input", "Temperature (${uiState.tempFromUnit.symbol})")
                            }
                            .testTag("temp_input_field"),
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                        color = if (isTempFocused) primaryColor.copy(alpha = 0.08f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (isTempFocused) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editing",
                                    tint = primaryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = uiState.tempInput,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = if (isTempFocused) primaryColor else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // SWAP BUTTON
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.dp, primaryColor.copy(alpha = 0.5f), CircleShape)
                    .clickable { viewModel.swapTempUnits() }
                    .testTag("temp_swap_button"),
                shape = CircleShape,
                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap Temp",
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Temperature Converted Result
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.dp, primaryColor.copy(alpha = 0.35f), cardShape),
            shape = cardShape,
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CONVERTED TEMPERATURE",
                    fontSize = 11.sp,
                    fontFamily = DigitalMonospaceFont,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box {
                        Surface(
                            modifier = Modifier
                                .clip(inputShape)
                                .border(1.dp, primaryColor.copy(alpha = 0.4f), inputShape)
                                .clickable { showToMenu = true }
                                .testTag("temp_to_picker"),
                            shape = inputShape,
                            color = primaryColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${uiState.tempToUnit.displayName} (${uiState.tempToUnit.symbol})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showToMenu,
                            onDismissRequest = { showToMenu = false }
                        ) {
                            UnitRegistry.TempUnit.values().forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text("${unit.displayName} (${unit.symbol})") },
                                    onClick = {
                                        viewModel.setTempTo(unit)
                                        showToMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$tempResult ${uiState.tempToUnit.symbol}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = primaryColor,
                            modifier = Modifier.testTag("temp_result_text")
                        )
                        IconButton(
                            onClick = { onCopyToClipboard(tempResult, "Temperature Result") },
                            modifier = Modifier.testTag("copy_temp_result")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.25f else 0.15f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formula,
                    fontSize = 12.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // All Temperatures Matrix
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f), cardShape),
            shape = cardShape,
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ALL TEMPERATURE SCALES",
                    fontSize = 11.sp,
                    fontFamily = DigitalMonospaceFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                matrix.forEachIndexed { idx, (unit, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${unit.displayName} (${unit.symbol})",
                            fontSize = 13.sp,
                            color = if (unit == uiState.tempToUnit) primaryColor else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$value ${unit.symbol}",
                            fontSize = 13.sp,
                            fontFamily = DigitalMonospaceFont,
                            fontWeight = if (unit == uiState.tempToUnit) FontWeight.Bold else FontWeight.Normal,
                            color = if (unit == uiState.tempToUnit) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (idx < matrix.size - 1) {
                        HorizontalDivider(color = if (isDark) Color(0x0CFFFFFF) else Color(0x0A000000))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 5. NUMBER SYSTEM CONVERTER MODULE (Decimal, Binary, Hex, Octal)
// -------------------------------------------------------------------------------------------------
@Composable
private fun NumberSystemConverterModule(
    viewModel: ConverterViewModel,
    uiState: ConverterUiState,
    focusedFieldId: String?,
    onFieldClicked: (String, String) -> Unit,
    shapeStructureStyle: ShapeStructureStyle,
    onCopyToClipboard: (String, String) -> Unit,
    isDark: Boolean,
    primaryColor: Color
) {
    val cardShape = shapeStructureStyle.toCardShape()
    val inputShape = shapeStructureStyle.toInputShape()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Decimal Base 10
        SmartNumberBaseCard(
            label = "Decimal (Base 10)",
            prefix = "DEC",
            value = uiState.numDecimal,
            isFocused = focusedFieldId == "num_decimal",
            onCardClicked = { onFieldClicked("num_decimal", "Decimal (Base 10)") },
            onCopy = { onCopyToClipboard(uiState.numDecimal, "Decimal") },
            inputShape = inputShape,
            cardShape = cardShape,
            accentColor = primaryColor,
            isDark = isDark,
            testTag = "num_decimal_input"
        )

        // Binary Base 2
        SmartNumberBaseCard(
            label = "Binary (Base 2) [Nibbles 4-bit]",
            prefix = "BIN",
            value = uiState.numBinary,
            isFocused = focusedFieldId == "num_binary",
            onCardClicked = { onFieldClicked("num_binary", "Binary (Base 2)") },
            onCopy = { onCopyToClipboard(uiState.numBinary, "Binary") },
            inputShape = inputShape,
            cardShape = cardShape,
            accentColor = primaryColor,
            isDark = isDark,
            testTag = "num_binary_input"
        )

        // Hexadecimal Base 16
        SmartNumberBaseCard(
            label = "Hexadecimal (Base 16)",
            prefix = "0x",
            value = uiState.numHex,
            isFocused = focusedFieldId == "num_hex",
            onCardClicked = { onFieldClicked("num_hex", "Hexadecimal (Base 16)") },
            onCopy = { onCopyToClipboard(uiState.numHex, "Hexadecimal") },
            inputShape = inputShape,
            cardShape = cardShape,
            accentColor = primaryColor,
            isDark = isDark,
            testTag = "num_hex_input"
        )

        // Octal Base 8
        SmartNumberBaseCard(
            label = "Octal (Base 8)",
            prefix = "0o",
            value = uiState.numOctal,
            isFocused = focusedFieldId == "num_octal",
            onCardClicked = { onFieldClicked("num_octal", "Octal (Base 8)") },
            onCopy = { onCopyToClipboard(uiState.numOctal, "Octal") },
            inputShape = inputShape,
            cardShape = cardShape,
            accentColor = primaryColor,
            isDark = isDark,
            testTag = "num_octal_input"
        )

        // Quick Bit Breakdown & ASCII
        val decLong = uiState.numDecimal.toLongOrNull() ?: 0L
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f), cardShape),
            shape = cardShape,
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "BITWISE & ASCII DECODE",
                    fontSize = 11.sp,
                    fontFamily = DigitalMonospaceFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ASCII Char (32..126)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val asciiChar = if (decLong in 32..126) "'${decLong.toInt().toChar()}'" else "N/A (Control)"
                    Text(asciiChar, fontSize = 13.sp, fontFamily = DigitalMonospaceFont, fontWeight = FontWeight.Bold, color = primaryColor)
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = if (isDark) Color(0x0CFFFFFF) else Color(0x0A000000))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Int32 Value", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(decLong.toInt().toString(), fontSize = 13.sp, fontFamily = DigitalMonospaceFont, color = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = if (isDark) Color(0x0CFFFFFF) else Color(0x0A000000))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bit Length (Active Bits)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val bitLength = if (decLong == 0L) 1 else java.lang.Long.toBinaryString(decLong).length
                    Text("$bitLength bits", fontSize = 13.sp, fontFamily = DigitalMonospaceFont, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun SmartNumberBaseCard(
    label: String,
    prefix: String,
    value: String,
    isFocused: Boolean,
    onCardClicked: () -> Unit,
    onCopy: () -> Unit,
    inputShape: androidx.compose.ui.graphics.Shape,
    cardShape: androidx.compose.ui.graphics.Shape,
    accentColor: Color,
    isDark: Boolean,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(
                1.dp,
                if (isFocused) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f),
                cardShape
            )
            .clickable { onCardClicked() },
        shape = cardShape,
        color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontFamily = DigitalMonospaceFont,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy $label",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val numDotColor = if (isFocused) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.50f else 0.35f)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .straightDottedLine(
                        color = numDotColor,
                        strokeWidth = if (isFocused) 2.dp else 1.3.dp,
                        dotLength = 4.dp,
                        spaceLength = 3.dp
                    )
                    .testTag(testTag),
                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                color = if (isFocused) accentColor.copy(alpha = 0.08f) else Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = prefix,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = accentColor,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = value.ifEmpty { "0" },
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        fontSize = 16.sp,
                        color = if (isFocused) accentColor else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (isFocused) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Active Keypad",
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
