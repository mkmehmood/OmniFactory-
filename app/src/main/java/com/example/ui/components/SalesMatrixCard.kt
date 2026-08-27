package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SalesTransactionEntity
import com.example.domain.sales.ReconciliationStatus
import com.example.domain.util.IndianNumberFormatter
import com.example.ui.theme.DigitalMonospaceFont
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MatrixSortOption(val displayName: String) {
    EXPECTED_CASH("Highest Expected Cash"),
    NET_SALES("Highest Net Sold Units"),
    CASH_COLLECTED("Highest Cash Received"),
    SHARED_VOLUME("Highest Shared Units"),
    COMMISSION("Highest Commission"),
    LOSS_RATE("Highest Loss/Return %"),
    VARIANCE_RISK("Variance (Shortage First)"),
    REP_NAME("Representative Name A-Z")
}

data class RepMatrixSummary(
    val repName: String,
    val transactionCount: Int,
    val totalGrossSoldUnits: Double = 0.0,
    val totalReturnedUnits: Double = 0.0,
    val totalExpiredUnits: Double = 0.0,
    val totalSharedUnits: Double = 0.0,
    val totalNetSoldUnits: Double,
    val totalCashQtyUnits: Double,
    val totalCreditSalesQty: Double,
    val totalProductCashSales: Double,
    val totalRecoveredCredit: Double,
    val totalGrossCommission: Double = 0.0,
    val totalCommissionPaid: Double = 0.0,
    val totalCommission: Double, // Net commission payable
    val totalExpense: Double,
    val totalExpectedCash: Double,
    val totalPhysicalCash: Double,
    val netDiscrepancy: Double,
    val collectionRate: Double,
    val returnRatePercent: Double = 0.0,
    val lossRatePercent: Double = 0.0,
    val creditRatioPercent: Double = 0.0,
    val balancedCount: Int,
    val latestTimestamp: Long,
    val transactions: List<SalesTransactionEntity>
) {
    val status: ReconciliationStatus
        get() = when {
            kotlin.math.abs(netDiscrepancy) < 0.01 -> ReconciliationStatus.BALANCED
            netDiscrepancy > 0.01 -> ReconciliationStatus.OVER
            else -> ReconciliationStatus.SHORT
        }

    val formattedGrossSold: String
        get() = IndianNumberFormatter.formatIndianDouble(totalGrossSoldUnits, 0)

    val formattedReturned: String
        get() = IndianNumberFormatter.formatIndianDouble(totalReturnedUnits, 0)

    val formattedExpired: String
        get() = IndianNumberFormatter.formatIndianDouble(totalExpiredUnits, 0)

    val formattedShared: String
        get() = IndianNumberFormatter.formatIndianDouble(totalSharedUnits, 0)

    val formattedNetSold: String
        get() = IndianNumberFormatter.formatIndianDouble(totalNetSoldUnits, 0)

    val formattedCashQty: String
        get() = IndianNumberFormatter.formatIndianDouble(totalCashQtyUnits, 0)

    val formattedCreditQty: String
        get() = IndianNumberFormatter.formatIndianDouble(totalCreditSalesQty, 0)

    val formattedProductCashSales: String
        get() = IndianNumberFormatter.formatIndianCurrency(totalProductCashSales)

    val formattedGrossCommission: String
        get() = IndianNumberFormatter.formatIndianCurrency(totalGrossCommission)

    val formattedCommissionPaid: String
        get() = IndianNumberFormatter.formatIndianCurrency(totalCommissionPaid)

    val formattedTotalCommission: String
        get() = IndianNumberFormatter.formatIndianCurrency(totalCommission)

    val formattedTotalExpense: String
        get() = IndianNumberFormatter.formatIndianCurrency(totalExpense)

    val formattedRecoveredCredit: String
        get() = IndianNumberFormatter.formatIndianCurrency(totalRecoveredCredit)

    val formattedExpectedCash: String
        get() = IndianNumberFormatter.formatIndianCurrency(totalExpectedCash)

    val formattedPhysicalCash: String
        get() = IndianNumberFormatter.formatIndianCurrency(totalPhysicalCash)

    val formattedDiscrepancy: String
        get() = when {
            kotlin.math.abs(netDiscrepancy) < 0.01 -> "Rs. 0"
            netDiscrepancy > 0.0 -> "+Rs. ${IndianNumberFormatter.formatIndianDouble(netDiscrepancy, 2)}"
            else -> "-Rs. ${IndianNumberFormatter.formatIndianDouble(kotlin.math.abs(netDiscrepancy), 2)}"
        }
}

@Composable
fun SalesRepresentativesMatrixCard(
    transactions: List<SalesTransactionEntity>,
    onSelectRep: (String) -> Unit,
    onLoadTransaction: (SalesTransactionEntity) -> Unit,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var currentSortOption by remember { mutableStateOf(MatrixSortOption.EXPECTED_CASH) }
    var isSortMenuExpanded by remember { mutableStateOf(false) }
    var expandedRepName by remember { mutableStateOf<String?>(null) }

    // Dynamic aggregation per Sales Representative
    val repSummaries by remember(transactions) {
        derivedStateOf {
            if (transactions.isEmpty()) return@derivedStateOf emptyList<RepMatrixSummary>()

            val grouped = transactions.groupBy { it.salesRep.trim().ifEmpty { "General Sales" } }
            grouped.map { (name, list) ->
                val txCount = list.size
                val grossSold = list.sumOf { it.totalSoldQty }
                val returned = list.sumOf { it.returnedQty }
                val expired = list.sumOf { it.expiredQty }
                val shared = list.sumOf { it.sharedQty }
                val netUnits = list.sumOf { it.netSoldUnits }
                val cashQty = list.sumOf { it.cashQtyUnits }
                val creditQty = list.sumOf { it.creditSalesQty }
                val prodCashSales = list.sumOf { it.cashQtyUnits * it.unitSalePrice }
                val recovered = list.sumOf { it.recoveredCredit }
                val grossComm = list.sumOf { if (it.grossCommission > 0.0) it.grossCommission else it.totalCommission }
                val commPaid = list.sumOf { it.commissionPaid }
                val netComm = list.sumOf { it.totalCommission }
                val expense = list.sumOf { it.expense }
                val expectedCash = list.sumOf { it.expectedCash }
                val physicalCash = list.sumOf { it.physicalCashInHand }
                val discrepancy = physicalCash - expectedCash
                val collectionRate = if (expectedCash > 0.0) (physicalCash / expectedCash) * 100.0 else 100.0
                val returnRate = if (grossSold > 0.0) (returned / grossSold) * 100.0 else 0.0
                val lossRate = if (grossSold > 0.0) ((returned + expired + shared) / grossSold) * 100.0 else 0.0
                val creditRatio = if (netUnits > 0.0) (creditQty / netUnits) * 100.0 else 0.0
                val balanced = list.count { it.status == "BALANCED" || kotlin.math.abs(it.physicalCashInHand - it.expectedCash) < 0.01 }
                val latestTime = list.maxOfOrNull { it.timestamp } ?: 0L

                RepMatrixSummary(
                    repName = name,
                    transactionCount = txCount,
                    totalGrossSoldUnits = grossSold,
                    totalReturnedUnits = returned,
                    totalExpiredUnits = expired,
                    totalSharedUnits = shared,
                    totalNetSoldUnits = netUnits,
                    totalCashQtyUnits = cashQty,
                    totalCreditSalesQty = creditQty,
                    totalProductCashSales = prodCashSales,
                    totalRecoveredCredit = recovered,
                    totalGrossCommission = grossComm,
                    totalCommissionPaid = commPaid,
                    totalCommission = netComm,
                    totalExpense = expense,
                    totalExpectedCash = expectedCash,
                    totalPhysicalCash = physicalCash,
                    netDiscrepancy = discrepancy,
                    collectionRate = collectionRate,
                    returnRatePercent = returnRate,
                    lossRatePercent = lossRate,
                    creditRatioPercent = creditRatio,
                    balancedCount = balanced,
                    latestTimestamp = latestTime,
                    transactions = list.sortedByDescending { it.dateMillis }
                )
            }
        }
    }

    // Filtered and Sorted summaries
    val filteredSummaries by remember(repSummaries, searchQuery, currentSortOption) {
        derivedStateOf {
            val filtered = if (searchQuery.isBlank()) {
                repSummaries
            } else {
                repSummaries.filter { it.repName.contains(searchQuery.trim(), ignoreCase = true) }
            }

            when (currentSortOption) {
                MatrixSortOption.EXPECTED_CASH -> filtered.sortedByDescending { it.totalExpectedCash }
                MatrixSortOption.NET_SALES -> filtered.sortedByDescending { it.totalNetSoldUnits }
                MatrixSortOption.CASH_COLLECTED -> filtered.sortedByDescending { it.totalPhysicalCash }
                MatrixSortOption.SHARED_VOLUME -> filtered.sortedByDescending { it.totalSharedUnits }
                MatrixSortOption.COMMISSION -> filtered.sortedByDescending { it.totalCommission }
                MatrixSortOption.LOSS_RATE -> filtered.sortedByDescending { it.lossRatePercent }
                MatrixSortOption.VARIANCE_RISK -> filtered.sortedBy { it.netDiscrepancy }
                MatrixSortOption.REP_NAME -> filtered.sortedBy { it.repName.lowercase(Locale.ROOT) }
            }
        }
    }

    // Dynamic Fleet Overview Metrics
    val fleetTotalGrossUnits = remember(repSummaries) { repSummaries.sumOf { it.totalGrossSoldUnits } }
    val fleetTotalNetUnits = remember(repSummaries) { repSummaries.sumOf { it.totalNetSoldUnits } }
    val fleetTotalSharedUnits = remember(repSummaries) { repSummaries.sumOf { it.totalSharedUnits } }
    val fleetTotalReturnedUnits = remember(repSummaries) { repSummaries.sumOf { it.totalReturnedUnits } }
    val fleetTotalExpectedCash = remember(repSummaries) { repSummaries.sumOf { it.totalExpectedCash } }
    val fleetTotalPhysicalCash = remember(repSummaries) { repSummaries.sumOf { it.totalPhysicalCash } }
    val fleetTotalCommission = remember(repSummaries) { repSummaries.sumOf { it.totalCommission } }
    val fleetTotalCommissionPaid = remember(repSummaries) { repSummaries.sumOf { it.totalCommissionPaid } }
    val fleetTotalExpenses = remember(repSummaries) { repSummaries.sumOf { it.totalExpense } }
    val fleetTotalRecovered = remember(repSummaries) { repSummaries.sumOf { it.totalRecoveredCredit } }
    val fleetVariance = fleetTotalPhysicalCash - fleetTotalExpectedCash

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sales_representatives_matrix_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SALES & RECONCILIATION MATRIX",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "${repSummaries.size} Reps • ${transactions.size} Records Analyzed",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val report = buildMatrixExecutiveReport(
                            repSummaries = repSummaries,
                            fleetExpectedCash = fleetTotalExpectedCash,
                            fleetPhysicalCash = fleetTotalPhysicalCash,
                            fleetVariance = fleetVariance,
                            fleetShared = fleetTotalSharedUnits,
                            fleetCommPaid = fleetTotalCommissionPaid
                        )
                        clipboardManager.setText(AnnotatedString(report))
                        Toast.makeText(context, "Sales matrix report copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(30.dp).testTag("copy_matrix_report_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Matrix Report",
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Fleet Summary KPI Banner
            FleetSummaryKpiBanner(
                totalReps = repSummaries.size,
                totalNetUnits = fleetTotalNetUnits,
                totalSharedUnits = fleetTotalSharedUnits,
                totalExpectedCash = fleetTotalExpectedCash,
                totalPhysicalCash = fleetTotalPhysicalCash,
                totalCommission = fleetTotalCommission,
                totalCommissionPaid = fleetTotalCommissionPaid,
                totalExpenses = fleetTotalExpenses,
                totalRecovered = fleetTotalRecovered,
                fleetVariance = fleetVariance,
                primaryColor = primaryColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Row: Comparative Matrix vs Reconciliation vs Loss vs Profiles
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = primaryColor,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = primaryColor,
                        height = 2.5.dp
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Leaderboard",
                            fontSize = 11.5.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = DigitalMonospaceFont
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Cash vs Sales",
                            fontSize = 11.5.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = DigitalMonospaceFont
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "Loss & Returns",
                            fontSize = 11.5.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = DigitalMonospaceFont
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text(
                            text = "Rep Profiles (${repSummaries.size})",
                            fontSize = 11.5.sp,
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = DigitalMonospaceFont
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search and Sort Control Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter rep name...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(44.dp).testTag("matrix_rep_search_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0x1AFFFFFF) else Color(0x0A000000),
                        unfocusedContainerColor = if (isDark) Color(0x10FFFFFF) else Color(0x05000000),
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = if (isDark) Color(0x22FFFFFF) else Color(0x1E000000)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 12.sp,
                        fontFamily = DigitalMonospaceFont
                    )
                )

                // Sort selector trigger
                Box {
                    Surface(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                1.dp,
                                if (isDark) Color(0x22FFFFFF) else Color(0x1E000000),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { isSortMenuExpanded = true }
                            .testTag("matrix_sort_menu_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0x15FFFFFF) else Color(0x08000000)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Sort",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isSortMenuExpanded,
                        onDismissRequest = { isSortMenuExpanded = false }
                    ) {
                        MatrixSortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.displayName,
                                        fontWeight = if (option == currentSortOption) FontWeight.Bold else FontWeight.Normal,
                                        color = if (option == currentSortOption) primaryColor else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    currentSortOption = option
                                    isSortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body Content based on Tab
            if (filteredSummaries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (transactions.isEmpty()) "No saved transactions to analyze yet" else "No matching representatives found",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                when (selectedTab) {
                    0 -> {
                        // Fleet Leaderboard / Comparative Matrix
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredSummaries.forEachIndexed { index, summary ->
                                RepLeaderboardRow(
                                    rank = index + 1,
                                    summary = summary,
                                    isExpanded = expandedRepName == summary.repName,
                                    onToggleExpand = {
                                        expandedRepName = if (expandedRepName == summary.repName) null else summary.repName
                                    },
                                    onSelectRep = onSelectRep,
                                    onLoadTransaction = onLoadTransaction,
                                    primaryColor = primaryColor,
                                    isDark = isDark
                                )
                            }
                        }
                    }
                    1 -> {
                        // Cash vs Sales Audit Analysis
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredSummaries.forEach { summary ->
                                RepCashSalesAuditCard(
                                    summary = summary,
                                    onSelectRep = onSelectRep,
                                    primaryColor = primaryColor,
                                    isDark = isDark
                                )
                            }
                        }
                    }
                    2 -> {
                        // Loss, Return & Shared Analysis
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredSummaries.forEach { summary ->
                                RepLossReturnsCard(
                                    summary = summary,
                                    onSelectRep = onSelectRep,
                                    primaryColor = primaryColor,
                                    isDark = isDark
                                )
                            }
                        }
                    }
                    3 -> {
                        // Detailed Representative Profiles
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredSummaries.forEach { summary ->
                                RepProfileDetailCard(
                                    summary = summary,
                                    onSelectRep = onSelectRep,
                                    onLoadTransaction = onLoadTransaction,
                                    primaryColor = primaryColor,
                                    isDark = isDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FleetSummaryKpiBanner(
    totalReps: Int,
    totalNetUnits: Double,
    totalSharedUnits: Double,
    totalExpectedCash: Double,
    totalPhysicalCash: Double,
    totalCommission: Double,
    totalCommissionPaid: Double,
    totalExpenses: Double,
    totalRecovered: Double,
    fleetVariance: Double,
    primaryColor: Color,
    isDark: Boolean
) {
    val varianceColor = when {
        kotlin.math.abs(fleetVariance) < 0.01 -> Color(0xFF10B981)
        fleetVariance > 0 -> Color(0xFF38BDF8)
        else -> Color(0xFFEF4444)
    }

    val varianceLabel = when {
        kotlin.math.abs(fleetVariance) < 0.01 -> "Fleet Balanced"
        fleetVariance > 0 -> "+Rs. ${IndianNumberFormatter.formatIndianDouble(fleetVariance, 2)} Surplus"
        else -> "−Rs. ${IndianNumberFormatter.formatIndianDouble(kotlin.math.abs(fleetVariance), 2)} Shortage"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                primaryColor.copy(alpha = 0.35f),
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = primaryColor.copy(alpha = 0.08f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL FLEET SETTLEMENT STATUS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = primaryColor
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = varianceColor.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = varianceLabel,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = varianceColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MatrixStatBox(
                    label = "Total Expected",
                    value = IndianNumberFormatter.formatIndianCurrency(totalExpectedCash),
                    tintColor = primaryColor,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Cash Collected",
                    value = IndianNumberFormatter.formatIndianCurrency(totalPhysicalCash),
                    tintColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Net Commission",
                    value = IndianNumberFormatter.formatIndianCurrency(totalCommission),
                    tintColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Net Units",
                    value = IndianNumberFormatter.formatIndianDouble(totalNetUnits, 0),
                    tintColor = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RepLeaderboardRow(
    rank: Int,
    summary: RepMatrixSummary,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSelectRep: (String) -> Unit,
    onLoadTransaction: (SalesTransactionEntity) -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    val rankBadgeColor = when (rank) {
        1 -> Color(0xFFF59E0B)
        2 -> Color(0xFF94A3B8)
        3 -> Color(0xFFB45309)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val varianceColor = when (summary.status) {
        ReconciliationStatus.BALANCED -> Color(0xFF10B981)
        ReconciliationStatus.OVER -> Color(0xFF38BDF8)
        ReconciliationStatus.SHORT -> Color(0xFFEF4444)
    }

    val progressFraction = if (summary.totalExpectedCash > 0.0) {
        (summary.totalPhysicalCash / summary.totalExpectedCash).toFloat().coerceIn(0f, 1.5f)
    } else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isExpanded) primaryColor.copy(alpha = 0.5f) else if (isDark) Color(0x22FFFFFF) else Color(0x1A000000),
                RoundedCornerShape(12.dp)
            )
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF121B2D) else Color(0xFFF9FAFB)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Rank badge
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = rankBadgeColor.copy(alpha = 0.18f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "#$rank",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = rankBadgeColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = summary.repName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (summary.totalSharedUnits > 0.0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF8B5CF6).copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = "${summary.formattedShared} shared",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = DigitalMonospaceFont,
                                        color = Color(0xFF8B5CF6),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${summary.transactionCount} audits • ${summary.formattedNetSold} net sold",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = summary.formattedExpectedCash,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = primaryColor
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = varianceColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = summary.formattedDiscrepancy,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = varianceColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Expected Cash vs Physical Cash Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progressFraction.coerceAtMost(1f) },
                    modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = if (progressFraction >= 1f) Color(0xFF10B981) else Color(0xFFF59E0B),
                    trackColor = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000),
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", summary.collectionRate)}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded detail drawer for this rep
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = "QUANTITIES (5 INPUTS)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MatrixStatBox(
                            label = "Gross Sold",
                            value = summary.formattedGrossSold,
                            tintColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        MatrixStatBox(
                            label = "Returned",
                            value = summary.formattedReturned,
                            tintColor = if (summary.totalReturnedUnits > 0) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        MatrixStatBox(
                            label = "Expired",
                            value = summary.formattedExpired,
                            tintColor = if (summary.totalExpiredUnits > 0) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        MatrixStatBox(
                            label = "Shared",
                            value = summary.formattedShared,
                            tintColor = if (summary.totalSharedUnits > 0) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        MatrixStatBox(
                            label = "Credit Qty",
                            value = summary.formattedCreditQty,
                            tintColor = if (summary.totalCreditSalesQty > 0) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "FINANCIALS & COMMISSION (6 INPUTS)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MatrixStatBox(
                            label = "Cash Sales",
                            value = summary.formattedProductCashSales,
                            tintColor = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f)
                        )
                        MatrixStatBox(
                            label = "Recovered",
                            value = summary.formattedRecoveredCredit,
                            tintColor = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        MatrixStatBox(
                            label = "Comm Paid",
                            value = summary.formattedCommissionPaid,
                            tintColor = if (summary.totalCommissionPaid > 0) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        MatrixStatBox(
                            label = "Net Comm",
                            value = summary.formattedTotalCommission,
                            tintColor = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        MatrixStatBox(
                            label = "Expenses",
                            value = summary.formattedTotalExpense,
                            tintColor = Color(0xFFEF4444),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onSelectRep(summary.repName) },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Select in Form",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }

                        if (summary.transactions.isNotEmpty()) {
                            TextButton(
                                onClick = { onLoadTransaction(summary.transactions.first()) },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "Load Latest Audit",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepCashSalesAuditCard(
    summary: RepMatrixSummary,
    onSelectRep: (String) -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    val varianceColor = when (summary.status) {
        ReconciliationStatus.BALANCED -> Color(0xFF10B981)
        ReconciliationStatus.OVER -> Color(0xFF38BDF8)
        ReconciliationStatus.SHORT -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isDark) Color(0x22FFFFFF) else Color(0x1A000000),
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF121B2D) else Color(0xFFF9FAFB)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.repName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = varianceColor.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = summary.status.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = varianceColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Audit Formula Decomposition Row
            Text(
                text = "Sales (${summary.formattedProductCashSales}) + Recovery (${summary.formattedRecoveredCredit}) − Net Comm (${summary.formattedTotalCommission}) − Exp (${summary.formattedTotalExpense})",
                fontSize = 10.sp,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (summary.totalCommissionPaid > 0.0) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Gross Comm: ${summary.formattedGrossCommission} | Paid: ${summary.formattedCommissionPaid} | Remaining: ${summary.formattedTotalCommission}",
                    fontSize = 9.5.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = Color(0xFFF59E0B)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MatrixStatBox(
                    label = "Expected Cash",
                    value = summary.formattedExpectedCash,
                    tintColor = primaryColor,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Handover Cash",
                    value = summary.formattedPhysicalCash,
                    tintColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Discrepancy",
                    value = summary.formattedDiscrepancy,
                    tintColor = varianceColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RepLossReturnsCard(
    summary: RepMatrixSummary,
    onSelectRep: (String) -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isDark) Color(0x22FFFFFF) else Color(0x1A000000),
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF121B2D) else Color(0xFFF9FAFB)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.repName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (summary.lossRatePercent > 10.0) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Loss: ${String.format(Locale.US, "%.1f", summary.lossRatePercent)}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = if (summary.lossRatePercent > 10.0) Color(0xFFEF4444) else Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MatrixStatBox(
                    label = "Gross Sold",
                    value = summary.formattedGrossSold,
                    tintColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Returned",
                    value = summary.formattedReturned,
                    tintColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Expired",
                    value = summary.formattedExpired,
                    tintColor = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Shared",
                    value = summary.formattedShared,
                    tintColor = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Net Sold",
                    value = summary.formattedNetSold,
                    tintColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RepProfileDetailCard(
    summary: RepMatrixSummary,
    onSelectRep: (String) -> Unit,
    onLoadTransaction: (SalesTransactionEntity) -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
    val lastDateStr = remember(summary.latestTimestamp) {
        if (summary.latestTimestamp > 0) dateFormat.format(Date(summary.latestTimestamp)) else "N/A"
    }

    val varianceColor = when (summary.status) {
        ReconciliationStatus.BALANCED -> Color(0xFF10B981)
        ReconciliationStatus.OVER -> Color(0xFF38BDF8)
        ReconciliationStatus.SHORT -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (isDark) Color(0x22FFFFFF) else Color(0x1A000000),
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF121B2D) else Color(0xFFF9FAFB)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = summary.repName,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${summary.transactionCount} Audits Recorded • Last: $lastDateStr",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = varianceColor.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = summary.formattedDiscrepancy,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = varianceColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 11 INPUT FIELDS COMPLETE MATRIX
            Text(
                text = "1. PRODUCT QUANTITIES (5 INPUTS)",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MatrixStatBox(
                    label = "Total Sold",
                    value = summary.formattedGrossSold,
                    tintColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Returned",
                    value = summary.formattedReturned,
                    tintColor = if (summary.totalReturnedUnits > 0) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Expired",
                    value = summary.formattedExpired,
                    tintColor = if (summary.totalExpiredUnits > 0) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Shared",
                    value = summary.formattedShared,
                    tintColor = if (summary.totalSharedUnits > 0) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Credit Qty",
                    value = summary.formattedCreditQty,
                    tintColor = if (summary.totalCreditSalesQty > 0) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "2. PRICING & COMMISSION (3 INPUTS)",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MatrixStatBox(
                    label = "Net Sold Units",
                    value = summary.formattedNetSold,
                    tintColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Gross Comm",
                    value = summary.formattedGrossCommission,
                    tintColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1.1f)
                )
                MatrixStatBox(
                    label = "Comm Paid",
                    value = summary.formattedCommissionPaid,
                    tintColor = if (summary.totalCommissionPaid > 0) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.1f)
                )
                MatrixStatBox(
                    label = "Net Comm",
                    value = summary.formattedTotalCommission,
                    tintColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1.1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "3. CASH & SETTLEMENT (3 INPUTS)",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MatrixStatBox(
                    label = "Recovered Cr",
                    value = summary.formattedRecoveredCredit,
                    tintColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Field Exp",
                    value = summary.formattedTotalExpense,
                    tintColor = if (summary.totalExpense > 0) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Cash Handover",
                    value = summary.formattedPhysicalCash,
                    tintColor = primaryColor,
                    modifier = Modifier.weight(1.15f)
                )
                MatrixStatBox(
                    label = "Expected Cash",
                    value = summary.formattedExpectedCash,
                    tintColor = primaryColor,
                    modifier = Modifier.weight(1.15f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Collection Accuracy: ${String.format(Locale.US, "%.1f", summary.collectionRate)}%",
                    fontSize = 10.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (summary.transactions.isNotEmpty()) {
                        TextButton(
                            onClick = { onLoadTransaction(summary.transactions.first()) },
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Load Latest", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                    TextButton(
                        onClick = { onSelectRep(summary.repName) },
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Select Rep", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun MatrixStatBox(
    label: String,
    value: String,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                tintColor.copy(alpha = 0.25f),
                RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = tintColor.copy(alpha = 0.07f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.dp))
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

private fun buildMatrixExecutiveReport(
    repSummaries: List<RepMatrixSummary>,
    fleetExpectedCash: Double,
    fleetPhysicalCash: Double,
    fleetVariance: Double,
    fleetShared: Double = 0.0,
    fleetCommPaid: Double = 0.0
): String {
    return buildString {
        appendLine("========================================")
        appendLine("EXECUTIVE SALES & RECONCILIATION MATRIX")
        appendLine("========================================")
        appendLine("Total Active Reps: ${repSummaries.size}")
        appendLine("Fleet Expected Cash: ${IndianNumberFormatter.formatIndianCurrency(fleetExpectedCash)}")
        appendLine("Fleet Cash Collected: ${IndianNumberFormatter.formatIndianCurrency(fleetPhysicalCash)}")
        if (fleetShared > 0.0) {
            appendLine("Fleet Shared Units: ${IndianNumberFormatter.formatIndianDouble(fleetShared, 0)}")
        }
        if (fleetCommPaid > 0.0) {
            appendLine("Fleet Commission Paid: ${IndianNumberFormatter.formatIndianCurrency(fleetCommPaid)}")
        }
        val varianceStr = when {
            kotlin.math.abs(fleetVariance) < 0.01 -> "Rs. 0 (Balanced)"
            fleetVariance > 0 -> "+Rs. ${IndianNumberFormatter.formatIndianDouble(fleetVariance, 2)} (Surplus)"
            else -> "-Rs. ${IndianNumberFormatter.formatIndianDouble(kotlin.math.abs(fleetVariance), 2)} (Shortage)"
        }
        appendLine("Fleet Net Variance: $varianceStr")
        appendLine("----------------------------------------")
        appendLine("REPRESENTATIVE PERFORMANCE BREAKDOWN:")
        repSummaries.forEachIndexed { i, r ->
            appendLine("${i + 1}. ${r.repName} [${r.status.name}]")
            appendLine("   • Gross Sold: ${r.formattedGrossSold} | Returned: ${r.formattedReturned} | Expired: ${r.formattedExpired} | Shared: ${r.formattedShared}")
            appendLine("   • Net Sold: ${r.formattedNetSold} units | Cash Qty: ${r.formattedCashQty}")
            appendLine("   • Cash Sales: ${r.formattedProductCashSales}")
            appendLine("   • Recoveries: ${r.formattedRecoveredCredit}")
            if (r.totalCommissionPaid > 0.0) {
                appendLine("   • Gross Comm: ${r.formattedGrossCommission} | Paid: ${r.formattedCommissionPaid} | Net Comm: ${r.formattedTotalCommission}")
            } else {
                appendLine("   • Commission: ${r.formattedTotalCommission}")
            }
            appendLine("   • Expenses: ${r.formattedTotalExpense}")
            appendLine("   • Expected: ${r.formattedExpectedCash}")
            appendLine("   • Handover: ${r.formattedPhysicalCash}")
            appendLine("   • Variance: ${r.formattedDiscrepancy}")
            appendLine("   • Accuracy: ${String.format(Locale.US, "%.1f", r.collectionRate)}%")
        }
        appendLine("========================================")
    }
}
