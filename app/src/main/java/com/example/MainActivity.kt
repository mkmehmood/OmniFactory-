package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.domain.export.CsvExporter
import com.example.ui.BatchScalingUiState
import com.example.ui.BatchScalingViewModel
import com.example.ui.CalculatorUiState
import com.example.ui.CalculatorViewModel
import com.example.ui.MixtureViewModel
import com.example.ui.SalesViewModel
import com.example.ui.components.AppNavigationDrawer
import com.example.ui.components.DrawerDestination
import com.example.ui.components.TapeHistorySheet
import com.example.ui.screens.BasicCalculatorScreen
import com.example.ui.screens.BatchScalingScreen
import com.example.ui.screens.MixtureCalculatorScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val calculatorViewModel: CalculatorViewModel by viewModels()
    private val batchScalingViewModel: BatchScalingViewModel by viewModels()
    private val salesViewModel: SalesViewModel by viewModels()
    private val mixtureViewModel: MixtureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by calculatorViewModel.uiState.collectAsStateWithLifecycle()
            val batchUiState by batchScalingViewModel.uiState.collectAsStateWithLifecycle()
            val salesUiState by salesViewModel.uiState.collectAsStateWithLifecycle()
            val savedSalesTransactions by salesViewModel.savedTransactions.collectAsStateWithLifecycle()
            val historyList by calculatorViewModel.historyList.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }
            val context = LocalContext.current
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            var currentDestination by remember { mutableStateOf(DrawerDestination.CALCULATOR) }
            var showSplash by rememberSaveable { mutableStateOf(true) }

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
                accentTheme = uiState.accentTheme
            ) {
                Crossfade(
                    targetState = showSplash,
                    label = "SplashOrMainContent"
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen(
                            onSplashFinished = { showSplash = false }
                        )
                    } else {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                AppNavigationDrawer(
                                    currentDestination = currentDestination,
                                    historyCount = historyList.size,
                                    onSelectDestination = { destination ->
                                        scope.launch { drawerState.close() }
                                        when (destination) {
                                            DrawerDestination.CALCULATOR -> {
                                                currentDestination = DrawerDestination.CALCULATOR
                                                calculatorViewModel.setTapeOpen(false)
                                            }
                                            DrawerDestination.SALES -> {
                                                currentDestination = DrawerDestination.SALES
                                                calculatorViewModel.setTapeOpen(false)
                                            }
                                            DrawerDestination.MIXTURE_CALCULATOR -> {
                                                currentDestination = DrawerDestination.MIXTURE_CALCULATOR
                                                calculatorViewModel.setTapeOpen(false)
                                            }
                                            DrawerDestination.BATCH_SCALING -> {
                                                currentDestination = DrawerDestination.BATCH_SCALING
                                                calculatorViewModel.setTapeOpen(false)
                                            }
                                            DrawerDestination.HISTORY -> {
                                                calculatorViewModel.setTapeOpen(true)
                                            }
                                            DrawerDestination.SETTINGS -> {
                                                currentDestination = DrawerDestination.SETTINGS
                                                calculatorViewModel.setTapeOpen(false)
                                            }
                                        }
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
                                    showDoubleZeroKey = uiState.showDoubleZeroKey,
                                    enableAnimations = uiState.enableAnimations,
                                    hapticEnabled = uiState.hapticEnabled,
                                    historyList = historyList,
                                    salesTransactions = savedSalesTransactions,
                                    onThemeModeChange = { calculatorViewModel.setThemeMode(it) },
                                    onAccentChange = { calculatorViewModel.setAccentTheme(it) },
                                    onShapeStructureChange = { calculatorViewModel.setShapeStructureStyle(it) },
                                    onToggleDoubleZero = { calculatorViewModel.setShowDoubleZeroKey(it) },
                                    onToggleAnimations = { calculatorViewModel.setEnableAnimations(it) },
                                    onHapticToggle = { calculatorViewModel.setHapticEnabled(it) },
                                    onExportTapeCsv = {
                                        try {
                                            val csv = CsvExporter.exportCalculationHistoryToCsv(historyList)
                                            val file = CsvExporter.saveCsvToCacheFile(context, "calculator_tape_history.csv", csv)
                                            val shareIntent = CsvExporter.createShareCsvIntent(context, file, "Calculation Tape History CSV")
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Tape History CSV"))
                                        } catch (e: Exception) {
                                            scope.launch { snackbarHostState.showSnackbar("Export failed: ${e.message}") }
                                        }
                                    },
                                    onExportSalesCsv = {
                                        try {
                                            val csv = CsvExporter.exportSalesTransactionsToCsv(savedSalesTransactions)
                                            val file = CsvExporter.saveCsvToCacheFile(context, "sales_reconciliation_audit.csv", csv)
                                            val shareIntent = CsvExporter.createShareCsvIntent(context, file, "Sales Reconciliation Audit CSV")
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Sales Reconciliation CSV"))
                                        } catch (e: Exception) {
                                            scope.launch { snackbarHostState.showSnackbar("Export failed: ${e.message}") }
                                        }
                                    },
                                    onExportAllCsv = {
                                        try {
                                            val tapeCsv = CsvExporter.exportCalculationHistoryToCsv(historyList)
                                            val salesCsv = CsvExporter.exportSalesTransactionsToCsv(savedSalesTransactions)
                                            val combined = "=== CALCULATION HISTORY ===\r\n$tapeCsv\r\n\r\n=== SALES RECONCILIATION AUDIT ===\r\n$salesCsv"
                                            val file = CsvExporter.saveCsvToCacheFile(context, "complete_calculator_backup.csv", combined)
                                            val shareIntent = CsvExporter.createShareCsvIntent(context, file, "Complete Calculator & Sales CSV Export")
                                            context.startActivity(Intent.createChooser(shareIntent, "Share All Data CSV"))
                                        } catch (e: Exception) {
                                            scope.launch { snackbarHostState.showSnackbar("Export failed: ${e.message}") }
                                        }
                                    },
                                    onCopyTapeCsv = {
                                        val csv = CsvExporter.exportCalculationHistoryToCsv(historyList)
                                        copyToClipboard(csv, "Calculation History CSV")
                                    },
                                    onCopySalesCsv = {
                                        val csv = CsvExporter.exportSalesTransactionsToCsv(savedSalesTransactions)
                                        copyToClipboard(csv, "Sales Reconciliation CSV")
                                    },
                                    onNavigateBack = {
                                        currentDestination = DrawerDestination.CALCULATOR
                                    }
                                )
                            } else {
                                Scaffold(
                                    modifier = Modifier.fillMaxSize(),
                                    topBar = {
                                        val topBarTitle = when (currentDestination) {
                                            DrawerDestination.CALCULATOR -> "Sarim Calculator"
                                            DrawerDestination.SALES -> "Sales Reconciliation"
                                            DrawerDestination.MIXTURE_CALCULATOR -> "Mixture Ratio Recalibrator"
                                            DrawerDestination.BATCH_SCALING -> "Batch Scaler"
                                            else -> "Sarim"
                                        }
                                        val topBarIcon = when (currentDestination) {
                                            DrawerDestination.CALCULATOR -> Icons.Default.WaterDrop
                                            DrawerDestination.SALES -> Icons.Default.ReceiptLong
                                            DrawerDestination.MIXTURE_CALCULATOR -> Icons.Default.Science
                                            DrawerDestination.BATCH_SCALING -> Icons.Default.Scale
                                            else -> Icons.Default.WaterDrop
                                        }
                                        LiquidGlassTopBar(
                                            title = topBarTitle,
                                            icon = topBarIcon,
                                            onOpenDrawer = {
                                                scope.launch { drawerState.open() }
                                            },
                                            onOpenSettings = {
                                                currentDestination = DrawerDestination.SETTINGS
                                            }
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
                                                        enableAnimations = uiState.enableAnimations,
                                                        hapticEnabled = uiState.hapticEnabled,
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
                                                        onSetCreditSalesQty = { salesViewModel.setCreditSalesQty(it) },
                                                        onSetRecoveredCredit = { salesViewModel.setRecoveredCredit(it) },
                                                        onSetPhysicalCash = { salesViewModel.setPhysicalCash(it) },
                                                        onSetExpense = { salesViewModel.setExpense(it) },
                                                        onSetUnitSalePrice = { salesViewModel.setUnitSalePrice(it) },
                                                        onSetCommissionPerUnit = { salesViewModel.setCommissionPerUnit(it) },
                                                        onSaveTransaction = { salesViewModel.saveTransaction() },
                                                        onClearInputs = { salesViewModel.clearInputs() },
                                                        onCopySummary = { copyToClipboard(it, "Sales reconciliation audit") },
                                                        onOpenHistorySheet = { salesViewModel.setSavedHistorySheetOpen(true) },
                                                        onCloseHistorySheet = { salesViewModel.setSavedHistorySheetOpen(false) },
                                                        onLoadTransaction = { salesViewModel.loadTransactionIntoForm(it) },
                                                        onDeleteTransaction = { salesViewModel.deleteTransaction(it) },
                                                        onExportCsv = {
                                                            try {
                                                                val csv = CsvExporter.exportSalesTransactionsToCsv(savedSalesTransactions)
                                                                val file = CsvExporter.saveCsvToCacheFile(context, "sales_reconciliation_audit.csv", csv)
                                                                val shareIntent = CsvExporter.createShareCsvIntent(context, file, "Sales Reconciliation Audit CSV")
                                                                context.startActivity(Intent.createChooser(shareIntent, "Share Sales Reconciliation CSV"))
                                                            } catch (e: Exception) {
                                                                scope.launch { snackbarHostState.showSnackbar("Export failed: ${e.message}") }
                                                            }
                                                        }
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
                                                else -> {
                                                    BasicCalculatorScreen(
                                                        expression = uiState.expression,
                                                        liveResult = uiState.liveResult,
                                                        isAllClear = uiState.expression.isEmpty(),
                                                        showDoubleZeroKey = uiState.showDoubleZeroKey,
                                                        enableAnimations = uiState.enableAnimations,
                                                        hapticEnabled = uiState.hapticEnabled,
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
                                    onCopyTape = { tapeText -> copyToClipboard(tapeText, "History tape") },
                                    onExportCsv = {
                                        try {
                                            val csv = CsvExporter.exportCalculationHistoryToCsv(historyList)
                                            val file = CsvExporter.saveCsvToCacheFile(context, "calculator_tape_history.csv", csv)
                                            val shareIntent = CsvExporter.createShareCsvIntent(context, file, "Calculation Tape History CSV")
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Tape History CSV"))
                                        } catch (e: Exception) {
                                            scope.launch { snackbarHostState.showSnackbar("Export failed: ${e.message}") }
                                        }
                                    }
                                )
                            }
                        }
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
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit
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
                onClick = onOpenSettings,
                modifier = Modifier.testTag("topbar_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Open Settings Screen",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}
