package com.mahfouz.app.ui.screens.add_edit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mahfouz.app.data.local.entity.InvoiceEntity
import com.mahfouz.app.data.local.entity.InvoiceItemEntity
import com.mahfouz.app.data.repository.InvoiceRepository
import com.mahfouz.app.utils.FileUtils
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
                    totalAmountText = if (inv.totalAmount == 0.0) "" else inv.totalAmount.toString(),
                    invoiceDate = inv.invoiceDate,
                    imageUri = inv.imageUri,
                    isPaid = inv.isPaid,
                    notes = inv.notes,
                    items = invoiceWithItems.items.map {
                        InvoiceItemDraft(
                            id = it.id,
                            productName = it.productName,
                            quantityText = if (it.quantity % 1.0 == 0.0) it.quantity.toInt().toString() else it.quantity.toString(),
                            unitPriceText = if (it.unitPrice % 1.0 == 0.0) it.unitPrice.toInt().toString() else it.unitPrice.toString()
                        )
                    }
                )
            }
        }
    }

    fun onSupplierNameChange(value: String) { _uiState.value = _uiState.value.copy(supplierName = value, errorMessage = null) }
    fun onInvoiceNumberChange(value: String) { _uiState.value = _uiState.value.copy(invoiceNumber = value) }
    fun onTotalAmountChange(value: String) { _uiState.value = _uiState.value.copy(totalAmountText = value, errorMessage = null) }
    fun onPaymentStatusChange(isPaid: Boolean) { _uiState.value = _uiState.value.copy(isPaid = isPaid) }
    fun onDateSelected(timestamp: Long) { _uiState.value = _uiState.value.copy(invoiceDate = timestamp) }
    fun onNotesChange(value: String) { _uiState.value = _uiState.value.copy(notes = value) }

    fun onImagePicked(context: Context, uri: Uri) {
        viewModelScope.launch {
            val persistedPath = FileUtils.persistImageToInternalStorage(context, uri)
            _uiState.value = _uiState.value.copy(imageUri = persistedPath ?: uri.toString())
        }
    }

    fun removeImage() {
        _uiState.value = _uiState.value.copy(imageUri = null)
    }

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
            val validItems = items.filter { it.productName.isNotBlank() }
            if (validItems.isNotEmpty()) {
                val sum = validItems.sumOf {
                    val q = it.quantityText.toDoubleOrNull() ?: 0.0
                    val p = it.unitPriceText.toDoubleOrNull() ?: 0.0
                    q * p
                }
                if (sum > 0) {
                    _uiState.value = _uiState.value.copy(totalAmountText = String.format("%.2f", sum))
                }
            }
        }
    }

    fun saveInvoice() {
        val state = _uiState.value
        val supplier = state.supplierName.trim()
        val total = state.totalAmountText.toDoubleOrNull()

        if (supplier.isBlank()) {
            _uiState.value = state.copy(errorMessage = "يرجى كتابة اسم المورد أو الشركة")
            return
        }

        if (total == null || total <= 0) {
            _uiState.value = state.copy(errorMessage = "يرجى إدخال مبلغ إجمالي صحيح أكبر من الصفر")
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
                            id = 0L,
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
