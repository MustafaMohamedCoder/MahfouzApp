package com.mahfouz.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class InvoiceWithItems(
    @Embedded
    val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItemEntity>
)

data class CategorySummary(
    val id: Long,
    val name: String,
    val description: String,
    val colorHex: String,
    val invoiceCount: Int,
    val totalSpent: Double,
    val unpaidAmount: Double = 0.0
)

data class OverallStats(
    val totalSpent: Double = 0.0,
    val totalUnpaid: Double = 0.0,
    val totalInvoices: Int = 0
)
