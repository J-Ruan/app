package com.warmcycle.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import com.warmcycle.data.PeriodEntity
import java.time.LocalDate

@Composable fun WarmApp(vm: AppViewModel, state: AppState) {
    val snackbar = remember { SnackbarHostState() }
    val nav = rememberNavController()
    var periodId by rememberSaveable { mutableStateOf<Long?>(null) }
    var periodDate by rememberSaveable { mutableStateOf<String?>(null) }
    val busy by vm.busy.collectAsStateWithLifecycle()
    LaunchedEffect(vm) { vm.messages.collect { snackbar.showSnackbar(it) } }
    LaunchedEffect(state.settings.ready) { if (!state.settings.ready) nav.popBackStack("home", false) }
    val routes = listOf("home", "calendar", "record", "stats", "settings")
    val labels = listOf("首页", "日历", "记录", "统计", "设置")
    val icons = listOf(Icons.Outlined.Home, Icons.Outlined.CalendarMonth, Icons.Outlined.AddCircleOutline, Icons.Outlined.Insights, Icons.Outlined.Settings)
    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination?.route?.substringBefore('/') ?: "home"
    fun editPeriod(p: PeriodEntity?, date: LocalDate) { periodId = p?.id ?: 0L; periodDate = date.toString() }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, bottomBar = {
        if (state.settings.ready) NavigationBar {
            routes.forEachIndexed { i, route -> NavigationBarItem(selected = current == route,
                onClick = { nav.navigate(route) { popUpTo("home"); launchSingleTop = true } },
                icon = { Icon(icons[i], labels[i]) }, label = { Text(labels[i]) }) }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (!state.settings.ready) Onboarding(busy, vm::setup)
            else NavHost(nav, startDestination = "home") {
                composable("home") { HomeScreen(state, onRecord = { nav.navigate("record") }, onPeriod = { editPeriod(it, LocalDate.now()) }, onEndPeriod = { vm.period(it.id, LocalDate.ofEpochDay(it.start), LocalDate.now()) }) }
                composable("calendar") { CalendarScreen(state, onRecord = { nav.navigate("record/$it") }, onPeriod = { p, d -> editPeriod(p, d) }) }
                composable("record") { DailyScreen(LocalDate.now(), state, vm) }
                composable("record/{date}") { back -> DailyScreen(LocalDate.parse(back.arguments?.getString("date")), state, vm) }
                composable("stats") { StatsScreen(state, { editPeriod(it, LocalDate.ofEpochDay(it.start)) }, vm::deletePeriod) }
                composable("settings") { SettingsScreen(state, vm, snackbar) }
            }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
    if (state.settings.ready && periodDate != null) {
        val existing = state.periods.find { it.id == periodId }
        PeriodDialog(existing, LocalDate.parse(periodDate), state.periods, { periodDate = null }, vm::period)
    }
}
@Composable fun Page(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
}
