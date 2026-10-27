package com.warmcycle.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.warmcycle.data.PeriodEntity
import com.warmcycle.domain.CycleCalculator

@Composable fun StatsScreen(state: AppState, edit: (PeriodEntity) -> Unit, delete: (Long) -> Unit) {
    val s = CycleCalculator.calculate(state.periods.map { it.model() }, state.settings.cycle, state.settings.duration)
    var deleting by remember { mutableStateOf<PeriodEntity?>(null) }
    var limit by remember { mutableIntStateOf(12) }
    Page {
        Heading("周期与回忆", "慢慢记录，慢慢了解")
        WarmCard("周期概览") {
            Text("平均周期　${s.averageCycle} 天", style = MaterialTheme.typography.titleLarge)
            Text("平均持续　${s.averageDuration} 天")
            Text("依据最近最多 6 个周期间隔 / 已结束经期；不足时使用设置中的默认值。", style = MaterialTheme.typography.bodySmall)
        }
        WarmCard("最近 6 次周期") {
            if (s.lengths.isEmpty()) Text("继续记录几次周期后，这里会出现趋势。")
            else s.lengths.forEachIndexed { index, days ->
                Text("${index + 1}　$days 天")
                LinearProgressIndicator(progress = { (days / 90f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
            }
            if (s.variable) Text("周期波动较大，请留意实际情况；预测仅供参考。")
        }
        Heading("历史周期")
        if (state.periods.isEmpty()) WarmCard("") { Text("还没有记录。从首页或日历写下第一次吧。") }
        state.periods.asReversed().take(limit).forEach { p ->
            WarmCard("${p.model().start} ～ ${p.model().end ?: "进行中"}") {
                p.end?.let { Text("持续 ${it - p.start + 1} 天") }
                val idx = state.periods.indexOf(p)
                state.periods.getOrNull(idx + 1)?.let { Text("至下次开始的周期：${it.start - p.start} 天") }
                Row { TextButton(onClick = { edit(p) }) { Text("查看 / 修改") }; TextButton(onClick = { deleting = p }) { Text("删除") } }
            }
        }
        if (state.periods.size > limit) TextButton(onClick = { limit += 12 }) { Text("显示更多") }
        Text("本工具提供生活记录和估算，不替代医疗建议。", style = MaterialTheme.typography.bodySmall)
    }
    deleting?.let { p -> Confirm("删除这次生理期？", "${p.model().start} 的经期将被删除，预测会重新计算，每日记录会保留。", { deleting = null }) { delete(p.id) } }
}
