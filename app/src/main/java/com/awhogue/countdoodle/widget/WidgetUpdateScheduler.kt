package com.awhogue.countdoodle.widget

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WidgetUpdateScheduler {

    private const val PERIODIC_NAME = "countdoodle.widget.periodic"
    private const val IMMEDIATE_NAME = "countdoodle.widget.immediate"

    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(Constraints.NONE).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun requestImmediateUpdate(context: Context) {
        val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
