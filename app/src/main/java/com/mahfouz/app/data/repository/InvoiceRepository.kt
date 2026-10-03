package com.mahfouz.app.data.repository

import com.mahfouz.app.data.local.dao.CategoryDao
import com.mahfouz.app.data.local.dao.InvoiceDao
import com.mahfouz.app.data.local.entity.CategoryEntity
import com.mahfouz.app.data.local.entity.CategorySummary
import com.mahfouz.app.data.local.entity.InvoiceEntity
import com.mahfouz.app.data.local.entity.InvoiceItemEntity
import com.mahfouz.app.data.local.entity.InvoiceWithItems
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(
    private val categoryDao: CategoryDao,
    private val invoiceDao: InvoiceDao
) {
    // Categories
    val categoriesSummary: Flow<List<CategorySummary>> = categoryDao.getCategoriesSummary()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(id: Long): CategoryEntity? = categoryDao.getCategoryById(id)
    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)
    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    // Invoices
    fun getInvoicesByCategory(categoryId: Long): Flow<List<InvoiceEntity>> =
        invoiceDao.getInvoicesByCategory(categoryId)

    fun getInvoiceWithItems(invoiceId: Long): Flow<InvoiceWithItems?> =
        invoiceDao.getInvoiceWithItems(invoiceId)

    fun filterInvoices(categoryId: Long, query: String, isPaid: Boolean?): Flow<List<InvoiceEntity>> =
        invoiceDao.filterInvoices(categoryId, query, isPaid)

    suspend fun insertInvoice(invoice: InvoiceEntity, items: List<InvoiceItemEntity>): Long =
        invoiceDao.insertInvoiceWithItems(invoice, items)

    suspend fun updateInvoice(invoice: InvoiceEntity, items: List<InvoiceItemEntity>) =
        invoiceDao.updateInvoiceWithItems(invoice, items)

    suspend fun updatePaymentStatus(invoiceId: Long, isPaid: Boolean) =
        invoiceDao.updatePaymentStatus(invoiceId, isPaid)

    suspend fun deleteInvoice(invoice: InvoiceEntity) =
        invoiceDao.deleteInvoice(invoice)

    val totalExpenses: Flow<Double> = invoiceDao.getTotalExpenses()
    val totalUnpaidExpenses: Flow<Double> = invoiceDao.getTotalUnpaidExpenses()
    val totalInvoiceCount: Flow<Int> = invoiceDao.getTotalInvoiceCount()
}
