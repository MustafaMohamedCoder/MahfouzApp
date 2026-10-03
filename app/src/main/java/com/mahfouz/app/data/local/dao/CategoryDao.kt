package com.mahfouz.app.data.local.dao

import androidx.room.*
import com.mahfouz.app.data.local.entity.CategoryEntity
import com.mahfouz.app.data.local.entity.CategorySummary
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY createdAt DESC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("""
        SELECT 
            c.id, 
            c.name, 
            c.description, 
            c.colorHex, 
            COUNT(i.id) AS invoiceCount, 
            COALESCE(SUM(i.totalAmount), 0.0) AS totalSpent,
            COALESCE(SUM(CASE WHEN i.isPaid = 0 THEN i.totalAmount ELSE 0.0 END), 0.0) AS unpaidAmount
        FROM categories c
        LEFT JOIN invoices i ON c.id = i.categoryId
        GROUP BY c.id
        ORDER BY c.createdAt DESC
    """)
    fun getCategoriesSummary(): Flow<List<CategorySummary>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}
