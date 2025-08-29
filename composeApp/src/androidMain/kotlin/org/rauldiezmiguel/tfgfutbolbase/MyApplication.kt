package org.rauldiezmiguel.tfgfutbolbase

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa FirebaseApp antes de cualquier uso de Firestore/Storage
        FirebaseApp.initializeApp(this)
    }
}