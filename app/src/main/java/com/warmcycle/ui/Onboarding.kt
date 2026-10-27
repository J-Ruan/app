package com.warmcycle.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable fun Onboarding(busy: Boolean, finish: (LocalDate?, Int, Int, LocalDate?) -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    var consent by rememberSaveable { mutableStateOf(false) }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var unknown by rememberSaveable { mutableStateOf(false) }
    var cycle by rememberSaveable { mutableStateOf("28") }
    var duration by rememberSaveable { mutableStateOf("5") }
    var picker by remember { mutableIntStateOf(0) }
    var ended by rememberSaveable { mutableStateOf(false) }
    var end by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    Page {
        Spacer(Modifier.height(24.dp))
        Text("🌷", style = MaterialTheme.typography.displayLarge)
        Heading(if (page == 0) "暖期" else "从了解开始", if (page == 0) "记住她的重要日子" else "简单记录，温柔陪伴")
        if (page == 0) {
            WarmCard("留一点心意给日常") { Text("用日历记下周期，也记下今天的心情。预测仅供参考，不用于诊断或避孕。") }
            WarmCard("隐私优先") { Text("暖期默认将记录保存在你的设备中，不会自动上传到服务器。请仅在对方知情并同意的情况下记录相关信息。") }
            Row { Checkbox(consent, { consent = it }); Text("我已阅读隐私说明，并确认已取得对方知情同意。", Modifier.padding(top = 12.dp)) }
            Button(onClick = { page = 1 }, enabled = consent, modifier = Modifier.fillMaxWidth()) { Text("开始使用") }
        } else {
            WarmCard("最近一次生理期") {
                OutlinedButton(onClick = { picker = 1 }, enabled = !unknown) { Text("开始日期：$date") }
                Row { Checkbox(unknown, { unknown = it }); Text("暂不确定，稍后记录", Modifier.padding(top = 12.dp)) }
                if (!unknown) {
                    Row { Checkbox(ended, { ended = it }); Text("这次已经结束", Modifier.padding(top = 12.dp)) }
                    if (ended) OutlinedButton(onClick = { picker = 2 }) { Text("结束日期：$end") }
                }
                Text("持续天数仅作预测默认值，不会自动结束实际经期。", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedTextField(cycle, { cycle = it.filter(Char::isDigit).take(2) }, label = { Text("通常周期（15–90 天）") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(duration, { duration = it.filter(Char::isDigit).take(2) }, label = { Text("通常持续（1–20 天）") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Text("如果不确定，可以稍后修改。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = { finish(if (unknown) null else LocalDate.parse(date), cycle.toInt(), duration.toInt(), if (!unknown && ended) LocalDate.parse(end) else null) },
                enabled = !busy && cycle.toIntOrNull() in 15..90 && duration.toIntOrNull() in 1..20,
                modifier = Modifier.fillMaxWidth()) { Text("进入暖期") }
            TextButton(onClick = { page = 0 }) { Text("返回隐私说明") }
        }
    }
    if (picker != 0) DatePicker(LocalDate.parse(if (picker == 1) date else end), { picker = 0 }) { if (picker == 1) date = it.toString() else end = it.toString() }
}
