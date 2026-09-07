package com.pantrypal.app.ui.shoppinglist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pantrypal.app.data.model.ShoppingListItem
import com.pantrypal.app.ui.common.rememberApp

@Composable
fun ShoppingListScreen(householdId: String) {
    val app = rememberApp()
    val viewModel: ShoppingListViewModel = viewModel(
        key = "shopping_$householdId",
        factory = viewModelFactory {
            initializer {
                ShoppingListViewModel(
                    app.shoppingListRepository,
                    app.inventoryRepository,
                    app.mealPlanRepository,
                    app.recipeRepository,
                    householdId
                )
            }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var newItemName by remember { mutableStateOf("") }
    var newItemQuantity by remember { mutableStateOf("1") }
    var newItemUnit by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = viewModel::refreshFromInventoryAndPlan, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Text(" Refresh from inventory & plan")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    label = { Text("Add item") },
                    singleLine = true,
                    modifier = Modifier.weight(2f)
                )
                OutlinedTextField(
                    value = newItemQuantity,
                    onValueChange = { newItemQuantity = it },
                    label = { Text("Qty") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = newItemUnit,
                    onValueChange = { newItemUnit = it },
                    label = { Text("Unit") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    viewModel.addManualItem(newItemName, newItemQuantity.toDoubleOrNull() ?: 1.0, newItemUnit)
                    newItemName = ""
                    newItemQuantity = "1"
                    newItemUnit = ""
                }) { Text("Add") }
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                item {
                    Text(
                        "To buy (${uiState.pending.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                    )
                }
                if (uiState.pending.isEmpty()) {
                    item {
                        Text(
                            "Nothing to buy right now.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    items(uiState.pending, key = { it.id }) { shoppingItem ->
                        ShoppingRow(
                            item = shoppingItem,
                            onCheckedChange = { checked -> viewModel.setChecked(shoppingItem, checked) },
                            onDelete = { viewModel.deleteItem(shoppingItem) }
                        )
                    }
                }

                if (uiState.checked.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("In cart (${uiState.checked.size})", style = MaterialTheme.typography.titleMedium)
                            Button(onClick = viewModel::applyPurchasedToInventory) {
                                Text("Add to inventory")
                            }
                        }
                    }
                    items(uiState.checked, key = { it.id }) { shoppingItem ->
                        ShoppingRow(
                            item = shoppingItem,
                            onCheckedChange = { checked -> viewModel.setChecked(shoppingItem, checked) },
                            onDelete = { viewModel.deleteItem(shoppingItem) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingRow(
    item: ShoppingListItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = item.checked, onCheckedChange = onCheckedChange)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.checked) TextDecoration.LineThrough else null
            )
            val qtyText = if (item.quantity == item.quantity.toLong().toDouble()) {
                item.quantity.toLong().toString()
            } else {
                item.quantity.toString()
            }
            Text(
                text = "$qtyText ${item.unit}".trim(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Remove")
        }
    }
}
