package com.pantrypal.app.ui.mealplan

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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pantrypal.app.data.model.MealType
import com.pantrypal.app.data.model.Recipe
import com.pantrypal.app.ui.common.rememberApp
import com.pantrypal.app.util.formatShort
import java.time.LocalDate

@Composable
fun MealPlanScreen(householdId: String) {
    val app = rememberApp()
    val viewModel: MealPlanViewModel = viewModel(
        key = "mealplan_$householdId",
        factory = viewModelFactory {
            initializer {
                MealPlanViewModel(app.recipeRepository, app.mealPlanRepository, householdId)
            }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var pickerTarget by remember { mutableStateOf<Pair<LocalDate, MealType>?>(null) }
    var editingRecipe by remember { mutableStateOf<Recipe?>(null) }
    var showAddRecipe by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(onClick = { showAddRecipe = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add recipe")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("This Week") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Recipes") })
            }

            if (selectedTab == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = viewModel::previousWeek) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous week")
                    }
                    Text(
                        "${uiState.weekStart.formatShort()} – ${uiState.weekStart.plusDays(6).formatShort()}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    IconButton(onClick = viewModel::nextWeek) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next week")
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.days) { day ->
                        DayCard(
                            day = day,
                            entries = uiState.entriesByDay[day].orEmpty(),
                            onSlotClick = { mealType -> pickerTarget = day to mealType }
                        )
                    }
                }
            } else {
                if (uiState.recipes.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No recipes yet. Tap + to add one.", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.recipes, key = { it.id }) { recipe ->
                            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                                TextButton(
                                    onClick = { editingRecipe = recipe },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(recipe.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "${recipe.ingredients.size} ingredients • serves ${recipe.servings}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pickerTarget?.let { (day, mealType) ->
        RecipePickerDialog(
            recipes = uiState.recipes,
            hasExistingAssignment = uiState.entriesByDay[day]?.containsKey(mealType) == true,
            onDismiss = { pickerTarget = null },
            onPick = { recipe ->
                viewModel.assignRecipe(day, mealType, recipe)
                pickerTarget = null
            },
            onClear = {
                viewModel.clearSlot(day, mealType)
                pickerTarget = null
            }
        )
    }

    if (showAddRecipe) {
        AddEditRecipeDialog(
            initialRecipe = null,
            onDismiss = { showAddRecipe = false },
            onSave = { recipe ->
                viewModel.saveRecipe(recipe)
                showAddRecipe = false
            }
        )
    }

    editingRecipe?.let { recipe ->
        AddEditRecipeDialog(
            initialRecipe = recipe,
            onDismiss = { editingRecipe = null },
            onSave = { updated ->
                viewModel.saveRecipe(updated)
                editingRecipe = null
            },
            onDelete = { toDelete ->
                viewModel.deleteRecipe(toDelete)
                editingRecipe = null
            }
        )
    }
}

@Composable
private fun DayCard(
    day: LocalDate,
    entries: Map<MealType, com.pantrypal.app.data.model.MealPlanEntry>,
    onSlotClick: (MealType) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(day.formatShort(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            MealType.entries.forEach { mealType ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(mealType.label, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { onSlotClick(mealType) }) {
                        Text(entries[mealType]?.recipeName ?: "+ Add")
                    }
                }
            }
        }
    }
}
