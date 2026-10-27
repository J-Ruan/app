package com.warmcycle

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import android.content.Context
import com.warmcycle.data.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RepositoryTest {
    private lateinit var db: WarmDatabase
    private lateinit var repo: Repository
    private val context: Context = ApplicationProvider.getApplicationContext()
    @Before fun before() { context.deleteDatabase("repository-test"); open() }
    private fun open() {
        db = Room.databaseBuilder(context, WarmDatabase::class.java, "repository-test").build()
        repo = Repository(db, (context as WarmCycleApp).repository.preferences)
    }
    @After fun after() { db.close(); context.deleteDatabase("repository-test") }
    @Test fun persistsAcrossReopenAndBackupRestoresAtomically() = runBlocking {
        val start = LocalDate.now().minusDays(30).toEpochDay()
        repo.savePeriod(PeriodEntity(start = start, end = start + 4))
        repo.dao.putDaily(DailyEntity(start, "😌 平静", "正常", "腰酸", "想喝热饮"))
        db.close(); open()
        assertEquals(1, repo.dao.allPeriods().size)
        assertEquals("想喝热饮", repo.dao.allDaily().single().note)
        val backup = repo.export()
        repo.db.withTransactionForTestClear()
        repo.importBackup(backup)
        assertEquals(1, repo.dao.allPeriods().size)
        assertEquals("腰酸", repo.dao.allDaily().single().symptoms)
        try { repo.importBackup(backup.replace("WarmCycle", "Invalid")); fail("Must reject malformed backup") } catch (_: IllegalArgumentException) { }
        assertEquals(1, repo.dao.allPeriods().size)
        val p = repo.dao.allPeriods().single()
        repo.savePeriod(p.copy(end = start + 5))
        assertEquals(start + 5, repo.dao.allPeriods().single().end)
        repo.dao.deletePeriod(p.id); assertTrue(repo.dao.allPeriods().isEmpty())
        repo.dao.clearDaily(); assertTrue(repo.dao.allDaily().isEmpty())
    }
    private suspend fun WarmDatabase.withTransactionForTestClear() { records().clearPeriods(); records().clearDaily() }
}
