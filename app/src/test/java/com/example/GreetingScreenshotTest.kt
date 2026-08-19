package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.screens.BasicCalculatorScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        BasicCalculatorScreen(
          expression = "1,500 + 350",
          liveResult = "1,850",
          isAllClear = false,
          showDoubleZeroKey = true,
          enableAnimations = true,
          hapticEnabled = true,
          onDigit = {},
          onDoubleZero = {},
          onOperator = {},
          onPercent = {},
          onClear = {},
          onBackspace = {},
          onEquals = {},
          onNegate = {},
          onCopyExpression = {},
          onCopyResult = {},
          onPaste = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
