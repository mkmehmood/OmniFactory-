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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
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
import com.example.domain.sales.ReconciliationStatus
import com.example.domain.sales.SalesReconciliationResult
import com.example.domain.util.IndianNumberFormatter
import com.example.ui.SalesUiState
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.LocalShapeStructure
import com.example.ui.theme.toButtonShape
import com.example.ui.theme.toCardShape
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
    onSetCreditSalesQty: (String) -> Unit,
    onSetRecoveredCredit: (String) -> Unit,
    onSetPhysicalCash: (String) -> Unit,
    onSetExpense: (String) -> Unit,
    onSetUnitSalePrice: (String) -> Unit,
    onSetCommissionPerUnit: (String) -> Unit,
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
            // Header Bar with Quick Action Buttons
            SalesScreenHeader(
                savedCount = savedTransactions.size,
                onOpenSavedSheet = onOpenHistorySheet,
                onClearAll = onClearInputs,
                primaryColor = primaryColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Sales Representative Selector & Date Picker Card
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

            // 2. Numeric Input Grid (Quantities, Cash, Unit Price & Commission per Unit)
            NumericInputGridCard(
                totalSoldQty = uiState.totalSoldQtyText,
                returnedQty = uiState.returnedQtyText,
                expiredQty = uiState.expiredQtyText,
                creditSalesQty = uiState.creditSalesQtyText,
                recoveredCredit = uiState.recoveredCreditText,
                physicalCash = uiState.physicalCashText,
                expense = uiState.expenseText,
                unitSalePrice = uiState.unitSalePriceText,
                commissionPerUnit = uiState.commissionPerUnitText,
                onSetTotalSoldQty = onSetTotalSoldQty,
                onSetReturnedQty = onSetReturnedQty,
                onSetExpiredQty = onSetExpiredQty,
                onSetCreditSalesQty = onSetCreditSalesQty,
                onSetRecoveredCredit = onSetRecoveredCredit,
                onSetPhysicalCash = onSetPhysicalCash,
                onSetExpense = onSetExpense,
                onSetUnitSalePrice = onSetUnitSalePrice,
                onSetCommissionPerUnit = onSetCommissionPerUnit,
                primaryColor = primaryColor,
                surfaceBg = surfaceBg,
                borderColor = cardBorderColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Calculated Reconciliation Result Panel
            CalculatedReconciliationResultPanel(
                result = uiState.reconciliationResult,
                onCopySummary = {
                    val summaryText = buildReconciliationSummaryText(uiState)
                    onCopySummary(summaryText)
                    Toast.makeText(context, "Reconciliation summary copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                primaryColor = primaryColor,
                surfaceBg = surfaceBg,
                borderColor = cardBorderColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Full-Width Primary Action Button: "Save This Transaction"
            val buttonShape = LocalShapeStructure.current.toButtonShape()
            Button(
                onClick = onSaveTransaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_transaction_button"),
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Save This Transaction",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
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
            title = { Text("Add Sales Representative", fontWeight = FontWeight.Bold) },
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
                text = "Sales Audit & Reconciliation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Live dynamic balance calculation",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Saved History Button
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onOpenSavedSheet() }
                    .testTag("open_saved_sales_button"),
                shape = RoundedCornerShape(10.dp),
                color = primaryColor.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
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
                        text = "Saved ($savedCount)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            // Clear Button
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
                        contentDescription = "Clear Form Inputs",
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
                                text = if (selectedRep.isBlank()) "Select or Add Representative..." else selectedRep,
                                fontSize = 14.sp,
                                fontWeight = if (selectedRep.isBlank()) FontWeight.Normal else FontWeight.SemiBold,
                                color = if (selectedRep.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown Chevron",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
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
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (rep == selectedRep) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
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
    commissionPerUnit: String,
    onSetTotalSoldQty: (String) -> Unit,
    onSetReturnedQty: (String) -> Unit,
    onSetExpiredQty: (String) -> Unit,
    onSetCreditSalesQty: (String) -> Unit,
    onSetRecoveredCredit: (String) -> Unit,
    onSetPhysicalCash: (String) -> Unit,
    onSetExpense: (String) -> Unit,
    onSetUnitSalePrice: (String) -> Unit,
    onSetCommissionPerUnit: (String) -> Unit,
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
                text = "QUANTITIES, CASH & COMMISSION AUDIT",
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
                    label = "Recovered Credit (Rs.)",
                    placeholder = "0.00",
                    value = recoveredCredit,
                    onValueChange = onSetRecoveredCredit,
                    testTag = "recovered_credit_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                NumericGridInputField(
                    label = "Physical Cash (Rs.)",
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

            // Row 4: Expense (Rs.) | Unit Sale Price (Rs.)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NumericGridInputField(
                    label = "Expense (Rs.)",
                    placeholder = "0.00",
                    value = expense,
                    onValueChange = onSetExpense,
                    testTag = "expense_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                EmphasizedUnitPriceInputField(
                    label = "Unit Sale Price (Rs.)",
                    placeholder = "0.00",
                    value = unitSalePrice,
                    onValueChange = onSetUnitSalePrice,
                    testTag = "unit_sale_price_input",
                    primaryColor = primaryColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 5: Commission Per Unit (Rs.)
            EmphasizedCommissionInputField(
                label = "Commission Per Unit (Rs.)",
                placeholder = "0.00",
                value = commissionPerUnit,
                onValueChange = onSetCommissionPerUnit,
                testTag = "commission_per_unit_input",
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
    val highlightAmber = Color(0xFFF59E0B)
    val inputShape = LocalShapeStructure.current.toInputShape()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = highlightAmber,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        placeholder = { Text(placeholder, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
            .testTag(testTag)
            .border(1.5.dp, highlightAmber.copy(alpha = 0.6f), inputShape),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = highlightAmber,
            unfocusedBorderColor = highlightAmber.copy(alpha = 0.4f),
            focusedContainerColor = highlightAmber.copy(alpha = 0.12f),
            unfocusedContainerColor = highlightAmber.copy(alpha = 0.06f)
        ),
        shape = inputShape,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 14.sp,
            fontFamily = DigitalMonospaceFont,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
        )
    )
}

@Composable
private fun EmphasizedCommissionInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String,
    primaryColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val highlightGreen = Color(0xFF10B981)
    val inputShape = LocalShapeStructure.current.toInputShape()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = highlightGreen,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = highlightGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        placeholder = { Text(placeholder, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
            .testTag(testTag)
            .border(1.5.dp, highlightGreen.copy(alpha = 0.6f), inputShape),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = highlightGreen,
            unfocusedBorderColor = highlightGreen.copy(alpha = 0.4f),
            focusedContainerColor = highlightGreen.copy(alpha = 0.12f),
            unfocusedContainerColor = highlightGreen.copy(alpha = 0.06f)
        ),
        shape = inputShape,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 14.sp,
            fontFamily = DigitalMonospaceFont,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color(0xFFA7F3D0) else Color(0xFF065F46)
        )
    )
}

@Composable
private fun CalculatedReconciliationResultPanel(
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
        shape = RoundedCornerShape(18.dp),
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
                            text = "Live Calculation",
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
                        text = "Expected: (${result.formattedCashQty} units × ${result.formattedUnitPrice}) + Rec Credit: ${result.formattedRecoveredCredit}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = DigitalMonospaceFont
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Reconciled Cash: Physical (${result.formattedPhysicalCash}) + Expense (${result.formattedExpense}) = ${result.formattedReconciledCash}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryColor,
                        fontFamily = DigitalMonospaceFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Commission Summary Card (if commission is set)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        1.dp,
                        Color(0xFF10B981).copy(alpha = 0.35f),
                        RoundedCornerShape(14.dp)
                    ),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF10B981).copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TOTAL COMMISSION EARNED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = Color(0xFF10B981)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${result.formattedNetSold} net units × ${result.formattedCommissionPerUnit}/unit",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = DigitalMonospaceFont
                        )
                    }

                    Text(
                        text = result.formattedTotalCommission,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = if (isDark) Color(0xFFA7F3D0) else Color(0xFF047857),
                        modifier = Modifier.testTag("total_commission_display")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Reconciliation Status Badge — Three States: Balanced, Over (Surplus), Short (Deficit)
            ReconciliationStatusBadge(
                status = result.status,
                discrepancyFormatted = result.formattedDiscrepancy,
                reconciledCashFormatted = result.formattedReconciledCash,
                expectedCashFormatted = result.formattedExpectedCash,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Four summary stat boxes: Net Sold Units, Cash Qty Units, Unit Price, Commission
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatBox(
                    label = "Net Units",
                    value = result.formattedNetSold,
                    subtext = "Sold-Ret-Exp",
                    tintColor = Color(0xFF38BDF8),
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_net_sold_units"
                )

                SummaryStatBox(
                    label = "Cash Qty",
                    value = result.formattedCashQty,
                    subtext = "Net - Credit",
                    tintColor = Color(0xFF10B981),
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_cash_qty_units"
                )

                SummaryStatBox(
                    label = "Unit Price",
                    value = result.formattedUnitPrice,
                    subtext = "Per Unit",
                    tintColor = Color(0xFFF59E0B),
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_unit_sale_price"
                )

                SummaryStatBox(
                    label = "Commission",
                    value = result.formattedCommissionPerUnit,
                    subtext = "Per Unit",
                    tintColor = Color(0xFF10B981),
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_commission_rate"
                )
            }
        }
    }
}

@Composable
private fun ReconciliationStatusBadge(
    status: ReconciliationStatus,
    discrepancyFormatted: String,
    reconciledCashFormatted: String,
    expectedCashFormatted: String,
    isDark: Boolean
) {
    val (badgeBgColor, badgeBorderColor, contentColor, statusTitle, statusSubtitle, statusIcon) = when (status) {
        ReconciliationStatus.BALANCED -> {
            Tuple6(
                Color(0xFF10B981).copy(alpha = 0.14f),
                Color(0xFF10B981).copy(alpha = 0.45f),
                Color(0xFF10B981),
                "BALANCED (EXACT MATCH)",
                "Reconciled cash ($reconciledCashFormatted) matches expected cash ($expectedCashFormatted) exactly",
                Icons.Default.CheckCircle
            )
        }
        ReconciliationStatus.OVER -> {
            Tuple6(
                Color(0xFF0284C7).copy(alpha = 0.14f),
                Color(0xFF0284C7).copy(alpha = 0.45f),
                Color(0xFF38BDF8),
                "SURPLUS (OVER / EXCESS)",
                "Reconciled cash exceeds expected cash by $discrepancyFormatted surplus",
                Icons.Default.TrendingUp
            )
        }
        ReconciliationStatus.SHORT -> {
            Tuple6(
                Color(0xFFEF4444).copy(alpha = 0.14f),
                Color(0xFFEF4444).copy(alpha = 0.45f),
                Color(0xFFF87171),
                "DEFICIT (SHORTAGE)",
                "Reconciled cash is short of expected cash by $discrepancyFormatted deficit",
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
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = tintColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtext,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
                    .padding(vertical = 36.dp),
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

        Spacer(modifier = Modifier.height(16.dp))
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
    val reconciledCash = if (tx.reconciledCash > 0) tx.reconciledCash else (tx.physicalCashInHand + tx.expense)
    val discrepancy = reconciledCash - tx.expectedCash

    val (statusBg, statusFg, statusText, statusIcon) = when {
        tx.status == "BALANCED" || kotlin.math.abs(discrepancy) < 0.005 -> {
            Tuple4(
                Color(0xFF10B981),
                Color(0xFFFFFFFF),
                "BALANCED • Exact Match (Rs. 0.00 Difference)",
                Icons.Default.CheckCircle
            )
        }
        tx.status == "OVER" || discrepancy > 0.005 -> {
            Tuple4(
                Color(0xFF0284C7),
                Color(0xFFFFFFFF),
                "SURPLUS • Excess of +Rs. ${IndianNumberFormatter.formatIndianDouble(discrepancy, 2)}",
                Icons.Default.TrendingUp
            )
        }
        else -> {
            Tuple4(
                Color(0xFFEF4444),
                Color(0xFFFFFFFF),
                "DEFICIT • Shortage of -Rs. ${IndianNumberFormatter.formatIndianDouble(kotlin.math.abs(discrepancy), 2)}",
                Icons.Default.TrendingDown
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isDark) Color(0x2AFFFFFF) else Color(0x20000000),
                RoundedCornerShape(16.dp)
            )
            .clickable { onLoad() }
            .testTag("sales_record_card_${tx.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF141E33) else Color(0xFFFFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Top Bar: Representative Tag + Date + Delete Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Representative Tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tx.salesRep,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Date & Time (12-hour format)
                    Text(
                        text = dateFormat.format(Date(tx.dateMillis)),
                        fontSize = 11.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Delete Action Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_transaction_button_${tx.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // 2. Labeled Key-Value Field Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: Sold | Returned | Expired
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GridKeyValueBox(
                        label = "Sold",
                        value = formatQuantity(tx.totalSoldQty),
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    GridKeyValueBox(
                        label = "Returned",
                        value = formatQuantity(tx.returnedQty),
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    GridKeyValueBox(
                        label = "Expired",
                        value = formatQuantity(tx.expiredQty),
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Credit Sales | Recovered | Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GridKeyValueBox(
                        label = "Credit Sales",
                        value = formatQuantity(tx.creditSalesQty),
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    GridKeyValueBox(
                        label = "Recovered",
                        value = "Rs. ${IndianNumberFormatter.formatIndianDouble(tx.recoveredCredit, 2)}",
                        isDark = isDark,
                        modifier = Modifier.weight(1.2f)
                    )
                    GridKeyValueBox(
                        label = "Price",
                        value = "Rs. ${IndianNumberFormatter.formatIndianDouble(tx.unitSalePrice, 2)}",
                        isDark = isDark,
                        modifier = Modifier.weight(1.1f)
                    )
                }

                // Row 3: Commission Rate | Total Commission
                if (tx.commissionPerUnit > 0 || tx.totalCommission > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GridKeyValueBox(
                            label = "Commission Rate",
                            value = "Rs. ${IndianNumberFormatter.formatIndianDouble(tx.commissionPerUnit, 2)}/unit",
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )
                        GridKeyValueBox(
                            label = "Total Commission",
                            value = "Rs. ${IndianNumberFormatter.formatIndianDouble(tx.totalCommission, 2)}",
                            highlightColor = Color(0xFF10B981),
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Row 4: Expected Cash | Physical Cash
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        label = "Physical Cash",
                        value = "Rs. ${IndianNumberFormatter.formatIndianDouble(tx.physicalCashInHand, 2)}",
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 5: Expense | Reconciled Cash
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GridKeyValueBox(
                        label = "Expense",
                        value = "Rs. ${IndianNumberFormatter.formatIndianDouble(tx.expense, 2)}",
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    GridKeyValueBox(
                        label = "Reconciled Cash",
                        value = "Rs. ${IndianNumberFormatter.formatIndianDouble(reconciledCash, 2)}",
                        highlightColor = Color(0xFF10B981),
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Full-Width Status Banner Capping the Card Bottom
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("status_banner_${tx.id}"),
                color = statusBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusFg,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
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
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = highlightColor ?: MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatQuantity(qty: Double): String {
    return if (qty % 1.0 == 0.0) {
        IndianNumberFormatter.formatIndianDouble(qty, 0)
    } else {
        IndianNumberFormatter.formatIndianDouble(qty, 2)
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
        appendLine("\n--- CASH, COMMISSION & RECONCILIATION ---")
        appendLine("• Unit Sale Price: ${res.formattedUnitPrice}")
        appendLine("• Commission / Unit: ${res.formattedCommissionPerUnit}")
        appendLine("• Total Commission Earned: ${res.formattedTotalCommission}")
        appendLine("• Sales Cash Revenue: ${res.formattedCashFromSales}")
        appendLine("• Recovered Credit: ${res.formattedRecoveredCredit}")
        appendLine("• Expected Cash: ${res.formattedExpectedCash}")
        appendLine("• Physical Cash: ${res.formattedPhysicalCash}")
        appendLine("• Expense: ${res.formattedExpense}")
        appendLine("• Reconciled Cash: ${res.formattedReconciledCash}")
        appendLine("• Difference (Status): ${res.formattedDiscrepancy}")
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
