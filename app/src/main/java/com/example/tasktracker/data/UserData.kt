package com.example.tasktracker.data

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserData(
    var fullName: String = "",
    var age: String = "",
    var gender: String = "",
    var phone: String = "",
    var email: String = "",
    var method: String = "phone"
) {
    // get map of user data
    @Exclude
    fun toMap(): Map<String, String> {
        return mapOf(
            "fullName" to fullName,
            "age" to age,
            "gender" to gender,
            "phone" to phone,
            "email" to email,
            "method" to method
        )
    }
}
