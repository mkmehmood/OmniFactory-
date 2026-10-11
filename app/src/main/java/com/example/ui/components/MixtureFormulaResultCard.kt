package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.mixture.EquationFormat
import com.example.domain.mixture.FormulaSummary

/** Step 4 view for the solver methods: the solved formulation, ready for batch scaling in Step 5. */
@Composable
fun FormulaResultCard(
    summary: FormulaSummary,
    primaryColor: Color,
    surfaceBg: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = surfaceBg,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "${summary.method.label} formulation",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Scale ${EquationFormat.factor(summary.scaleFactor)}  |  Total ${EquationFormat.withUnit(summary.totalFinal, summary.unit)}",
                fontSize = 11.sp,
                color = primaryColor.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(10.dp))

            summary.lines.forEach { line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(line.name, fontSize = 12.sp, color = primaryColor)
                        Text(
                            text = "${line.roleLabel}  |  ${summary.baseHeader} ${EquationFormat.withUnit(line.baseQty, summary.unit)}",
                            fontSize = 10.sp,
                            color = primaryColor.copy(alpha = 0.6f)
                        )
                        if (summary.hasInTub) {
                            val (text, color) = when {
                                line.excess > 1e-6 -> "In tub ${EquationFormat.withUnit(line.inTubQty, summary.unit)}  |  remove ${EquationFormat.withUnit(line.excess, summary.unit)}" to Color(0xFFEF4444)
                                line.stillNeeded > 1e-6 -> "In tub ${EquationFormat.withUnit(line.inTubQty, summary.unit)}  |  add ${EquationFormat.withUnit(line.stillNeeded, summary.unit)}" to Color(0xFFF59E0B)
                                else -> "In tub ${EquationFormat.withUnit(line.inTubQty, summary.unit)}  |  complete" to Color(0xFF10B981)
                            }
                            Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            EquationFormat.withUnit(line.finalQty, summary.unit),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            EquationFormat.percent(summary.share(line)),
                            fontSize = 10.sp,
                            color = primaryColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            HorizontalDivider(color = borderColor, modifier = Modifier.padding(vertical = 6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                Text(
                    EquationFormat.withUnit(summary.totalFinal, summary.unit),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }

            if (summary.hasInTub) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Still to add", fontSize = 11.sp, color = primaryColor)
                    Text(
                        EquationFormat.withUnit(summary.totalStillNeeded, summary.unit),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
                if (summary.totalExcess > 1e-6) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Over target (remove)", fontSize = 11.sp, color = primaryColor)
                        Text(
                            EquationFormat.withUnit(summary.totalExcess, summary.unit),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }

            summary.warnings.forEach {
                Spacer(modifier = Modifier.height(4.dp))
                Text(it, fontSize = 11.sp, color = Color(0xFFF59E0B))
            }
            if (summary.steps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                summary.steps.forEach {
                    Text(it, fontSize = 10.sp, color = primaryColor.copy(alpha = 0.65f))
                }
            }
        }
    }
}
