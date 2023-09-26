package com.example.myfirebaseprojectwithdb

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

object myfireobj{
    val auth = FirebaseAuth.getInstance()
    @SuppressLint("StaticFieldLeak")
    val db = FirebaseFirestore.getInstance()
    val storage  = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    val firebaseKey = "AIzaSyDlW9X5V0tIC-UzkKQt8qRGoT2K-tCJFd8"  //"AIzaSyAz8VNqeUdcSBt3HScuMXSSUgVul5H-4YY"
    val firebaseProjectId = "userloginfirestorestorage"          //"shergillcave"

}

