package com.example.autoclassify.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CategoryDao {
    @Query("SELECT * FROM categories ORDER BY parentId, normalizedName")
    abstract fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentId IS NULL ORDER BY normalizedName")
    abstract fun observeTopLevelCategories(): Flow<List<CategoryEntity>>

    @Query(
        "SELECT * FROM categories " +
            "WHERE parentId = :parentId ORDER BY normalizedName"
    )
    abstract fun observeSubcategories(parentId: Long): Flow<List<CategoryEntity>>

    @Query(
        """
        SELECT * FROM categories
        WHERE normalizedName = :normalizedName
          AND parentId IS :parentId
        LIMIT 1
        """
    )
    abstract suspend fun findByNormalizedName(
        normalizedName: String,
        parentId: Long?
    ): CategoryEntity?

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    protected abstract suspend fun getById(categoryId: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertInternal(category: CategoryEntity): Long

    @Query(
        """
        UPDATE categories
        SET name = :name,
            normalizedName = :normalizedName
        WHERE id = :categoryId
        """
    )
    protected abstract suspend fun updateNameInternal(
        categoryId: Long,
        name: String,
        normalizedName: String
    ): Int

    @Query(
        """
        UPDATE classifications
        SET categoryId = NULL,
            status = 'PENDING',
            confidence = NULL,
            errorMessage = NULL,
            updatedAt = :updatedAt
        WHERE categoryId IN (
            SELECT id FROM categories
            WHERE id = :categoryId OR parentId = :categoryId
        )
        """
    )
    protected abstract suspend fun resetImagesForCategoryTree(
        categoryId: Long,
        updatedAt: Long
    ): Int

    @Query("DELETE FROM categories WHERE parentId = :categoryId")
    protected abstract suspend fun deleteChildrenInternal(categoryId: Long): Int

    @Query("DELETE FROM categories WHERE id = :categoryId")
    protected abstract suspend fun deleteInternal(categoryId: Long): Int

    /**
     * Adds a root category or a subcategory whose parent is a root category.
     * A matching normalized name under the same parent returns the existing ID.
     */
    @Transaction
    open suspend fun addCategory(category: CategoryEntity): Long {
        requireValidParent(category.parentId)

        val existing = findByNormalizedName(
            normalizedName = category.normalizedName,
            parentId = category.parentId
        )
        return existing?.id ?: insertInternal(category)
    }

    /**
     * Renames a category without allowing a duplicate normalized name at its level.
     * Returns 0 when the category is missing or the name is already in use.
     */
    @Transaction
    open suspend fun rename(
        categoryId: Long,
        name: String,
        normalizedName: String
    ): Int {
        val current = getById(categoryId) ?: return 0
        requireValidParent(current.parentId)

        val duplicate = findByNormalizedName(normalizedName, current.parentId)
        if (duplicate != null && duplicate.id != categoryId) {
            return 0
        }
        return updateNameInternal(categoryId, name, normalizedName)
    }

    /**
     * Removes a category and its subcategories, returning their images to PENDING.
     */
    @Transaction
    open suspend fun deleteById(categoryId: Long, updatedAt: Long): Int {
        if (getById(categoryId) == null) return 0

        resetImagesForCategoryTree(categoryId, updatedAt)
        val deletedChildren = deleteChildrenInternal(categoryId)
        val deletedCategory = deleteInternal(categoryId)
        return deletedChildren + deletedCategory
    }

    private suspend fun requireValidParent(parentId: Long?) {
        if (parentId == null) return

        val parent = getById(parentId)
        require(parent != null && parent.parentId == null) {
            "A subcategory must reference an existing top-level category."
        }
    }
}
