package com.example.ui.components

import com.example.ui.theme.SoftColors
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
import com.example.ui.components.LiquidGlassSearchField
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
import com.example.domain.util.PkrNumberFormatter
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
    val totalExactCashBeforeCommission: Double = 0.0,
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

    val formattedTotalExactCashBeforeCommission: String
        get() = IndianNumberFormatter.formatIndianCurrency(if (totalExactCashBeforeCommission != 0.0) totalExactCashBeforeCommission else (totalProductCashSales + totalRecoveredCredit - totalExpense))

    val formattedExpectedCash: String
        get() = IndianNumberFormatter.formatIndianCurrency(totalExpectedCash)

    val formattedPhysicalCash: String
        get() = IndianNumberFormatter.formatIndianCurrency(totalPhysicalCash)

    val formattedDiscrepancy: String
        get() = IndianNumberFormatter.formatIndianCurrency(netDiscrepancy, showSign = true)
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
                val exactCash = prodCashSales + recovered - expense
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
                    totalExactCashBeforeCommission = exactCash,
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
    val fleetTotalExactCash = remember(repSummaries) { repSummaries.sumOf { it.totalExactCashBeforeCommission } }
    val fleetTotalExpectedCash = remember(repSummaries) { repSummaries.sumOf { it.totalExpectedCash } }
    val fleetTotalPhysicalCash = remember(repSummaries) { repSummaries.sumOf { it.totalPhysicalCash } }
    val fleetTotalCommission = remember(repSummaries) { repSummaries.sumOf { it.totalCommission } }
    val fleetTotalCommissionPaid = remember(repSummaries) { repSummaries.sumOf { it.totalCommissionPaid } }
    val fleetTotalExpenses = remember(repSummaries) { repSummaries.sumOf { it.totalExpense } }
    val fleetTotalRecovered = remember(repSummaries) { repSummaries.sumOf { it.totalRecoveredCredit } }
    val fleetVariance = fleetTotalPhysicalCash - fleetTotalExpectedCash

    SoftCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sales_representatives_matrix_card"),
        shape = RoundedCornerShape(16.dp)) {
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
                            .softWell(CircleShape, tint = primaryColor),
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

                SoftIconButton(
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
                totalExactCash = fleetTotalExactCash,
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
                LiquidGlassSearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholderText = "Filter rep name...",
                    modifier = Modifier.weight(1f).testTag("matrix_rep_search_input")
                )

                // Sort selector trigger
                Box {
                    SoftSurface(
                        modifier = Modifier
                            .height(44.dp)
                            .clickable { isSortMenuExpanded = true }
                            .testTag("matrix_sort_menu_button"),
                        shape = RoundedCornerShape(10.dp)) {
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
    totalExactCash: Double,
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
        kotlin.math.abs(fleetVariance) < 0.01 -> SoftColors.positive
        fleetVariance > 0 -> SoftColors.sky
        else -> SoftColors.negative
    }

    val varianceLabel = when {
        kotlin.math.abs(fleetVariance) < 0.01 -> "Fleet Balanced"
        fleetVariance > 0 -> "+${PkrNumberFormatter.formatPkrAmount(fleetVariance, decimals = 2)} Surplus"
        else -> "−${PkrNumberFormatter.formatPkrAmount(kotlin.math.abs(fleetVariance), decimals = 2)} Shortage"
    }

    SoftSurface(
        modifier = Modifier
            .fillMaxWidth(),
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
                    text = "TOTAL FLEET SETTLEMENT & CASH AUDIT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = primaryColor
                )

                SoftSurface(
                    shape = RoundedCornerShape(6.dp)) {
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
                    label = "Exact Cash (No Comm)",
                    value = IndianNumberFormatter.formatIndianCurrency(totalExactCash),
                    tintColor = SoftColors.sky,
                    modifier = Modifier.weight(1.1f)
                )
                MatrixStatBox(
                    label = "Comm Paid",
                    value = "−" + IndianNumberFormatter.formatIndianCurrency(totalCommissionPaid),
                    tintColor = SoftColors.warning,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Net Expected",
                    value = IndianNumberFormatter.formatIndianCurrency(totalExpectedCash),
                    tintColor = primaryColor,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Cash Collected",
                    value = IndianNumberFormatter.formatIndianCurrency(totalPhysicalCash),
                    tintColor = SoftColors.positive,
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
        1 -> SoftColors.warning
        2 -> SoftColors.silver
        3 -> SoftColors.bronze
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val varianceColor = when (summary.status) {
        ReconciliationStatus.BALANCED -> SoftColors.positive
        ReconciliationStatus.OVER -> SoftColors.sky
        ReconciliationStatus.SHORT -> SoftColors.negative
    }

    val progressFraction = if (summary.totalExpectedCash > 0.0) {
        (summary.totalPhysicalCash / summary.totalExpectedCash).toFloat().coerceIn(0f, 1.5f)
    } else 1f

    SoftCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(12.dp)) {
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
                    SoftSurface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape) {
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
                                SoftSurface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SoftColors.violet.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = "${summary.formattedShared} shared",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = DigitalMonospaceFont,
                                        color = SoftColors.violet,
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
                    SoftSurface(
                        shape = RoundedCornerShape(4.dp)) {
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
                    color = if (progressFraction >= 1f) SoftColors.positive else SoftColors.warning,
                    trackColor = softTrackColor(),
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
                            tintColor = if (summary.totalReturnedUnits > 0) SoftColors.warning else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        MatrixStatBox(
                            label = "Expired",
                            value = summary.formattedExpired,
                            tintColor = if (summary.totalExpiredUnits > 0) SoftColors.negative else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        MatrixStatBox(
                            label = "Shared",
                            value = summary.formattedShared,
                            tintColor = if (summary.totalSharedUnits > 0) SoftColors.violet else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        MatrixStatBox(
                            label = "Credit Qty",
                            value = summary.formattedCreditQty,
                            tintColor = if (summary.totalCreditSalesQty > 0) SoftColors.sky else MaterialTheme.colorScheme.onSurfaceVariant,
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
                            tintColor = SoftColors.sky,
                            modifier = Modifier.weight(1f)
                        )
                        MatrixStatBox(
                            label = "Recovered",
                            value = summary.formattedRecoveredCredit,
                            tintColor = SoftColors.positive,
                            modifier = Modifier.weight(1f)
                        )
                        MatrixStatBox(
                            label = "Comm Paid",
                            value = summary.formattedCommissionPaid,
                            tintColor = if (summary.totalCommissionPaid > 0) SoftColors.warning else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        MatrixStatBox(
                            label = "Net Comm",
                            value = summary.formattedTotalCommission,
                            tintColor = SoftColors.positive,
                            modifier = Modifier.weight(1f)
                        )
                        MatrixStatBox(
                            label = "Expenses",
                            value = summary.formattedTotalExpense,
                            tintColor = SoftColors.negative,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SoftButton(
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
                            SoftButton(
                                onClick = { onLoadTransaction(summary.transactions.first()) },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "Load Latest Audit",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftColors.positive
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
        ReconciliationStatus.BALANCED -> SoftColors.positive
        ReconciliationStatus.OVER -> SoftColors.sky
        ReconciliationStatus.SHORT -> SoftColors.negative
    }

    SoftCard(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)) {
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
                SoftSurface(
                    shape = RoundedCornerShape(6.dp)) {
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
                text = "Sales (${summary.formattedProductCashSales}) + Recovery (${summary.formattedRecoveredCredit}) − Exp (${summary.formattedTotalExpense}) = Exact Cash: ${summary.formattedTotalExactCashBeforeCommission}",
                fontSize = 10.sp,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (summary.totalCommissionPaid > 0.0) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Paid Comm of ${summary.formattedCommissionPaid} deducted from Cash in Hand → Net Expected: ${summary.formattedExpectedCash}",
                    fontSize = 9.5.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = SoftColors.warning,
                    fontWeight = FontWeight.SemiBold
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
                    tintColor = SoftColors.positive,
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
    SoftCard(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)) {
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
                SoftSurface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (summary.lossRatePercent > 10.0) SoftColors.negative.copy(alpha = 0.15f) else SoftColors.positive.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Loss: ${String.format(Locale.US, "%.1f", summary.lossRatePercent)}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = if (summary.lossRatePercent > 10.0) SoftColors.negative else SoftColors.positive,
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
                    tintColor = SoftColors.warning,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Expired",
                    value = summary.formattedExpired,
                    tintColor = SoftColors.negative,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Shared",
                    value = summary.formattedShared,
                    tintColor = SoftColors.violet,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Net Sold",
                    value = summary.formattedNetSold,
                    tintColor = SoftColors.positive,
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
        ReconciliationStatus.BALANCED -> SoftColors.positive
        ReconciliationStatus.OVER -> SoftColors.sky
        ReconciliationStatus.SHORT -> SoftColors.negative
    }

    SoftCard(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)) {
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

                SoftSurface(
                    shape = RoundedCornerShape(6.dp)) {
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
                    tintColor = if (summary.totalReturnedUnits > 0) SoftColors.warning else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Expired",
                    value = summary.formattedExpired,
                    tintColor = if (summary.totalExpiredUnits > 0) SoftColors.negative else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Shared",
                    value = summary.formattedShared,
                    tintColor = if (summary.totalSharedUnits > 0) SoftColors.violet else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Credit Qty",
                    value = summary.formattedCreditQty,
                    tintColor = if (summary.totalCreditSalesQty > 0) SoftColors.sky else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    tintColor = SoftColors.positive,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Gross Comm",
                    value = summary.formattedGrossCommission,
                    tintColor = SoftColors.warning,
                    modifier = Modifier.weight(1.1f)
                )
                MatrixStatBox(
                    label = "Comm Paid",
                    value = summary.formattedCommissionPaid,
                    tintColor = if (summary.totalCommissionPaid > 0) SoftColors.warning else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.1f)
                )
                MatrixStatBox(
                    label = "Net Comm",
                    value = summary.formattedTotalCommission,
                    tintColor = SoftColors.positive,
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
                    tintColor = SoftColors.positive,
                    modifier = Modifier.weight(1f)
                )
                MatrixStatBox(
                    label = "Field Exp",
                    value = summary.formattedTotalExpense,
                    tintColor = if (summary.totalExpense > 0) SoftColors.negative else MaterialTheme.colorScheme.onSurfaceVariant,
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
                        SoftButton(
                            onClick = { onLoadTransaction(summary.transactions.first()) },
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Load Latest", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoftColors.positive)
                        }
                    }
                    SoftButton(
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
    SoftSurface(
        modifier = modifier,
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
            kotlin.math.abs(fleetVariance) < 0.01 -> "0.00 (Balanced)"
            fleetVariance > 0 -> "+${PkrNumberFormatter.formatPkrAmount(fleetVariance, decimals = 2)} (Surplus)"
            else -> "-${PkrNumberFormatter.formatPkrAmount(kotlin.math.abs(fleetVariance), decimals = 2)} (Shortage)"
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
