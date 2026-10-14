package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.security.AppLockState
import com.example.domain.util.HapticFeedbackHelper
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.HapticIntensity
import kotlinx.coroutines.delay

/** Which drawer destinations share one lock switch. Mixture history is part of the Mixture Calculator. */
fun DrawerDestination.lockKey(): String =
    if (this == DrawerDestination.MIXTURE_HISTORY) DrawerDestination.MIXTURE_CALCULATOR.name else name

private val lockableSections = listOf(
    DrawerDestination.CALCULATOR to "Standard Calculator",
    DrawerDestination.LEDGER to "Daily Ledger / Cashbook",
    DrawerDestination.EXPENSES to "Expenses",
    DrawerDestination.SALES to "Sales",
    DrawerDestination.MIXTURE_CALCULATOR to "Mixture Calculator & History",
    DrawerDestination.CONVERTER to "Converter",
    DrawerDestination.SETTINGS to "Settings"
)

/** Row of dots that fill as digits are typed. */
@Composable
private fun PinDots(entered: Int, total: Int, error: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(total) { i ->
            val filled = i < entered
            Box(
                modifier = Modifier.size(22.dp).softWell(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (filled) {
                    SkeuoLed(
                        color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        size = 10.dp
                    )
                }
            }
        }
    }
}

/** Soft number pad: 1-9, then a blank, 0 and backspace. */
@Composable
private fun PinPad(onDigit: (Char) -> Unit, onBackspace: () -> Unit, enabled: Boolean = true) {
    val context = LocalContext.current
    val view = LocalView.current
    val tap = { HapticFeedbackHelper.performKeypadTap(context, view, HapticIntensity.LIGHT) }
    val rows = listOf("123", "456", "789")
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                row.forEach { d ->
                    PinKey(label = d.toString(), enabled = enabled, testTag = "pin_key_$d") { tap(); onDigit(d) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Spacer(modifier = Modifier.size(68.dp))
            PinKey(label = "0", enabled = enabled, testTag = "pin_key_0") { tap(); onDigit('0') }
            SoftSurface(
                onClick = { tap(); onBackspace() },
                enabled = enabled,
                shape = CircleShape,
                modifier = Modifier.size(68.dp).testTag("pin_key_backspace")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete digit", modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun PinKey(label: String, enabled: Boolean, testTag: String, onClick: () -> Unit) {
    SoftSurface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        modifier = Modifier.size(68.dp).testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontSize = 26.sp, fontWeight = FontWeight.Medium, fontFamily = DigitalMonospaceFont)
        }
    }
}

/** Full-screen gate shown in place of a locked section. */
@Composable
fun AppLockScreen(lock: AppLockState, sectionLabel: String, modifier: Modifier = Modifier) {
    var entry by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var lockoutSeconds by remember { mutableStateOf(lock.remainingLockoutSeconds()) }

    // Count the cooldown down once a second
    LaunchedEffect(lockoutSeconds) {
        if (lockoutSeconds > 0) {
            delay(1000)
            lockoutSeconds = lock.remainingLockoutSeconds()
        }
    }
    LaunchedEffect(error) {
        if (error) {
            delay(450)
            error = false
        }
    }

    fun submit(pin: String) {
        when (lock.unlock(pin)) {
            AppLockState.Result.OK -> Unit // the gate disappears as soon as the session unlocks
            AppLockState.Result.WRONG -> { error = true; entry = "" }
            AppLockState.Result.LOCKED_OUT -> { error = true; entry = ""; lockoutSeconds = lock.remainingLockoutSeconds() }
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp).testTag("app_lock_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(64.dp).softPlate(CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Locked", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            when {
                lockoutSeconds > 0 -> "Too many attempts. Try again in ${lockoutSeconds}s."
                error -> "Incorrect PIN"
                else -> "Enter your PIN to open $sectionLabel"
            },
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = if (error || lockoutSeconds > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 22.dp)
        )
        PinDots(entered = entry.length, total = lock.pinLength, error = error)
        Spacer(modifier = Modifier.height(28.dp))
        PinPad(
            enabled = lockoutSeconds == 0,
            onDigit = { d ->
                if (entry.length < lock.pinLength) {
                    entry += d
                    if (entry.length == lock.pinLength) submit(entry)
                }
            },
            onBackspace = { entry = entry.dropLast(1) }
        )
    }
}

private enum class PinStage { CURRENT, NEW, CONFIRM }
enum class PinAction { SET, CHANGE, REMOVE }

/** Step-by-step dialog for setting, changing or removing the PIN. */
@Composable
private fun PinDialog(lock: AppLockState, action: PinAction, onDismiss: () -> Unit) {
    var stage by remember { mutableStateOf(if (action == PinAction.SET) PinStage.NEW else PinStage.CURRENT) }
    var entry by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val maxLen = if (stage == PinStage.CURRENT) lock.pinLength else AppLockState.MAX_LENGTH

    fun advance() {
        when (stage) {
            PinStage.CURRENT -> when (lock.verify(entry)) {
                AppLockState.Result.OK -> {
                    if (action == PinAction.REMOVE) { lock.removePin(); onDismiss() }
                    else { stage = PinStage.NEW; entry = ""; message = null }
                }
                AppLockState.Result.WRONG -> { message = "Incorrect PIN"; entry = "" }
                AppLockState.Result.LOCKED_OUT -> { message = "Too many attempts. Wait ${lock.remainingLockoutSeconds()}s."; entry = "" }
            }
            PinStage.NEW -> {
                if (entry.length < AppLockState.MIN_LENGTH) {
                    message = "Use at least ${AppLockState.MIN_LENGTH} digits"
                } else {
                    newPin = entry; entry = ""; message = null; stage = PinStage.CONFIRM
                }
            }
            PinStage.CONFIRM -> {
                if (entry == newPin) { lock.setPin(newPin); onDismiss() }
                else { message = "PINs don't match. Start again."; entry = ""; newPin = ""; stage = PinStage.NEW }
            }
        }
    }

    SoftAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (stage) {
                    PinStage.CURRENT -> if (action == PinAction.REMOVE) "Enter PIN to remove" else "Enter current PIN"
                    PinStage.NEW -> if (action == PinAction.CHANGE) "Enter new PIN" else "Choose a PIN"
                    PinStage.CONFIRM -> "Confirm PIN"
                }
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                PinDots(
                    entered = entry.length,
                    total = if (stage == PinStage.CURRENT) lock.pinLength else maxOf(entry.length, AppLockState.MIN_LENGTH),
                    error = message != null
                )
                Text(
                    message ?: if (stage == PinStage.NEW) "${AppLockState.MIN_LENGTH}-${AppLockState.MAX_LENGTH} digits" else " ",
                    fontSize = 12.sp,
                    color = if (message != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                PinPad(
                    onDigit = { d ->
                        if (entry.length < maxLen) {
                            entry += d; message = null
                            if (stage == PinStage.CURRENT && entry.length == lock.pinLength) advance()
                        }
                    },
                    onBackspace = { entry = entry.dropLast(1) }
                )
            }
        },
        confirmButton = {
            if (stage != PinStage.CURRENT) {
                SoftButton(
                    onClick = { advance() },
                    enabled = entry.length >= AppLockState.MIN_LENGTH,
                    accentColor = MaterialTheme.colorScheme.primary,
                    filled = true
                ) { Text(if (stage == PinStage.CONFIRM) "Save PIN" else "Next") }
            }
        },
        dismissButton = { SoftButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** Settings block: set the PIN and choose which sidebar sections ask for it. */
@Composable
fun AppLockSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lock = remember { AppLockState.get(context) }
    var dialogAction by remember { mutableStateOf<PinAction?>(null) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text("App Lock", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                if (lock.hasPin) "PIN is set. Tick the sections that should ask for it."
                else "Set a PIN, then choose which sections it protects.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            if (!lock.hasPin) {
                SoftButton(
                    onClick = { dialogAction = PinAction.SET },
                    accentColor = MaterialTheme.colorScheme.primary,
                    filled = true,
                    modifier = Modifier.weight(1f).testTag("app_lock_set_pin")
                ) { Text("Set PIN code", fontSize = 13.sp) }
            } else {
                SoftButton(
                    onClick = { dialogAction = PinAction.CHANGE },
                    modifier = Modifier.weight(1f).testTag("app_lock_change_pin")
                ) { Text("Change PIN", fontSize = 13.sp) }
                SoftButton(
                    onClick = { dialogAction = PinAction.REMOVE },
                    modifier = Modifier.weight(1f).testTag("app_lock_remove_pin")
                ) { Text("Remove PIN", fontSize = 13.sp) }
            }
        }

        Text("Lock these sections", fontSize = 13.sp, fontWeight = FontWeight.Medium)
        lockableSections.forEach { (destination, label) ->
            val key = destination.name
            val checked = key in lock.lockedSections
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = lock.hasPin) { lock.setSectionLocked(key, !checked) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SoftCheckbox(
                    checked = checked,
                    onCheckedChange = { lock.setSectionLocked(key, it) },
                    enabled = lock.hasPin,
                    modifier = Modifier.testTag("app_lock_check_$key")
                )
                Text(
                    label,
                    fontSize = 14.sp,
                    color = if (lock.hasPin) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (checked) {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text("Lock again when app is closed", fontSize = 14.sp)
                Text(
                    "Ask for the PIN after the app has been in the background",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SoftSwitch(
                checked = lock.relockOnBackground,
                onCheckedChange = { lock.updateRelockOnBackground(it) },
                enabled = lock.hasPin
            )
        }

        Text(
            "There is no PIN recovery. If you forget it, the only way back in is to clear the app's data, which erases your saved records.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    dialogAction?.let { action -> PinDialog(lock, action) { dialogAction = null } }
}
