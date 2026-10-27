package com.warmcycle

import com.warmcycle.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CycleCalculatorTest {
    private val base = LocalDate.of(2026, 1, 1)
    @Test fun emptyHasNoPrediction() { val s = CycleCalculator.calculate(emptyList()); assertNull(s.next); assertEquals(28, s.averageCycle) }
    @Test fun averagesActualStartIntervalsAndInclusiveDurations() {
        val ps = listOf(Period(1, base, base.plusDays(4)), Period(2, base.plusDays(28), base.plusDays(33)), Period(3, base.plusDays(57), base.plusDays(61)))
        val s = CycleCalculator.calculate(ps, today = base.plusDays(65))
        assertEquals(listOf(28, 29), s.lengths); assertEquals(29, s.averageCycle); assertEquals(5, s.averageDuration)
        assertEquals(base.plusDays(86), s.next); assertEquals(9, s.day); assertNull(s.activeDay)
    }
    @Test fun overdueDoesNotSilentlyRollForward() {
        val s = CycleCalculator.calculate(listOf(Period(1, base, base.plusDays(4))), today = base.plusDays(70))
        assertEquals(base.plusDays(28), s.next)
    }
    @Test fun activeIsOnlyAnActualRecord() {
        assertEquals(3, CycleCalculator.calculate(listOf(Period(1, base)), today = base.plusDays(2)).activeDay)
        assertNull(CycleCalculator.calculate(listOf(Period(1, base, base.plusDays(4))), today = base.plusDays(28)).activeDay)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsOverlap() { CycleCalculator.validate(listOf(Period(1, base, base.plusDays(5)), Period(2, base.plusDays(5)))) }
    @Test(expected = IllegalArgumentException::class) fun rejectsOpenPeriodBeforeAnother() { CycleCalculator.validate(listOf(Period(1, base), Period(2, base.plusDays(28)))) }
    @Test(expected = IllegalArgumentException::class) fun rejectsReversedDates() { CycleCalculator.validate(listOf(Period(1, base, base.minusDays(1)))) }
    @Test(expected = IllegalArgumentException::class) fun rejectsFuture() { CycleCalculator.validate(listOf(Period(1, LocalDate.now().plusDays(1)))) }
    @Test fun irregularCyclesAreMarked() {
        val s = CycleCalculator.calculate(listOf(Period(1, base, base.plusDays(4)), Period(2, base.plusDays(20), base.plusDays(24)), Period(3, base.plusDays(55))), today = base.plusDays(57))
        assertTrue(s.variable)
    }
}
