package com.pantrypal.app.ui.inventory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pantrypal.app.data.model.InventoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemDialog(
    initialItem: InventoryItem?,
    onDismiss: () -> Unit,
    onSave: (InventoryItem) -> Unit,
    onDelete: ((InventoryItem) -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialItem?.name.orEmpty()) }
    var category by remember { mutableStateOf(initialItem?.category ?: InventoryItem.DEFAULT_CATEGORIES.last()) }
    var quantity by remember { mutableStateOf((initialItem?.quantity ?: 1.0).let { formatNumber(it) }) }
    var unit by remember { mutableStateOf(initialItem?.unit ?: InventoryItem.DEFAULT_UNITS.first()) }
    var threshold by remember { mutableStateOf((initialItem?.lowStockThreshold ?: 0.0).let { formatNumber(it) }) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialItem == null) "Add item" else "Edit item") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        InventoryItem.DEFAULT_CATEGORIES.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = {
                                category = option
                                categoryExpanded = false
                            })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        DropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                            InventoryItem.DEFAULT_UNITS.forEach { option ->
                                DropdownMenuItem(text = { Text(option) }, onClick = {
                                    unit = option
                                    unitExpanded = false
                                })
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = threshold,
                    onValueChange = { threshold = it },
                    label = { Text("Low stock alert below") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                if (onDelete != null && initialItem != null) {
                    TextButton(onClick = { onDelete(initialItem) }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Delete item")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        (initialItem ?: InventoryItem()).copy(
                            name = name.trim(),
                            category = category,
                            quantity = quantity.toDoubleOrNull() ?: 0.0,
                            unit = unit,
                            lowStockThreshold = threshold.toDoubleOrNull() ?: 0.0
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun formatNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
