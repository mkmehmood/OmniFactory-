package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.currency.CurrencyInfo
import com.example.data.currency.supportedCurrencies
import com.example.domain.converter.ConversionUnit
import com.example.domain.converter.ConverterCategory
import com.example.domain.converter.UnitRegistry
import com.example.ui.BmiCategory
import com.example.ui.BmiUnitSystem
import com.example.ui.ConverterUiState
import com.example.ui.ConverterViewModel
import com.example.ui.theme.AnimationIntensity
import com.example.ui.theme.ComponentSizeStyle
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.ShapeStructureStyle
import com.example.ui.theme.toButtonShape
import com.example.ui.theme.toCardShape
import com.example.ui.theme.toInputShape
import com.example.ui.theme.toSmallShape

@OptIn(ExperimentalMaterial3Api::class)
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
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary

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
            label = "converter_view_toggle"
        ) { inGridView ->
            if (inGridView) {
                // Directory Grid Hub Screen (Clean & Professional, no horizontal top buttons)
                ConverterDirectoryGrid(
                    activeCategory = uiState.activeCategory,
                    onSelectCategory = { category ->
                        viewModel.selectCategoryAndOpenDedicatedScreen(category)
                    },
                    shapeStructureStyle = shapeStructureStyle,
                    isDark = isDark,
                    primaryColor = primaryColor
                )
            } else {
                // Dedicated Screen for Selected Category
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        ConverterActiveHeader(
                            category = uiState.activeCategory,
                            shapeStructureStyle = shapeStructureStyle,
                            onBackToGrid = { viewModel.showCategoryGrid() },
                            isDark = isDark
                        )
                    }

                    when (uiState.activeCategory) {
                        ConverterCategory.CURRENCY -> {
                            item {
                                CurrencyConverterModule(
                                    viewModel = viewModel,
                                    uiState = uiState,
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
    }
}

// -------------------------------------------------------------------------------------------------
// DIRECTORY GRID HUB (CLEAN, MINIMAL & PROFESSIONAL CATEGORIES GRID)
// -------------------------------------------------------------------------------------------------
@Composable
fun ConverterDirectoryGrid(
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
        // Grid of Categories (Simple & Professional, no unnecessary texts)
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
                    category.accentColor.copy(alpha = 0.20f)
                } else {
                    if (isDark) Color(0xFF131C2D) else Color(0xFFFFFFFF)
                }
                val borderClr = if (isSelected) {
                    category.accentColor.copy(alpha = 0.8f)
                } else {
                    if (isDark) Color(0x22FFFFFF) else Color(0x18000000)
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
                                .background(category.accentColor.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.shortName,
                                tint = category.accentColor,
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
                            color = if (isSelected) category.accentColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 12th card to complete the grid structure
            item {
                val infoColor = primaryColor
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .border(
                            1.dp,
                            if (isDark) Color(0x22FFFFFF) else Color(0x18000000),
                            cardShape
                        )
                        .clickable {
                            onSelectCategory(ConverterCategory.CURRENCY)
                        }
                        .testTag("grid_item_all_suites"),
                    shape = cardShape,
                    color = if (isDark) Color(0xFF131C2D) else Color(0xFFFFFFFF),
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
                                .background(infoColor.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
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
// CONVERTER DIALPAD (FEATURING "00" DOUBLE ZERO KEY)
// -------------------------------------------------------------------------------------------------
@Composable
fun ConverterDialpad(
    onDigit: (String) -> Unit,
    onDoubleZero: () -> Unit,
    onDecimal: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onNegate: (() -> Unit)? = null,
    shapeStructureStyle: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    isDark: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val cardShape = shapeStructureStyle.toCardShape()
    val buttonShape = shapeStructureStyle.toButtonShape()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), cardShape)
            .testTag("converter_dialpad"),
        shape = cardShape,
        color = if (isDark) Color(0xFF101726) else Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: 7, 8, 9, ⌫ (Backspace)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DialpadKey(text = "7", onClick = { onDigit("7") }, shape = buttonShape, isDark = isDark, modifier = Modifier.weight(1f))
                DialpadKey(text = "8", onClick = { onDigit("8") }, shape = buttonShape, isDark = isDark, modifier = Modifier.weight(1f))
                DialpadKey(text = "9", onClick = { onDigit("9") }, shape = buttonShape, isDark = isDark, modifier = Modifier.weight(1f))
                DialpadActionKey(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    onClick = onBackspace,
                    shape = buttonShape,
                    isDark = isDark,
                    tint = Color(0xFFEF4444),
                    testTag = "dialpad_backspace",
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: 4, 5, 6, C (Clear)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DialpadKey(text = "4", onClick = { onDigit("4") }, shape = buttonShape, isDark = isDark, modifier = Modifier.weight(1f))
                DialpadKey(text = "5", onClick = { onDigit("5") }, shape = buttonShape, isDark = isDark, modifier = Modifier.weight(1f))
                DialpadKey(text = "6", onClick = { onDigit("6") }, shape = buttonShape, isDark = isDark, modifier = Modifier.weight(1f))
                DialpadActionKey(
                    text = "C",
                    onClick = onClear,
                    shape = buttonShape,
                    isDark = isDark,
                    tint = Color(0xFFF59E0B),
                    testTag = "dialpad_clear",
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3: 1, 2, 3, ± (Negate / Sign)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DialpadKey(text = "1", onClick = { onDigit("1") }, shape = buttonShape, isDark = isDark, modifier = Modifier.weight(1f))
                DialpadKey(text = "2", onClick = { onDigit("2") }, shape = buttonShape, isDark = isDark, modifier = Modifier.weight(1f))
                DialpadKey(text = "3", onClick = { onDigit("3") }, shape = buttonShape, isDark = isDark, modifier = Modifier.weight(1f))
                DialpadActionKey(
                    text = "±",
                    onClick = { onNegate?.invoke() ?: onClear() },
                    shape = buttonShape,
                    isDark = isDark,
                    tint = primaryColor,
                    testTag = "dialpad_negate",
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 4: 0, 00 (Double Zero), ., AC
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DialpadKey(text = "0", onClick = { onDigit("0") }, shape = buttonShape, isDark = isDark, modifier = Modifier.weight(1f))
                // DOUBLE ZERO KEY
                DialpadKey(
                    text = "00",
                    onClick = onDoubleZero,
                    shape = buttonShape,
                    isDark = isDark,
                    isSpecial = true,
                    specialColor = primaryColor,
                    testTag = "key_00",
                    modifier = Modifier.weight(1f)
                )
                DialpadKey(text = ".", onClick = onDecimal, shape = buttonShape, isDark = isDark, modifier = Modifier.weight(1f))
                DialpadActionKey(
                    text = "AC",
                    onClick = onClear,
                    shape = buttonShape,
                    isDark = isDark,
                    tint = Color(0xFFF59E0B),
                    testTag = "dialpad_all_clear",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DialpadKey(
    text: String,
    onClick: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
    isDark: Boolean,
    isSpecial: Boolean = false,
    specialColor: Color = Color.Unspecified,
    testTag: String = "dialpad_key_$text",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .border(
                1.dp,
                if (isSpecial) specialColor.copy(alpha = 0.5f) else (if (isDark) Color(0x20FFFFFF) else Color(0x18000000)),
                shape
            )
            .clickable { onClick() }
            .testTag(testTag),
        shape = shape,
        color = if (isSpecial) specialColor.copy(alpha = 0.15f) else (if (isDark) Color(0xFF182234) else Color(0xFFFFFFFF)),
        shadowElevation = if (isDark) 0.dp else 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = if (text == "00") 16.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = if (isSpecial) specialColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DialpadActionKey(
    text: String? = null,
    icon: ImageVector? = null,
    contentDescription: String? = null,
    onClick: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
    isDark: Boolean,
    tint: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .border(1.dp, tint.copy(alpha = 0.35f), shape)
            .clickable { onClick() }
            .testTag(testTag),
        shape = shape,
        color = tint.copy(alpha = 0.12f),
        shadowElevation = if (isDark) 0.dp else 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            } else if (text != null) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = tint
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// ACTIVE CONVERTER HEADER (WITH CLEAN CATEGORIES BACK BUTTON)
// -------------------------------------------------------------------------------------------------
@Composable
private fun ConverterActiveHeader(
    category: ConverterCategory,
    shapeStructureStyle: ShapeStructureStyle,
    onBackToGrid: () -> Unit,
    isDark: Boolean
) {
    val shape = shapeStructureStyle.toCardShape()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(
                1.dp,
                category.accentColor.copy(alpha = 0.35f),
                shape
            ),
        shape = shape,
        color = category.accentColor.copy(alpha = 0.09f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(category.accentColor.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = category.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = category.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
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

            // Back to Categories Button
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBackToGrid() }
                    .testTag("back_to_categories_button"),
                shape = RoundedCornerShape(8.dp),
                color = category.accentColor.copy(alpha = 0.18f),
                border = androidx.compose.foundation.BorderStroke(1.dp, category.accentColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "All Categories",
                        tint = category.accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Categories",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = category.accentColor
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COUNTRY & CURRENCY SEARCH AND CLICK DIALOG
// -------------------------------------------------------------------------------------------------
@Composable
fun CountryCurrencySearchDialog(
    currentSelectedCode: String,
    onSelectCurrency: (CurrencyInfo) -> Unit,
    onDismiss: () -> Unit,
    shapeStructureStyle: ShapeStructureStyle,
    isDark: Boolean,
    primaryColor: Color
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("All") }

    val regions = listOf("All", "Americas", "Europe", "Asia", "Middle East", "Africa")

    val filteredList = remember(searchQuery, selectedRegion) {
        supportedCurrencies.filter { item ->
            val matchesRegion = selectedRegion == "All" || item.region.equals(selectedRegion, ignoreCase = true)
            val q = searchQuery.trim()
            val matchesSearch = q.isEmpty() ||
                    item.country.contains(q, ignoreCase = true) ||
                    item.name.contains(q, ignoreCase = true) ||
                    item.code.contains(q, ignoreCase = true) ||
                    item.symbol.contains(q, ignoreCase = true)
            matchesRegion && matchesSearch
        }
    }

    val cardShape = shapeStructureStyle.toCardShape()
    val inputShape = shapeStructureStyle.toInputShape()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(560.dp)
                .clip(cardShape)
                .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0x22000000), cardShape)
                .testTag("country_currency_search_dialog"),
            shape = cardShape,
            color = if (isDark) Color(0xFF131C2D) else Color(0xFFFFFFFF),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Select Country & Currency",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Search across 40+ countries & live exchange rates",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_currency_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search country, currency name, code...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = inputShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("currency_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x33000000)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Region Filter Chips (Horizontal Scroll)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    regions.forEach { region ->
                        val isSelected = selectedRegion == region
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) primaryColor else (if (isDark) Color(0x22FFFFFF) else Color(0x18000000)),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedRegion = region }
                                .testTag("region_chip_$region"),
                            color = if (isSelected) primaryColor.copy(alpha = 0.18f) else Color.Transparent
                        ) {
                            Text(
                                text = region,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = if (isDark) Color(0x18FFFFFF) else Color(0x18000000))
                Spacer(modifier = Modifier.height(6.dp))

                // Results Count
                Text(
                    text = "${filteredList.size} countries available",
                    fontSize = 11.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Searchable Country List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("currency_search_results_list"),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredList) { item ->
                        val isCurrent = item.code == currentSelectedCode
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    1.dp,
                                    if (isCurrent) primaryColor else (if (isDark) Color(0x14FFFFFF) else Color(0x10000000)),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onSelectCurrency(item)
                                    onDismiss()
                                }
                                .testTag("country_item_${item.code}"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) primaryColor.copy(alpha = 0.12f) else (if (isDark) Color(0xFF182234) else Color(0xFFF8FAFC))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = item.flagEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.country,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${item.name} (${item.symbol})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isDark) Color(0x33FFFFFF) else Color(0x18000000)
                                    ) {
                                        Text(
                                            text = item.code,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = DigitalMonospaceFont,
                                            fontSize = 12.sp,
                                            color = if (isCurrent) primaryColor else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = primaryColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 1. LIVE CURRENCY EXCHANGE RATE MODULE (5 COUNTRIES AT A TIME + SEARCH & CLICK)
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CurrencyConverterModule(
    viewModel: ConverterViewModel,
    uiState: ConverterUiState,
    shapeStructureStyle: ShapeStructureStyle,
    onCopyToClipboard: (String, String) -> Unit,
    isDark: Boolean,
    primaryColor: Color
) {
    val cardShape = shapeStructureStyle.toCardShape()
    val inputShape = shapeStructureStyle.toInputShape()
    val ratesState = uiState.currencyRates

    // 5 active countries list
    val activeCodes = remember(uiState.activeCurrencyCodes) {
        val list = uiState.activeCurrencyCodes.toMutableList()
        val defaultFive = listOf("USD", "EUR", "GBP", "JPY", "CAD")
        while (list.size < 5) {
            val next = defaultFive.firstOrNull { !list.contains(it) } ?: "AUD"
            list.add(next)
        }
        list.take(5)
    }

    var searchDialogSlotIndex by remember { mutableStateOf<Int?>(null) }

    if (searchDialogSlotIndex != null) {
        val slot = searchDialogSlotIndex!!
        val currentCode = activeCodes.getOrElse(slot) { "USD" }
        CountryCurrencySearchDialog(
            currentSelectedCode = currentCode,
            onSelectCurrency = { selectedCurr ->
                viewModel.setCurrencySlot(slot, selectedCurr.code)
            },
            onDismiss = { searchDialogSlotIndex = null },
            shapeStructureStyle = shapeStructureStyle,
            isDark = isDark,
            primaryColor = primaryColor
        )
    }

    val baseCurrencyInfo = supportedCurrencies.find { it.code == uiState.currencyBase }
        ?: supportedCurrencies.first()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Live Status & Refresh Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), cardShape),
            shape = cardShape,
            color = if (isDark) Color(0xFF141C2B) else Color(0xFFFFFFFF)
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
                        text = if (ratesState.isLiveOnline) "Live Rates (${ratesState.lastUpdated.take(16)})" else "Live Rates (Cached)",
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

        // BASE CURRENCY INPUT CARD
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.5.dp, primaryColor.copy(alpha = 0.5f), cardShape),
            shape = cardShape,
            color = if (isDark) Color(0xFF141C2B) else Color(0xFFFFFFFF)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = primaryColor.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "BASE INPUT",
                                fontSize = 10.sp,
                                fontFamily = DigitalMonospaceFont,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = baseCurrencyInfo.country,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Click to search & replace Base Country
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, primaryColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .clickable {
                                val baseIndex = activeCodes.indexOf(uiState.currencyBase).let { if (it >= 0) it else 0 }
                                searchDialogSlotIndex = baseIndex
                            }
                            .testTag("search_change_base_button"),
                        color = primaryColor.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Country",
                                tint = primaryColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Search Country",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = baseCurrencyInfo.flagEmoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = baseCurrencyInfo.code,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                fontSize = 18.sp,
                                color = primaryColor
                            )
                            Text(
                                text = baseCurrencyInfo.name,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Editable Amount Input
                    OutlinedTextField(
                        value = uiState.currencyAmount,
                        onValueChange = { viewModel.setCurrencyAmount(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            textAlign = TextAlign.End
                        ),
                        modifier = Modifier
                            .width(150.dp)
                            .testTag("currency_amount_input"),
                        shape = inputShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x33000000)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Amount Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("10", "50", "100", "500", "1,000", "5,000").forEach { preset ->
                        val clean = preset.replace(",", "")
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, if (isDark) Color(0x18FFFFFF) else Color(0x18000000), RoundedCornerShape(8.dp))
                                .clickable { viewModel.setCurrencyAmount(clean) },
                            color = if (uiState.currencyAmount == clean) primaryColor.copy(alpha = 0.2f) else Color.Transparent
                        ) {
                            Text(
                                text = preset,
                                fontSize = 10.sp,
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

        // 5 COUNTRIES MULTI-EXCHANGE BOARD
        Text(
            text = "5 COUNTRIES REAL-TIME EXCHANGE (CLICK TO SELECT / SEARCH)",
            fontSize = 11.sp,
            fontFamily = DigitalMonospaceFont,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        // Render 5 Countries Simultaneously
        activeCodes.forEachIndexed { index, code ->
            val curr = supportedCurrencies.find { it.code == code } ?: supportedCurrencies[0]
            val isBase = code == uiState.currencyBase
            val convertedVal = if (isBase) uiState.currencyAmount else viewModel.getConvertedAmountFor(code)
            val singleRate = viewModel.getSingleRateFor(code)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .border(
                        1.dp,
                        if (isBase) primaryColor else (if (isDark) Color(0x22FFFFFF) else Color(0x18000000)),
                        cardShape
                    )
                    .clickable {
                        // Click makes this country the active base!
                        if (!isBase) {
                            viewModel.setCurrencyBase(code)
                        }
                    }
                    .testTag("currency_slot_$index"),
                shape = cardShape,
                color = if (isBase) primaryColor.copy(alpha = 0.12f) else (if (isDark) Color(0xFF141C2B) else Color(0xFFFFFFFF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Country Details & Search Option Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = curr.flagEmoji, fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = curr.code,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    fontSize = 15.sp,
                                    color = if (isBase) primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isBase) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = primaryColor.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = "BASE",
                                            fontSize = 9.sp,
                                            fontFamily = DigitalMonospaceFont,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = curr.country,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!isBase) {
                                Text(
                                    text = "1 ${uiState.currencyBase} = $singleRate ${curr.symbol}",
                                    fontSize = 10.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Converted Value & Search/Change Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${curr.symbol} $convertedVal",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = if (isBase) primaryColor else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("currency_result_text_$index")
                            )
                        }

                        IconButton(
                            onClick = { onCopyToClipboard(convertedVal, "${curr.country} (${curr.code})") },
                            modifier = Modifier.size(32.dp).testTag("copy_currency_$index")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Value",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Search & Change Country Button for this slot
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0x22000000), CircleShape)
                                .clickable { searchDialogSlotIndex = index }
                                .testTag("search_slot_button_$index"),
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF1B2436) else Color(0xFFF1F5F9)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search & Change Country",
                                    tint = primaryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive Dialpad with 00 Double Zero
        ConverterDialpad(
            onDigit = { viewModel.appendActiveValue(it) },
            onDoubleZero = { viewModel.appendActiveValue("00") },
            onDecimal = { viewModel.appendActiveValue(".") },
            onBackspace = { viewModel.backspaceActiveValue() },
            onClear = { viewModel.clearActiveValue() },
            onNegate = null,
            shapeStructureStyle = shapeStructureStyle,
            isDark = isDark,
            primaryColor = primaryColor
        )
    }
}

// -------------------------------------------------------------------------------------------------
// 2. BMI CONVERTER & HEALTH GAUGE MODULE
// -------------------------------------------------------------------------------------------------
@Composable
private fun BmiConverterModule(
    viewModel: ConverterViewModel,
    uiState: ConverterUiState,
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
                .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), cardShape),
            shape = cardShape,
            color = if (isDark) Color(0xFF141C2B) else Color(0xFFFFFFFF)
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

        // Input Fields
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), cardShape),
            shape = cardShape,
            color = if (isDark) Color(0xFF141C2B) else Color(0xFFFFFFFF)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (uiState.bmiUnitSystem == BmiUnitSystem.METRIC) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.bmiWeightMetricKg,
                            onValueChange = { viewModel.setBmiMetricWeight(it) },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bmi_input_weight_metric"),
                            shape = inputShape
                        )

                        OutlinedTextField(
                            value = uiState.bmiHeightMetricCm,
                            onValueChange = { viewModel.setBmiMetricHeight(it) },
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bmi_input_height_metric"),
                            shape = inputShape
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = uiState.bmiWeightImperialLbs,
                        onValueChange = { viewModel.setBmiImperialWeight(it) },
                        label = { Text("Weight (lbs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bmi_input_weight_imperial"),
                        shape = inputShape
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.bmiHeightImperialFeet,
                            onValueChange = { viewModel.setBmiImperialFeet(it) },
                            label = { Text("Height (Feet)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bmi_input_feet"),
                            shape = inputShape
                        )

                        OutlinedTextField(
                            value = uiState.bmiHeightImperialInches,
                            onValueChange = { viewModel.setBmiImperialInches(it) },
                            label = { Text("Inches (0-11)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bmi_input_inches"),
                            shape = inputShape
                        )
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
            color = if (isDark) Color(0xFF141C2B) else Color(0xFFFFFFFF)
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

                // Progress Bar representation (10 to 45 scale)
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

        // Interactive Dialpad with 00 Double Zero
        ConverterDialpad(
            onDigit = { viewModel.appendActiveValue(it) },
            onDoubleZero = { viewModel.appendActiveValue("00") },
            onDecimal = { viewModel.appendActiveValue(".") },
            onBackspace = { viewModel.backspaceActiveValue() },
            onClear = { viewModel.clearActiveValue() },
            onNegate = null,
            shapeStructureStyle = shapeStructureStyle,
            isDark = isDark,
            primaryColor = primaryColor
        )
    }
}

// -------------------------------------------------------------------------------------------------
// 3. STANDARD UNITS CONVERTER MODULE (Length, Area, Volume, Weight, Speed, Pressure, Power)
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StandardUnitsConverterModule(
    viewModel: ConverterViewModel,
    uiState: ConverterUiState,
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

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // FROM CARD
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), cardShape),
            shape = cardShape,
            color = if (isDark) Color(0xFF141C2B) else Color(0xFFFFFFFF)
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

                    // Value input
                    OutlinedTextField(
                        value = uiState.standardInput,
                        onValueChange = { viewModel.setStandardInput(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            textAlign = TextAlign.End
                        ),
                        modifier = Modifier
                            .width(160.dp)
                            .testTag("standard_value_input"),
                        shape = inputShape
                    )
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
                color = if (isDark) Color(0xFF1B2436) else Color(0xFFF1F5F9),
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
            color = if (isDark) Color(0xFF141C2B) else Color(0xFFFFFFFF)
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
                .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), cardShape),
            shape = cardShape,
            color = if (isDark) Color(0xFF141C2B) else Color(0xFFFFFFFF)
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

        // Interactive Dialpad with 00 Double Zero
        ConverterDialpad(
            onDigit = { viewModel.appendActiveValue(it) },
            onDoubleZero = { viewModel.appendActiveValue("00") },
            onDecimal = { viewModel.appendActiveValue(".") },
            onBackspace = { viewModel.backspaceActiveValue() },
            onClear = { viewModel.clearActiveValue() },
            onNegate = { viewModel.negateActiveValue() },
            shapeStructureStyle = shapeStructureStyle,
            isDark = isDark,
            primaryColor = primaryColor
        )
    }
}

// -------------------------------------------------------------------------------------------------
// 4. TEMPERATURE CONVERTER MODULE
// -------------------------------------------------------------------------------------------------
@Composable
private fun TemperatureConverterModule(
    viewModel: ConverterViewModel,
    uiState: ConverterUiState,
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

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Temperature Input Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), cardShape),
            shape = cardShape,
            color = if (isDark) Color(0xFF141C2B) else Color(0xFFFFFFFF)
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

                    OutlinedTextField(
                        value = uiState.tempInput,
                        onValueChange = { viewModel.setTempInput(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            textAlign = TextAlign.End
                        ),
                        modifier = Modifier
                            .width(160.dp)
                            .testTag("temp_input_field"),
                        shape = inputShape
                    )
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
                color = if (isDark) Color(0xFF1B2436) else Color(0xFFF1F5F9),
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
            color = if (isDark) Color(0xFF141C2B) else Color(0xFFFFFFFF)
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
                HorizontalDivider(color = if (isDark) Color(0x18FFFFFF) else Color(0x18000000))
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
                .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), cardShape),
            shape = cardShape,
            color = if (isDark) Color(0xFF141C2B) else Color(0xFFFFFFFF)
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

        // Interactive Dialpad with 00 Double Zero
        ConverterDialpad(
            onDigit = { viewModel.appendActiveValue(it) },
            onDoubleZero = { viewModel.appendActiveValue("00") },
            onDecimal = { viewModel.appendActiveValue(".") },
            onBackspace = { viewModel.backspaceActiveValue() },
            onClear = { viewModel.clearActiveValue() },
            onNegate = { viewModel.negateActiveValue() },
            shapeStructureStyle = shapeStructureStyle,
            isDark = isDark,
            primaryColor = primaryColor
        )
    }
}

// -------------------------------------------------------------------------------------------------
// 5. NUMBER SYSTEM CONVERTER MODULE (Decimal, Binary, Hex, Octal)
// -------------------------------------------------------------------------------------------------
@Composable
private fun NumberSystemConverterModule(
    viewModel: ConverterViewModel,
    uiState: ConverterUiState,
    shapeStructureStyle: ShapeStructureStyle,
    onCopyToClipboard: (String, String) -> Unit,
    isDark: Boolean,
    primaryColor: Color
) {
    val cardShape = shapeStructureStyle.toCardShape()
    val inputShape = shapeStructureStyle.toInputShape()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Decimal Base 10
        NumberBaseCard(
            label = "Decimal (Base 10)",
            prefix = "DEC",
            value = uiState.numDecimal,
            onValueChange = { viewModel.onNumberSystemChanged(10, it) },
            keyboardType = KeyboardType.Number,
            onCopy = { onCopyToClipboard(uiState.numDecimal, "Decimal") },
            inputShape = inputShape,
            cardShape = cardShape,
            accentColor = Color(0xFF3B82F6),
            isDark = isDark,
            testTag = "num_decimal_input"
        )

        // Binary Base 2
        NumberBaseCard(
            label = "Binary (Base 2) [Nibbles 4-bit]",
            prefix = "BIN",
            value = uiState.numBinary,
            onValueChange = { viewModel.onNumberSystemChanged(2, it) },
            keyboardType = KeyboardType.Number,
            onCopy = { onCopyToClipboard(uiState.numBinary, "Binary") },
            inputShape = inputShape,
            cardShape = cardShape,
            accentColor = Color(0xFF10B981),
            isDark = isDark,
            testTag = "num_binary_input"
        )

        // Hexadecimal Base 16
        NumberBaseCard(
            label = "Hexadecimal (Base 16)",
            prefix = "0x",
            value = uiState.numHex,
            onValueChange = { viewModel.onNumberSystemChanged(16, it) },
            keyboardType = KeyboardType.Text,
            onCopy = { onCopyToClipboard(uiState.numHex, "Hexadecimal") },
            inputShape = inputShape,
            cardShape = cardShape,
            accentColor = Color(0xFFA855F7),
            isDark = isDark,
            testTag = "num_hex_input"
        )

        // Octal Base 8
        NumberBaseCard(
            label = "Octal (Base 8)",
            prefix = "0o",
            value = uiState.numOctal,
            onValueChange = { viewModel.onNumberSystemChanged(8, it) },
            keyboardType = KeyboardType.Number,
            onCopy = { onCopyToClipboard(uiState.numOctal, "Octal") },
            inputShape = inputShape,
            cardShape = cardShape,
            accentColor = Color(0xFFF59E0B),
            isDark = isDark,
            testTag = "num_octal_input"
        )

        // Quick Bit Breakdown & ASCII
        val decLong = uiState.numDecimal.toLongOrNull() ?: 0L
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), cardShape),
            shape = cardShape,
            color = if (isDark) Color(0xFF141C2B) else Color(0xFFFFFFFF)
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
                    Text("${java.lang.Long.toBinaryString(decLong).length} bits", fontSize = 13.sp, fontFamily = DigitalMonospaceFont, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun NumberBaseCard(
    label: String,
    prefix: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
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
            .border(1.dp, accentColor.copy(alpha = 0.35f), cardShape),
        shape = cardShape,
        color = if (isDark) Color(0xFF141C2B) else Color(0xFFFFFFFF)
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

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                leadingIcon = {
                    Text(
                        text = prefix,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = accentColor,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag),
                shape = inputShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x33000000)
                )
            )
        }
    }
}
