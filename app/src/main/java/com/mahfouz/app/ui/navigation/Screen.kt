package com.mahfouz.app.ui.navigation

sealed class Screen(val route: String) {
    object Groups : Screen("groups")

    object Invoices : Screen("invoices/{categoryId}/{categoryName}") {
        fun createRoute(categoryId: Long, categoryName: String) =
            "invoices/$categoryId/$categoryName"
    }

    object AddEditInvoice : Screen("add_edit_invoice/{categoryId}?invoiceId={invoiceId}") {
        fun createRoute(categoryId: Long, invoiceId: Long = -1L) =
            "add_edit_invoice/$categoryId?invoiceId=$invoiceId"
    }

    object InvoiceDetail : Screen("invoice_detail/{invoiceId}") {
        fun createRoute(invoiceId: Long) = "invoice_detail/$invoiceId"
    }
}
