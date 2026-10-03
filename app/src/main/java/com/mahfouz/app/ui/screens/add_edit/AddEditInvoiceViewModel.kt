package com.mahfouz.app.ui.screens.add_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mahfouz.app.data.local.entity.InvoiceEntity
import com.mahfouz.app.data.local.entity.InvoiceItemEntity
import com.mahfouz.app.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class InvoiceItemDraft(
    val id: Long = 0,
    val productName: String = "",
    val quantityText: String = "1",
    val unitPriceText: String = "0"
)

data class AddEditInvoiceUiState(
    val supplierName: String = "",
    val invoiceNumber: String = "",
    val totalAmountText: String = "",
    val invoiceDate: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val isPaid: Boolean = true,
    val notes: String = "",
    val items: List<InvoiceItemDraft> = emptyList(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AddEditInvoiceViewModel(
    private val repository: InvoiceRepository,
    private val categoryId: Long,
    private val invoiceId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditInvoiceUiState())
    val uiState: StateFlow<AddEditInvoiceUiState> = _uiState.asStateFlow()

    init {
        if (invoiceId > 0) {
            loadExistingInvoice()
        }
    }

    private fun loadExistingInvoice() {
        viewModelScope.launch {
            val invoiceWithItems = repository.getInvoiceWithItems(invoiceId).firstOrNull()
            if (invoiceWithItems != null) {
                val inv = invoiceWithItems.invoice
                _uiState.value = AddEditInvoiceUiState(
                    supplierName = inv.supplierName,
                    invoiceNumber = inv.invoiceNumber,
                    totalAmountText = inv.totalAmount.toString(),
                    invoiceDate = inv.invoiceDate,
                    imageUri = inv.imageUri,
                    isPaid = inv.isPaid,
                    notes = inv.notes,
                    items = invoiceWithItems.items.map {
                        InvoiceItemDraft(
                            id = it.id,
                            productName = it.productName,
                            quantityText = it.quantity.toString(),
                            unitPriceText = it.unitPrice.toString()
                        )
                    }
                )
            }
        }
    }

    fun onSupplierNameChange(value: String) { _uiState.value = _uiState.value.copy(supplierName = value) }
    fun onInvoiceNumberChange(value: String) { _uiState.value = _uiState.value.copy(invoiceNumber = value) }
    fun onTotalAmountChange(value: String) { _uiState.value = _uiState.value.copy(totalAmountText = value) }
    fun onPaymentStatusChange(isPaid: Boolean) { _uiState.value = _uiState.value.copy(isPaid = isPaid) }
    fun onNotesChange(value: String) { _uiState.value = _uiState.value.copy(notes = value) }
    fun onImageSelected(uriString: String?) { _uiState.value = _uiState.value.copy(imageUri = uriString) }

    fun addItem() {
        val currentItems = _uiState.value.items.toMutableList()
        currentItems.add(InvoiceItemDraft())
        _uiState.value = _uiState.value.copy(items = currentItems)
    }

    fun removeItem(index: Int) {
        val currentItems = _uiState.value.items.toMutableList()
        if (index in currentItems.indices) {
            currentItems.removeAt(index)
            _uiState.value = _uiState.value.copy(items = currentItems)
            recalculateTotalFromItems(currentItems)
        }
    }

    fun updateItem(index: Int, updated: InvoiceItemDraft) {
        val currentItems = _uiState.value.items.toMutableList()
        if (index in currentItems.indices) {
            currentItems[index] = updated
            _uiState.value = _uiState.value.copy(items = currentItems)
            recalculateTotalFromItems(currentItems)
        }
    }

    private fun recalculateTotalFromItems(items: List<InvoiceItemDraft>) {
        if (items.isNotEmpty()) {
            val sum = items.sumOf {
                val q = it.quantityText.toDoubleOrNull() ?: 0.0
                val p = it.unitPriceText.toDoubleOrNull() ?: 0.0
                q * p
            }
            if (sum > 0) {
                _uiState.value = _uiState.value.copy(totalAmountText = String.format("%.2f", sum))
            }
        }
    }

    fun saveInvoice() {
        val state = _uiState.value
        val supplier = state.supplierName.trim()
        val total = state.totalAmountText.toDoubleOrNull() ?: 0.0

        if (supplier.isBlank()) {
            _uiState.value = state.copy(errorMessage = "يرجى إدخال اسم المورد أو الشركة")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null)
            try {
                val invoiceEntity = InvoiceEntity(
                    id = if (invoiceId > 0) invoiceId else 0L,
                    categoryId = categoryId,
                    supplierName = supplier,
                    invoiceNumber = state.invoiceNumber.trim(),
                    totalAmount = total,
                    invoiceDate = state.invoiceDate,
                    imageUri = state.imageUri,
                    isPaid = state.isPaid,
                    notes = state.notes.trim()
                )

                val itemEntities = state.items
                    .filter { it.productName.isNotBlank() }
                    .map {
                        InvoiceItemEntity(
                            id = it.id,
                            invoiceId = if (invoiceId > 0) invoiceId else 0L,
                            productName = it.productName.trim(),
                            quantity = it.quantityText.toDoubleOrNull() ?: 1.0,
                            unitPrice = it.unitPriceText.toDoubleOrNull() ?: 0.0
                        )
                    }

                if (invoiceId > 0) {
                    repository.updateInvoice(invoiceEntity, itemEntities)
                } else {
                    repository.insertInvoice(invoiceEntity, itemEntities)
                }

                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.localizedMessage)
            }
        }
    }
}

class AddEditInvoiceViewModelFactory(
    private val repository: InvoiceRepository,
    private val categoryId: Long,
    private val invoiceId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditInvoiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditInvoiceViewModel(repository, categoryId, invoiceId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
