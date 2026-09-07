package com.pantrypal.app.ui.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrypal.app.data.model.MealPlanEntry
import com.pantrypal.app.data.model.MealType
import com.pantrypal.app.data.model.Recipe
import com.pantrypal.app.data.repository.MealPlanRepository
import com.pantrypal.app.data.repository.RecipeRepository
import com.pantrypal.app.util.toEpochMillis
import com.pantrypal.app.util.toLocalDate
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MealPlanUiState(
    val weekStart: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY),
    val recipes: List<Recipe> = emptyList(),
    val entriesByDay: Map<LocalDate, Map<MealType, MealPlanEntry>> = emptyMap()
) {
    val days: List<LocalDate> get() = (0..6).map { weekStart.plusDays(it.toLong()) }
}

class MealPlanViewModel(
    private val recipeRepository: RecipeRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val householdId: String
) : ViewModel() {

    private val weekStart = MutableStateFlow(LocalDate.now().with(DayOfWeek.MONDAY))

    private val recipes = recipeRepository.observeRecipes(householdId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val entries = mealPlanRepository.observeEntries(householdId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<MealPlanUiState> = combine(weekStart, recipes, entries) { start, recipeList, entryList ->
        val days = (0..6).map { start.plusDays(it.toLong()) }
        val byDay = days.associateWith { day ->
            entryList.filter { it.dateMillis.toLocalDate() == day }.associateBy { it.mealTypeEnum }
        }
        MealPlanUiState(weekStart = start, recipes = recipeList, entriesByDay = byDay)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MealPlanUiState())

    fun previousWeek() {
        weekStart.value = weekStart.value.minusWeeks(1)
    }

    fun nextWeek() {
        weekStart.value = weekStart.value.plusWeeks(1)
    }

    fun assignRecipe(day: LocalDate, mealType: MealType, recipe: Recipe) {
        val existing = uiState.value.entriesByDay[day]?.get(mealType)
        viewModelScope.launch {
            mealPlanRepository.setEntry(
                householdId,
                MealPlanEntry(
                    id = existing?.id.orEmpty(),
                    dateMillis = day.toEpochMillis(),
                    mealType = mealType.name,
                    recipeId = recipe.id,
                    recipeName = recipe.name
                )
            )
        }
    }

    fun clearSlot(day: LocalDate, mealType: MealType) {
        val existing = uiState.value.entriesByDay[day]?.get(mealType) ?: return
        viewModelScope.launch { mealPlanRepository.deleteEntry(householdId, existing.id) }
    }

    fun saveRecipe(recipe: Recipe) {
        viewModelScope.launch {
            if (recipe.id.isBlank()) recipeRepository.addRecipe(householdId, recipe)
            else recipeRepository.updateRecipe(householdId, recipe)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch { recipeRepository.deleteRecipe(householdId, recipe.id) }
    }
}
