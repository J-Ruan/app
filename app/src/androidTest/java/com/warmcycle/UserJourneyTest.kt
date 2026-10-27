package com.warmcycle

import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.lifecycle.ViewModelProvider
import com.warmcycle.ui.AppViewModel
import kotlinx.coroutines.flow.first
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserJourneyTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Before fun reset() {
        runBlocking { (rule.activity.application as WarmCycleApp).repository.clear() }
        rule.waitUntil(120000) { rule.onAllNodesWithText("开始使用").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun saved() { rule.waitUntil(120000) { !ViewModelProvider(rule.activity)[AppViewModel::class.java].busy.value } }
    @Test fun onboardingRecordCalendarStatsSettingsAndDelete() {
        rule.onNode(isToggleable()).performClick()
        rule.onNodeWithText("开始使用").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }
        rule.onNodeWithText("进入暖期").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }
        rule.waitUntil(120000) { rule.onAllNodesWithText("记录今天").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("记录今天").performClick()
        rule.onNodeWithText("😌 平静").performClick()
        rule.onNodeWithText("腰酸").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }
        rule.onNodeWithText("今天有点累，想喝一杯热饮……").performScrollTo().performTextInput("今天想早点休息")
        rule.onNodeWithText("今天想早点休息").performImeAction()
        rule.onNodeWithText("保存这一天").assertIsDisplayed().performClick()
        rule.waitUntil(120000) { runBlocking { (rule.activity.application as WarmCycleApp).repository.dao.allDaily().isNotEmpty() } }
        saved()
        rule.onNodeWithText("日历").performClick()
        rule.onNodeWithText("编辑当天感受").performScrollTo().assertExists()
        rule.onNodeWithText("统计").performClick()
        rule.onNodeWithText("查看 / 修改").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }
        rule.onNodeWithText("已经结束").assertExists()
        rule.onNodeWithText("今天结束").performClick()
        rule.onNodeWithText("保存").performClick()
        rule.waitUntil(120000) { runBlocking { (rule.activity.application as WarmCycleApp).repository.dao.allPeriods().single().end != null } }
        saved()
        rule.onNodeWithText("设置").performClick()
        rule.onNodeWithText("深色").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }
        rule.waitUntil(120000) { runBlocking { (rule.activity.application as WarmCycleApp).repository.preferences.settings.first().theme == "dark" } }
        saved()
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        rule.onNodeWithText("记录").performClick()
        rule.onNodeWithText("今天想早点休息").performScrollTo().assertExists()
        rule.onNodeWithText("删除当天记录").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }
        rule.onNodeWithText("确认").performClick()
        rule.waitUntil(120000) { runBlocking { (rule.activity.application as WarmCycleApp).repository.dao.allDaily().isEmpty() } }
        saved()
        rule.onNodeWithText("统计").performClick()
        rule.onNodeWithText("删除").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }
        rule.onNodeWithText("确认").performClick()
        rule.waitUntil(120000) { runBlocking { (rule.activity.application as WarmCycleApp).repository.dao.allPeriods().isEmpty() } }
        saved()
        rule.onNodeWithText("设置").performClick()
        rule.onNodeWithText("清除全部数据").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }
        rule.onNodeWithText("确认").performClick()
        rule.waitUntil(120000) { rule.onAllNodesWithText("开始使用").fetchSemanticsNodes().isNotEmpty() }
    }
}
