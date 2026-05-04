package com.awhogue.countdoodle

import android.app.Application
import androidx.work.Configuration
import com.awhogue.countdoodle.data.AppDatabase
import com.awhogue.countdoodle.data.EventRepository
import com.awhogue.countdoodle.widget.WidgetUpdateScheduler

class CountDoodleApp : Application(), Configuration.Provider {

    val database: AppDatabase by lazy { AppDatabase.create(this) }
    val repository: EventRepository by lazy {
        EventRepository(database.eventDao()) { WidgetUpdateScheduler.requestImmediateUpdate(this) }
    }

    override fun onCreate() {
        super.onCreate()
        WidgetUpdateScheduler.schedulePeriodic(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    companion object {
        fun from(applicationContext: android.content.Context): CountDoodleApp =
            applicationContext.applicationContext as CountDoodleApp
    }
}
