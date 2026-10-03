package com.mahfouz.app.ui.screens.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mahfouz.app.data.local.entity.InvoiceEntity
import com.mahfouz.app.data.repository.InvoiceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class PaymentFilterType {
    ALL, PAID, UNPAID
}

@OptIn(ExperimentalCoroutinesApi::class)
class InvoicesViewModel(
    private val repository: InvoiceRepository,
    private val categoryId: Long
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _paymentFilter = MutableStateFlow(PaymentFilterType.ALL)
    val paymentFilter: StateFlow<PaymentFilterType> = _paymentFilter.asStateFlow()

    val invoices: StateFlow<List<InvoiceEntity>> = combine(
        _searchQuery,
        _paymentFilter
    ) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        val isPaid = when (filter) {
            PaymentFilterType.ALL -> null
            PaymentFilterType.PAID -> true
            PaymentFilterType.UNPAID -> false
        }
        repository.filterInvoices(categoryId, query.trim(), isPaid)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onPaymentFilterChanged(filter: PaymentFilterType) {
        _paymentFilter.value = filter
    }

    fun togglePaymentStatus(invoice: InvoiceEntity) {
        viewModelScope.launch {
            repository.updatePaymentStatus(invoice.id, !invoice.isPaid)
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }
}

class InvoicesViewModelFactory(
    private val repository: InvoiceRepository,
    private val categoryId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoicesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InvoicesViewModel(repository, categoryId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
