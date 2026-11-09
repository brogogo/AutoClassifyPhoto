package com.example.autoclassify.data.repository

import com.example.autoclassify.data.local.ClassificationDao
import com.example.autoclassify.data.local.ClassificationEntity
import com.example.autoclassify.data.local.ClassificationStatus
import kotlinx.coroutines.flow.Flow

/**
 * Metadata supplied by the gallery boundary without depending on a UI/gallery model.
 */
data class ImageRegistration(
    val imageUri: String,
    val displayName: String
)

class ClassificationRepository(
    private val classificationDao: ClassificationDao
) {
    suspend fun registerImage(
        image: ImageRegistration,
        timestamp: Long = System.currentTimeMillis()
    ) {
        classificationDao.upsertAll(
            listOf(
                ClassificationEntity(
                    imageUri = image.imageUri,
                    displayName = image.displayName,
                    status = ClassificationStatus.PENDING,
                    createdAt = timestamp,
                    updatedAt = timestamp
                )
            )
        )
    }

    suspend fun registerImages(
        images: List<ImageRegistration>,
        timestamp: Long = System.currentTimeMillis()
    ) {
        if (images.isEmpty()) return

        classificationDao.upsertAll(
            images.map { image ->
                ClassificationEntity(
                    imageUri = image.imageUri,
                    displayName = image.displayName,
                    status = ClassificationStatus.PENDING,
                    createdAt = timestamp,
                    updatedAt = timestamp
                )
            }
        )
    }

    suspend fun getImage(imageUri: String): ClassificationEntity? =
        classificationDao.getByImageUri(imageUri)

    fun observeAllImages(): Flow<List<ClassificationEntity>> =
        classificationDao.observeAll()

    fun observeImagesByStatus(status: String): Flow<List<ClassificationEntity>> =
        classificationDao.observeByStatus(status)

    fun observeImageCountByStatus(status: String): Flow<Int> =
        classificationDao.observeCountByStatus(status)

    suspend fun getPendingOrRetryableFailedImages(
        maxAttempts: Int
    ): List<ClassificationEntity> =
        classificationDao.getPendingOrRetryableFailed(maxAttempts)

    /**
     * Returns the number of rows transitioned. A value of 0 means the DAO's
     * current-state guard did not allow the transition.
     */
    suspend fun markProcessing(
        imageUri: String,
        updatedAt: Long = System.currentTimeMillis()
    ): Int = classificationDao.markProcessing(imageUri, updatedAt)

    suspend fun markCompleted(
        imageUri: String,
        categoryId: Long,
        confidence: Float?,
        updatedAt: Long = System.currentTimeMillis()
    ): Int = classificationDao.markCompleted(
        imageUri = imageUri,
        categoryId = categoryId,
        confidence = confidence,
        updatedAt = updatedAt
    )

    suspend fun markFailed(
        imageUri: String,
        errorMessage: String,
        updatedAt: Long = System.currentTimeMillis()
    ): Int = classificationDao.markFailed(
        imageUri = imageUri,
        errorMessage = errorMessage,
        updatedAt = updatedAt
    )
}
