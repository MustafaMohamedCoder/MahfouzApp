package com.mahfouz.app.ui.screens.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mahfouz.app.data.local.entity.InvoiceEntity
import com.mahfouz.app.data.repository.InvoiceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class InvoicesViewModel(
    private val repository: InvoiceRepository,
    private val categoryId: Long
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val invoices: StateFlow<List<InvoiceEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getInvoicesByCategory(categoryId)
            } else {
                repository.searchInvoices(categoryId, query.trim())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
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
