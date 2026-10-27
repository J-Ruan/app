package com.warmcycle.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.warmcycle.data.PeriodEntity
import com.warmcycle.domain.*
import java.time.LocalDate

@Composable fun PeriodDialog(existing: PeriodEntity?, date: LocalDate, periods: List<PeriodEntity>, dismiss: () -> Unit,
    save: (Long, LocalDate, LocalDate?) -> Unit) {
    var start by rememberSaveable { mutableStateOf(existing?.model()?.start?.toString() ?: date.toString()) }
    var end by rememberSaveable { mutableStateOf(existing?.model()?.end?.toString() ?: LocalDate.now().toString()) }
    var ended by rememberSaveable { mutableStateOf(existing?.end != null) }
    var picker by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = dismiss, title = { Text(if (existing == null) "记录生理期" else "修改这次记录") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { picker = 1 }) { Text("开始：$start") }
            Row { Checkbox(ended, { ended = it }); Text("已经结束", Modifier.padding(top = 12.dp)) }
            if (ended) OutlinedButton(onClick = { picker = 2 }) { Text("结束：$end") }
            if (existing != null && existing.end == null) TextButton(onClick = { end = LocalDate.now().toString(); ended = true }) { Text("今天结束") }
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = { TextButton(onClick = {
        val s = LocalDate.parse(start); val e = if (ended) LocalDate.parse(end) else null
        try { CycleCalculator.validate(periods.filter { it.id != existing?.id }.map { it.model() } + Period(existing?.id ?: 0, s, e)); save(existing?.id ?: 0, s, e); dismiss() }
        catch (ex: IllegalArgumentException) { error = ex.message ?: "请核对日期" }
    }) { Text("保存") } }, dismissButton = { TextButton(onClick = dismiss) { Text("取消") } })
    if (picker != 0) DatePicker(LocalDate.parse(if (picker == 1) start else end), { picker = 0 }) { if (picker == 1) start = it.toString() else end = it.toString() }
}
