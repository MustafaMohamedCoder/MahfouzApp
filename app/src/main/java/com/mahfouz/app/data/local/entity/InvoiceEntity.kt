package com.mahfouz.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val supplierName: String,
    val invoiceNumber: String = "",
    val totalAmount: Double,
    val invoiceDate: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val isPaid: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
