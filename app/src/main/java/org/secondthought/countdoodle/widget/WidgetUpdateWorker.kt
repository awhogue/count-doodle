package org.secondthought.countdoodle.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetUpdateWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            SingleEventWidget().updateAll(applicationContext)
            MultiEventWidget().updateAll(applicationContext)
            Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }
}
