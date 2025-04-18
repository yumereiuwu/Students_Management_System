package com.example.students_system.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Long = -1, // -1 ch dc save vao db
    val username: String,
    var passwordHash: String, // save hash mk , k save mk goc
    var name: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var avatarPath: String? = null // path file avatar user
) : Parcelable
