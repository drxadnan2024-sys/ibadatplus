package com.ibadatplus.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class IbadatApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Global crash handler to catch and log any unhandled startup exceptions
        val defaultUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("IbadatApp", "Unhandled exception caught on thread ${thread.name}: ${throwable.message}", throwable)
            defaultUncaughtHandler?.uncaughtException(thread, throwable)
        }

        // Safe Firebase initialization
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Throwable) {
            Log.e("IbadatApp", "FirebaseApp.initializeApp safely caught: ${e.message}", e)
        }
    }
}
