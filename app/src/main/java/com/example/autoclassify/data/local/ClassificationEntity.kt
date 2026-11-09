package com.example.autoclassify.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object ClassificationStatus {
    const val PENDING = "PENDING"
    const val PROCESSING = "PROCESSING"
    const val COMPLETED = "COMPLETED"
    const val FAILED = "FAILED"
}

@Entity(
    tableName = "classifications",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["status", "createdAt"]),
        Index(value = ["categoryId"])
    ]
)
data class ClassificationEntity(
    @PrimaryKey
    val imageUri: String,
    val displayName: String,
    val status: String = ClassificationStatus.PENDING,
    val categoryId: Long? = null,
    val confidence: Float? = null,
    val errorMessage: String? = null,
    val attemptCount: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)
