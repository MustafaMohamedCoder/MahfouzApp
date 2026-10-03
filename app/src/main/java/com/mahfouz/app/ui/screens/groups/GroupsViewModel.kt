package com.mahfouz.app.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mahfouz.app.data.local.entity.CategoryEntity
import com.mahfouz.app.data.local.entity.CategorySummary
import com.mahfouz.app.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroupsViewModel(
    private val repository: InvoiceRepository
) : ViewModel() {

    val categories: StateFlow<List<CategorySummary>> = repository.categoriesSummary
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalSpent: StateFlow<Double> = repository.totalExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    fun addCategory(name: String, description: String, colorHex: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertCategory(
                    CategoryEntity(
                        name = name.trim(),
                        description = description.trim(),
                        colorHex = colorHex
                    )
                )
            }
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            val cat = repository.getCategoryById(categoryId)
            if (cat != null) {
                repository.deleteCategory(cat)
            }
        }
    }
}

class GroupsViewModelFactory(private val repository: InvoiceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
