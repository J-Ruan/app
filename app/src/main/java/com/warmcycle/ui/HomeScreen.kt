package com.warmcycle.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.warmcycle.data.PeriodEntity
import com.warmcycle.domain.CycleCalculator
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Composable fun HomeScreen(state: AppState, onRecord: () -> Unit, onPeriod: (PeriodEntity?) -> Unit, onEndPeriod: (PeriodEntity) -> Unit) {
    val today = LocalDate.now()
    val s = CycleCalculator.calculate(state.periods.map { it.model() }, state.settings.cycle, state.settings.duration, today)
    val left = s.next?.let { ChronoUnit.DAYS.between(today, it) }
    val greeting = when (LocalTime.now().hour) { in 5..11 -> "早上好"; in 12..17 -> "下午好"; else -> "晚上好" }
    Page {
        Heading("暖期", "$greeting · 让关心成为日常")
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (s.activeDay != null) "这一段日子，慢慢来" else "下一次重要日子")
                Text(when { s.activeDay != null -> "生理期第 ${s.activeDay} 天"; left == null -> "从一条记录开始"; left > 0 -> "预计还有 $left 天"; left == 0L -> "今天可能开始"; else -> "预计日期已过 ${-left} 天" },
                    style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(if (s.next == null) "记下最近一次开始日期，慢慢了解周期。" else "预计 ${s.next} 左右 · 仅供参考")
                s.day?.let { Text("当前周期第 $it 天") }
                if (left != null && left < 0) Text("请核对并补充实际记录，预测可能存在偏差。")
                Button(onClick = onRecord, modifier = Modifier.fillMaxWidth()) { Text("记录今天") }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("平均周期" to s.averageCycle, "平均持续" to s.averageDuration).forEach { (label, value) ->
                Card(Modifier.weight(1f)) { Column(Modifier.padding(20.dp)) { Text(label); Text("$value 天", style = MaterialTheme.typography.headlineSmall) } }
            }
        }
        WarmCard("🌿 今日关怀") {
            Text(if (s.activeDay != null) "如果她觉得疲惫，可以多一点耐心和陪伴。先问问她需要什么，尊重她自己的感受。" else "没有特别需要提醒的事情。听听她今天的心情，一起留一点时间给休息。")
            Text("持续或明显不适，请咨询专业医疗人员。", style = MaterialTheme.typography.bodySmall)
        }
        WarmCard("快速记录") {
            val open = state.periods.lastOrNull()?.takeIf { it.end == null }
            if (open == null) OutlinedButton(onClick = { onPeriod(null) }, modifier = Modifier.fillMaxWidth()) { Text("今天开始 / 补录经期") }
            else {
                OutlinedButton(onClick = { onEndPeriod(open) }, modifier = Modifier.fillMaxWidth()) { Text("今天结束 · 一键记录") }
                TextButton(onClick = { onPeriod(open) }) { Text("修改本次日期") }
            }
            Text("最近开始：${state.periods.lastOrNull()?.model()?.start ?: "暂无记录"}")
        }
        Text(if (s.lengths.size < 3) "记录更多周期后，预测会更加稳定。当前不足 3 个完整周期间隔，使用已有记录或默认值估算。" else if (s.variable) "最近周期波动较大，预计日期可能存在较大偏差。" else "预测仅供参考，不用于医疗诊断或避孕。", style = MaterialTheme.typography.bodySmall)
    }
}
