package com.example.domain.calculator

import com.example.domain.util.IndianNumberFormatter
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object CalculatorEngine {

    private val mathContext = MathContext(16, RoundingMode.HALF_UP)

    /**
     * Evaluates a calculation expression in series/sequential order (left-to-right calculation style):
     * The result of each step immediately acts as the base for the next operation.
     * Examples:
     * - 2 + 5 = 7
     * - 2 + 3 - 2 = (2 + 3) - 2 = 5 - 2 = 3
     * - 10 + 5 ÷ 2 = (10 + 5) ÷ 2 = 15 ÷ 2 = 7.5
     * - 100 + 10% = 110
     * - 100 - 10% = 90
     */
    fun evaluate(expression: String): CalculationResult {
        if (expression.isBlank()) {
            return CalculationResult.Empty
        }

        try {
            var sanitized = sanitizeExpression(expression)
            if (sanitized.isBlank()) return CalculationResult.Empty

            // Trim trailing operators for calculation
            sanitized = sanitized.trimEnd('+', '-', '*', '/', '^')

            if (sanitized.isBlank()) return CalculationResult.Empty

            // Auto-close open parentheses
            val openCount = sanitized.count { it == '(' }
            val closeCount = sanitized.count { it == ')' }
            if (openCount > closeCount) {
                sanitized += ")".repeat(openCount - closeCount)
            }

            val tokens = tokenize(sanitized)
            if (tokens.isEmpty()) return CalculationResult.Empty

            val resultValue = evaluateSequential(tokens)
            return CalculationResult.Success(resultValue)
        } catch (e: ArithmeticException) {
            return CalculationResult.Error(e.message ?: "Math Error")
        } catch (e: Exception) {
            return CalculationResult.Error("Invalid Expression")
        }
    }

    fun quickEvaluatePreview(expression: String): String? {
        if (expression.isBlank()) return null
        var expr = expression.trimEnd { it == '+' || it == '−' || it == '-' || it == '×' || it == '*' || it == '÷' || it == '/' }
        if (expr.isBlank()) return null

        val result = evaluate(expr)
        return when (result) {
            is CalculationResult.Success -> formatNumber(result.value)
            else -> null
        }
    }

    private fun sanitizeExpression(expr: String): String {
        return expr
            .replace(",", "")
            .replace(" ", "")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val len = expr.length

        while (i < len) {
            val c = expr[i]

            when {
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < len && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                    continue
                }
                c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '^' || c == '(' || c == ')' -> {
                    // Check for unary minus:
                    if (c == '-' && (tokens.isEmpty() || tokens.last() in listOf("+", "-", "*", "/", "(", "%", "^"))) {
                        i++
                        val sb = StringBuilder("-")
                        while (i < len && (expr[i].isDigit() || expr[i] == '.')) {
                            sb.append(expr[i])
                            i++
                        }
                        if (sb.length > 1) {
                            tokens.add(sb.toString())
                        } else {
                            tokens.add("-")
                        }
                        continue
                    } else {
                        tokens.add(c.toString())
                    }
                }
            }
            i++
        }
        return tokens
    }

    /**
     * Sequential Left-To-Right execution.
     * Evaluates sub-expressions within parentheses first, and chains operations in series.
     */
    private fun evaluateSequential(tokens: List<String>): BigDecimal {
        // Resolve parentheses recursively first if present
        val resolvedTokens = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            if (tokens[i] == "(") {
                var depth = 1
                val sub = mutableListOf<String>()
                i++
                while (i < tokens.size && depth > 0) {
                    if (tokens[i] == "(") depth++
                    else if (tokens[i] == ")") depth--

                    if (depth > 0) {
                        sub.add(tokens[i])
                    }
                    i++
                }
                val subResult = evaluateSequential(sub)
                resolvedTokens.add(subResult.toPlainString())
            } else {
                resolvedTokens.add(tokens[i])
                i++
            }
        }

        if (resolvedTokens.isEmpty()) return BigDecimal.ZERO

        // Sequential computation: acc starts with the first operand, then operator + operand pairs
        var acc = resolvedTokens[0].toBigDecimalOrNull()
            ?: throw IllegalArgumentException("Invalid initial number: ${resolvedTokens[0]}")

        var idx = 1
        while (idx < resolvedTokens.size) {
            val op = resolvedTokens[idx]
            if (op == "%") {
                acc = acc.divide(BigDecimal(100), mathContext)
                idx++
                continue
            }

            if (idx + 1 >= resolvedTokens.size) {
                // Trailing operator
                break
            }

            val nextToken = resolvedTokens[idx + 1]
            var nextNum = nextToken.toBigDecimalOrNull()
                ?: throw IllegalArgumentException("Invalid operand: $nextToken")

            var consumedSteps = 2
            var isPercent = false
            // Check if next number has an immediate percent attached (e.g. 100 + 10%)
            if (idx + 2 < resolvedTokens.size && resolvedTokens[idx + 2] == "%") {
                isPercent = true
                consumedSteps = 3
            }

            acc = when (op) {
                "+" -> {
                    if (isPercent) {
                        // 100 + 10% = 100 + (100 * 0.10) = 110
                        val percentValue = acc.multiply(nextNum.divide(BigDecimal(100), mathContext), mathContext)
                        acc.add(percentValue, mathContext)
                    } else {
                        acc.add(nextNum, mathContext)
                    }
                }
                "-" -> {
                    if (isPercent) {
                        // 100 - 10% = 100 - (100 * 0.10) = 90
                        val percentValue = acc.multiply(nextNum.divide(BigDecimal(100), mathContext), mathContext)
                        acc.subtract(percentValue, mathContext)
                    } else {
                        acc.subtract(nextNum, mathContext)
                    }
                }
                "*" -> {
                    val multiplier = if (isPercent) nextNum.divide(BigDecimal(100), mathContext) else nextNum
                    acc.multiply(multiplier, mathContext)
                }
                "/" -> {
                    val divisor = if (isPercent) nextNum.divide(BigDecimal(100), mathContext) else nextNum
                    if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                        throw ArithmeticException("Cannot divide by 0")
                    }
                    acc.divide(divisor, mathContext)
                }
                "^" -> {
                    val doubleBase = acc.toDouble()
                    val doubleExp = nextNum.toDouble()
                    val power = Math.pow(doubleBase, doubleExp)
                    if (power.isInfinite() || power.isNaN()) {
                        throw ArithmeticException("Result out of range")
                    }
                    BigDecimal(power, mathContext)
                }
                else -> throw IllegalArgumentException("Unknown operator: $op")
            }
            idx += consumedSteps
        }

        return acc.stripTrailingZeros()
    }

    /**
     * Formats number in Indian numbering format: 10,50,345
     */
    fun formatNumber(value: BigDecimal, maxDecimals: Int = 8, useGrouping: Boolean = true): String {
        return IndianNumberFormatter.formatIndian(value, maxDecimals = maxDecimals, useGrouping = useGrouping)
    }

    fun formatExpressionForDisplay(expr: String): String {
        return expr
            .replace("*", " × ")
            .replace("/", " ÷ ")
            .replace("+", " + ")
            .replace("-", " − ")
            .replace("  ", " ")
    }
}

sealed class CalculationResult {
    data class Success(val value: BigDecimal) : CalculationResult()
    data class Error(val message: String) : CalculationResult()
    data object Empty : CalculationResult()
}
