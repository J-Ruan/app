package com.warmcycle.domain

import java.time.LocalDate
import kotlin.math.roundToInt

data class Period(val id: Long = 0, val start: LocalDate, val end: LocalDate? = null)
data class CycleSummary(val lengths: List<Int>, val averageCycle: Int, val averageDuration: Int,
    val next: LocalDate?, val day: Int?, val activeDay: Int?, val variable: Boolean)

object CycleCalculator {
    fun calculate(periods: List<Period>, defaultCycle: Int = 28, defaultDuration: Int = 5,
                  today: LocalDate = LocalDate.now()): CycleSummary {
        val sorted = periods.sortedBy { it.start }
        val lengths = sorted.zipWithNext { a, b -> (b.start.toEpochDay() - a.start.toEpochDay()).toInt() }
        val recent = lengths.takeLast(6)
        val durations = sorted.mapNotNull { p -> p.end?.let { (it.toEpochDay() - p.start.toEpochDay() + 1).toInt() } }.takeLast(6)
        val cycle = if (recent.isEmpty()) defaultCycle else recent.average().roundToInt()
        val duration = if (durations.isEmpty()) defaultDuration else durations.average().roundToInt()
        val last = sorted.lastOrNull()
        // Do not roll an overdue prediction forward: a missing period must never become a recorded one.
        val active = last?.takeIf { !today.isBefore(it.start) && (it.end == null || !today.isAfter(it.end)) }
        return CycleSummary(lengths.takeLast(6), cycle, duration, last?.start?.plusDays(cycle.toLong()),
            last?.let { (today.toEpochDay() - it.start.toEpochDay() + 1).toInt() },
            active?.let { (today.toEpochDay() - it.start.toEpochDay() + 1).toInt() },
            recent.size > 1 && recent.max() - recent.min() > 7)
    }
    fun validate(periods: List<Period>, today: LocalDate = LocalDate.now()) {
        val sorted = periods.sortedBy { it.start }
        require(sorted.all { !it.start.isAfter(today) && (it.end == null || (!it.end.isBefore(it.start) && !it.end.isAfter(today))) }) { "日期应在今天或之前，结束日期不能早于开始日期" }
        sorted.zipWithNext().forEach { (a, b) ->
            require(a.end != null && a.end.isBefore(b.start)) { "经期不能重叠，请先结束或修改上一条记录" }
        }
    }
}
