package com.mahfouz.app.data.local.dao

import androidx.room.*
import com.mahfouz.app.data.local.entity.InvoiceEntity
import com.mahfouz.app.data.local.entity.InvoiceItemEntity
import com.mahfouz.app.data.local.entity.InvoiceWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    @Query("SELECT * FROM invoices WHERE categoryId = :categoryId ORDER BY invoiceDate DESC")
    fun getInvoicesByCategory(categoryId: Long): Flow<List<InvoiceEntity>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    fun getInvoiceWithItems(invoiceId: Long): Flow<InvoiceWithItems?>

    @Query("""
        SELECT * FROM invoices 
        WHERE categoryId = :categoryId 
        AND (supplierName LIKE '%' || :query || '%' OR invoiceNumber LIKE '%' || :query || '%')
        ORDER BY invoiceDate DESC
    """)
    fun searchInvoices(categoryId: Long, query: String): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsForInvoice(invoiceId: Long)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    @Transaction
    suspend fun insertInvoiceWithItems(invoice: InvoiceEntity, items: List<InvoiceItemEntity>): Long {
        val invoiceId = insertInvoice(invoice)
        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        insertInvoiceItems(itemsWithId)
        return invoiceId
    }

    @Transaction
    suspend fun updateInvoiceWithItems(invoice: InvoiceEntity, items: List<InvoiceItemEntity>) {
        updateInvoice(invoice)
        deleteItemsForInvoice(invoice.id)
        val itemsWithId = items.map { it.copy(invoiceId = invoice.id) }
        insertInvoiceItems(itemsWithId)
    }

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM invoices")
    fun getTotalExpenses(): Flow<Double>
}
