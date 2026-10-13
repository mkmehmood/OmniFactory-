package com.example

import com.example.ui.components.SoftIconButton
import com.example.ui.components.softPlate
import com.example.ui.components.SoftSurface
import com.example.ui.components.SoftCard
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CalculatorViewModel
import com.example.ui.ConverterViewModel
import com.example.ui.ExpensesViewModel
import com.example.ui.LedgerViewModel
import com.example.ui.MixtureCalculatorViewModel
import com.example.ui.MixtureViewModel
import com.example.ui.SalesViewModel
import com.example.ui.SettingsViewModel
import com.example.ui.components.AppNavigationDrawer
import com.example.ui.components.DrawerDestination
import com.example.ui.components.OrnamentalThemeBackground
import com.example.ui.components.SimpleCalculatorDialog
import com.example.ui.components.TapeHistorySheet
import com.example.ui.components.MixtureHistorySheet
import com.example.ui.screens.BasicCalculatorScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.MixtureCalculatorScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val calculatorViewModel: CalculatorViewModel by viewModels()
    private val salesViewModel: SalesViewModel by viewModels()
    private val ledgerViewModel: LedgerViewModel by viewModels()
    private val expensesViewModel: ExpensesViewModel by viewModels()
    private val mixtureViewModel: MixtureViewModel by viewModels()
    private val mixtureCalculatorViewModelLazy = viewModels<MixtureCalculatorViewModel>()
    private val mixtureCalculatorViewModel: MixtureCalculatorViewModel by mixtureCalculatorViewModelLazy
    private val converterViewModel: ConverterViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onStop() {
        // Make sure the Mixture Calculator's latest edits are on disk before the app can be killed.
        if (mixtureCalculatorViewModelLazy.isInitialized()) mixtureCalculatorViewModel.flush()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by calculatorViewModel.uiState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            var currentDestination by remember { mutableStateOf(DrawerDestination.CALCULATOR) }
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            var showQuickCalculatorDialog by remember { mutableStateOf(false) }

            BackHandler(enabled = drawerState.isOpen) {
                scope.launch { drawerState.close() }
            }

            BackHandler(enabled = !drawerState.isOpen && currentDestination != DrawerDestination.CALCULATOR) {
                currentDestination = when (currentDestination) {
                    DrawerDestination.MIXTURE_HISTORY -> DrawerDestination.MIXTURE_CALCULATOR
                    else -> DrawerDestination.CALCULATOR
                }
            }

            LaunchedEffect(Unit) {
                calculatorViewModel.userMessage.collect { message ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }

            LaunchedEffect(Unit) {
                salesViewModel.userMessage.collect { message ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }

            LaunchedEffect(Unit) {
                ledgerViewModel.userMessage.collect { message ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }

            LaunchedEffect(Unit) {
                expensesViewModel.userMessage.collect { message ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }

            LaunchedEffect(Unit) {
                converterViewModel.userMessage.collect { message ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }

            LaunchedEffect(Unit) {
                settingsViewModel.userMessage.collect { message ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }

            val copyToClipboard: (String, String) -> Unit = { text, label ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(label, text)
                clipboard.setPrimaryClip(clip)
                calculatorViewModel.showToast("$label copied to clipboard")
            }

            val pasteFromClipboard: () -> Unit = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = clipboard.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val text = clip.getItemAt(0).text?.toString() ?: ""
                    calculatorViewModel.onPaste(text)
                } else {
                    calculatorViewModel.showToast("Clipboard is empty")
                }
            }

            MyApplicationTheme(
                appThemeSkin = uiState.appThemeSkin,
                themeMode = uiState.themeMode,
                shapeStructureStyle = uiState.shapeStructureStyle,
                componentSizeStyle = uiState.componentSizeStyle,
                animationIntensity = uiState.animationIntensity,
                fontStyle = uiState.fontStyle
            ) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppNavigationDrawer(
                            currentDestination = currentDestination,
                            activeConverterCategory = null,
                            onSelectDestination = { destination ->
                                calculatorViewModel.setTapeOpen(false)
                                scope.launch {
                                    drawerState.close()
                                    currentDestination = destination
                                }
                            },
                            onOpenConverterGrid = {
                                converterViewModel.showCategoryGrid()
                            },
                            onSelectConverterCategory = { category ->
                                converterViewModel.setCategory(category)
                                currentDestination = DrawerDestination.CONVERTER
                                scope.launch { drawerState.close() }
                            },
                            onCloseDrawer = {
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                ) {
                    OrnamentalThemeBackground(
                        appThemeSkin = uiState.appThemeSkin,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (currentDestination == DrawerDestination.SETTINGS) {
                            SettingsScreen(
                                currentAppThemeSkin = uiState.appThemeSkin,
                                currentThemeMode = uiState.themeMode,
                                currentShapeStructure = uiState.shapeStructureStyle,
                                currentComponentSize = uiState.componentSizeStyle,
                                currentAnimationIntensity = uiState.animationIntensity,
                                currentFontStyle = uiState.fontStyle,
                                currentHapticIntensity = uiState.hapticIntensity,
                                currentSoundIntensity = uiState.soundIntensity,
                                showDoubleZeroKey = uiState.showDoubleZeroKey,
                                onAppThemeSkinChange = { calculatorViewModel.setAppThemeSkin(it) },
                                onThemeModeChange = { calculatorViewModel.setThemeMode(it) },
                                onShapeStructureChange = { calculatorViewModel.setShapeStructureStyle(it) },
                                onComponentSizeChange = { calculatorViewModel.setComponentSizeStyle(it) },
                                onAnimationIntensityChange = { calculatorViewModel.setAnimationIntensity(it) },
                                onFontStyleChange = { calculatorViewModel.setFontStyle(it) },
                                onHapticIntensityChange = { calculatorViewModel.setHapticIntensity(it) },
                                onSoundIntensityChange = { calculatorViewModel.setSoundIntensity(it) },
                                onToggleDoubleZero = { calculatorViewModel.setShowDoubleZeroKey(it) },
                                settingsViewModel = settingsViewModel,
                                onPreferencesReload = { calculatorViewModel.reloadPreferences() },
                                onCopyToClipboard = copyToClipboard,
                                onOpenSidebar = {
                                    scope.launch { drawerState.open() }
                                }
                            )
                        } else {
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                topBar = {
                                    val topBarTitle = when (currentDestination) {
                                        DrawerDestination.CALCULATOR -> "Calculator"
                                        DrawerDestination.LEDGER -> "Daily Ledger"
                                        DrawerDestination.EXPENSES -> "Expenses"
                                        DrawerDestination.SALES -> "Sales Reconciliation"
                                        DrawerDestination.MIXTURE_CALCULATOR -> "Mixture Calculator"
                                        DrawerDestination.MIXTURE_HISTORY -> "Mixture History"
                                        DrawerDestination.CONVERTER -> "Units Converter"
                                        else -> "Calculator"
                                    }
                                    val topBarIcon = when (currentDestination) {
                                        DrawerDestination.CALCULATOR -> Icons.Default.Calculate
                                        DrawerDestination.LEDGER -> Icons.Default.AccountBalanceWallet
                                        DrawerDestination.EXPENSES -> Icons.Default.Receipt
                                        DrawerDestination.SALES -> Icons.Default.ReceiptLong
                                        DrawerDestination.MIXTURE_CALCULATOR -> Icons.Default.Science
                                        DrawerDestination.MIXTURE_HISTORY -> Icons.Default.History
                                        DrawerDestination.CONVERTER -> Icons.Default.Category
                                        else -> Icons.Default.Calculate
                                    }
                                    val actionIcon = when (currentDestination) {
                                        DrawerDestination.CALCULATOR -> Icons.Default.History
                                        DrawerDestination.LEDGER -> Icons.Default.History
                                        DrawerDestination.EXPENSES -> Icons.Default.Add
                                        DrawerDestination.SALES -> Icons.Default.ReceiptLong
                                        DrawerDestination.CONVERTER -> Icons.Default.Calculate
                                        DrawerDestination.MIXTURE_CALCULATOR -> Icons.Default.History
                                        else -> Icons.Default.History
                                    }
                                    val actionDescription = when (currentDestination) {
                                        DrawerDestination.CALCULATOR -> "History Tape"
                                        DrawerDestination.LEDGER -> "Transaction History"
                                        DrawerDestination.EXPENSES -> "Log Expense"
                                        DrawerDestination.SALES -> "Saved Sales"
                                        DrawerDestination.CONVERTER -> "Quick Calculator"
                                        DrawerDestination.MIXTURE_CALCULATOR -> "Formulations History"
                                        else -> "Settings"
                                    }
                                    val onActionClick: () -> Unit = when (currentDestination) {
                                        DrawerDestination.CALCULATOR -> {
                                            { calculatorViewModel.setTapeOpen(true) }
                                        }
                                        DrawerDestination.LEDGER -> {
                                            { ledgerViewModel.setHistoryPanelOpen(true) }
                                        }
                                        DrawerDestination.EXPENSES -> {
                                            { expensesViewModel.openAddExpense() }
                                        }
                                        DrawerDestination.SALES -> {
                                            { salesViewModel.setSavedHistorySheetOpen(true) }
                                        }
                                        DrawerDestination.MIXTURE_CALCULATOR -> {
                                            { mixtureViewModel.setDatabaseBrowserSheetOpen(true) }
                                        }
                                        DrawerDestination.CONVERTER -> {
                                            { showQuickCalculatorDialog = true }
                                        }
                                        else -> {
                                            { currentDestination = DrawerDestination.SETTINGS }
                                        }
                                    }

                                    LiquidGlassTopBar(
                                        title = topBarTitle,
                                        icon = topBarIcon,
                                        actionIcon = actionIcon,
                                        actionDescription = actionDescription,
                                        onOpenSidebar = {
                                            scope.launch { drawerState.open() }
                                        },
                                        onActionClick = onActionClick
                                    )
                                },
                                snackbarHost = { SnackbarHost(snackbarHostState) },
                                containerColor = Color.Transparent
                            ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                androidx.compose.animation.AnimatedContent(
                                    targetState = currentDestination,
                                    modifier = Modifier.fillMaxSize(),
                                    transitionSpec = {
                                        val base = if (uiState.animationIntensity == com.example.ui.theme.AnimationIntensity.OFF) {
                                            androidx.compose.animation.EnterTransition.None togetherWith
                                                androidx.compose.animation.ExitTransition.None
                                        } else {
                                            // Short, direction-aware slide + fade: the incoming screen eases in
                                            // from the side it logically lives on while the old one fades out fast,
                                            // so both heavy screens overlap for as little time as possible.
                                            val dir = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                                            (androidx.compose.animation.fadeIn(
                                                androidx.compose.animation.core.tween(
                                                    170, delayMillis = 50,
                                                    easing = androidx.compose.animation.core.LinearOutSlowInEasing
                                                )
                                            ) + androidx.compose.animation.slideInHorizontally(
                                                androidx.compose.animation.core.tween(
                                                    260, easing = androidx.compose.animation.core.FastOutSlowInEasing
                                                )
                                            ) { dir * (it / 14) }) togetherWith
                                                (androidx.compose.animation.fadeOut(
                                                    androidx.compose.animation.core.tween(90)
                                                ) + androidx.compose.animation.slideOutHorizontally(
                                                    androidx.compose.animation.core.tween(
                                                        200, easing = androidx.compose.animation.core.FastOutSlowInEasing
                                                    )
                                                ) { -dir * (it / 14) })
                                        }
                                        base.using(androidx.compose.animation.SizeTransform(clip = false) { _, _ ->
                                            androidx.compose.animation.core.snap()
                                        })
                                    },
                                    label = "ScreenTransition"
                                ) { destination ->
                                    when (destination) {
                                        DrawerDestination.CALCULATOR -> {
                                            BasicCalculatorScreen(
                                                expression = uiState.expression,
                                                liveResult = uiState.liveResult,
                                                isAllClear = uiState.expression.isEmpty(),
                                                showDoubleZeroKey = uiState.showDoubleZeroKey,
                                                animationIntensity = uiState.animationIntensity,
                                                hapticIntensity = uiState.hapticIntensity,
                                                soundIntensity = uiState.soundIntensity,
                                                componentSizeStyle = uiState.componentSizeStyle,
                                                shapeStructureStyle = uiState.shapeStructureStyle,
                                                onDigit = { calculatorViewModel.onDigit(it) },
                                                onDoubleZero = { calculatorViewModel.onDoubleZero() },
                                                onOperator = { calculatorViewModel.onOperator(it) },
                                                onPercent = { calculatorViewModel.onPercent() },
                                                onClear = { calculatorViewModel.onClear() },
                                                onBackspace = { calculatorViewModel.onBackspace() },
                                                onEquals = { calculatorViewModel.onEquals() },
                                                onNegate = { calculatorViewModel.onNegate() },
                                                onCopyExpression = {
                                                    val text = if (uiState.expression.isEmpty()) "0" else uiState.expression
                                                    copyToClipboard(text, "Expression")
                                                },
                                                onCopyResult = {
                                                    val text = uiState.liveResult ?: if (uiState.expression.isEmpty()) "0" else uiState.expression
                                                    copyToClipboard(text, "Result")
                                                },
                                                onPaste = { pasteFromClipboard() }
                                            )
                                        }
                                        DrawerDestination.LEDGER -> {
                                            LedgerScreen(
                                                viewModel = ledgerViewModel,
                                                onOpenCalculator = { showQuickCalculatorDialog = true }
                                            )
                                        }
                                        DrawerDestination.EXPENSES -> {
                                            ExpensesScreen(
                                                viewModel = expensesViewModel
                                            )
                                        }
                                        DrawerDestination.SALES -> {
                                            val salesUiState by salesViewModel.uiState.collectAsStateWithLifecycle()
                                            val savedSalesTransactions by salesViewModel.savedTransactions.collectAsStateWithLifecycle()
                                            SalesScreen(
                                                uiState = salesUiState,
                                                savedTransactions = savedSalesTransactions,
                                                onSetSalesRep = { salesViewModel.setSalesRep(it) },
                                                onAddCustomSalesRep = { salesViewModel.addCustomSalesRep(it) },
                                                onSetDateMillis = { salesViewModel.setDateMillis(it) },
                                                onSetTotalSoldQty = { salesViewModel.setTotalSoldQty(it) },
                                                onSetReturnedQty = { salesViewModel.setReturnedQty(it) },
                                                onSetExpiredQty = { salesViewModel.setExpiredQty(it) },
                                                onSetSharedQty = { salesViewModel.setSharedQty(it) },
                                                onSetCreditSalesQty = { salesViewModel.setCreditSalesQty(it) },
                                                onSetRecoveredCredit = { salesViewModel.setRecoveredCredit(it) },
                                                onSetPhysicalCash = { salesViewModel.setPhysicalCash(it) },
                                                onSetExpense = { salesViewModel.setExpense(it) },
                                                onSetUnitSalePrice = { salesViewModel.setUnitSalePrice(it) },
                                                onSetCommissionPerUnit = { salesViewModel.setCommissionPerUnit(it) },
                                                onSetCommissionPaid = { salesViewModel.setCommissionPaid(it) },
                                                onShiftDeficitToCommissionPaid = { salesViewModel.shiftDeficitToCommissionPaid() },
                                                onSaveTransaction = { salesViewModel.saveTransaction() },
                                                onClearInputs = { salesViewModel.clearInputs() },
                                                onCopySummary = { copyToClipboard(it, "Sales reconciliation audit") },
                                                onOpenHistorySheet = { salesViewModel.setSavedHistorySheetOpen(true) },
                                                onCloseHistorySheet = { salesViewModel.setSavedHistorySheetOpen(false) },
                                                onLoadTransaction = { salesViewModel.loadTransactionIntoForm(it) },
                                                onDeleteTransaction = { salesViewModel.deleteTransaction(it) }
                                            )
                                        }
                                        DrawerDestination.MIXTURE_CALCULATOR -> {
                                            val mixtureUiState by mixtureViewModel.uiState.collectAsStateWithLifecycle()
                                            val mixtureRecipes by mixtureViewModel.persistentRecipes.collectAsStateWithLifecycle()
                                            val localContext = LocalContext.current

                                            MixtureCalculatorScreen(
                                                onNavigateToHistory = { mixtureViewModel.setDatabaseBrowserSheetOpen(true) },
                                                calcViewModel = mixtureCalculatorViewModel
                                            )

                                            if (mixtureUiState.showDatabaseBrowserSheet) {
                                                MixtureHistorySheet(
                                                    recipes = mixtureRecipes,
                                                    onDismiss = { mixtureViewModel.setDatabaseBrowserSheetOpen(false) },
                                                    onLoadRecipe = { recipe ->
                                                        mixtureViewModel.loadRecipeFromDb(recipe)
                                                        mixtureViewModel.setDatabaseBrowserSheetOpen(false)
                                                    },
                                                    onEditRecipe = { recipe, title, category, desc ->
                                                        mixtureViewModel.updateRecipeInDb(recipe, title, category, desc)
                                                    },
                                                    onDeleteRecipe = { recipe ->
                                                        mixtureViewModel.deleteRecipeFromDb(recipe)
                                                    },
                                                    onToggleFavorite = { recipe ->
                                                        mixtureViewModel.toggleFavorite(recipe)
                                                    },
                                                    onClearAll = {
                                                        mixtureViewModel.clearAllRecipes()
                                                    }
                                                )
                                            }
                                        }
                                        DrawerDestination.MIXTURE_HISTORY -> {
                                            LaunchedEffect(Unit) {
                                                mixtureViewModel.setDatabaseBrowserSheetOpen(true)
                                                currentDestination = DrawerDestination.MIXTURE_CALCULATOR
                                            }
                                        }
                                        DrawerDestination.CONVERTER -> {
                                            val converterUiState by converterViewModel.uiState.collectAsStateWithLifecycle()
                                            ConverterScreen(
                                                viewModel = converterViewModel,
                                                uiState = converterUiState,
                                                shapeStructureStyle = uiState.shapeStructureStyle,
                                                componentSizeStyle = uiState.componentSizeStyle,
                                                animationIntensity = uiState.animationIntensity,
                                                onCopyToClipboard = copyToClipboard
                                            )
                                        }
                                        else -> {
                                            BasicCalculatorScreen(
                                                expression = uiState.expression,
                                                liveResult = uiState.liveResult,
                                                isAllClear = uiState.expression.isEmpty(),
                                                showDoubleZeroKey = uiState.showDoubleZeroKey,
                                                animationIntensity = uiState.animationIntensity,
                                                hapticIntensity = uiState.hapticIntensity,
                                                soundIntensity = uiState.soundIntensity,
                                                componentSizeStyle = uiState.componentSizeStyle,
                                                shapeStructureStyle = uiState.shapeStructureStyle,
                                                onDigit = { calculatorViewModel.onDigit(it) },
                                                onDoubleZero = { calculatorViewModel.onDoubleZero() },
                                                onOperator = { calculatorViewModel.onOperator(it) },
                                                onPercent = { calculatorViewModel.onPercent() },
                                                onClear = { calculatorViewModel.onClear() },
                                                onBackspace = { calculatorViewModel.onBackspace() },
                                                onEquals = { calculatorViewModel.onEquals() },
                                                onNegate = { calculatorViewModel.onNegate() },
                                                onCopyExpression = {
                                                    val text = if (uiState.expression.isEmpty()) "0" else uiState.expression
                                                    copyToClipboard(text, "Expression")
                                                },
                                                onCopyResult = {
                                                    val text = uiState.liveResult ?: if (uiState.expression.isEmpty()) "0" else uiState.expression
                                                    copyToClipboard(text, "Result")
                                                },
                                                onPaste = { pasteFromClipboard() }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                    // Clean, Unlimited and Persistent History Sheet with All Time, Today, Weekly, Monthly, Yearly separation
                    if (uiState.isTapeOpen) {
                        val historyList by calculatorViewModel.historyList.collectAsStateWithLifecycle()
                        TapeHistorySheet(
                            historyList = historyList,
                            selectedTimeSplit = uiState.historyTimeSplit,
                            onTimeSplitChange = { calculatorViewModel.setHistoryTimeSplit(it) },
                            onDismiss = {
                                calculatorViewModel.setTapeOpen(false)
                            },
                            onRestoreItem = {
                                calculatorViewModel.restoreCalculation(it)
                                currentDestination = DrawerDestination.CALCULATOR
                            },
                            onReuseExpression = {
                                calculatorViewModel.reuseExpression(it)
                                currentDestination = DrawerDestination.CALCULATOR
                            },
                            onDeleteItem = { calculatorViewModel.deleteCalculation(it) },
                            onTogglePin = { calculatorViewModel.togglePin(it) },
                            onUpdateNote = { entity, note ->
                                calculatorViewModel.updateCalculationNote(entity, note)
                            },
                            onClearAll = { calculatorViewModel.clearAllHistory() },
                            onCopyTape = { tapeText -> copyToClipboard(tapeText, "History tape") }
                        )
                    }

                    if (showQuickCalculatorDialog) {
                        SimpleCalculatorDialog(
                            onDismiss = { showQuickCalculatorDialog = false }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LiquidGlassTopBar(
    title: String,
    icon: ImageVector,
    actionIcon: ImageVector,
    actionDescription: String,
    onOpenSidebar: () -> Unit,
    onActionClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val currentSkin = com.example.ui.theme.LocalAppThemeSkin.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val isSuperAmoled = currentSkin == com.example.ui.theme.AppThemeSkin.DARK_CHARCOAL

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        CenterAlignedTopAppBar(
            navigationIcon = {
                // Symmetrical spacing placeholder to preserve centered capsule alignment
                Spacer(modifier = Modifier.size(40.dp))
            },
            title = {
                SoftSurface(
                    onClick = onOpenSidebar,
                    shape = RoundedCornerShape(22.dp),
                    shadowElevation = if (!isDark && !isSuperAmoled) 1.5.dp else 0.dp,
                    modifier = Modifier.testTag("topbar_title_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            actions = {
                SoftIconButton(
                    onClick = onActionClick,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .softPlate(CircleShape)
                        .testTag("topbar_action_button")
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = actionDescription,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}
