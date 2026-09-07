package com.pantrypal.app.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pantrypal.app.data.model.InventoryItem
import com.pantrypal.app.ui.common.rememberApp

@Composable
fun InventoryScreen(householdId: String) {
    val app = rememberApp()
    val viewModel: InventoryViewModel = viewModel(
        key = "inventory_$householdId",
        factory = viewModelFactory {
            initializer { InventoryViewModel(app.inventoryRepository, householdId) }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var editingItem by remember { mutableStateOf<InventoryItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add item")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchChange,
                label = { Text("Search inventory") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (uiState.items.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Your inventory is empty. Tap + to add the first item.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    uiState.filteredGrouped.forEach { (category, categoryItems) ->
                        item {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(categoryItems, key = { it.id }) { item ->
                            InventoryRow(
                                item = item,
                                onIncrement = { viewModel.adjustQuantity(item, 1.0) },
                                onDecrement = { viewModel.adjustQuantity(item, -1.0) },
                                onClick = { editingItem = item }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditItemDialog(
            initialItem = null,
            onDismiss = { showAddDialog = false },
            onSave = { item ->
                viewModel.saveItem(item)
                showAddDialog = false
            }
        )
    }

    editingItem?.let { item ->
        AddEditItemDialog(
            initialItem = item,
            onDismiss = { editingItem = null },
            onSave = { updated ->
                viewModel.saveItem(updated)
                editingItem = null
            },
            onDelete = { toDelete ->
                viewModel.deleteItem(toDelete)
                editingItem = null
            }
        )
    }
}

@Composable
private fun InventoryRow(
    item: InventoryItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                    if (item.isLowStock) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Low stock",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
                Text(
                    text = "${formatQuantity(item.quantity)} ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDecrement) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease quantity")
            }
            IconButton(onClick = onIncrement) {
                Icon(Icons.Filled.Add, contentDescription = "Increase quantity")
            }
        }
    }
}

private fun formatQuantity(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else "%.2f".format(value)
