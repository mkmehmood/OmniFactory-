package com.example

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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BatchScalingViewModel
import com.example.ui.CalculatorViewModel
import com.example.ui.ConverterViewModel
import com.example.ui.MixtureViewModel
import com.example.ui.SalesViewModel
import com.example.ui.components.AppNavigationDrawer
import com.example.ui.components.DrawerDestination
import com.example.ui.components.SimpleCalculatorDialog
import com.example.ui.components.TapeHistorySheet
import com.example.ui.screens.BasicCalculatorScreen
import com.example.ui.screens.BatchScalingScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.MixtureCalculatorScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.material.icons.filled.Category
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val calculatorViewModel: CalculatorViewModel by viewModels()
    private val batchScalingViewModel: BatchScalingViewModel by viewModels()
    private val salesViewModel: SalesViewModel by viewModels()
    private val mixtureViewModel: MixtureViewModel by viewModels()
    private val converterViewModel: ConverterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by calculatorViewModel.uiState.collectAsStateWithLifecycle()
            val batchUiState by batchScalingViewModel.uiState.collectAsStateWithLifecycle()
            val salesUiState by salesViewModel.uiState.collectAsStateWithLifecycle()
            val converterUiState by converterViewModel.uiState.collectAsStateWithLifecycle()
            val savedSalesTransactions by salesViewModel.savedTransactions.collectAsStateWithLifecycle()
            val historyList by calculatorViewModel.historyList.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }
            val context = LocalContext.current
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            var currentDestination by remember { mutableStateOf(DrawerDestination.CALCULATOR) }
            var showQuickCalculatorDialog by remember { mutableStateOf(false) }

            BackHandler(enabled = drawerState.isOpen) {
                scope.launch { drawerState.close() }
            }

            BackHandler(enabled = !drawerState.isOpen && currentDestination != DrawerDestination.CALCULATOR) {
                currentDestination = DrawerDestination.CALCULATOR
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
                converterViewModel.userMessage.collect { message ->
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
                themeMode = uiState.themeMode,
                accentTheme = uiState.accentTheme,
                shapeStructureStyle = uiState.shapeStructureStyle,
                componentSizeStyle = uiState.componentSizeStyle,
                animationIntensity = uiState.animationIntensity
            ) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppNavigationDrawer(
                            currentDestination = currentDestination,
                            activeConverterCategory = converterUiState.activeCategory,
                            onSelectDestination = { destination ->
                                scope.launch { drawerState.close() }
                                currentDestination = destination
                                calculatorViewModel.setTapeOpen(false)
                            },
                            onOpenConverterGrid = {
                                converterViewModel.showCategoryGrid()
                            },
                            onSelectConverterCategory = { category ->
                                converterViewModel.selectCategoryAndOpenDedicatedScreen(category)
                            },
                            onCloseDrawer = {
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                ) {
                    if (currentDestination == DrawerDestination.SETTINGS) {
                        SettingsScreen(
                            currentThemeMode = uiState.themeMode,
                            currentAccent = uiState.accentTheme,
                            currentShapeStructure = uiState.shapeStructureStyle,
                            currentComponentSize = uiState.componentSizeStyle,
                            currentAnimationIntensity = uiState.animationIntensity,
                            currentHapticIntensity = uiState.hapticIntensity,
                            currentSoundIntensity = uiState.soundIntensity,
                            showDoubleZeroKey = uiState.showDoubleZeroKey,
                            onThemeModeChange = { calculatorViewModel.setThemeMode(it) },
                            onAccentChange = { calculatorViewModel.setAccentTheme(it) },
                            onShapeStructureChange = { calculatorViewModel.setShapeStructureStyle(it) },
                            onComponentSizeChange = { calculatorViewModel.setComponentSizeStyle(it) },
                            onAnimationIntensityChange = { calculatorViewModel.setAnimationIntensity(it) },
                            onHapticIntensityChange = { calculatorViewModel.setHapticIntensity(it) },
                            onSoundIntensityChange = { calculatorViewModel.setSoundIntensity(it) },
                            onToggleDoubleZero = { calculatorViewModel.setShowDoubleZeroKey(it) },
                            onNavigateBack = {
                                currentDestination = DrawerDestination.CALCULATOR
                            }
                        )
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                val topBarTitle = when (currentDestination) {
                                    DrawerDestination.CALCULATOR -> "Calculator"
                                    DrawerDestination.SALES -> "Sales Reconciliation"
                                    DrawerDestination.MIXTURE_CALCULATOR -> "Mixture Calculator"
                                    DrawerDestination.BATCH_SCALING -> "Batch Scaler"
                                    DrawerDestination.CONVERTER -> {
                                        if (converterUiState.isCategoryGridActive) "Units Converter"
                                        else "Units: ${converterUiState.activeCategory.shortName}"
                                    }
                                    else -> "Calculator"
                                }
                                val topBarIcon = when (currentDestination) {
                                    DrawerDestination.CALCULATOR -> Icons.Default.Calculate
                                    DrawerDestination.SALES -> Icons.Default.ReceiptLong
                                    DrawerDestination.MIXTURE_CALCULATOR -> Icons.Default.Science
                                    DrawerDestination.BATCH_SCALING -> Icons.Default.Scale
                                    DrawerDestination.CONVERTER -> {
                                        if (converterUiState.isCategoryGridActive) Icons.Default.Category
                                        else converterUiState.activeCategory.icon
                                    }
                                    else -> Icons.Default.Calculate
                                }
                                val actionIcon = when (currentDestination) {
                                    DrawerDestination.CALCULATOR -> Icons.Default.History
                                    DrawerDestination.SALES -> Icons.Default.ReceiptLong
                                    DrawerDestination.CONVERTER -> Icons.Default.Calculate
                                    DrawerDestination.MIXTURE_CALCULATOR -> Icons.Default.Calculate
                                    DrawerDestination.BATCH_SCALING -> Icons.Default.Calculate
                                    else -> Icons.Default.History
                                }
                                val actionDescription = when (currentDestination) {
                                    DrawerDestination.CALCULATOR -> "History Tape"
                                    DrawerDestination.SALES -> "Saved Sales"
                                    DrawerDestination.CONVERTER -> "Quick Calculator"
                                    DrawerDestination.MIXTURE_CALCULATOR -> "Quick Calculator"
                                    DrawerDestination.BATCH_SCALING -> "Quick Calculator"
                                    else -> "Settings"
                                }
                                val onActionClick: () -> Unit = when (currentDestination) {
                                    DrawerDestination.CALCULATOR -> {
                                        { calculatorViewModel.setTapeOpen(true) }
                                    }
                                    DrawerDestination.SALES -> {
                                        { salesViewModel.setSavedHistorySheetOpen(true) }
                                    }
                                    DrawerDestination.CONVERTER,
                                    DrawerDestination.MIXTURE_CALCULATOR,
                                    DrawerDestination.BATCH_SCALING -> {
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
                                    onOpenDrawer = {
                                        scope.launch { drawerState.open() }
                                    },
                                    onActionClick = onActionClick
                                )
                            },
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            containerColor = MaterialTheme.colorScheme.background
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                Crossfade(
                                    targetState = currentDestination,
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
                                        DrawerDestination.SALES -> {
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
                                                onSetDeficitThreshold = { salesViewModel.setDeficitThreshold(it) },
                                                onSetDeficitThresholdText = { salesViewModel.setDeficitThresholdText(it) },
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
                                            MixtureCalculatorScreen(
                                                viewModel = mixtureViewModel
                                            )
                                        }
                                        DrawerDestination.BATCH_SCALING -> {
                                            BatchScalingScreen(
                                                uiState = batchUiState,
                                                onAddComponent = { batchScalingViewModel.addComponent() },
                                                onRemoveComponent = { batchScalingViewModel.removeComponent(it) },
                                                onUpdateComponentName = { id, name -> batchScalingViewModel.updateComponentName(id, name) },
                                                onUpdateComponentAmount = { id, amount -> batchScalingViewModel.updateComponentAmount(id, amount) },
                                                onToggleComponentLock = { batchScalingViewModel.toggleComponentLock(it) },
                                                onSetScalingMode = { batchScalingViewModel.setScalingMode(it) },
                                                onSetBatchSubMode = { batchScalingViewModel.setBatchSubMode(it) },
                                                onSetTargetTotal = { batchScalingViewModel.setTargetTotal(it) },
                                                onSetBatchSize = { batchScalingViewModel.setBatchSize(it) },
                                                onSetBatchCount = { batchScalingViewModel.setBatchCount(it) },
                                                onSetAvailableAmount = { batchScalingViewModel.setAvailableAmount(it) },
                                                onSetRoundingOption = { batchScalingViewModel.setRoundingOption(it) },
                                                onToggleFormulaExpanded = { batchScalingViewModel.toggleFormulaExpanded() },
                                                onApplyMultiplier = { batchScalingViewModel.applyMultiplier(it) },
                                                onLoadPreset = { batchScalingViewModel.loadPreset(it) },
                                                onClearAll = { batchScalingViewModel.clearAll() },
                                                onCopyResults = { copyToClipboard(it, "Batch scaling report") }
                                            )
                                        }
                                        DrawerDestination.CONVERTER -> {
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

                    // Clean, Unlimited and Persistent History Sheet with All Time, Today, Weekly, Monthly, Yearly separation
                    if (uiState.isTapeOpen) {
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
    onOpenDrawer: () -> Unit,
    onActionClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.testTag("open_sidebar_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Sidebar Navigation",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = DigitalMonospaceFont,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            IconButton(
                onClick = onActionClick,
                modifier = Modifier.testTag("topbar_action_button")
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}
