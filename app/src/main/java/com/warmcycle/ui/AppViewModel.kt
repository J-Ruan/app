package com.warmcycle.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.warmcycle.WarmCycleApp
import com.warmcycle.data.*
import com.warmcycle.reminder.ReminderWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate

data class AppState(val loading: Boolean = true, val periods: List<PeriodEntity> = emptyList(),
    val daily: List<DailyEntity> = emptyList(), val settings: Settings = Settings())
class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as WarmCycleApp).repository
    val state = combine(repo.dao.periods(), repo.dao.daily(), repo.preferences.settings) { p, d, s -> AppState(false, p, d, s) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppState())
    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()
    var busy = MutableStateFlow(false); private set
    init { viewModelScope.launch { repo.preferences.settings.collect { ReminderWorker.schedule(app, it.ready && it.reminder) } } }
    private fun run(success: String = "已保存", operation: suspend () -> Unit) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try { withContext(Dispatchers.IO) { operation() }; _messages.emit(success) }
            catch (e: Exception) { if (e is CancellationException) throw e; _messages.emit(e.message ?: "操作失败，请重试") }
            finally { busy.value = false }
        }
    }
    fun setup(date: LocalDate?, cycle: Int, duration: Int, end: LocalDate?) = run("欢迎来到暖期") {
        if (date != null) {
            repo.savePeriod(PeriodEntity(start = date.toEpochDay(), end = end?.toEpochDay()))
        }
        repo.preferences.save(Settings(ready = true, cycle = cycle, duration = duration))
    }
    fun period(id: Long, start: LocalDate, end: LocalDate?) = run { repo.savePeriod(PeriodEntity(id, start.toEpochDay(), end?.toEpochDay())) }
    fun deletePeriod(id: Long) = run("经期已删除，每日记录仍保留") { repo.dao.deletePeriod(id) }
    fun daily(d: DailyEntity) = run { require(d.note.length <= 4000); repo.dao.putDaily(d) }
    fun deleteDaily(date: Long) = run("当天记录已删除") { repo.dao.deleteDaily(date) }
    fun settings(s: Settings) = run { require(s.cycle in 15..90 && s.duration in 1..20); repo.preferences.save(s) }
    fun export(uri: Uri) = run("备份已导出，请妥善保管明文文件") {
        getApplication<Application>().contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { it.write(repo.export()) }
            ?: error("无法写入文件")
    }
    fun import(uri: Uri) = run("备份已导入") {
        val text = getApplication<Application>().contentResolver.openInputStream(uri)?.bufferedReader()?.use { r ->
            val result = StringBuilder(); val buffer = CharArray(4096)
            while (true) { val n = r.read(buffer); if (n == -1) break; result.append(buffer, 0, n); require(result.length <= 2_000_000) { "备份文件过大" } }
            result.toString()
        } ?: error("无法读取文件")
        repo.importBackup(text)
        ReminderWorker.schedule(getApplication(), state.value.settings.reminder)
    }
    fun clear() = run("所有本地记录与设置已清除") {
        ReminderWorker.schedule(getApplication(), false)
        getApplication<Application>().getSharedPreferences("notifications", 0).edit().clear().commit()
        repo.clear()
    }
}
