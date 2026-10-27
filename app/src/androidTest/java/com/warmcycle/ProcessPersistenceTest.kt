package com.warmcycle

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.warmcycle.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/** Run with -e mode prepare, force-stop/start the app, then -e mode verify. Synthetic data only. */
@RunWith(AndroidJUnit4::class)
class ProcessPersistenceTest {
    @Test fun recordsAndSettingsSurviveProcessRestart() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<WarmCycleApp>()
        val repo = app.repository
        val mode = InstrumentationRegistry.getArguments().getString("mode", "roundtrip")
        val today = LocalDate.now().toEpochDay()
        if (mode != "verify") {
            repo.clear()
            repo.savePeriod(PeriodEntity(start = today - 51, end = today - 47))
            repo.savePeriod(PeriodEntity(start = today - 23, end = today - 19))
            repo.dao.putDaily(DailyEntity(today, "😌 平静", "", "", "测试记录：今天早点休息"))
            repo.preferences.save(Settings(ready = true, cycle = 29, duration = 6, theme = "dark"))
        }
        assertEquals(2, repo.dao.allPeriods().size)
        assertEquals("测试记录：今天早点休息", repo.dao.allDaily().single().note)
        val settings = repo.preferences.settings.first()
        assertTrue(settings.ready)
        assertEquals(29, settings.cycle)
        assertEquals("dark", settings.theme)
    }
}
