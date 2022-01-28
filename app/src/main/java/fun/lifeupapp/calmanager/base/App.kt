package `fun`.lifeupapp.calmanager.base

import android.app.Application
import com.google.android.material.color.DynamicColors

/**
 * our application class
 *
 * MIT License
 * Copyright (c) 2021 AyagiKei
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // This is all you need.
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}