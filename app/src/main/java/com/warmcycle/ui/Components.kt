package com.warmcycle.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.*

@Composable fun LoadingScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
@Composable fun Heading(title: String, subtitle: String = "") {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (subtitle.isNotEmpty()) Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}
@Composable fun WarmCard(title: String, body: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (title.isNotEmpty()) Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            body()
        }
    }
}
@Composable fun Confirm(title: String, text: String, dismiss: () -> Unit, confirm: () -> Unit) {
    AlertDialog(onDismissRequest = dismiss, title = { Text(title) }, text = { Text(text) },
        confirmButton = { TextButton(onClick = { confirm(); dismiss() }) { Text("确认") } },
        dismissButton = { TextButton(onClick = dismiss) { Text("取消") } })
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun DatePicker(date: LocalDate, onDismiss: () -> Unit, onDate: (LocalDate) -> Unit) {
    val picker = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates { override fun isSelectableDate(utcTimeMillis: Long) =
            Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate() <= LocalDate.now() })
    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = {
        picker.selectedDateMillis?.let { onDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }; onDismiss()
    }) { Text("选择") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }) { DatePicker(state = picker) }
}
fun authenticators() = if (android.os.Build.VERSION.SDK_INT >= 30)
    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    else BiometricManager.Authenticators.BIOMETRIC_STRONG
fun authenticate(activity: FragmentActivity, success: () -> Unit, error: (String) -> Unit) {
    val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { success() }
        override fun onAuthenticationError(code: Int, errString: CharSequence) { error(errString.toString()) }
    })
    val info = BiometricPrompt.PromptInfo.Builder().setTitle("解锁暖期").setSubtitle("请验证设备身份")
        .setAllowedAuthenticators(authenticators())
    if (android.os.Build.VERSION.SDK_INT < 30) info.setNegativeButtonText("取消")
    prompt.authenticate(info.build())
}
@Composable fun LockScreen(activity: FragmentActivity, success: () -> Unit) {
    var error by remember { mutableStateOf("") }
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("🌷", style = MaterialTheme.typography.displayLarge)
            Heading("暖期已锁定", "只属于你们的记录")
            Button(onClick = { authenticate(activity, success) { error = it } }) { Text("解锁") }
            Text(error)
        }
    }
}
