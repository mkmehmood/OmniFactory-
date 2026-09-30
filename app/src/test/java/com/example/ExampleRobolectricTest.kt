package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.calculator.CalculationResult
import com.example.domain.calculator.CalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Calculator", appName)
  }

  @Test
  fun `test calculator engine double zero and evaluation`() {
    val result = CalculatorEngine.evaluate("2500+500")
    assertTrue(result is CalculationResult.Success)
    assertEquals(0, BigDecimal("3000").compareTo((result as CalculationResult.Success).value))
  }

  @Test
  fun `test standard mathematical order of operations`() {
    // 10 + 5 ÷ 2 = 10 + 2.5 = 12.5
    val result = CalculatorEngine.evaluate("10+5÷2")
    assertTrue(result is CalculationResult.Success)
    assertEquals(0, BigDecimal("12.5").compareTo((result as CalculationResult.Success).value))

    // (10 + 5) ÷ 2 = 7.5
    val resultParentheses = CalculatorEngine.evaluate("(10+5)÷2")
    assertTrue(resultParentheses is CalculationResult.Success)
    assertEquals(0, BigDecimal("7.5").compareTo((resultParentheses as CalculationResult.Success).value))

    // 10 + 5 × 3 ÷ 5 = 10 + (15 ÷ 5) = 13
    val result2 = CalculatorEngine.evaluate("10+5×3÷5")
    assertTrue(result2 is CalculationResult.Success)
    assertEquals(0, BigDecimal("13").compareTo((result2 as CalculationResult.Success).value))
  }

  @Test
  fun `test decimal and percentage calculation`() {
    val result = CalculatorEngine.evaluate("500×15%")
    assertTrue(result is CalculationResult.Success)
    assertEquals(0, BigDecimal("75").compareTo((result as CalculationResult.Success).value))
  }

  @Test
  fun `test font style preference persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.data.preferences.UserPreferencesManager(context)
    prefs.fontStyle = com.example.ui.theme.AppFontStyle.CASUAL_HANDWRITING
    assertEquals(com.example.ui.theme.AppFontStyle.CASUAL_HANDWRITING, prefs.fontStyle)

    prefs.fontStyle = com.example.ui.theme.AppFontStyle.DANCING_BOUNCY_SCRIPT
    assertEquals(com.example.ui.theme.AppFontStyle.DANCING_BOUNCY_SCRIPT, prefs.fontStyle)

    prefs.fontStyle = com.example.ui.theme.AppFontStyle.CYBER_CONDENSED
    assertEquals(com.example.ui.theme.AppFontStyle.CYBER_CONDENSED, prefs.fontStyle)

    prefs.fontStyle = com.example.ui.theme.AppFontStyle.CLASSIC_SERIF
    assertEquals(com.example.ui.theme.AppFontStyle.CLASSIC_SERIF, prefs.fontStyle)

    prefs.fontStyle = com.example.ui.theme.AppFontStyle.TERMINAL_MONOSPACE
    assertEquals(com.example.ui.theme.AppFontStyle.TERMINAL_MONOSPACE, prefs.fontStyle)
  }
}
