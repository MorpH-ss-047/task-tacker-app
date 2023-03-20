package com.example.tasktracker.data

import android.util.Log
import com.example.tasktracker.crypto.CryptoManager
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class NoteData(
    var id: String = "",
    var title: String,
    var description: String,
){
    // get map of user data
    @Exclude
    fun toMap(): Map<String, String?> {
        val cryptoManager = CryptoManager()
        val encryptedTitle = cryptoManager.encryptData(title)
        val encryptedDescription = cryptoManager.encryptData(description)
        Log.d("NoteData", "Encrypted Note title = $encryptedTitle")
        Log.d("NoteData", "Encrypted Note description = $encryptedDescription")
        return mapOf(
            "title" to encryptedTitle,
            "description" to encryptedDescription
        )
    }
}
