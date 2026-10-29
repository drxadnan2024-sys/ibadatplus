package com.ibadatplus.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class IbadatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Log.e("IbadatApp", "FirebaseApp.initializeApp failed or safely bypassed: ${e.message}", e)
        }
    }
}
