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
  fun `test sequential series order of operations`() {
    // 10 + 5 ÷ 2 = (10 + 5) ÷ 2 = 7.5
    val result = CalculatorEngine.evaluate("10+5÷2")
    assertTrue(result is CalculationResult.Success)
    assertEquals(0, BigDecimal("7.5").compareTo((result as CalculationResult.Success).value))

    // 10 + 5 × 3 ÷ 5 = (10 + 5) × 3 ÷ 5 = 15 × 3 ÷ 5 = 45 ÷ 5 = 9
    val result2 = CalculatorEngine.evaluate("10+5×3÷5")
    assertTrue(result2 is CalculationResult.Success)
    assertEquals(0, BigDecimal("9").compareTo((result2 as CalculationResult.Success).value))
  }

  @Test
  fun `test decimal and percentage calculation`() {
    val result = CalculatorEngine.evaluate("500×15%")
    assertTrue(result is CalculationResult.Success)
    assertEquals(0, BigDecimal("75").compareTo((result as CalculationResult.Success).value))
  }
}
