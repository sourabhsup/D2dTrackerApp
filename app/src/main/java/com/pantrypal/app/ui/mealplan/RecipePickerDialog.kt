package com.pantrypal.app.ui.mealplan

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrypal.app.data.model.Recipe

@Composable
fun RecipePickerDialog(
    recipes: List<Recipe>,
    hasExistingAssignment: Boolean,
    onDismiss: () -> Unit,
    onPick: (Recipe) -> Unit,
    onClear: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose a recipe") },
        text = {
            if (recipes.isEmpty()) {
                Text("No recipes yet. Add one from the Recipes tab first.")
            } else {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(recipes, key = { it.id }) { recipe ->
                        TextButton(
                            onClick = { onPick(recipe) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(recipe.name, modifier = Modifier.fillMaxWidth())
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            if (hasExistingAssignment) {
                TextButton(onClick = onClear) { Text("Clear slot") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
