package com.pantrypal.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pantrypal.app.ui.inventory.InventoryScreen
import com.pantrypal.app.ui.mealplan.MealPlanScreen
import com.pantrypal.app.ui.settings.SettingsScreen
import com.pantrypal.app.ui.shoppinglist.ShoppingListScreen

@Composable
fun MainNavGraph(householdId: String) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                Screen.bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Inventory.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Inventory.route) { InventoryScreen(householdId = householdId) }
            composable(Screen.MealPlan.route) { MealPlanScreen(householdId = householdId) }
            composable(Screen.ShoppingList.route) { ShoppingListScreen(householdId = householdId) }
            composable(Screen.Settings.route) { SettingsScreen(householdId = householdId) }
        }
    }
}
