package com.example.myfirebaseprojectwithdb.application

import android.app.Application
import android.content.Context
import com.example.myfirebaseprojectwithdb.myfireobj
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MyApplication :Application() {
    override fun onCreate() {
        super.onCreate()
        firebaseAppInit(this)
    }
    fun firebaseAppInit(context: Context){
        if (FirebaseApp.getApps(context).isEmpty()){
            val options = FirebaseOptions.Builder()
                .setApiKey(myfireobj.firebaseKey)
                .setApplicationId(myfireobj.firebaseProjectId)
                .setProjectId(myfireobj.firebaseProjectId)
                .build()
            FirebaseApp.initializeApp(context, options)


        }

    }
}