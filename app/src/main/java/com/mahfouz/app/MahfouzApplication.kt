package com.mahfouz.app

import android.app.Application
import com.mahfouz.app.data.local.MahfouzDatabase
import com.mahfouz.app.data.repository.InvoiceRepository

class MahfouzApplication : Application() {

    val database by lazy { MahfouzDatabase.getDatabase(this) }
    val repository by lazy {
        InvoiceRepository(
            categoryDao = database.categoryDao(),
            invoiceDao = database.invoiceDao()
        )
    }
}
