package com.warmcycle.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.warmcycle.data.*
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable fun Chips(values: List<String>, selected: Set<String>, toggle: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { FilterChip(selected = it in selected, onClick = { toggle(it) }, label = { Text(it) }) }
    }
}
@Composable fun DailyScreen(date: LocalDate, state: AppState, vm: AppViewModel) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val old = state.daily.find { it.date == date.toEpochDay() }
    var mood by rememberSaveable(date.toString(), old) { mutableStateOf(old?.mood ?: "") }
    var flow by rememberSaveable(date.toString(), old) { mutableStateOf(old?.flow ?: "") }
    var symptoms by rememberSaveable(date.toString(), old) { mutableStateOf(old?.symptoms ?: "") }
    var note by rememberSaveable(date.toString(), old) { mutableStateOf(old?.note ?: "") }
    var confirm by remember { mutableStateOf(false) }
    val busy by vm.busy.collectAsState()
    Column(Modifier.fillMaxSize()) {
        Surface(shadowElevation = 1.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(date.toString(), style = MaterialTheme.typography.labelLarge)
                Button(onClick = { focus.clearFocus(); keyboard?.hide(); vm.daily(DailyEntity(date.toEpochDay(), mood, flow, symptoms, note)) }, enabled = !busy) { Text("保存这一天") }
            }
        }
        Box(Modifier.weight(1f)) {
            Page {
                Heading(if (date == LocalDate.now()) "记录今天" else "记下这一天", date.toString())
                WarmCard("此刻的心情") { Chips(listOf("😊 开心", "😌 平静", "😐 一般", "😣 烦躁", "😔 低落", "😴 疲惫"), setOf(mood)) { mood = if (mood == it) "" else it } }
                WarmCard("身体的小信号") { Chips(Repository.symptoms, symptoms.split("|").toSet()) { value ->
                    val set = symptoms.split("|").filter(String::isNotEmpty).toMutableSet()
                    if (!set.add(value)) set.remove(value)
                    symptoms = set.joinToString("|")
                } }
                WarmCard("经量 · 可不填") { Chips(listOf("少", "正常", "多"), setOf(flow)) { flow = if (flow == it) "" else it } }
                WarmCard("今天的备注") {
                    OutlinedTextField(note, { note = it.take(4000) }, placeholder = { Text("今天有点累，想喝一杯热饮……") },
                        modifier = Modifier.fillMaxWidth(), minLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focus.clearFocus(); keyboard?.hide() }), supportingText = { Text("${note.length}/4000 · 只保存在本地") })
                }
                if (old != null) TextButton(onClick = { confirm = true }, enabled = !busy) { Text("删除当天记录") }
            }
        }
    }
    if (confirm) Confirm("删除当天记录？", "仅删除这天的情绪、身体状态和备注，不影响生理期。", { confirm = false }) { vm.deleteDaily(date.toEpochDay()) }
}
