package com.warmcycle.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore("settings")
data class Settings(val ready: Boolean = false, val cycle: Int = 28, val duration: Int = 5,
    val theme: String = "system", val reminder: Boolean = false, val lead: Int = 2,
    val privateNotification: Boolean = true, val lock: Boolean = false)
class Preferences(private val context: Context) {
    private val ready = booleanPreferencesKey("ready")
    private val cycle = intPreferencesKey("cycle")
    private val duration = intPreferencesKey("duration")
    private val theme = stringPreferencesKey("theme")
    private val reminder = booleanPreferencesKey("reminder")
    private val lead = intPreferencesKey("lead")
    private val privacy = booleanPreferencesKey("private")
    private val lock = booleanPreferencesKey("lock")
    val settings = context.store.data.map { p -> Settings(p[ready] ?: false, p[cycle] ?: 28, p[duration] ?: 5,
        p[theme] ?: "system", p[reminder] ?: false, p[lead] ?: 2, p[privacy] ?: true, p[lock] ?: false) }
    suspend fun save(s: Settings) { context.store.edit { p ->
        p[ready] = s.ready; p[cycle] = s.cycle; p[duration] = s.duration; p[theme] = s.theme
        p[reminder] = s.reminder; p[lead] = s.lead; p[privacy] = s.privateNotification; p[lock] = s.lock
    } }
    suspend fun clear() { context.store.edit { it.clear() } }
}
