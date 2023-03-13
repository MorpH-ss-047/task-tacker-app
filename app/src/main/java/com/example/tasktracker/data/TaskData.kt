package com.example.tasktracker.data

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class TaskData(
    var id: String = "",
    var title: String,
    var description: String = "",
    var startDate: String? = null,
    var endDate: String? = null,
    var priority: String = "Low",
    var completed: Boolean = false,
) {
    @Exclude
    fun toMap(): Map<String, String> {
        return mapOf(
            "title" to title,
            "description" to description,
            "startDate" to startDate.toString(),
            "endDate" to endDate.toString(),
            "priority" to priority,
            "completed" to completed.toString(),
        )
    }
}
