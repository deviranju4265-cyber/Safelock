package com.example.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import org.json.JSONObject

enum class OperationType {
    GET,
    LIST,
    CREATE,
    UPDATE,
    DELETE
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String) {
    val errorDetails = JSONObject().apply {
        put("timestamp", System.currentTimeMillis())
        put("operation", operationType.name)
        put("targetPath", path)
        if (exception is FirebaseFirestoreException) {
            put("code", exception.code.name)
            put("message", exception.message ?: "Unknown Firestore error")
        } else {
            put("code", "UNKNOWN")
            put("message", exception.message ?: "General exception")
        }
    }
    Log.e("FirestoreSecurity", errorDetails.toString(), exception)
}
