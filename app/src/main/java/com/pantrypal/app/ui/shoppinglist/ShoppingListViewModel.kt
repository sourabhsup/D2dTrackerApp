package com.pantrypal.app.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrypal.app.data.model.InventoryItem
import com.pantrypal.app.data.model.ShoppingListItem
import com.pantrypal.app.data.model.ShoppingListSource
import com.pantrypal.app.data.repository.InventoryRepository
import com.pantrypal.app.data.repository.MealPlanRepository
import com.pantrypal.app.data.repository.RecipeRepository
import com.pantrypal.app.data.repository.ShoppingListRepository
import com.pantrypal.app.util.toLocalDate
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShoppingListUiState(
    val pending: List<ShoppingListItem> = emptyList(),
    val checked: List<ShoppingListItem> = emptyList()
)

class ShoppingListViewModel(
    private val shoppingListRepository: ShoppingListRepository,
    private val inventoryRepository: InventoryRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository,
    private val householdId: String
) : ViewModel() {

    private val shoppingItems = shoppingListRepository.observeItems(householdId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val inventory = inventoryRepository.observeItems(householdId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val mealPlanEntries = mealPlanRepository.observeEntries(householdId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val recipes = recipeRepository.observeRecipes(householdId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<ShoppingListUiState> = shoppingItems.map { items ->
        ShoppingListUiState(
            pending = items.filter { !it.checked }.sortedBy { it.name.lowercase() },
            checked = items.filter { it.checked }.sortedBy { it.name.lowercase() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShoppingListUiState())

    fun addManualItem(name: String, quantity: Double, unit: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            shoppingListRepository.addItem(
                householdId,
                ShoppingListItem(
                    name = name.trim(),
                    quantity = quantity,
                    unit = unit.trim(),
                    source = ShoppingListSource.MANUAL.name
                )
            )
        }
    }

    fun setChecked(item: ShoppingListItem, checked: Boolean) {
        viewModelScope.launch { shoppingListRepository.setChecked(householdId, item, checked) }
    }

    fun deleteItem(item: ShoppingListItem) {
        viewModelScope.launch { shoppingListRepository.deleteItem(householdId, item.id) }
    }

    /** Adds low-stock inventory items and any ingredients this week's meal plan needs but inventory doesn't cover. */
    fun refreshFromInventoryAndPlan() {
        viewModelScope.launch {
            val currentNames = shoppingItems.value
                .filter { !it.checked }
                .map { it.name.lowercase() }
                .toMutableSet()
            val toAdd = mutableListOf<ShoppingListItem>()

            inventory.value.filter { it.isLowStock }.forEach { item ->
                val key = item.name.lowercase()
                if (key !in currentNames) {
                    val needed = (item.lowStockThreshold - item.quantity).let { if (it <= 0) 1.0 else it }
                    toAdd.add(
                        ShoppingListItem(
                            name = item.name,
                            quantity = needed,
                            unit = item.unit,
                            source = ShoppingListSource.LOW_STOCK.name
                        )
                    )
                    currentNames.add(key)
                }
            }

            val today = LocalDate.now()
            val weekAhead = today.plusDays(6)
            val recipesById = recipes.value.associateBy { it.id }
            // name.lowercase() -> (total quantity needed this week, unit, display name)
            val neededFromPlan = mutableMapOf<String, Triple<Double, String, String>>()
            mealPlanEntries.value
                .filter { entry ->
                    val date = entry.dateMillis.toLocalDate()
                    !date.isBefore(today) && !date.isAfter(weekAhead)
                }
                .mapNotNull { entry -> recipesById[entry.recipeId] }
                .forEach { recipe ->
                    recipe.ingredients.forEach { ingredient ->
                        if (ingredient.name.isBlank()) return@forEach
                        val key = ingredient.name.lowercase()
                        val existing = neededFromPlan[key]
                        val qty = (existing?.first ?: 0.0) + ingredient.quantity
                        neededFromPlan[key] = Triple(qty, ingredient.unit, ingredient.name)
                    }
                }

            val inventoryByName = inventory.value.associateBy { it.name.lowercase() }
            neededFromPlan.forEach { (key, value) ->
                if (key in currentNames) return@forEach
                val (neededQty, unit, displayName) = value
                val available = inventoryByName[key]?.quantity ?: 0.0
                val deficit = neededQty - available
                if (deficit > 0) {
                    toAdd.add(
                        ShoppingListItem(
                            name = displayName,
                            quantity = deficit,
                            unit = unit,
                            source = ShoppingListSource.MEAL_PLAN.name
                        )
                    )
                    currentNames.add(key)
                }
            }

            shoppingListRepository.addItems(householdId, toAdd)
        }
    }

    /** Marks checked items as purchased: folds their quantity into inventory and clears them off the list. */
    fun applyPurchasedToInventory() {
        viewModelScope.launch {
            val purchased = shoppingItems.value.filter { it.checked }
            if (purchased.isEmpty()) return@launch

            val inventoryByName = inventory.value.associateBy { it.name.lowercase() }
            purchased.forEach { purchasedItem ->
                val existing = inventoryByName[purchasedItem.name.lowercase()]
                if (existing != null) {
                    inventoryRepository.adjustQuantity(householdId, existing, purchasedItem.quantity)
                } else {
                    inventoryRepository.addItem(
                        householdId,
                        InventoryItem(
                            name = purchasedItem.name,
                            quantity = purchasedItem.quantity,
                            unit = purchasedItem.unit
                        )
                    )
                }
            }
            shoppingListRepository.deleteItems(householdId, purchased.map { it.id })
        }
    }
}
