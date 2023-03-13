package com.example.tasktracker.data

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
    fun toMap(): Map<String, String> {
        return mapOf(
            "title" to title,
            "description" to description,
        )
    }
}
