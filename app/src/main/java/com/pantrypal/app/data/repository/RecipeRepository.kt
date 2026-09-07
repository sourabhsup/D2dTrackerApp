package com.pantrypal.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pantrypal.app.data.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class RecipeRepository(private val firestore: FirebaseFirestore) {

    private fun collection(householdId: String) =
        firestore.collection("households").document(householdId).collection("recipes")

    fun observeRecipes(householdId: String): Flow<List<Recipe>> =
        collection(householdId).observeList { doc ->
            doc.toObject(Recipe::class.java)?.copy(id = doc.id)
        }

    suspend fun addRecipe(householdId: String, recipe: Recipe) {
        collection(householdId).add(recipe).await()
    }

    suspend fun updateRecipe(householdId: String, recipe: Recipe) {
        collection(householdId).document(recipe.id).set(recipe).await()
    }

    suspend fun deleteRecipe(householdId: String, recipeId: String) {
        collection(householdId).document(recipeId).delete().await()
    }
}
