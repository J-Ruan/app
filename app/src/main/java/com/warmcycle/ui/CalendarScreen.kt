package com.warmcycle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.warmcycle.data.PeriodEntity
import com.warmcycle.domain.CycleCalculator
import java.time.*

@Composable fun CalendarScreen(state: AppState, onRecord: (LocalDate) -> Unit, onPeriod: (PeriodEntity?, LocalDate) -> Unit) {
    var monthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    var selectedText by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val month = YearMonth.parse(monthText); val selected = LocalDate.parse(selectedText)
    val summary = CycleCalculator.calculate(state.periods.map { it.model() }, state.settings.cycle, state.settings.duration)
    val today = LocalDate.now()
    fun periodAt(d: LocalDate) = state.periods.find { d.toEpochDay() >= it.start && d.toEpochDay() <= (it.end ?: today.toEpochDay()) }
    val actual = periodAt(selected)
    val daily = state.daily.find { it.date == selected.toEpochDay() }
    Page {
        Heading("日历", "每个被记住的日子，都有温度")
        WarmCard("") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { monthText = month.minusMonths(1).toString() }) { Text("‹ 上月") }
                Text("${month.year} 年 ${month.monthValue} 月", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { monthText = month.plusMonths(1).toString() }) { Text("下月 ›") }
            }
            Row { listOf("一", "二", "三", "四", "五", "六", "日").forEach { Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text(it) } } }
            val offset = month.atDay(1).dayOfWeek.value - 1
            val cells = ((offset + month.lengthOfMonth() + 6) / 7) * 7
            repeat(cells / 7) { row -> Row(Modifier.fillMaxWidth()) {
                repeat(7) { column ->
                    val day = row * 7 + column - offset + 1
                    Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                        if (day in 1..month.lengthOfMonth()) {
                            val d = month.atDay(day)
                            val recorded = periodAt(d) != null
                            val predicted = !recorded && summary.next?.let { !d.isBefore(it) && d.isBefore(it.plusDays(summary.averageDuration.toLong())) } == true
                            val bg = when { d == selected -> MaterialTheme.colorScheme.primary; recorded -> MaterialTheme.colorScheme.primaryContainer; predicted -> MaterialTheme.colorScheme.secondaryContainer; else -> Color.Transparent }
                            Box(Modifier.fillMaxSize().background(bg, CircleShape)
                                .then(if (d == today) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                                .clickable { selectedText = d.toString() }, contentAlignment = Alignment.Center) {
                                Text(day.toString(), color = if (d == selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            } }
            Text("粉色 · 已记录　浅绿 · 预计　圆圈 · 今天", style = MaterialTheme.typography.labelSmall)
            TextButton(onClick = { monthText = YearMonth.now().toString(); selectedText = today.toString() }) { Text("回到今天") }
        }
        WarmCard(selected.toString()) {
            Text(if (actual == null) "这天没有生理期记录" else "已记录生理期 · ${actual.model().start} 至 ${actual.model().end ?: "进行中"}")
            if (daily == null) Text("尚未写下当天感受") else {
                Text(listOf(daily.mood, daily.flow.takeIf(String::isNotEmpty)?.let { "经量：$it" } ?: "", daily.symptoms.replace("|", " · "), daily.note).filter(String::isNotEmpty).joinToString("\n"))
            }
            if (!selected.isAfter(today)) {
                Button(onClick = { onRecord(selected) }, modifier = Modifier.fillMaxWidth()) { Text("${if (daily == null) "记录" else "编辑"}当天感受") }
                TextButton(onClick = { onPeriod(actual, selected) }) { Text(if (actual == null) "补录生理期" else "修改本次生理期") }
            } else Text("未来日期仅展示预测，实际记录请在当天或之后填写。")
        }
        Text("预测仅供参考，不用于诊断或避孕。", style = MaterialTheme.typography.bodySmall)
    }
}
