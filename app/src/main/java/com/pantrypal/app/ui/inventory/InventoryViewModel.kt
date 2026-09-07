package com.pantrypal.app.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrypal.app.data.model.InventoryItem
import com.pantrypal.app.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InventoryUiState(
    val items: List<InventoryItem> = emptyList(),
    val searchQuery: String = ""
) {
    val filteredGrouped: Map<String, List<InventoryItem>>
        get() = items
            .filter { it.name.contains(searchQuery, ignoreCase = true) }
            .sortedBy { it.name.lowercase() }
            .groupBy { it.category }
            .toSortedMap()
}

class InventoryViewModel(
    private val repository: InventoryRepository,
    private val householdId: String
) : ViewModel() {

    private val items = repository.observeItems(householdId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<InventoryUiState> = combine(items, searchQuery) { currentItems, query ->
        InventoryUiState(items = currentItems, searchQuery = query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InventoryUiState())

    fun onSearchChange(query: String) {
        searchQuery.value = query
    }

    fun saveItem(item: InventoryItem) {
        viewModelScope.launch {
            if (item.id.isBlank()) repository.addItem(householdId, item)
            else repository.updateItem(householdId, item)
        }
    }

    fun adjustQuantity(item: InventoryItem, delta: Double) {
        viewModelScope.launch { repository.adjustQuantity(householdId, item, delta) }
    }

    fun deleteItem(item: InventoryItem) {
        viewModelScope.launch { repository.deleteItem(householdId, item.id) }
    }
}
