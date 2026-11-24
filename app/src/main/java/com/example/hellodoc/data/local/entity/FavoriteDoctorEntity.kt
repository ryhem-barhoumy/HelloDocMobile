package com.example.hellodoc.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite_doctors",
    indices = [
        Index(
            value = ["ownerId", "doctorId"],
            unique = true
        )
    ]
)
data class FavoriteDoctorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val ownerId: String,
    val doctorId: String,
    val doctorName: String,

    val specialty: String? = null,
    val city: String? = null,

    val personalLabel: String = "",
    val savedAt: Long = System.currentTimeMillis()
)