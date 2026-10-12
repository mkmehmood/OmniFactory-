package com.example.ui.screens

import com.example.ui.components.SoftAlertDialog
import com.example.ui.components.SoftButton
import com.example.ui.components.SoftIconButton
import com.example.ui.components.SoftRadioButton
import com.example.ui.components.softWell
import com.example.ui.components.SoftSurface
import com.example.ui.components.SoftCard
import com.example.ui.theme.SoftColors
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.ui.components.LiquidGlassTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import com.example.ui.components.straightDottedLine
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.components.BlinkingCursorPointer
import com.example.ui.components.InputFieldCopyPasteMenu
import com.example.data.db.SalesTransactionEntity
import com.example.ui.components.SalesRepresentativesMatrixCard
import com.example.ui.components.SimpleCalculatorDialog
import com.example.ui.components.SmartStationKeypad
import com.example.domain.sales.ReconciliationStatus
import com.example.domain.sales.SalesReconciliationResult
import com.example.domain.util.IndianNumberFormatter
import com.example.domain.util.PkrNumberFormatter
import com.example.ui.SalesUiState
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.LocalShapeStructure
import com.example.ui.theme.toButtonShape
import com.example.ui.theme.toInputShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    uiState: SalesUiState,
    savedTransactions: List<SalesTransactionEntity>,
    onSetSalesRep: (String) -> Unit,
    onAddCustomSalesRep: (String) -> Unit,
    onSetDateMillis: (Long) -> Unit,
    onSetTotalSoldQty: (String) -> Unit,
    onSetReturnedQty: (String) -> Unit,
    onSetExpiredQty: (String) -> Unit,
    onSetSharedQty: (String) -> Unit,
    onSetCreditSalesQty: (String) -> Unit,
    onSetRecoveredCredit: (String) -> Unit,
    onSetPhysicalCash: (String) -> Unit,
    onSetExpense: (String) -> Unit,
    onSetUnitSalePrice: (String) -> Unit,
    onSetCommissionPerUnit: (String) -> Unit,
    onSetCommissionPaid: (String) -> Unit,
    onShiftDeficitToCommissionPaid: () -> Unit,
    onSaveTransaction: () -> Unit,
    onClearInputs: () -> Unit,
    onCopySummary: (String) -> Unit,
    onOpenHistorySheet: () -> Unit,
    onCloseHistorySheet: () -> Unit,
    onLoadTransaction: (SalesTransactionEntity) -> Unit,
    onDeleteTransaction: (SalesTransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showCustomRepDialog by remember { mutableStateOf(false) }
    var showCalculatorDialog by remember { mutableStateOf(false) }

    var focusedFieldId by remember { mutableStateOf<String?>(null) }
    var focusedFieldLabel by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val isImeOpen = WindowInsets.ime.getBottom(density) > 0

    // Dismiss keypad automatically on scroll or when system keyboard for letters opens
    LaunchedEffect(scrollState.isScrollInProgress, isImeOpen) {
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
        when (focusedFieldId) {
            "total_sold" -> onSetTotalSoldQty(appendNumericToken(uiState.totalSoldQtyText, token))
            "returned" -> onSetReturnedQty(appendNumericToken(uiState.returnedQtyText, token))
            "expired" -> onSetExpiredQty(appendNumericToken(uiState.expiredQtyText, token))
            "shared" -> onSetSharedQty(appendNumericToken(uiState.sharedQtyText, token))
            "credit_sales" -> onSetCreditSalesQty(appendNumericToken(uiState.creditSalesQtyText, token))
            "unit_price" -> onSetUnitSalePrice(appendNumericToken(uiState.unitSalePriceText, token))
            "commission_rate" -> onSetCommissionPerUnit(appendNumericToken(uiState.commissionPerUnitText, token))
            "commission_paid" -> onSetCommissionPaid(appendNumericToken(uiState.commissionPaidText, token))
            "recovered_credit" -> onSetRecoveredCredit(appendNumericToken(uiState.recoveredCreditText, token))
            "expense" -> onSetExpense(appendNumericToken(uiState.expenseText, token))
            "physical_cash" -> onSetPhysicalCash(appendNumericToken(uiState.physicalCashText, token))
        }
    }

    val handleBackspace: () -> Unit = {
        when (focusedFieldId) {
            "total_sold" -> if (uiState.totalSoldQtyText.isNotEmpty()) onSetTotalSoldQty(uiState.totalSoldQtyText.dropLast(1))
            "returned" -> if (uiState.returnedQtyText.isNotEmpty()) onSetReturnedQty(uiState.returnedQtyText.dropLast(1))
            "expired" -> if (uiState.expiredQtyText.isNotEmpty()) onSetExpiredQty(uiState.expiredQtyText.dropLast(1))
            "shared" -> if (uiState.sharedQtyText.isNotEmpty()) onSetSharedQty(uiState.sharedQtyText.dropLast(1))
            "credit_sales" -> if (uiState.creditSalesQtyText.isNotEmpty()) onSetCreditSalesQty(uiState.creditSalesQtyText.dropLast(1))
            "unit_price" -> if (uiState.unitSalePriceText.isNotEmpty()) onSetUnitSalePrice(uiState.unitSalePriceText.dropLast(1))
            "commission_rate" -> if (uiState.commissionPerUnitText.isNotEmpty()) onSetCommissionPerUnit(uiState.commissionPerUnitText.dropLast(1))
            "commission_paid" -> if (uiState.commissionPaidText.isNotEmpty()) onSetCommissionPaid(uiState.commissionPaidText.dropLast(1))
            "recovered_credit" -> if (uiState.recoveredCreditText.isNotEmpty()) onSetRecoveredCredit(uiState.recoveredCreditText.dropLast(1))
            "expense" -> if (uiState.expenseText.isNotEmpty()) onSetExpense(uiState.expenseText.dropLast(1))
            "physical_cash" -> if (uiState.physicalCashText.isNotEmpty()) onSetPhysicalCash(uiState.physicalCashText.dropLast(1))
        }
    }

    val handleClear: () -> Unit = {
        when (focusedFieldId) {
            "total_sold" -> onSetTotalSoldQty("")
            "returned" -> onSetReturnedQty("")
            "expired" -> onSetExpiredQty("")
            "shared" -> onSetSharedQty("")
            "credit_sales" -> onSetCreditSalesQty("")
            "unit_price" -> onSetUnitSalePrice("")
            "commission_rate" -> onSetCommissionPerUnit("")
            "commission_paid" -> onSetCommissionPaid("")
            "recovered_credit" -> onSetRecoveredCredit("")
            "expense" -> onSetExpense("")
            "physical_cash" -> onSetPhysicalCash("")
        }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceBg = MaterialTheme.colorScheme.surface
    val cardBorderColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("sales_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = if (focusedFieldId != null) 300.dp else 24.dp)
        ) {
            // Header Bar
            SalesScreenHeader(
                onClearAll = onClearInputs,
                primaryColor = primaryColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Sales Representative & Date Card
            SalesRepAndDateCard(
                selectedRep = uiState.salesRep,
                availableReps = uiState.availableReps,
                dateMillis = uiState.dateMillis,
                onSelectRep = onSetSalesRep,
                onOpenCustomRepDialog = { showCustomRepDialog = true },
                onOpenDatePicker = { showDatePickerDialog = true },
                primaryColor = primaryColor,
                surfaceBg = surfaceBg,
                borderColor = cardBorderColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Input Fields Card
            SalesInputFormCard(
                totalSoldQty = uiState.totalSoldQtyText,
                returnedQty = uiState.returnedQtyText,
                expiredQty = uiState.expiredQtyText,
                sharedQty = uiState.sharedQtyText,
                creditSalesQty = uiState.creditSalesQtyText,
                recoveredCredit = uiState.recoveredCreditText,
                physicalCash = uiState.physicalCashText,
                expense = uiState.expenseText,
                unitSalePrice = uiState.unitSalePriceText,
                commissionPerUnit = uiState.commissionPerUnitText,
                commissionPaid = uiState.commissionPaidText,
                focusedFieldId = focusedFieldId,
                onFocusField = { id, label ->
                    if (focusedFieldId == id) {
                        focusedFieldId = null
                        focusedFieldLabel = null
                    } else {
                        focusedFieldId = id
                        focusedFieldLabel = label
                    }
                },
                onSetTotalSoldQty = onSetTotalSoldQty,
                onSetReturnedQty = onSetReturnedQty,
                onSetExpiredQty = onSetExpiredQty,
                onSetSharedQty = onSetSharedQty,
                onSetCreditSalesQty = onSetCreditSalesQty,
                onSetRecoveredCredit = onSetRecoveredCredit,
                onSetPhysicalCash = onSetPhysicalCash,
                onSetExpense = onSetExpense,
                onSetUnitSalePrice = onSetUnitSalePrice,
                onSetCommissionPerUnit = onSetCommissionPerUnit,
                onSetCommissionPaid = onSetCommissionPaid,
                onOpenCalculator = { showCalculatorDialog = true },
                primaryColor = primaryColor,
                surfaceBg = surfaceBg,
                borderColor = cardBorderColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Expected Cash & Settlement Result Panel
            SettlementResultPanel(
                uiState = uiState,
                onShiftDeficitToCommissionPaid = onShiftDeficitToCommissionPaid,
                onCopySummary = {
                    val summaryText = buildReconciliationSummaryText(uiState)
                    onCopySummary(summaryText)
                    Toast.makeText(context, "Settlement summary copied!", Toast.LENGTH_SHORT).show()
                },
                primaryColor = primaryColor,
                surfaceBg = surfaceBg,
                borderColor = cardBorderColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Save Record Button
            val buttonShape = LocalShapeStructure.current.toButtonShape()
            SoftButton(
                onClick = onSaveTransaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                shape = buttonShape, accentColor = primaryColor, filled = true) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Settlement Record",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Dynamic Sales Representatives Matrix & Financial Audit
            SalesRepresentativesMatrixCard(
                transactions = savedTransactions,
                onSelectRep = onSetSalesRep,
                onLoadTransaction = onLoadTransaction,
                primaryColor = primaryColor,
                surfaceBg = surfaceBg,
                borderColor = cardBorderColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(24.dp))
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

    // Quick Calculator Popup Dialog
    if (showCalculatorDialog) {
        SimpleCalculatorDialog(
            onDismiss = { showCalculatorDialog = false },
            onResultSelected = { calcResult ->
                // Dialog can be closed or value copied
            }
        )
    }

    // Date Picker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                SoftButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onSetDateMillis(it)
                        }
                        showDatePickerDialog = false
                    },
                    modifier = Modifier.testTag("date_picker_confirm_button")
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                SoftButton(
                    onClick = { showDatePickerDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Custom Representative Dialog
    if (showCustomRepDialog) {
        var customRepInput by remember { mutableStateOf("") }
        SoftAlertDialog(
            onDismissRequest = { showCustomRepDialog = false },
            title = { Text("Add Representative", fontWeight = FontWeight.Bold) },
            text = {
                LiquidGlassTextField(
                    value = customRepInput,
                    onValueChange = { customRepInput = it },
                    label = { Text("Representative Name") },
                    placeholder = { Text("e.g. Rahul Sharma") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_rep_input")
                )
            },
            confirmButton = {
                SoftButton(
                    onClick = {
                        if (customRepInput.isNotBlank()) {
                            onAddCustomSalesRep(customRepInput)
                            showCustomRepDialog = false
                        }
                    },
                    modifier = Modifier.testTag("custom_rep_confirm_button"), accentColor = MaterialTheme.colorScheme.primary, filled = true) {
                    Text("Add")
                }
            },
            dismissButton = {
                SoftButton(onClick = { showCustomRepDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Saved Transactions Bottom Sheet
    if (uiState.showSavedHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = onCloseHistorySheet,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            SavedTransactionsBottomSheet(
                transactions = savedTransactions,
                onLoadTransaction = onLoadTransaction,
                onDeleteTransaction = onDeleteTransaction,
                onClose = onCloseHistorySheet,
                primaryColor = primaryColor,
                isDark = isDark
            )
        }
    }
}

@Composable
private fun SalesScreenHeader(
    onClearAll: () -> Unit,
    primaryColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Sales Settlement",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Representative Cash Settlement",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SoftSurface(
            modifier = Modifier
                .clickable { onClearAll() }
                .testTag("clear_sales_inputs_button"),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset Form",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Reset",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SalesRepAndDateCard(
    selectedRep: String,
    availableReps: List<String>,
    dateMillis: Long,
    onSelectRep: (String) -> Unit,
    onOpenCustomRepDialog: () -> Unit,
    onOpenDatePicker: () -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US) }
    val formattedDate = remember(dateMillis) { dateFormat.format(Date(dateMillis)) }

    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "REPRESENTATIVE & DATE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sales Rep Selector Box (Frameless with straight dotted line)
                Box(modifier = Modifier.weight(1.2f)) {
                    SoftSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable { dropdownExpanded = true }
                            .testTag("sales_rep_dropdown_selector"),
                        shape = RectangleShape,
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedRep.isBlank()) "Select Rep..." else selectedRep,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedRep.isBlank()) FontWeight.Normal else FontWeight.SemiBold,
                                    color = if (selectedRep.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.testTag("sales_rep_dropdown_menu")
                    ) {
                        availableReps.forEach { rep ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = rep,
                                        fontWeight = if (rep == selectedRep) FontWeight.Bold else FontWeight.Normal,
                                        color = if (rep == selectedRep) primaryColor else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onSelectRep(rep)
                                    dropdownExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "+ Add Representative...",
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            },
                            onClick = {
                                dropdownExpanded = false
                                onOpenCustomRepDialog()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                // Date Selector Box (Frameless with straight dotted line)
                SoftSurface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { onOpenDatePicker() }
                        .testTag("sales_date_picker_button"),
                    shape = RectangleShape,
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedDate,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SalesInputFormCard(
    totalSoldQty: String,
    returnedQty: String,
    expiredQty: String,
    sharedQty: String,
    creditSalesQty: String,
    recoveredCredit: String,
    physicalCash: String,
    expense: String,
    unitSalePrice: String,
    commissionPerUnit: String,
    commissionPaid: String,
    focusedFieldId: String?,
    onFocusField: (id: String, label: String) -> Unit,
    onSetTotalSoldQty: (String) -> Unit,
    onSetReturnedQty: (String) -> Unit,
    onSetExpiredQty: (String) -> Unit,
    onSetSharedQty: (String) -> Unit,
    onSetCreditSalesQty: (String) -> Unit,
    onSetRecoveredCredit: (String) -> Unit,
    onSetPhysicalCash: (String) -> Unit,
    onSetExpense: (String) -> Unit,
    onSetUnitSalePrice: (String) -> Unit,
    onSetCommissionPerUnit: (String) -> Unit,
    onSetCommissionPaid: (String) -> Unit,
    onOpenCalculator: () -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean
) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRODUCT QUANTITIES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
                SoftSurface(
                    shape = RectangleShape,
                    color = Color.Transparent,
                    modifier = Modifier
                        .clickable { onOpenCalculator() }
                        .testTag("sales_form_calc_button")
                ) {
                    Box(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Quick Calculator",
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Row 1: Total Sold & Returned
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NumericGridInputField(
                    label = "Total Sold",
                    placeholder = "0",
                    value = totalSoldQty,
                    onValueChange = onSetTotalSoldQty,
                    isSelected = focusedFieldId == "total_sold",
                    onFocus = { onFocusField("total_sold", "Total Sold Qty") },
                    testTag = "total_sold_qty_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                NumericGridInputField(
                    label = "Returned",
                    placeholder = "0",
                    value = returnedQty,
                    onValueChange = onSetReturnedQty,
                    isSelected = focusedFieldId == "returned",
                    onFocus = { onFocusField("returned", "Returned Qty") },
                    testTag = "returned_qty_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Expired / Damage & Shared (Deducts from net sold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NumericGridInputField(
                    label = "Expired / Damage",
                    placeholder = "0",
                    value = expiredQty,
                    onValueChange = onSetExpiredQty,
                    isSelected = focusedFieldId == "expired",
                    onFocus = { onFocusField("expired", "Expired / Damage Qty") },
                    testTag = "expired_qty_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                NumericGridInputField(
                    label = "Shared (Deduction)",
                    placeholder = "0",
                    value = sharedQty,
                    onValueChange = onSetSharedQty,
                    isSelected = focusedFieldId == "shared",
                    onFocus = { onFocusField("shared", "Shared Qty") },
                    testTag = "shared_qty_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Credit Sales Qty
            NumericGridInputField(
                label = "Credit Sales Qty",
                placeholder = "0",
                value = creditSalesQty,
                onValueChange = onSetCreditSalesQty,
                isSelected = focusedFieldId == "credit_sales",
                onFocus = { onFocusField("credit_sales", "Credit Sales Qty") },
                testTag = "credit_sales_qty_input",
                primaryColor = primaryColor,
                isDark = isDark,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Section 2: Rate & Commission
            Text(
                text = "PRICING & COMMISSION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NumericGridInputField(
                    label = "Unit Sale Price",
                    placeholder = "0",
                    value = unitSalePrice,
                    onValueChange = onSetUnitSalePrice,
                    isSelected = focusedFieldId == "unit_price",
                    onFocus = { onFocusField("unit_price", "Unit Sale Price") },
                    testTag = "unit_sale_price_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                NumericGridInputField(
                    label = "Commission / Unit",
                    placeholder = "0",
                    value = commissionPerUnit,
                    onValueChange = onSetCommissionPerUnit,
                    isSelected = focusedFieldId == "commission_rate",
                    onFocus = { onFocusField("commission_rate", "Commission / Unit") },
                    testTag = "commission_per_unit_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Commission Paid (Deducted directly from expected cash in hand)
            NumericGridInputField(
                label = "Commission Paid (Deducted from Cash)",
                placeholder = "0",
                value = commissionPaid,
                onValueChange = onSetCommissionPaid,
                isSelected = focusedFieldId == "commission_paid",
                onFocus = { onFocusField("commission_paid", "Commission Paid (Deducted from Cash)") },
                testTag = "commission_paid_input",
                primaryColor = primaryColor,
                isDark = isDark,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "• Paid commission deducts directly from expected cash in hand to be remitted",
                fontSize = 10.sp,
                fontFamily = DigitalMonospaceFont,
                color = SoftColors.warning
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Section 3: Recovery, Expenses & Physical Cash
            Text(
                text = "COLLECTIONS & HANDOVER CASH",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NumericGridInputField(
                    label = "Recovered Credit",
                    placeholder = "0",
                    value = recoveredCredit,
                    onValueChange = onSetRecoveredCredit,
                    isSelected = focusedFieldId == "recovered_credit",
                    onFocus = { onFocusField("recovered_credit", "Recovered Credit") },
                    testTag = "recovered_credit_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                NumericGridInputField(
                    label = "Field Expenses",
                    placeholder = "0",
                    value = expense,
                    onValueChange = onSetExpense,
                    isSelected = focusedFieldId == "expense",
                    onFocus = { onFocusField("expense", "Field Expenses") },
                    testTag = "expense_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            NumericGridInputField(
                label = "Physical Cash Received from Rep",
                placeholder = "0",
                value = physicalCash,
                onValueChange = onSetPhysicalCash,
                isSelected = focusedFieldId == "physical_cash",
                onFocus = { onFocusField("physical_cash", "Physical Cash Received") },
                testTag = "physical_cash_input",
                primaryColor = primaryColor,
                isDark = isDark,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NumericGridInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    isSelected: Boolean = false,
    onFocus: () -> Unit = {},
    testTag: String,
    primaryColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val inputShape = LocalShapeStructure.current.toInputShape()
    var showContextMenu by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
    ) {
        LiquidGlassTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = true,
            label = {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            placeholder = { Text(placeholder, fontSize = 12.sp) },
            suffix = {
                if (isSelected) {
                    BlinkingCursorPointer(color = primaryColor)
                }
            },
            singleLine = true,
            forceFocused = isSelected,
            accentColor = primaryColor,
            shape = inputShape,
            modifier = Modifier.fillMaxWidth().testTag(testTag),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                fontFamily = DigitalMonospaceFont,
                fontWeight = FontWeight.SemiBold
            )
        )

        // Clickable & Long-press overlay over the input: tap opens keypad, long-press opens Copy/Paste
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onFocus()
                        },
                        onLongPress = { showContextMenu = true }
                    )
                }
        )

        InputFieldCopyPasteMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            value = value,
            onPaste = { onValueChange(it) }
        )
    }
}

@Composable
private fun SettlementResultPanel(
    uiState: SalesUiState,
    onShiftDeficitToCommissionPaid: () -> Unit,
    onCopySummary: () -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean
) {
    val result = uiState.reconciliationResult

    SoftCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calculated_reconciliation_result_panel"),
        shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SETTLEMENT AUDIT & CASH FLOW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )

                SoftIconButton(
                    onClick = onCopySummary,
                    modifier = Modifier.size(28.dp).testTag("copy_reconciliation_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Summary",
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Deficit Warning Indicator & Shift Deficit Radio Button
            if (result.deficitAmount > 0.0) {
                val alertBg = SoftColors.negative.copy(alpha = 0.12f)
                val alertBorder = SoftColors.negative.copy(alpha = 0.75f)
                val alertFg = SoftColors.negative
                val alertIcon = Icons.Default.TrendingDown

                SoftSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deficit_warning_indicator_banner"),
                    shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .softWell(CircleShape, tint = alertFg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = alertIcon,
                                    contentDescription = "Deficit Warning",
                                    tint = alertFg,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CASH DEFICIT DETECTED",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = alertFg
                                )
                                Text(
                                    text = "Shortfall: ${result.formattedDeficitAmount} (Physical Cash is less than Net Expected Cash)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = DigitalMonospaceFont
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Radio button to shift deficit into commission paid
                        SoftSurface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onShiftDeficitToCommissionPaid() }
                                .testTag("shift_deficit_radio_button_container"),
                            shape = RoundedCornerShape(10.dp),
                            color = SoftColors.positive.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SoftRadioButton(
                                    selected = false,
                                    onClick = { onShiftDeficitToCommissionPaid() },
                                    modifier = Modifier.testTag("shift_deficit_radio_button")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Shift deficit into commission paid",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftColors.positive,
                                        fontFamily = DigitalMonospaceFont
                                    )
                                    Text(
                                        text = "Eliminates ${result.formattedDeficitAmount} deficit and transfers that money to ${uiState.salesRep.ifBlank { "Sales Representative" }} as Commission Paid",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontFamily = DigitalMonospaceFont
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Hero Net Expected Cash Remittance Card with Exact Cash in Hand Breakdown
            SoftSurface(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = primaryColor.copy(alpha = 0.10f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NET EXPECTED CASH IN HAND",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = primaryColor
                        )
                        SoftSurface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (result.commissionPaid > 0) SoftColors.warning.copy(alpha = 0.2f) else primaryColor.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = if (result.commissionPaid > 0) "After Comm. Deduction" else "No Comm. Deducted",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (result.commissionPaid > 0) SoftColors.warning else primaryColor,
                                fontFamily = DigitalMonospaceFont,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = result.formattedExpectedCash,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("expected_cash_display")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dedicated EXACT CASH IN HAND AUDIT BOX (Without Commission Deduction)
                    SoftSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "1. Exact Cash in Hand (No Comm. Deduction):",
                                    fontSize = 10.5.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = result.formattedExactCashBeforeCommission,
                                    fontSize = 11.5.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftColors.sky
                                )
                            }
                            Text(
                                text = "   = Cash Sales (${result.formattedCashFromSales}) + Recovery (${result.formattedRecoveredCredit}) − Expenses (${result.formattedExpense})",
                                fontSize = 9.5.sp,
                                fontFamily = DigitalMonospaceFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (result.commissionPaid > 0.0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "2. Less Paid Commission (Deducted):",
                                        fontSize = 10.5.sp,
                                        fontFamily = DigitalMonospaceFont,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SoftColors.warning
                                    )
                                    Text(
                                        text = "− ${result.formattedCommissionPaid}",
                                        fontSize = 11.5.sp,
                                        fontFamily = DigitalMonospaceFont,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftColors.warning
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "3. Net Expected Cash Handover:",
                                        fontSize = 10.5.sp,
                                        fontFamily = DigitalMonospaceFont,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                    Text(
                                        text = "= ${result.formattedExpectedCash}",
                                        fontSize = 12.sp,
                                        fontFamily = DigitalMonospaceFont,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                }
                            }
                        }
                    }

                    if (result.sharedQty > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Shared Deductions: ${result.formattedSharedQty} units deducted from gross sold",
                            fontSize = 10.sp,
                            color = primaryColor,
                            fontFamily = DigitalMonospaceFont,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Commission Payable & Paid Dedicated Audit Panel
            SoftSurface(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SoftColors.warning.copy(alpha = 0.08f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = "COMMISSION ACCOUNTING (EARNED, PAID & PAYABLE)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = SoftColors.warning,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CommissionMiniBox(
                            label = "Gross Earned",
                            value = result.formattedGrossCommission,
                            color = SoftColors.sky,
                            modifier = Modifier.weight(1f)
                        )
                        CommissionMiniBox(
                            label = "Paid (Deducted)",
                            value = result.formattedCommissionPaid,
                            color = SoftColors.warning,
                            modifier = Modifier.weight(1.1f)
                        )
                        CommissionMiniBox(
                            label = "Net Payable",
                            value = result.formattedCommissionPayable,
                            color = SoftColors.positive,
                            modifier = Modifier.weight(1.1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Settlement Status Banner
            SettlementStatusBadge(
                status = result.status,
                discrepancyFormatted = result.formattedDiscrepancy,
                physicalCashFormatted = result.formattedPhysicalCash,
                expectedCashFormatted = result.formattedExpectedCash,
                exactCashFormatted = result.formattedExactCashBeforeCommission,
                commissionPaidFormatted = result.formattedCommissionPaid,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Key Financial Stats Grid - Row 1: Units & Quantities
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SummaryStatBox(
                    label = "Net Units",
                    value = result.formattedNetSold,
                    tintColor = SoftColors.sky,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_net_sold_units"
                )

                SummaryStatBox(
                    label = "Cash Qty",
                    value = result.formattedCashQty,
                    tintColor = SoftColors.positive,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_cash_qty_units"
                )

                SummaryStatBox(
                    label = "Gross Comm",
                    value = result.formattedGrossCommission,
                    tintColor = SoftColors.violet,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_gross_commission"
                )

                SummaryStatBox(
                    label = "Comm Payable",
                    value = result.formattedCommissionPayable,
                    tintColor = SoftColors.warning,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_commission_rate"
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Key Financial Stats Grid - Row 2: Cash in Hand Audit (Exact vs Deduction vs Net Expected)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SummaryStatBox(
                    label = "Exact Cash (No Comm)",
                    value = result.formattedExactCashBeforeCommission,
                    tintColor = SoftColors.sky,
                    modifier = Modifier.weight(1.2f),
                    testTag = "stat_exact_cash_before_comm"
                )

                SummaryStatBox(
                    label = "Comm Paid",
                    value = if (result.commissionPaid > 0) "−${result.formattedCommissionPaid}" else result.formattedCommissionPaid,
                    tintColor = SoftColors.warning,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_commission_paid_deduction"
                )

                SummaryStatBox(
                    label = "Net Expected",
                    value = result.formattedExpectedCash,
                    tintColor = SoftColors.positive,
                    modifier = Modifier.weight(1.1f),
                    testTag = "stat_net_expected_cash"
                )

                SummaryStatBox(
                    label = "Cash Handover",
                    value = result.formattedPhysicalCash,
                    tintColor = primaryColor,
                    modifier = Modifier.weight(1.1f),
                    testTag = "stat_physical_cash"
                )
            }
        }
    }
}

@Composable
private fun CommissionMiniBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    SoftSurface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SettlementStatusBadge(
    status: ReconciliationStatus,
    discrepancyFormatted: String,
    physicalCashFormatted: String,
    expectedCashFormatted: String,
    exactCashFormatted: String,
    commissionPaidFormatted: String,
    isDark: Boolean
) {
    val (badgeBgColor, badgeBorderColor, contentColor, statusTitle, statusSubtitle, statusIcon) = when (status) {
        ReconciliationStatus.BALANCED -> {
            val sub = if (commissionPaidFormatted != "0.00" && commissionPaidFormatted != "0") {
                "Cash handover ($physicalCashFormatted) matches Net Expected Cash ($expectedCashFormatted) after $commissionPaidFormatted comm deduction (Exact Cash: $exactCashFormatted)"
            } else {
                "Cash received ($physicalCashFormatted) matches exact cash in hand ($expectedCashFormatted)"
            }
            Tuple6(
                SoftColors.positive.copy(alpha = 0.14f),
                SoftColors.positive.copy(alpha = 0.45f),
                SoftColors.positive,
                "SETTLED (BALANCED)",
                sub,
                Icons.Default.CheckCircle
            )
        }
        ReconciliationStatus.OVER -> {
            Tuple6(
                SoftColors.sky.copy(alpha = 0.14f),
                SoftColors.sky.copy(alpha = 0.45f),
                SoftColors.sky,
                "SURPLUS (+)",
                "Cash received exceeds net expected cash by $discrepancyFormatted (Exact Cash without comm: $exactCashFormatted)",
                Icons.Default.TrendingUp
            )
        }
        ReconciliationStatus.SHORT -> {
            Tuple6(
                SoftColors.negative.copy(alpha = 0.14f),
                SoftColors.negative.copy(alpha = 0.45f),
                SoftColors.negative,
                "SHORTAGE (−)",
                "Cash received is short of net expected cash by $discrepancyFormatted (Exact Cash without comm: $exactCashFormatted)",
                Icons.Default.TrendingDown
            )
        }
    }

    SoftSurface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reconciliation_status_badge"),
        shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .softWell(CircleShape, tint = contentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = statusTitle,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = contentColor
                )
                Text(
                    text = statusSubtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SummaryStatBox(
    label: String,
    value: String,
    tintColor: Color,
    modifier: Modifier = Modifier,
    testTag: String
) {
    SoftSurface(
        modifier = modifier
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        color = tintColor.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = tintColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SavedTransactionsBottomSheet(
    transactions: List<SalesTransactionEntity>,
    onLoadTransaction: (SalesTransactionEntity) -> Unit,
    onDeleteTransaction: (SalesTransactionEntity) -> Unit,
    onClose: () -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("saved_transactions_sheet")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Saved Records (${transactions.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont
                )
            }

            SoftButton(onClick = onClose) {
                Text("Close")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (transactions.isNotEmpty()) {
            val totalExactCash = transactions.sumOf { it.exactCashBeforeCommission }
            val totalCommPaid = transactions.sumOf { it.commissionPaid }
            val totalExpected = transactions.sumOf { it.expectedCash }
            val totalPhysical = transactions.sumOf { it.physicalCashInHand }

            SoftSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "SAVED RECORDS CASH IN HAND AUDIT SUMMARY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GridKeyValueBox(
                            label = "Exact Cash (No Comm)",
                            value = PkrNumberFormatter.formatPkrAmount(totalExactCash, decimals = 2),
                            highlightColor = SoftColors.sky,
                            isDark = isDark,
                            modifier = Modifier.weight(1.2f)
                        )
                        GridKeyValueBox(
                            label = "Total Comm Paid",
                            value = "−${PkrNumberFormatter.formatPkrAmount(totalCommPaid, decimals = 2)}",
                            highlightColor = SoftColors.warning,
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )
                        GridKeyValueBox(
                            label = "Net Expected",
                            value = PkrNumberFormatter.formatPkrAmount(totalExpected, decimals = 2),
                            highlightColor = SoftColors.positive,
                            isDark = isDark,
                            modifier = Modifier.weight(1.1f)
                        )
                        GridKeyValueBox(
                            label = "Cash Handover",
                            value = PkrNumberFormatter.formatPkrAmount(totalPhysical, decimals = 2),
                            highlightColor = primaryColor,
                            isDark = isDark,
                            modifier = Modifier.weight(1.1f)
                        )
                    }
                }
            }
        }

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No saved settlement records",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(transactions.size, key = { transactions[it].id }) { index ->
                    val tx = transactions[index]
                    SalesRecordCard(
                        tx = tx,
                        dateFormat = dateFormat,
                        onLoad = { onLoadTransaction(tx) },
                        onDelete = { onDeleteTransaction(tx) },
                        primaryColor = primaryColor,
                        isDark = isDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun SalesRecordCard(
    tx: SalesTransactionEntity,
    dateFormat: SimpleDateFormat,
    onLoad: () -> Unit,
    onDelete: () -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isExpanded by remember { mutableStateOf(false) }
    val discrepancy = tx.physicalCashInHand - tx.expectedCash

    val (statusBg, statusFg, statusText, statusIcon) = when {
        tx.status == "BALANCED" || kotlin.math.abs(discrepancy) < 0.005 -> {
            Tuple4(
                SoftColors.positive,
                Color.White,
                "BALANCED",
                Icons.Default.CheckCircle
            )
        }
        tx.status == "OVER" || discrepancy > 0.005 -> {
            Tuple4(
                SoftColors.sky,
                Color.White,
                "SURPLUS +${PkrNumberFormatter.formatPkrAmount(discrepancy, decimals = 2)}",
                Icons.Default.TrendingUp
            )
        }
        else -> {
            Tuple4(
                SoftColors.negative,
                Color.White,
                "SHORTAGE −${PkrNumberFormatter.formatPkrAmount(kotlin.math.abs(discrepancy), decimals = 2)}",
                Icons.Default.TrendingDown
            )
        }
    }

    SoftCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sales_record_card_${tx.id}"),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Rep Name & Audit Date
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
                    SoftSurface(
                        shape = RoundedCornerShape(6.dp),
                        color = primaryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = tx.salesRep,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = dateFormat.format(Date(tx.dateMillis)),
                        fontSize = 11.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SoftIconButton(
                        onClick = {
                            val auditReport = buildSingleTransactionAuditText(tx)
                            clipboardManager.setText(AnnotatedString(auditReport))
                            Toast.makeText(context, "Audit copied for ${tx.salesRep}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Audit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    SoftIconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp).testTag("delete_transaction_button_${tx.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // PRIMARY 11 INPUT FIELDS BREAKDOWN
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. QUANTITIES (5 Inputs: Total Sold, Returned, Expired, Shared, Credit Sales)
                Text(
                    text = "QUANTITIES (5 INPUTS)",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GridKeyValueBox(
                        label = "Total Sold",
                        value = IndianNumberFormatter.formatIndianDouble(tx.totalSoldQty, 0),
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    GridKeyValueBox(
                        label = "Returned",
                        value = IndianNumberFormatter.formatIndianDouble(tx.returnedQty, 0),
                        highlightColor = if (tx.returnedQty > 0) SoftColors.warning else null,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    GridKeyValueBox(
                        label = "Expired",
                        value = IndianNumberFormatter.formatIndianDouble(tx.expiredQty, 0),
                        highlightColor = if (tx.expiredQty > 0) SoftColors.negative else null,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    GridKeyValueBox(
                        label = "Shared",
                        value = IndianNumberFormatter.formatIndianDouble(tx.sharedQty, 0),
                        highlightColor = if (tx.sharedQty > 0) SoftColors.violet else null,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    GridKeyValueBox(
                        label = "Credit Qty",
                        value = IndianNumberFormatter.formatIndianDouble(tx.creditSalesQty, 0),
                        highlightColor = if (tx.creditSalesQty > 0) SoftColors.sky else null,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 2. PRICING & COMMISSION (3 Inputs: Unit Price, Comm/Unit, Comm Paid)
                Text(
                    text = "PRICING & COMMISSION (3 INPUTS)",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GridKeyValueBox(
                        label = "Unit Price",
                        value = PkrNumberFormatter.formatPkrAmount(tx.unitSalePrice, decimals = 2),
                        isDark = isDark,
                        modifier = Modifier.weight(1.1f)
                    )
                    GridKeyValueBox(
                        label = "Comm / Unit",
                        value = PkrNumberFormatter.formatPkrAmount(tx.commissionPerUnit, decimals = 2),
                        isDark = isDark,
                        modifier = Modifier.weight(1.1f)
                    )
                    GridKeyValueBox(
                        label = "Comm Paid",
                        value = PkrNumberFormatter.formatPkrAmount(tx.commissionPaid, decimals = 2),
                        highlightColor = if (tx.commissionPaid > 0) SoftColors.warning else null,
                        isDark = isDark,
                        modifier = Modifier.weight(1.1f)
                    )
                    GridKeyValueBox(
                        label = "Net Comm",
                        value = PkrNumberFormatter.formatPkrAmount(tx.totalCommission, decimals = 2),
                        highlightColor = SoftColors.positive,
                        isDark = isDark,
                        modifier = Modifier.weight(1.1f)
                    )
                }

                // 3. CASH IN HAND & COMMISSION DEDUCTION AUDIT
                Text(
                    text = "CASH IN HAND & COMMISSION AUDIT",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GridKeyValueBox(
                        label = "Recovered Cr",
                        value = PkrNumberFormatter.formatPkrAmount(tx.recoveredCredit, decimals = 2),
                        highlightColor = if (tx.recoveredCredit > 0) SoftColors.positive else null,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    GridKeyValueBox(
                        label = "Field Exp",
                        value = PkrNumberFormatter.formatPkrAmount(tx.expense, decimals = 2),
                        highlightColor = if (tx.expense > 0) SoftColors.negative else null,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    GridKeyValueBox(
                        label = "Exact Cash (No Comm)",
                        value = PkrNumberFormatter.formatPkrAmount(tx.exactCashBeforeCommission, decimals = 2),
                        highlightColor = SoftColors.sky,
                        isDark = isDark,
                        modifier = Modifier.weight(1.2f)
                    )
                    GridKeyValueBox(
                        label = "Comm Paid",
                        value = if (tx.commissionPaid > 0) "−${PkrNumberFormatter.formatPkrAmount(tx.commissionPaid, decimals = 2)}" else "0.00",
                        highlightColor = if (tx.commissionPaid > 0) SoftColors.warning else null,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GridKeyValueBox(
                        label = "Net Expected Cash",
                        value = PkrNumberFormatter.formatPkrAmount(tx.expectedCash, decimals = 2),
                        highlightColor = SoftColors.positive,
                        isDark = isDark,
                        modifier = Modifier.weight(1.2f)
                    )
                    GridKeyValueBox(
                        label = "Cash Handover",
                        value = PkrNumberFormatter.formatPkrAmount(tx.physicalCashInHand, decimals = 2),
                        highlightColor = primaryColor,
                        isDark = isDark,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                if (tx.commissionPaid > 0.0) {
                    SoftSurface(
                        shape = RoundedCornerShape(6.dp),
                        color = SoftColors.warning.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "• Paid Comm of ${PkrNumberFormatter.formatPkrAmount(tx.commissionPaid, decimals = 2)} deducted from Cash in Hand. Exact cash before comm was ${PkrNumberFormatter.formatPkrAmount(tx.exactCashBeforeCommission, decimals = 2)}.",
                            fontSize = 9.5.sp,
                            fontFamily = DigitalMonospaceFont,
                            color = SoftColors.warning,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action bar & Load into form button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Net Units: ${IndianNumberFormatter.formatIndianDouble(tx.netSoldUnits, 0)} • Cash Qty: ${IndianNumberFormatter.formatIndianDouble(tx.cashQtyUnits, 0)}",
                    fontSize = 10.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SoftButton(
                    onClick = onLoad,
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Load into Form",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            // Status Banner
            SoftSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("status_banner_${tx.id}"),
                color = statusBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusFg,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = statusFg,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun buildSingleTransactionAuditText(tx: SalesTransactionEntity): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    return buildString {
        appendLine("=== SALES RECONCILIATION AUDIT ===")
        appendLine("Representative: ${tx.salesRep}")
        appendLine("Audit Date: ${dateFormat.format(Date(tx.dateMillis))}")
        appendLine("Status: ${tx.status}")
        appendLine("----------------------------------")
        appendLine("ALL 11 INPUT FIELDS:")
        appendLine("1. Total Sold Qty: ${IndianNumberFormatter.formatIndianDouble(tx.totalSoldQty, 0)}")
        appendLine("2. Returned Qty: ${IndianNumberFormatter.formatIndianDouble(tx.returnedQty, 0)}")
        appendLine("3. Expired Qty: ${IndianNumberFormatter.formatIndianDouble(tx.expiredQty, 0)}")
        appendLine("4. Shared Qty: ${IndianNumberFormatter.formatIndianDouble(tx.sharedQty, 0)}")
        appendLine("5. Credit Sales Qty: ${IndianNumberFormatter.formatIndianDouble(tx.creditSalesQty, 0)}")
        appendLine("6. Unit Sale Price: ${PkrNumberFormatter.formatPkrAmount(tx.unitSalePrice, decimals = 2)}")
        appendLine("7. Commission / Unit: ${PkrNumberFormatter.formatPkrAmount(tx.commissionPerUnit, decimals = 2)}")
        appendLine("8. Commission Paid: ${PkrNumberFormatter.formatPkrAmount(tx.commissionPaid, decimals = 2)}")
        appendLine("9. Recovered Credit: ${PkrNumberFormatter.formatPkrAmount(tx.recoveredCredit, decimals = 2)}")
        appendLine("10. Field Expenses: ${PkrNumberFormatter.formatPkrAmount(tx.expense, decimals = 2)}")
        appendLine("11. Physical Cash Handover: ${PkrNumberFormatter.formatPkrAmount(tx.physicalCashInHand, decimals = 2)}")
        appendLine("----------------------------------")
        appendLine("AUDIT CALCULATIONS:")
        appendLine("• Net Sold Units: ${PkrNumberFormatter.formatPkrDouble(tx.netSoldUnits, decimals = 0)}")
        appendLine("• Cash Qty Units: ${PkrNumberFormatter.formatPkrDouble(tx.cashQtyUnits, decimals = 0)}")
        appendLine("• Cash Sales Revenue: ${PkrNumberFormatter.formatPkrAmount(tx.cashQtyUnits * tx.unitSalePrice, decimals = 2)}")
        appendLine("• Gross Commission: ${PkrNumberFormatter.formatPkrAmount(tx.grossCommission, decimals = 2)}")
        appendLine("• Net Commission Payable: ${PkrNumberFormatter.formatPkrAmount(tx.totalCommission, decimals = 2)}")
        appendLine("----------------------------------")
        appendLine("CASH IN HAND & COMMISSION AUDIT:")
        appendLine("• Exact Cash in Hand (WITHOUT Commission Deduction): ${PkrNumberFormatter.formatPkrAmount(tx.exactCashBeforeCommission, decimals = 2)}")
        appendLine("  [Formula: Cash Sales (${PkrNumberFormatter.formatPkrAmount(tx.cashQtyUnits * tx.unitSalePrice, decimals = 2)}) + Recovered Credit (${PkrNumberFormatter.formatPkrAmount(tx.recoveredCredit, decimals = 2)}) − Expenses (${PkrNumberFormatter.formatPkrAmount(tx.expense, decimals = 2)})]")
        appendLine("• Commission Paid (Deducted from Cash): −${PkrNumberFormatter.formatPkrAmount(tx.commissionPaid, decimals = 2)}")
        appendLine("• Net Expected Cash in Hand (AFTER Commission Deduction): ${PkrNumberFormatter.formatPkrAmount(tx.expectedCash, decimals = 2)}")
        appendLine("• Physical Cash Handover: ${PkrNumberFormatter.formatPkrAmount(tx.physicalCashInHand, decimals = 2)}")
        appendLine("• Actual Cash Collected (Before Commission Payout): ${PkrNumberFormatter.formatPkrAmount(tx.actualCashBeforeCommission, decimals = 2)}")
        val discrepancy = tx.physicalCashInHand - tx.expectedCash
        val discStr = if (kotlin.math.abs(discrepancy) < 0.01) "0.00 (Balanced)"
        else if (discrepancy > 0) "+${PkrNumberFormatter.formatPkrAmount(discrepancy, decimals = 2)} (Surplus)"
        else "-${PkrNumberFormatter.formatPkrAmount(kotlin.math.abs(discrepancy), decimals = 2)} (Shortage)"
        appendLine("• Variance / Discrepancy: $discStr")
        appendLine("==================================")
    }
}

@Composable
private fun GridKeyValueBox(
    label: String,
    value: String,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    highlightColor: Color? = null
) {
    SoftSurface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)) {
        Column(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = highlightColor ?: MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun buildReconciliationSummaryText(uiState: SalesUiState): String {
    val res = uiState.reconciliationResult
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val dateStr = dateFormat.format(Date(uiState.dateMillis))

    return buildString {
        appendLine("=== SALES SETTLEMENT AUDIT ===")
        appendLine("Representative: ${uiState.salesRep}")
        appendLine("Date: $dateStr")
        appendLine("Status: ${res.status.name}")
        appendLine("\n--- QUANTITIES ---")
        appendLine("• Total Sold: ${uiState.totalSoldQtyText}")
        appendLine("• Returned: ${uiState.returnedQtyText}")
        appendLine("• Expired: ${uiState.expiredQtyText}")
        appendLine("• Shared: ${uiState.sharedQtyText}")
        appendLine("• Credit Sales: ${uiState.creditSalesQtyText}")
        appendLine("• Net Sold Units: ${res.formattedNetSold}")
        appendLine("• Cash Qty Units: ${res.formattedCashQty}")
        appendLine("\n--- COMMISSION ACCOUNTING ---")
        appendLine("• Unit Sale Price: ${res.formattedUnitPrice}")
        appendLine("• Commission / Unit: ${res.formattedCommissionPerUnit}")
        appendLine("• Gross Commission: ${res.formattedGrossCommission}")
        appendLine("• Commission Paid (Advance): ${res.formattedCommissionPaid}")
        appendLine("• Commission Payable (Net): ${res.formattedCommissionPayable}")
        appendLine("\n--- CASH IN HAND & COMMISSION AUDIT ---")
        appendLine("• Exact Cash in Hand (WITHOUT Commission Deduction): ${res.formattedExactCashBeforeCommission}")
        appendLine("  [Formula: Product Sales (${res.formattedCashFromSales}) + Recovered Credit (${res.formattedRecoveredCredit}) − Field Expenses (${res.formattedExpense})]")
        appendLine("• Commission Paid (Deducted from Cash): −${res.formattedCommissionPaid}")
        appendLine("• Net Expected Cash in Hand (AFTER Commission Deduction): ${res.formattedExpectedCash}")
        appendLine("• Physical Cash Received Handover: ${res.formattedPhysicalCash}")
        appendLine("• Actual Cash Collected (Before Commission Payout): ${res.formattedActualCashBeforeCommission}")
        appendLine("• Variance / Discrepancy: ${res.formattedDiscrepancy}")
        if (res.deficitAmount > 0) {
            appendLine("• Deficit Shortfall: ${res.formattedDeficitAmount} (Alert Limit: ${uiState.deficitThresholdText})")
        }
    }
}

private data class Tuple6<A, B, C, D, E, F>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E,
    val f: F
)

private data class Tuple4<A, B, C, D>(
    val a: A,
    val b: B,
    val c: C,
    val d: D
)
