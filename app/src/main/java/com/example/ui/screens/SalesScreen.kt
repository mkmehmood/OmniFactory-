package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SalesTransactionEntity
import com.example.domain.sales.ReconciliationStatus
import com.example.domain.sales.SalesReconciliationResult
import com.example.ui.SalesUiState
import com.example.ui.theme.DigitalMonospaceFont
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
    onSetCreditSalesQty: (String) -> Unit,
    onSetRecoveredCredit: (String) -> Unit,
    onSetPhysicalCash: (String) -> Unit,
    onSetExpense: (String) -> Unit,
    onSetUnitSalePrice: (String) -> Unit,
    onSaveTransaction: () -> Unit,
    onClearInputs: () -> Unit,
    onPopulateSample: () -> Unit,
    onCopySummary: (String) -> Unit,
    onOpenHistorySheet: () -> Unit,
    onCloseHistorySheet: () -> Unit,
    onLoadTransaction: (SalesTransactionEntity) -> Unit,
    onDeleteTransaction: (SalesTransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary
    val scrollState = rememberScrollState()

    val surfaceBg = if (isDark) Color(0x14FFFFFF) else Color(0x0A000000)
    val cardBorderColor = if (isDark) Color(0x22FFFFFF) else Color(0x22000000)

    var showDatePicker by remember { mutableStateOf(false) }
    var showCustomRepDialog by remember { mutableStateOf(false) }
    var customRepText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("sales_reconciliation_screen")
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Sales Reconciliation",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cash & sales ledger balancing",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // History Button
                IconButton(
                    onClick = onOpenHistorySheet,
                    modifier = Modifier.testTag("sales_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Saved Transactions",
                        tint = primaryColor
                    )
                }

                // Reset Button
                IconButton(
                    onClick = onClearInputs,
                    modifier = Modifier.testTag("reset_sales_form_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Inputs",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Sales Representative & Date Selector Card
        SalesRepAndDateCard(
            selectedRep = uiState.salesRep,
            availableReps = uiState.availableReps,
            dateMillis = uiState.dateMillis,
            onSelectRep = onSetSalesRep,
            onOpenCustomRepDialog = { showCustomRepDialog = true },
            onOpenDatePicker = { showDatePicker = true },
            primaryColor = primaryColor,
            surfaceBg = surfaceBg,
            borderColor = cardBorderColor,
            isDark = isDark
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Two-Column Numeric Input Grid
        NumericInputGridCard(
            totalSoldQty = uiState.totalSoldQtyText,
            returnedQty = uiState.returnedQtyText,
            expiredQty = uiState.expiredQtyText,
            creditSalesQty = uiState.creditSalesQtyText,
            recoveredCredit = uiState.recoveredCreditText,
            physicalCash = uiState.physicalCashText,
            expense = uiState.expenseText,
            unitSalePrice = uiState.unitSalePriceText,
            onSetTotalSoldQty = onSetTotalSoldQty,
            onSetReturnedQty = onSetReturnedQty,
            onSetExpiredQty = onSetExpiredQty,
            onSetCreditSalesQty = onSetCreditSalesQty,
            onSetRecoveredCredit = onSetRecoveredCredit,
            onSetPhysicalCash = onSetPhysicalCash,
            onSetExpense = onSetExpense,
            onSetUnitSalePrice = onSetUnitSalePrice,
            primaryColor = primaryColor,
            surfaceBg = surfaceBg,
            borderColor = cardBorderColor,
            isDark = isDark
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Calculated Reconciliation Result Panel
        ReconciliationResultCard(
            result = uiState.reconciliationResult,
            onCopySummary = {
                val summary = buildReconciliationSummaryText(uiState)
                onCopySummary(summary)
            },
            primaryColor = primaryColor,
            surfaceBg = surfaceBg,
            borderColor = cardBorderColor,
            isDark = isDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Full-width Primary Action Button: "Save This Transaction"
        Button(
            onClick = onSaveTransaction,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(6.dp, RoundedCornerShape(14.dp), ambientColor = primaryColor, spotColor = primaryColor)
                .testTag("save_sales_transaction_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = if (isDark) Color.Black else Color.White
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (uiState.isSavedSuccess) Icons.Default.Check else Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isSavedSuccess) "Transaction Saved!" else "Save This Transaction",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onSetDateMillis(it) }
                        showDatePicker = false
                    },
                    modifier = Modifier.testTag("confirm_date_picker_button")
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Add Custom Sales Rep Dialog
    if (showCustomRepDialog) {
        AlertDialog(
            onDismissRequest = { showCustomRepDialog = false },
            title = { Text("Add Sales Representative", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = customRepText,
                    onValueChange = { customRepText = it },
                    placeholder = { Text("e.g. Johnathan Doe") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_sales_rep_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customRepText.isNotBlank()) {
                            onAddCustomSalesRep(customRepText)
                            customRepText = ""
                            showCustomRepDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_custom_sales_rep")
                ) {
                    Text("Add & Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomRepDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Saved Transactions Sheet
    if (uiState.showSavedHistorySheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onCloseHistorySheet,
            sheetState = sheetState,
            containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
        ) {
            SavedTransactionsSheetContent(
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Sales Representative Dropdown
            Text(
                text = "SALES REPRESENTATIVE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            if (isDark) Color(0x28FFFFFF) else Color(0x20000000),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { dropdownExpanded = true }
                        .testTag("sales_rep_dropdown_selector"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0x18FFFFFF) else Color(0x0A000000)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Sales Representative Icon",
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = selectedRep,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Sales Representative",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.background(if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF))
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
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (rep == selectedRep) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            trailingIcon = if (rep == selectedRep) {
                                { Icon(Icons.Default.Check, null, tint = primaryColor, modifier = Modifier.size(16.dp)) }
                            } else null,
                            onClick = {
                                onSelectRep(rep)
                                dropdownExpanded = false
                            },
                            modifier = Modifier.testTag("rep_option_$rep")
                        )
                    }

                    HorizontalDivider(color = if (isDark) Color(0x22FFFFFF) else Color(0x22000000))

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "+ Add Custom Rep...",
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        onClick = {
                            dropdownExpanded = false
                            onOpenCustomRepDialog()
                        },
                        modifier = Modifier.testTag("add_custom_rep_option")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Date Picker Selector
            Text(
                text = "TRANSACTION DATE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (isDark) Color(0x28FFFFFF) else Color(0x20000000),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onOpenDatePicker() }
                    .testTag("sales_date_picker_button"),
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0x18FFFFFF) else Color(0x0A000000)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Date Picker Calendar Icon",
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = formattedDate,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Change",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun NumericInputGridCard(
    totalSoldQty: String,
    returnedQty: String,
    expiredQty: String,
    creditSalesQty: String,
    recoveredCredit: String,
    physicalCash: String,
    expense: String,
    unitSalePrice: String,
    onSetTotalSoldQty: (String) -> Unit,
    onSetReturnedQty: (String) -> Unit,
    onSetExpiredQty: (String) -> Unit,
    onSetCreditSalesQty: (String) -> Unit,
    onSetRecoveredCredit: (String) -> Unit,
    onSetPhysicalCash: (String) -> Unit,
    onSetExpense: (String) -> Unit,
    onSetUnitSalePrice: (String) -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "SALES & QUANTITY DATA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Row 1: Total Sold Qty | Returned Qty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NumericGridInputField(
                    label = "Total Sold Qty",
                    placeholder = "0",
                    value = totalSoldQty,
                    onValueChange = onSetTotalSoldQty,
                    testTag = "total_sold_qty_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                NumericGridInputField(
                    label = "Returned Qty",
                    placeholder = "0",
                    value = returnedQty,
                    onValueChange = onSetReturnedQty,
                    testTag = "returned_qty_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Expired Qty | Credit Sales Qty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NumericGridInputField(
                    label = "Expired Qty",
                    placeholder = "0",
                    value = expiredQty,
                    onValueChange = onSetExpiredQty,
                    testTag = "expired_qty_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                NumericGridInputField(
                    label = "Credit Sales Qty",
                    placeholder = "0",
                    value = creditSalesQty,
                    onValueChange = onSetCreditSalesQty,
                    testTag = "credit_sales_qty_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Recovered Credit | Physical Cash In Hand
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NumericGridInputField(
                    label = "Recovered Credit ($)",
                    placeholder = "0.00",
                    value = recoveredCredit,
                    onValueChange = onSetRecoveredCredit,
                    testTag = "recovered_credit_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                NumericGridInputField(
                    label = "Physical Cash In Hand ($)",
                    placeholder = "0.00",
                    value = physicalCash,
                    onValueChange = onSetPhysicalCash,
                    testTag = "physical_cash_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 4: Expense | Unit Sale Price (Visually emphasized when focused / single column structure)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NumericGridInputField(
                    label = "Expense ($)",
                    placeholder = "0.00",
                    value = expense,
                    onValueChange = onSetExpense,
                    testTag = "expense_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                // Unit Sale Price — visually emphasized with special highlight & focus glow
                EmphasizedUnitPriceInputField(
                    label = "Unit Sale Price ($)",
                    placeholder = "0.00",
                    value = unitSalePrice,
                    onValueChange = onSetUnitSalePrice,
                    testTag = "unit_sale_price_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }
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
        shape = RoundedCornerShape(10.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 14.sp,
            fontFamily = DigitalMonospaceFont,
            fontWeight = FontWeight.SemiBold
        )
    )
}

@Composable
private fun EmphasizedUnitPriceInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String,
    primaryColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFocused) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        placeholder = { Text(placeholder, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .testTag(testTag),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primaryColor,
            unfocusedBorderColor = primaryColor.copy(alpha = 0.5f),
            focusedContainerColor = primaryColor.copy(alpha = 0.12f),
            unfocusedContainerColor = primaryColor.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(10.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 14.sp,
            fontFamily = DigitalMonospaceFont,
            fontWeight = FontWeight.Bold,
            color = if (isFocused) primaryColor else MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun ReconciliationResultCard(
    result: SalesReconciliationResult,
    onCopySummary: () -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with copy action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECONCILIATION RESULT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )

                IconButton(
                    onClick = onCopySummary,
                    modifier = Modifier.testTag("copy_reconciliation_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Reconciliation Summary",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Expected Cash Read-Only Computed Display
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        1.dp,
                        primaryColor.copy(alpha = 0.35f),
                        RoundedCornerShape(14.dp)
                    ),
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
                            text = "EXPECTED CASH (COMPUTED)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = primaryColor
                        )

                        Text(
                            text = "Read-Only",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Sales Cash: ${result.formattedCashFromSales} + Credit Rec: ${result.formattedRecoveredCredit} - Exp: ${result.formattedExpense}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = DigitalMonospaceFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Reconciliation Status Badge — Three States: Balanced, Over (Surplus), Short (Deficit)
            ReconciliationStatusBadge(
                status = result.status,
                discrepancyFormatted = result.formattedDiscrepancy,
                physicalCashFormatted = result.formattedPhysicalCash,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Three small stat / summary boxes: Net Sold Units, Cash Qty Units, Unit Sale Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatBox(
                    label = "Net Sold Units",
                    value = result.formattedNetSold,
                    subtext = "Sold - Ret - Exp",
                    tintColor = Color(0xFF38BDF8),
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_net_sold_units"
                )

                SummaryStatBox(
                    label = "Cash Qty Units",
                    value = result.formattedCashQty,
                    subtext = "Net - Credit",
                    tintColor = Color(0xFF10B981),
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_cash_qty_units"
                )

                SummaryStatBox(
                    label = "Unit Sale Price",
                    value = result.formattedUnitPrice,
                    subtext = "Per Unit",
                    tintColor = Color(0xFFF59E0B),
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_unit_sale_price"
                )
            }
        }
    }
}

@Composable
private fun ReconciliationStatusBadge(
    status: ReconciliationStatus,
    discrepancyFormatted: String,
    physicalCashFormatted: String,
    isDark: Boolean
) {
    val (badgeBgColor, badgeBorderColor, contentColor, statusTitle, statusSubtitle, statusIcon) = when (status) {
        ReconciliationStatus.BALANCED -> {
            Tuple6(
                Color(0xFF10B981).copy(alpha = 0.14f),
                Color(0xFF10B981).copy(alpha = 0.45f),
                Color(0xFF10B981),
                "BALANCED (EXACT MATCH)",
                "Physical cash ($physicalCashFormatted) matches expected cash exactly",
                Icons.Default.CheckCircle
            )
        }
        ReconciliationStatus.OVER -> {
            Tuple6(
                Color(0xFF0284C7).copy(alpha = 0.14f),
                Color(0xFF0284C7).copy(alpha = 0.45f),
                Color(0xFF38BDF8),
                "SURPLUS (OVER / EXCESS)",
                "Physical cash is over by $discrepancyFormatted surplus",
                Icons.Default.TrendingUp
            )
        }
        ReconciliationStatus.SHORT -> {
            Tuple6(
                Color(0xFFEF4444).copy(alpha = 0.14f),
                Color(0xFFEF4444).copy(alpha = 0.45f),
                Color(0xFFF87171),
                "DEFICIT (SHORTAGE)",
                "Physical cash is short by $discrepancyFormatted deficit",
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
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = statusTitle,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusTitle,
                    fontSize = 13.sp,
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
    subtext: String,
    tintColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                tintColor.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = tintColor.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = tintColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtext,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SavedTransactionsSheetContent(
    transactions: List<SalesTransactionEntity>,
    onLoadTransaction: (SalesTransactionEntity) -> Unit,
    onDeleteTransaction: (SalesTransactionEntity) -> Unit,
    onClose: () -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }

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
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Saved Transactions (${transactions.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont
                )
            }

            TextButton(onClick = onClose) {
                Text("Done")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No saved sales transactions yet",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                transactions.forEach { tx ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                if (isDark) Color(0x22FFFFFF) else Color(0x22000000),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onLoadTransaction(tx) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0x10FFFFFF) else Color(0x06000000)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tx.salesRep,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${dateFormat.format(Date(tx.dateMillis))}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = "Expected: $${String.format(Locale.US, "%.2f", tx.expectedCash)} | Cash: $${String.format(Locale.US, "%.2f", tx.physicalCashInHand)} | Net: ${tx.netSoldUnits.toInt()} units",
                                    fontSize = 11.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (tx.status) {
                                        "BALANCED" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                        "OVER" -> Color(0xFF0284C7).copy(alpha = 0.2f)
                                        else -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                    }
                                ) {
                                    Text(
                                        text = tx.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = DigitalMonospaceFont,
                                        color = when (tx.status) {
                                            "BALANCED" -> Color(0xFF10B981)
                                            "OVER" -> Color(0xFF38BDF8)
                                            else -> Color(0xFFF87171)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteTransaction(tx) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun buildReconciliationSummaryText(uiState: SalesUiState): String {
    val res = uiState.reconciliationResult
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val dateStr = dateFormat.format(Date(uiState.dateMillis))

    return buildString {
        appendLine("=== SALES RECONCILIATION AUDIT ===")
        appendLine("Representative: ${uiState.salesRep}")
        appendLine("Date: $dateStr")
        appendLine("Status: ${res.status.name}")
        appendLine("\n--- QUANTITIES ---")
        appendLine("• Total Sold Qty: ${uiState.totalSoldQtyText}")
        appendLine("• Returned Qty: ${uiState.returnedQtyText}")
        appendLine("• Expired Qty: ${uiState.expiredQtyText}")
        appendLine("• Credit Sales Qty: ${uiState.creditSalesQtyText}")
        appendLine("• Net Sold Units: ${res.formattedNetSold}")
        appendLine("• Cash Qty Units: ${res.formattedCashQty}")
        appendLine("\n--- CASH & RECONCILIATION ---")
        appendLine("• Unit Sale Price: ${res.formattedUnitPrice}")
        appendLine("• Sales Cash Revenue: ${res.formattedCashFromSales}")
        appendLine("• Recovered Credit: ${res.formattedRecoveredCredit}")
        appendLine("• Expense: ${res.formattedExpense}")
        appendLine("• EXPECTED CASH: ${res.formattedExpectedCash}")
        appendLine("• PHYSICAL CASH: ${res.formattedPhysicalCash}")
        appendLine("• DISCREPANCY: ${res.formattedDiscrepancy}")
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
