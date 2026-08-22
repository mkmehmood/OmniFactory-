package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SalesTransactionEntity
import com.example.ui.components.SalesRepresentativesMatrixCard
import com.example.ui.components.SimpleCalculatorDialog
import com.example.domain.sales.ReconciliationStatus
import com.example.domain.sales.SalesReconciliationResult
import com.example.domain.util.IndianNumberFormatter
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

    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceBg = if (isDark) Color(0x18FFFFFF) else Color(0x0C000000)
    val cardBorderColor = if (isDark) Color(0x28FFFFFF) else Color(0x1E000000)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("sales_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Bar
            SalesScreenHeader(
                savedCount = savedTransactions.size,
                onOpenSavedSheet = onOpenHistorySheet,
                onOpenCalculator = { showCalculatorDialog = true },
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
                result = uiState.reconciliationResult,
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
            Button(
                onClick = onSaveTransaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
            ) {
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
                TextButton(
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
                TextButton(
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
        AlertDialog(
            onDismissRequest = { showCustomRepDialog = false },
            title = { Text("Add Representative", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = customRepInput,
                    onValueChange = { customRepInput = it },
                    label = { Text("Representative Name") },
                    placeholder = { Text("e.g. Rahul Sharma") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_rep_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customRepInput.isNotBlank()) {
                            onAddCustomSalesRep(customRepInput)
                            showCustomRepDialog = false
                        }
                    },
                    modifier = Modifier.testTag("custom_rep_confirm_button")
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomRepDialog = false }) {
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
    savedCount: Int,
    onOpenSavedSheet: () -> Unit,
    onOpenCalculator: () -> Unit,
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

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onOpenCalculator() }
                    .testTag("open_calculator_popup_button"),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Open Calculator",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Calc",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onOpenSavedSheet() }
                    .testTag("open_saved_sales_button"),
                shape = RoundedCornerShape(10.dp),
                color = primaryColor.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Saved Sales Records",
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Records ($savedCount)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onClearAll() }
                    .testTag("clear_sales_inputs_button"),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
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
                // Sales Rep Selector Box
                Box(modifier = Modifier.weight(1.2f)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                1.dp,
                                if (isDark) Color(0x28FFFFFF) else Color(0x20000000),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { dropdownExpanded = true }
                            .testTag("sales_rep_dropdown_selector"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0x18FFFFFF) else Color(0x0A000000)
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

                // Date Selector Box
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            1.dp,
                            if (isDark) Color(0x28FFFFFF) else Color(0x20000000),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onOpenDatePicker() }
                        .testTag("sales_date_picker_button"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0x18FFFFFF) else Color(0x0A000000)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with Quick Calculator Icon
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
                IconButton(
                    onClick = onOpenCalculator,
                    modifier = Modifier.size(26.dp).testTag("quick_form_calc_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Quick Calculator",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
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
                    placeholder = "0.00",
                    value = unitSalePrice,
                    onValueChange = onSetUnitSalePrice,
                    testTag = "unit_sale_price_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                NumericGridInputField(
                    label = "Commission / Unit",
                    placeholder = "0.00",
                    value = commissionPerUnit,
                    onValueChange = onSetCommissionPerUnit,
                    testTag = "commission_per_unit_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Commission Paid (Deducts from commission, adds to expected cash)
            NumericGridInputField(
                label = "Commission Paid (Advance/Paid)",
                placeholder = "0.00",
                value = commissionPaid,
                onValueChange = onSetCommissionPaid,
                testTag = "commission_paid_input",
                primaryColor = primaryColor,
                isDark = isDark,
                modifier = Modifier.fillMaxWidth()
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
                    placeholder = "0.00",
                    value = recoveredCredit,
                    onValueChange = onSetRecoveredCredit,
                    testTag = "recovered_credit_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                NumericGridInputField(
                    label = "Field Expenses",
                    placeholder = "0.00",
                    value = expense,
                    onValueChange = onSetExpense,
                    testTag = "expense_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            NumericGridInputField(
                label = "Physical Cash Received from Rep",
                placeholder = "0.00",
                value = physicalCash,
                onValueChange = onSetPhysicalCash,
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
    testTag: String,
    primaryColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val inputShape = LocalShapeStructure.current.toInputShape()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        placeholder = { Text(placeholder, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.testTag(testTag),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primaryColor,
            unfocusedBorderColor = if (isDark) Color(0x28FFFFFF) else Color(0x20000000),
            focusedContainerColor = if (isDark) Color(0x1FFFFFFF) else Color(0x0E000000),
            unfocusedContainerColor = if (isDark) Color(0x10FFFFFF) else Color(0x06000000)
        ),
        shape = inputShape,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 14.sp,
            fontFamily = DigitalMonospaceFont,
            fontWeight = FontWeight.SemiBold
        )
    )
}

@Composable
private fun SettlementResultPanel(
    result: SalesReconciliationResult,
    onCopySummary: () -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calculated_reconciliation_result_panel"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SETTLEMENT AUDIT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )

                IconButton(
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

            // Hero Expected Cash Receivable Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        1.dp,
                        primaryColor.copy(alpha = 0.4f),
                        RoundedCornerShape(14.dp)
                    ),
                shape = RoundedCornerShape(14.dp),
                color = primaryColor.copy(alpha = 0.10f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "EXPECTED CASH RECEIVABLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = primaryColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = result.formattedExpectedCash,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("expected_cash_display")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Concise Formula Breakdown
                    Text(
                        text = "Cash Sales (${result.formattedCashFromSales}) + Recovery (${result.formattedRecoveredCredit}) − Net Comm (${result.formattedTotalCommission}) − Exp (${result.formattedExpense})",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = DigitalMonospaceFont
                    )

                    if (result.sharedQty > 0 || result.commissionPaid > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val extraDetails = buildList {
                            if (result.sharedQty > 0) add("Shared: ${result.formattedSharedQty} units deducted")
                            if (result.commissionPaid > 0) add("Gross Comm: ${result.formattedGrossCommission} | Paid: ${result.formattedCommissionPaid}")
                        }.joinToString(" • ")
                        Text(
                            text = extraDetails,
                            fontSize = 10.sp,
                            color = primaryColor,
                            fontFamily = DigitalMonospaceFont,
                            fontWeight = FontWeight.Medium
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
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Key Financial Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SummaryStatBox(
                    label = "Net Units",
                    value = result.formattedNetSold,
                    tintColor = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f),
                    testTag = "stat_net_sold_units"
                )

                SummaryStatBox(
                    label = "Cash Qty",
                    value = result.formattedCashQty,
                    tintColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    testTag = "stat_cash_qty_units"
                )

                SummaryStatBox(
                    label = "Commission",
                    value = result.formattedTotalCommission,
                    tintColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    testTag = "stat_commission_rate"
                )

                SummaryStatBox(
                    label = "Cash Handover",
                    value = result.formattedPhysicalCash,
                    tintColor = primaryColor,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_physical_cash"
                )
            }
        }
    }
}

@Composable
private fun SettlementStatusBadge(
    status: ReconciliationStatus,
    discrepancyFormatted: String,
    physicalCashFormatted: String,
    expectedCashFormatted: String,
    isDark: Boolean
) {
    val (badgeBgColor, badgeBorderColor, contentColor, statusTitle, statusSubtitle, statusIcon) = when (status) {
        ReconciliationStatus.BALANCED -> {
            Tuple6(
                Color(0xFF10B981).copy(alpha = 0.14f),
                Color(0xFF10B981).copy(alpha = 0.45f),
                Color(0xFF10B981),
                "SETTLED (BALANCED)",
                "Cash received ($physicalCashFormatted) matches expected cash ($expectedCashFormatted)",
                Icons.Default.CheckCircle
            )
        }
        ReconciliationStatus.OVER -> {
            Tuple6(
                Color(0xFF0284C7).copy(alpha = 0.14f),
                Color(0xFF0284C7).copy(alpha = 0.45f),
                Color(0xFF38BDF8),
                "SURPLUS (+)",
                "Cash received exceeds expected cash by $discrepancyFormatted",
                Icons.Default.TrendingUp
            )
        }
        ReconciliationStatus.SHORT -> {
            Tuple6(
                Color(0xFFEF4444).copy(alpha = 0.14f),
                Color(0xFFEF4444).copy(alpha = 0.45f),
                Color(0xFFF87171),
                "SHORTAGE (−)",
                "Cash received is short of expected cash by $discrepancyFormatted",
                Icons.Default.TrendingDown
            )
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, badgeBorderColor, RoundedCornerShape(12.dp))
            .testTag("reconciliation_status_badge"),
        shape = RoundedCornerShape(12.dp),
        color = badgeBgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.2f)),
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
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(
                1.dp,
                tintColor.copy(alpha = 0.25f),
                RoundedCornerShape(10.dp)
            )
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

            TextButton(onClick = onClose) {
                Text("Close")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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
    val discrepancy = tx.physicalCashInHand - tx.expectedCash

    val (statusBg, statusFg, statusText, statusIcon) = when {
        tx.status == "BALANCED" || kotlin.math.abs(discrepancy) < 0.005 -> {
            Tuple4(
                Color(0xFF10B981),
                Color(0xFFFFFFFF),
                "BALANCED",
                Icons.Default.CheckCircle
            )
        }
        tx.status == "OVER" || discrepancy > 0.005 -> {
            Tuple4(
                Color(0xFF0284C7),
                Color(0xFFFFFFFF),
                "SURPLUS +Rs. ${IndianNumberFormatter.formatIndianDouble(discrepancy, 2)}",
                Icons.Default.TrendingUp
            )
        }
        else -> {
            Tuple4(
                Color(0xFFEF4444),
                Color(0xFFFFFFFF),
                "SHORTAGE −Rs. ${IndianNumberFormatter.formatIndianDouble(kotlin.math.abs(discrepancy), 2)}",
                Icons.Default.TrendingDown
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (isDark) Color(0x2AFFFFFF) else Color(0x20000000),
                RoundedCornerShape(14.dp)
            )
            .clickable { onLoad() }
            .testTag("sales_record_card_${tx.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF141E33) else Color(0xFFFFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                    Surface(
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

                IconButton(
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

            // Key Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GridKeyValueBox(
                    label = "Expected Cash",
                    value = "Rs. ${IndianNumberFormatter.formatIndianDouble(tx.expectedCash, 2)}",
                    highlightColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                GridKeyValueBox(
                    label = "Cash Received",
                    value = "Rs. ${IndianNumberFormatter.formatIndianDouble(tx.physicalCashInHand, 2)}",
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                GridKeyValueBox(
                    label = "Commission",
                    value = "Rs. ${IndianNumberFormatter.formatIndianDouble(tx.totalCommission, 2)}",
                    highlightColor = Color(0xFF10B981),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Banner
            Surface(
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

@Composable
private fun GridKeyValueBox(
    label: String,
    value: String,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    highlightColor: Color? = null
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                highlightColor?.copy(alpha = 0.4f) ?: if (isDark) Color(0x1CFFFFFF) else Color(0x14000000),
                RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = highlightColor?.copy(alpha = 0.08f) ?: if (isDark) Color(0x10FFFFFF) else Color(0x06000000)
    ) {
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
        appendLine("\n--- FINANCIAL SETTLEMENT ---")
        appendLine("• Unit Sale Price: ${res.formattedUnitPrice}")
        appendLine("• Commission / Unit: ${res.formattedCommissionPerUnit}")
        appendLine("• Gross Commission: ${res.formattedGrossCommission}")
        appendLine("• Commission Paid: ${res.formattedCommissionPaid}")
        appendLine("• Net Commission Payable: ${res.formattedTotalCommission}")
        appendLine("• Product Cash Sales: ${res.formattedCashFromSales}")
        appendLine("• Recovered Credit: ${res.formattedRecoveredCredit}")
        appendLine("• Field Expenses: ${res.formattedExpense}")
        appendLine("• Expected Cash Receivable: ${res.formattedExpectedCash}")
        appendLine("• Physical Cash Received: ${res.formattedPhysicalCash}")
        appendLine("• Variance / Discrepancy: ${res.formattedDiscrepancy}")
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
