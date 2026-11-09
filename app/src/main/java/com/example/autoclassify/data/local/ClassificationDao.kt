package com.example.autoclassify.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ClassificationDao {
    @Query("SELECT * FROM classifications WHERE imageUri = :imageUri LIMIT 1")
    abstract suspend fun getByImageUri(imageUri: String): ClassificationEntity?

    @Query("SELECT * FROM classifications ORDER BY createdAt DESC")
    abstract fun observeAll(): Flow<List<ClassificationEntity>>

    @Query(
        "SELECT * FROM classifications " +
            "WHERE status = :status ORDER BY updatedAt DESC"
    )
    abstract fun observeByStatus(status: String): Flow<List<ClassificationEntity>>

    @Query("SELECT COUNT(*) FROM classifications WHERE status = :status")
    abstract fun observeCountByStatus(status: String): Flow<Int>

    @Query(
        """
        SELECT * FROM classifications
        WHERE status = 'PENDING'
           OR (status = 'FAILED' AND attemptCount < :maxAttempts)
        ORDER BY createdAt ASC
        """
    )
    abstract suspend fun getPendingOrRetryableFailed(
        maxAttempts: Int
    ): List<ClassificationEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertIfAbsent(
        classifications: List<ClassificationEntity>
    )

    @Query(
        """
        UPDATE classifications
        SET displayName = :displayName
        WHERE imageUri = :imageUri
          AND status != 'COMPLETED'
        """
    )
    protected abstract suspend fun updateDisplayNameUnlessCompleted(
        imageUri: String,
        displayName: String
    )

    /**
     * Imports image metadata without resetting any existing analysis state or result.
     * New images begin in PENDING; existing non-completed rows only refresh their name.
     */
    @Transaction
    open suspend fun upsertAll(classifications: List<ClassificationEntity>) {
        insertIfAbsent(
            classifications.map { classification ->
                classification.copy(
                    status = ClassificationStatus.PENDING,
                    categoryId = null,
                    confidence = null,
                    errorMessage = null,
                    attemptCount = 0
                )
            }
        )
        classifications.forEach { classification ->
            updateDisplayNameUnlessCompleted(
                imageUri = classification.imageUri,
                displayName = classification.displayName
            )
        }
    }

    @Query(
        """
        UPDATE classifications
        SET status = 'PROCESSING',
            attemptCount = attemptCount + 1,
            errorMessage = NULL,
            updatedAt = :updatedAt
        WHERE imageUri = :imageUri
          AND status IN ('PENDING', 'FAILED')
        """
    )
    abstract suspend fun markProcessing(imageUri: String, updatedAt: Long): Int

    @Query(
        """
        UPDATE classifications
        SET status = 'COMPLETED',
            categoryId = :categoryId,
            confidence = :confidence,
            errorMessage = NULL,
            updatedAt = :updatedAt
        WHERE imageUri = :imageUri
          AND status = 'PROCESSING'
        """
    )
    abstract suspend fun markCompleted(
        imageUri: String,
        categoryId: Long,
        confidence: Float?,
        updatedAt: Long
    ): Int

    @Query(
        """
        UPDATE classifications
        SET status = 'FAILED',
            categoryId = NULL,
            confidence = NULL,
            errorMessage = :errorMessage,
            updatedAt = :updatedAt
        WHERE imageUri = :imageUri
          AND status = 'PROCESSING'
        """
    )
    abstract suspend fun markFailed(
        imageUri: String,
        errorMessage: String,
        updatedAt: Long
    ): Int
}
