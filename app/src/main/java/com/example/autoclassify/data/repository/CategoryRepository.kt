package com.example.autoclassify.data.repository

import com.example.autoclassify.data.local.CategoryDao
import com.example.autoclassify.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class CategoryRepository(
    private val categoryDao: CategoryDao
) {
    fun observeAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.observeAll()

    fun observeTopLevelCategories(): Flow<List<CategoryEntity>> =
        categoryDao.observeTopLevelCategories()

    fun observeSubcategories(parentId: Long): Flow<List<CategoryEntity>> =
        categoryDao.observeSubcategories(parentId)

    /**
     * Adds a category or returns the existing ID when its normalized name is
     * already used at the same level, as enforced transactionally by the DAO.
     */
    suspend fun addCategory(
        name: String,
        parentId: Long? = null,
        createdAt: Long = System.currentTimeMillis()
    ): Long {
        val (displayName, normalizedName) = normalizeName(name)
        return categoryDao.addCategory(
            CategoryEntity(
                name = displayName,
                normalizedName = normalizedName,
                parentId = parentId,
                createdAt = createdAt
            )
        )
    }

    /**
     * Returns 0 when the category is missing or another category at that level
     * already has this normalized name.
     */
    suspend fun renameCategory(categoryId: Long, name: String): Int {
        val (displayName, normalizedName) = normalizeName(name)
        return categoryDao.rename(
            categoryId = categoryId,
            name = displayName,
            normalizedName = normalizedName
        )
    }

    suspend fun deleteCategory(
        categoryId: Long,
        updatedAt: Long = System.currentTimeMillis()
    ): Int = categoryDao.deleteById(categoryId, updatedAt)

    private fun normalizeName(name: String): Pair<String, String> {
        val displayName = name.trim().replace(WHITESPACE, " ")
        require(displayName.isNotEmpty()) { "Category name must not be blank." }
        return displayName to displayName.lowercase(Locale.ROOT)
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
