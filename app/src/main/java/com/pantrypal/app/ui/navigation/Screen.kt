package com.pantrypal.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Inventory : Screen("inventory", "Inventory", Icons.Filled.Kitchen)
    data object MealPlan : Screen("meal_plan", "Meal Plan", Icons.Filled.RestaurantMenu)
    data object ShoppingList : Screen("shopping_list", "Shopping", Icons.Filled.ShoppingCart)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    companion object {
        val bottomNavItems = listOf(Inventory, MealPlan, ShoppingList, Settings)
    }
}
