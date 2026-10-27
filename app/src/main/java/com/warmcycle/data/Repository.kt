package com.warmcycle.data

import androidx.room.withTransaction
import com.warmcycle.domain.CycleCalculator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class Repository(val db: WarmDatabase, val preferences: Preferences) {
    val dao = db.records()
    private val mutex = Mutex()
    suspend fun savePeriod(value: PeriodEntity) = mutex.withLock {
        db.withTransaction {
            val all = dao.allPeriods()
            val others = all.filter { it.id != value.id }
            CycleCalculator.validate((others + value).map { it.model() })
            dao.putPeriod(value.copy(createdAt = all.find { it.id == value.id }?.createdAt ?: value.createdAt))
        }
    }
    suspend fun export(): String = mutex.withLock {
        db.withTransaction {
            JSONObject().put("format", "WarmCycle").put("version", 1)
                .put("periods", JSONArray().apply { dao.allPeriods().forEach { put(JSONObject()
                    .put("start", LocalDate.ofEpochDay(it.start).toString())
                    .put("end", it.end?.let { d -> LocalDate.ofEpochDay(d).toString() } ?: JSONObject.NULL)) } })
                .put("daily", JSONArray().apply { dao.allDaily().forEach { put(JSONObject().put("date", LocalDate.ofEpochDay(it.date).toString())
                    .put("mood", it.mood).put("flow", it.flow).put("symptoms", it.symptoms).put("note", it.note)) } }).toString(2)
        }
    }
    // Validate the entire document before an atomic replacement; malformed files never partially import.
    suspend fun importBackup(text: String) = mutex.withLock {
        require(text.length <= 2_000_000) { "备份文件过大（最多 2 MB）" }
        val root = JSONObject(text)
        require(root.getString("format") == "WarmCycle" && root.getInt("version") == 1) { "不支持的备份格式" }
        val ps = root.getJSONArray("periods")
        val ds = root.getJSONArray("daily")
        require(ps.length() <= 5000 && ds.length() <= 20000) { "记录数量过多" }
        val periods = (0 until ps.length()).map { i -> ps.getJSONObject(i).let {
            PeriodEntity(start = LocalDate.parse(it.getString("start")).toEpochDay(),
                end = if (it.isNull("end")) null else LocalDate.parse(it.getString("end")).toEpochDay())
        } }
        CycleCalculator.validate(periods.map { it.model() })
        val daily = (0 until ds.length()).map { i -> ds.getJSONObject(i).let {
            DailyEntity(LocalDate.parse(it.getString("date")).toEpochDay(), it.getString("mood"), it.getString("flow"),
                it.getString("symptoms"), it.getString("note"))
        } }
        require(daily.map { it.date }.distinct().size == daily.size) { "备份包含重复日期" }
        require(daily.all { it.date <= LocalDate.now().toEpochDay() && it.note.length <= 4000 &&
            it.mood in listOf("", "😊 开心", "😌 平静", "😐 一般", "😣 烦躁", "😔 低落", "😴 疲惫") &&
            it.flow in listOf("", "少", "正常", "多") && it.symptoms.split("|").filter(String::isNotEmpty).all { s -> s in symptoms } }) { "备份中的每日记录无效" }
        db.withTransaction { dao.clearPeriods(); dao.clearDaily(); periods.forEach { dao.putPeriod(it) }; daily.forEach { dao.putDaily(it) } }
    }
    suspend fun clear() = mutex.withLock {
        db.withTransaction { dao.clearDaily(); dao.clearPeriods() }; preferences.clear()
    }
    companion object { val symptoms = listOf("腹部不适", "腰酸", "头痛", "疲惫", "胸部不适", "食欲变化", "睡眠变化", "痘痘") }
}
