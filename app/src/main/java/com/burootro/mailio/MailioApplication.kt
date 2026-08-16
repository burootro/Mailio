package com.burootro.mailio

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MailioApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
