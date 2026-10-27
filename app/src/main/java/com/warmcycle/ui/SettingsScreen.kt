package com.warmcycle.ui

import android.Manifest
import android.os.Build
import android.app.NotificationManager
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import android.net.Uri

@Composable fun SettingSwitch(title: String, checked: Boolean, changed: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(title, Modifier.weight(1f).padding(top = 12.dp)); Switch(checked, changed) }
}
@Composable fun SettingsScreen(state: AppState, vm: AppViewModel, snackbar: SnackbarHostState) {
    val s = state.settings
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cycle by rememberSaveable(s.cycle) { mutableStateOf(s.cycle.toString()) }
    var duration by rememberSaveable(s.duration) { mutableStateOf(s.duration.toString()) }
    var clear by remember { mutableStateOf(false) }
    var importUri by rememberSaveable { mutableStateOf<String?>(null) }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { it?.let(vm::export) }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { importUri = it?.toString() }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.settings(s.copy(reminder = true))
        else scope.launch { snackbar.showSnackbar("未获得通知权限，提醒保持关闭。可在系统设置中开启。") }
    }
    Page {
        Heading("设置", "让暖期更适合你们")
        WarmCard("基础设置") {
            OutlinedTextField(cycle, { cycle = it.filter(Char::isDigit).take(2) }, label = { Text("默认周期 · 15–90 天") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(duration, { duration = it.filter(Char::isDigit).take(2) }, label = { Text("默认持续 · 1–20 天") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text("只在对应历史数据不足时使用，不会修改实际记录。", style = MaterialTheme.typography.bodySmall)
            Button(onClick = { vm.settings(s.copy(cycle = cycle.toInt(), duration = duration.toInt())) }, enabled = cycle.toIntOrNull() in 15..90 && duration.toIntOrNull() in 1..20) { Text("保存默认天数") }
        }
        WarmCard("温柔提醒") {
            SettingSwitch("预计生理期提醒", s.reminder) { enabled ->
                if (!enabled) vm.settings(s.copy(reminder = false))
                else if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                    permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                else if (!context.getSystemService(NotificationManager::class.java).areNotificationsEnabled()) scope.launch { snackbar.showSnackbar("系统通知已关闭，请先在系统设置中允许暖期通知。") }
                else vm.settings(s.copy(reminder = true))
            }
            Text("提前几天")
            Chips(listOf("1 天", "2 天", "3 天"), setOf("${s.lead} 天")) { vm.settings(s.copy(lead = it.first().digitToInt())) }
            SettingSwitch("隐私通知", s.privateNotification) { vm.settings(s.copy(privateNotification = it)) }
            Text("隐私通知仅显示“暖期有一条提醒”。后台约每 12 小时检查，时间可能受系统省电影响；不是精确闹钟。", style = MaterialTheme.typography.bodySmall)
        }
        WarmCard("外观") {
            Chips(listOf("跟随系统", "浅色", "深色"), setOf(when (s.theme) { "light" -> "浅色"; "dark" -> "深色"; else -> "跟随系统" })) {
                vm.settings(s.copy(theme = when (it) { "浅色" -> "light"; "深色" -> "dark"; else -> "system" }))
            }
        }
        WarmCard("隐私与数据") {
            SettingSwitch("应用锁", s.lock) { enabled ->
                val activity = context as? FragmentActivity
                if (!enabled) vm.settings(s.copy(lock = false))
                else if (activity != null && BiometricManager.from(context).canAuthenticate(authenticators()) == BiometricManager.BIOMETRIC_SUCCESS)
                    authenticate(activity, { vm.settings(s.copy(lock = true)) }) { message -> scope.launch { snackbar.showSnackbar(message) } }
                else scope.launch { snackbar.showSnackbar("请先在设备中设置受支持的生物识别；Android 11 起也支持设备锁屏凭据。") }
            }
            Text("切到后台后重新验证。未实现独立 PIN；请保管好设备解锁凭据。界面禁止截屏和任务预览。", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { export.launch("WarmCycle-${java.time.LocalDate.now()}.json") }, modifier = Modifier.fillMaxWidth()) { Text("导出数据") }
            OutlinedButton(onClick = { import.launch(arrayOf("application/json", "text/plain")) }, modifier = Modifier.fillMaxWidth()) { Text("导入备份") }
            Text("备份为明文 JSON，包含私密记录；请使用可信的保存位置。导入将替换全部记录，不改变设置。系统文件选择器中的云存储由你自行选择，暖期不会自动上传。", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { clear = true }) { Text("清除全部数据") }
        }
        WarmCard("关于暖期") {
            Text("WarmCycle · v1.0.0\n记住她的重要日子")
            Text("只在双方知情和同意的前提下记录。数据默认仅存设备，无账号、广告或追踪，不自动上传；系统备份关闭。本地数据库未额外加密，设备解锁后请注意数据安全。")
            Text("预测仅供参考，不用于医疗诊断、排卵判断或避孕。若出现持续或明显不适，请咨询专业医疗人员。")
            Text("GitHub · github.com/J-Ruan/app", style = MaterialTheme.typography.bodySmall)
        }
    }
    if (clear) Confirm("清除全部数据？", "所有周期、每日记录和设置将永久删除，并取消提醒。已导出的备份需要你自行删除。", { clear = false }, vm::clear)
    importUri?.let { uri -> Confirm("替换全部记录？", "导入前建议先导出备份。文件通过校验后才会替换现有周期和每日记录。", { importUri = null }) { vm.import(Uri.parse(uri)) } }
}
