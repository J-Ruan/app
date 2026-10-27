package com.warmcycle.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warmcycle.domain.Period
import java.time.LocalDate

@Entity(tableName = "periods")
data class PeriodEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val start: Long, val end: Long? = null,
    val createdAt: Long = System.currentTimeMillis()) {
    fun model() = Period(id, LocalDate.ofEpochDay(start), end?.let(LocalDate::ofEpochDay))
}
@Entity(tableName = "daily")
data class DailyEntity(@PrimaryKey val date: Long, val mood: String = "", val flow: String = "",
    val symptoms: String = "", val note: String = "")
@Dao
interface RecordsDao {
    @Query("SELECT * FROM periods ORDER BY start") fun periods(): Flow<List<PeriodEntity>>
    @Query("SELECT * FROM daily ORDER BY date DESC") fun daily(): Flow<List<DailyEntity>>
    @Query("SELECT * FROM periods ORDER BY start") suspend fun allPeriods(): List<PeriodEntity>
    @Query("SELECT * FROM daily ORDER BY date DESC") suspend fun allDaily(): List<DailyEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putPeriod(value: PeriodEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putDaily(value: DailyEntity)
    @Query("DELETE FROM periods WHERE id = :id") suspend fun deletePeriod(id: Long)
    @Query("DELETE FROM daily WHERE date = :date") suspend fun deleteDaily(date: Long)
    @Query("DELETE FROM periods") suspend fun clearPeriods()
    @Query("DELETE FROM daily") suspend fun clearDaily()
}
@Database(entities = [PeriodEntity::class, DailyEntity::class], version = 1, exportSchema = true)
abstract class WarmDatabase : RoomDatabase() { abstract fun records(): RecordsDao }
