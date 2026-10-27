package com.warmcycle.reminder

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.warmcycle.*
import com.warmcycle.domain.CycleCalculator
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class ReminderWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val repo = (applicationContext as WarmCycleApp).repository
        val settings = repo.preferences.settings.first()
        if (!settings.ready || !settings.reminder) return Result.success()
        val periods = repo.dao.allPeriods().map { it.model() }
        val summary = CycleCalculator.calculate(periods, settings.cycle, settings.duration)
        val next = summary.next ?: return Result.success()
        val today = LocalDate.now()
        if (summary.activeDay != null || today.isBefore(next.minusDays(settings.lead.toLong())) || today.isAfter(next)) return Result.success()
        val sent = applicationContext.getSharedPreferences("notifications", Context.MODE_PRIVATE)
        val key = "${periods.last().start}:$next"
        if (sent.getString("sent", "") == key) return Result.success()
        if (android.os.Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(applicationContext,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return Result.success()
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("gentle", "温柔提醒", NotificationManager.IMPORTANCE_DEFAULT).apply {
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        })
        if (!manager.areNotificationsEnabled()) return Result.success()
        val pending = PendingIntent.getActivity(applicationContext, 0, Intent(applicationContext, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        manager.notify(1, NotificationCompat.Builder(applicationContext, "gentle")
            .setSmallIcon(com.warmcycle.R.drawable.ic_flower).setContentTitle("暖期")
            .setContentText(if (settings.privateNotification) "暖期有一条提醒" else "预计这几天可能进入生理期 🌷，预测仅供参考")
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setContentIntent(pending).setAutoCancel(true).build())
        sent.edit().putString("sent", key).apply()
        return Result.success()
    }
    companion object {
        fun schedule(context: Context, enabled: Boolean) {
            val work = WorkManager.getInstance(context)
            if (enabled) work.enqueueUniquePeriodicWork("cycle-reminder", ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                PeriodicWorkRequestBuilder<ReminderWorker>(12, TimeUnit.HOURS).build())
            else work.cancelUniqueWork("cycle-reminder")
            context.getSystemService(NotificationManager::class.java).cancel(1)
        }
    }
}
