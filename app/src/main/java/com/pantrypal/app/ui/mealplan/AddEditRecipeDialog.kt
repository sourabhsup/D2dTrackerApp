package com.pantrypal.app.ui.mealplan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrypal.app.data.model.Recipe
import com.pantrypal.app.data.model.RecipeIngredient
import java.util.UUID

private data class IngredientDraft(
    val key: String = UUID.randomUUID().toString(),
    var name: String = "",
    var quantity: String = "",
    var unit: String = ""
)

@Composable
fun AddEditRecipeDialog(
    initialRecipe: Recipe?,
    onDismiss: () -> Unit,
    onSave: (Recipe) -> Unit,
    onDelete: ((Recipe) -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialRecipe?.name.orEmpty()) }
    var servings by remember { mutableStateOf((initialRecipe?.servings ?: 2).toString()) }
    var instructions by remember { mutableStateOf(initialRecipe?.instructions.orEmpty()) }
    val ingredients = remember {
        mutableStateListOf<IngredientDraft>().apply {
            val source = initialRecipe?.ingredients.orEmpty()
            if (source.isEmpty()) {
                add(IngredientDraft())
            } else {
                source.forEach { add(IngredientDraft(name = it.name, quantity = formatQty(it.quantity), unit = it.unit)) }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialRecipe == null) "Add recipe" else "Edit recipe") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Recipe name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = servings,
                    onValueChange = { servings = it },
                    label = { Text("Servings") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Ingredients")
                ingredients.forEachIndexed { index, draft ->
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedTextField(
                            value = draft.name,
                            onValueChange = { ingredients[index] = draft.copy(name = it) },
                            label = { Text("Ingredient") },
                            singleLine = true,
                            modifier = Modifier.weight(2f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        OutlinedTextField(
                            value = draft.quantity,
                            onValueChange = { ingredients[index] = draft.copy(quantity = it) },
                            label = { Text("Qty") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        OutlinedTextField(
                            value = draft.unit,
                            onValueChange = { ingredients[index] = draft.copy(unit = it) },
                            label = { Text("Unit") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { if (ingredients.size > 1) ingredients.removeAt(index) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove ingredient")
                        }
                    }
                }
                TextButton(onClick = { ingredients.add(IngredientDraft()) }, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add ingredient")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions (optional)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                if (onDelete != null && initialRecipe != null) {
                    TextButton(onClick = { onDelete(initialRecipe) }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Delete recipe")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    val cleanedIngredients = ingredients
                        .filter { it.name.isNotBlank() }
                        .map { RecipeIngredient(it.name.trim(), it.quantity.toDoubleOrNull() ?: 0.0, it.unit.trim()) }
                    onSave(
                        (initialRecipe ?: Recipe()).copy(
                            name = name.trim(),
                            servings = servings.toIntOrNull() ?: 1,
                            instructions = instructions.trim(),
                            ingredients = cleanedIngredients
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

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
