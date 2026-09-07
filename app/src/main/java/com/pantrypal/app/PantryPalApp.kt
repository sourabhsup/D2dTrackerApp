package com.pantrypal.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pantrypal.app.data.repository.HouseholdRepository
import com.pantrypal.app.data.repository.InventoryRepository
import com.pantrypal.app.data.repository.MealPlanRepository
import com.pantrypal.app.data.repository.RecipeRepository
import com.pantrypal.app.data.repository.ShoppingListRepository
import com.pantrypal.app.data.session.HouseholdSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Simple hand-rolled service locator (no DI framework) exposing the
 * Firestore-backed repositories every screen needs.
 */
class PantryPalApp : Application() {

    lateinit var session: HouseholdSession
        private set
    lateinit var householdRepository: HouseholdRepository
        private set
    lateinit var inventoryRepository: InventoryRepository
        private set
    lateinit var recipeRepository: RecipeRepository
        private set
    lateinit var mealPlanRepository: MealPlanRepository
        private set
    lateinit var shoppingListRepository: ShoppingListRepository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        val firestore = FirebaseFirestore.getInstance()
        session = HouseholdSession(this)
        householdRepository = HouseholdRepository(firestore)
        inventoryRepository = InventoryRepository(firestore)
        recipeRepository = RecipeRepository(firestore)
        mealPlanRepository = MealPlanRepository(firestore)
        shoppingListRepository = ShoppingListRepository(firestore)

        appScope.launch {
            // Anonymous auth is enough to satisfy Firestore security rules
            // that require request.auth != null, without asking anyone to
            // create an account or sign in.
            if (FirebaseAuth.getInstance().currentUser == null) {
                runCatching { FirebaseAuth.getInstance().signInAnonymously().await() }
            }
        }
    }
}
